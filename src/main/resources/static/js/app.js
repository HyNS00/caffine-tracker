// 전역 상태
let beverages = [];
let customBeverages = [];
let categories = [];
let todayIntakes = [];
let selectedBeverage = null;
let editingCustomBeverage = null;

// 앱 초기화
async function initApp() {
    updateTodayDate();
    await loadCategories();
    await loadCustomBeverages();
    await loadTodayIntakes();
    setupTabListeners();
    setupSearchListener();
    setupCustomBeverageListeners();
    setupModalListeners();
}

// 오늘 날짜 표시
function updateTodayDate() {
    const today = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    document.getElementById('todayDate').textContent = today.toLocaleDateString('ko-KR', options);
}

// 카테고리 로드
async function loadCategories() {
    try {
        categories = await BeverageAPI.getCategories();
        populateCategorySelect();
    } catch (error) {
        console.error('카테고리 로드 실패:', error);
    }
}

// 카테고리 셀렉트 박스 채우기
function populateCategorySelect() {
    const select = document.getElementById('customCategory');
    select.innerHTML = '<option value="">선택하세요</option>';

    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.code;
        option.textContent = `${category.displayName} (기본: ${category.defaultServingSizeMl}ml, ${Math.round(category.caffeineMgPer100ml * category.defaultServingSizeMl / 100)}mg)`;
        select.appendChild(option);
    });
}

// 음료 목록 렌더링
function renderBeverages(beverageList) {
    const grid = document.getElementById('beverageGrid');

    if (beverageList.length === 0) {
        grid.innerHTML = '<div class="empty-state">검색 결과가 없습니다</div>';
        return;
    }

    grid.innerHTML = beverageList.map(beverage => `
        <div class="beverage-card" onclick="openIntakeModal(${beverage.id}, 'preset')">
            <div class="beverage-brand">${beverage.brandName}</div>
            <div class="beverage-name">${beverage.name}</div>
            <div class="beverage-info">
                <div class="beverage-detail">
                    <div class="beverage-detail-label">용량</div>
                    <div class="beverage-detail-value">${beverage.volumeMl}ml</div>
                </div>
                <div class="beverage-detail">
                    <div class="beverage-detail-label">카페인</div>
                    <div class="beverage-detail-value">${Math.round(beverage.caffeineMg)}mg</div>
                </div>
            </div>
        </div>
    `).join('');
}

// 검색 리스너 설정
function setupSearchListener() {
    const searchInput = document.getElementById('beverageSearch');
    const searchBtn = document.getElementById('searchBtn');

    // 검색 버튼 클릭
    searchBtn.addEventListener('click', performSearch);

    // Enter 키 입력
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            performSearch();
        }
    });
}

// 검색 수행
async function performSearch() {
    const keyword = document.getElementById('beverageSearch').value.trim();

    if (keyword === '') {
        alert('검색어를 입력해주세요');
        return;
    }

    try {
        beverages = await BeverageAPI.search(keyword);
        renderBeverages(beverages);
    } catch (error) {
        console.error('검색 실패:', error);
        alert('검색 중 오류가 발생했습니다');
    }
}

// 오늘 섭취 기록 로드
async function loadTodayIntakes() {
    try {
        todayIntakes = await IntakeAPI.getTodayIntakes();
        renderTodayIntakes();
        updateSummaryStats();
    } catch (error) {
        console.error('섭취 기록 로드 실패:', error);
    }
}

// 탭 리스너 설정
function setupTabListeners() {
    document.querySelectorAll('.beverage-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;

            // 탭 버튼 활성화
            document.querySelectorAll('.beverage-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // 탭 컨텐츠 전환
            document.querySelectorAll('.beverage-tab-content').forEach(content => content.classList.remove('active'));
            document.getElementById(`${tab}Tab`).classList.add('active');
        });
    });
}

// CustomBeverage 리스너 설정
function setupCustomBeverageListeners() {
    // 추가 버튼
    document.getElementById('addCustomBtn').addEventListener('click', () => {
        openCustomBeverageModal();
    });

    // 저장 버튼
    document.getElementById('saveCustomBtn').addEventListener('click', saveCustomBeverage);

    // 카테고리 선택 시 기본값 설정
    document.getElementById('customCategory').addEventListener('change', (e) => {
        const selectedCode = e.target.value;
        if (!selectedCode) return;

        const category = categories.find(c => c.code === selectedCode);
        if (category) {
            document.getElementById('customVolume').value = category.defaultServingSizeMl;
            // 카페인도 자동 계산
            updateCaffeineEstimate();
        }
    });

    // 용량 변경 시 카페인 자동 계산
    document.getElementById('customVolume').addEventListener('input', updateCaffeineEstimate);
}

// 모달 리스너 설정
function setupModalListeners() {
    // 모든 모달 닫기 버튼
    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllModals();
        });
    });

    // intakeModal 배경 클릭 시 닫기
    document.getElementById('intakeModal').addEventListener('click', (e) => {
        if (e.target.id === 'intakeModal') {
            closeAllModals();
        }
    });

    // customBeverageModal 배경 클릭 시 닫기
    document.getElementById('customBeverageModal').addEventListener('click', (e) => {
        if (e.target.id === 'customBeverageModal') {
            closeAllModals();
        }
    });

    // 섭취 기록 확인 버튼
    document.getElementById('confirmIntakeBtn').addEventListener('click', confirmIntake);
}

// 모든 모달 닫기
function closeAllModals() {
    document.getElementById('intakeModal').classList.remove('active');
    document.getElementById('customBeverageModal').classList.remove('active');
    selectedBeverage = null;
    editingCustomBeverage = null;
}

// 카페인량 자동 계산
function updateCaffeineEstimate() {
    const categoryCode = document.getElementById('customCategory').value;
    const volume = parseInt(document.getElementById('customVolume').value);

    if (!categoryCode || !volume) return;

    const category = categories.find(c => c.code === categoryCode);
    if (category) {
        const estimatedCaffeine = (category.caffeineMgPer100ml * volume / 100).toFixed(1);
        document.getElementById('customCaffeine').value = estimatedCaffeine;
    }
}

// CustomBeverage 로드
async function loadCustomBeverages() {
    try {
        customBeverages = await CustomBeverageAPI.getMyBeverages();
        renderCustomBeverages();
    } catch (error) {
        console.error('커스텀 음료 로드 실패:', error);
    }
}

// CustomBeverage 렌더링
function renderCustomBeverages() {
    const grid = document.getElementById('customBeverageGrid');

    if (customBeverages.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">☕</div>
                <p>나만의 음료를 추가해보세요</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = customBeverages.map(beverage => `
        <div class="beverage-card custom-beverage">
            <div class="beverage-card-actions">
                <button class="btn-icon" onclick="editCustomBeverage(${beverage.id})" title="수정">✏️</button>
                <button class="btn-icon delete" onclick="deleteCustomBeverage(${beverage.id})" title="삭제">🗑️</button>
            </div>
            <div onclick="openIntakeModal(${beverage.id}, 'custom')">
                <div class="beverage-brand">내 음료</div>
                <div class="beverage-name">${beverage.name}</div>
                <div class="beverage-info">
                    <div class="beverage-detail">
                        <div class="beverage-detail-label">용량</div>
                        <div class="beverage-detail-value">${beverage.volumeMl}ml</div>
                    </div>
                    <div class="beverage-detail">
                        <div class="beverage-detail-label">카페인</div>
                        <div class="beverage-detail-value">${Math.round(beverage.caffeineMg)}mg</div>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// CustomBeverage 모달 열기
function openCustomBeverageModal(beverage = null) {
    editingCustomBeverage = beverage;

    if (beverage) {
        // 수정 모드
        document.getElementById('customModalTitle').textContent = '음료 수정';
        document.getElementById('customName').value = beverage.name;

        // 카테고리는 displayName으로 저장되어 있으므로 code를 찾아야 함
        const category = categories.find(c => c.displayName === beverage.category);
        if (category) {
            document.getElementById('customCategory').value = category.code;
        }

        document.getElementById('customVolume').value = beverage.volumeMl;
        document.getElementById('customCaffeine').value = beverage.caffeineMg;
    } else {
        // 추가 모드
        document.getElementById('customModalTitle').textContent = '나만의 음료 추가';
        document.getElementById('customBeverageForm').reset();
    }

    document.getElementById('customBeverageModal').classList.add('active');
}

// CustomBeverage 저장
async function saveCustomBeverage() {
    const name = document.getElementById('customName').value.trim();
    const category = document.getElementById('customCategory').value;
    const volumeMl = parseInt(document.getElementById('customVolume').value);
    const caffeineMg = parseFloat(document.getElementById('customCaffeine').value);

    if (!name || !category || !volumeMl || !caffeineMg) {
        alert('모든 항목을 입력해주세요');
        return;
    }

    try {
        if (editingCustomBeverage) {
            // 수정
            await CustomBeverageAPI.update(editingCustomBeverage.id, {
                name,
                volumeMl,
                caffeineMg
            });
        } else {
            // 추가
            await CustomBeverageAPI.create({
                name,
                category,
                volumeMl,
                caffeineMg
            });
        }

        // 모달 닫기
        closeAllModals();

        // 목록 새로고침
        await loadCustomBeverages();

    } catch (error) {
        alert('저장 실패: ' + error.message);
    }
}

// CustomBeverage 수정
async function editCustomBeverage(beverageId) {
    const beverage = customBeverages.find(b => b.id === beverageId);
    if (beverage) {
        openCustomBeverageModal(beverage);
    }
}

// CustomBeverage 삭제
async function deleteCustomBeverage(beverageId) {
    if (!confirm('이 음료를 삭제하시겠습니까?')) {
        return;
    }

    try {
        await CustomBeverageAPI.delete(beverageId);
        await loadCustomBeverages();
    } catch (error) {
        alert('삭제 실패: ' + error.message);
    }
}

// 오늘 섭취 기록 렌더링
function renderTodayIntakes() {
    const timeline = document.getElementById('intakesTimeline');

    if (todayIntakes.length === 0) {
        timeline.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">☕</div>
                <p>아직 섭취 기록이 없습니다</p>
            </div>
        `;
        return;
    }

    timeline.innerHTML = todayIntakes.map(intake => {
        const time = new Date(intake.consumedAt).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });

        return `
            <div class="intake-item">
                <div class="intake-info">
                    <div class="intake-time">${time}</div>
                    <div class="intake-name">${intake.displayName}</div>
                    <div class="intake-details">${intake.category} · ${intake.volumeMl}ml</div>
                </div>
                <div class="intake-caffeine">${Math.round(intake.caffeineMg)}mg</div>
                <button class="btn-delete" onclick="deleteIntake(${intake.id})">삭제</button>
            </div>
        `;
    }).join('');
}

// 요약 통계 업데이트
function updateSummaryStats() {
    const totalCaffeine = todayIntakes.reduce((sum, intake) => sum + intake.caffeineMg, 0);
    const intakeCount = todayIntakes.length;

    // 카페인 총량 표시
    document.getElementById('totalCaffeine').textContent = Math.round(totalCaffeine);
    document.getElementById('intakeCount').textContent = `${intakeCount}회`;

    // 프로그레스 링 업데이트 (400mg 기준)
    const maxCaffeine = 400;
    const percentage = Math.min((totalCaffeine / maxCaffeine) * 100, 100);
    const circumference = 534.07; // 2 * PI * 85
    const offset = circumference - (circumference * percentage / 100);

    const progressRing = document.getElementById('caffeineProgress');
    progressRing.style.strokeDashoffset = offset;

    // 색상 변경 (과다 섭취 시 경고색)
    if (percentage > 100) {
        progressRing.style.stroke = '#E57373';  // 빨간색
    } else if (percentage > 80) {
        progressRing.style.stroke = '#FF9800';  // 주황색
    } else if (percentage > 50) {
        progressRing.style.stroke = '#FFC857';  // 노란색
    } else {
        progressRing.style.stroke = '#4CAF50';  // 녹색
    }
}

// 섭취 모달 열기
function openIntakeModal(beverageId, type) {
    selectedBeverage = { id: beverageId, type };

    // 선택한 음료 정보 찾기
    let beverage;
    if (type === 'preset') {
        beverage = beverages.find(b => b.id === beverageId);
    } else {
        beverage = customBeverages.find(b => b.id === beverageId);
    }

    if (!beverage) return;

    // 모달에 음료 정보 표시
    const brandHTML = beverage.brandName ? `<div class="beverage-brand">${beverage.brandName}</div>` : '';

    document.getElementById('selectedBeverageInfo').innerHTML = `
        ${brandHTML}
        <div class="beverage-name">${beverage.name}</div>
        <div class="beverage-info">
            <div class="beverage-detail">
                <div class="beverage-detail-label">용량</div>
                <div class="beverage-detail-value">${beverage.volumeMl}ml</div>
            </div>
            <div class="beverage-detail">
                <div class="beverage-detail-label">카페인</div>
                <div class="beverage-detail-value">${Math.round(beverage.caffeineMg)}mg</div>
            </div>
        </div>
    `;

    // 현재 시간으로 기본값 설정
    const now = new Date();
    const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
    document.getElementById('consumedAtInput').value = localDateTime;

    // 모달 표시
    document.getElementById('intakeModal').classList.add('active');
}

// 섭취 기록 확인
async function confirmIntake() {
    if (!selectedBeverage) return;

    const consumedAt = document.getElementById('consumedAtInput').value;

    if (!consumedAt) {
        alert('섭취 시간을 선택해주세요');
        return;
    }

    try {
        // 로컬 시간 그대로 전송 (시간대 문제 해결)
        const isoDateTime = consumedAt + ':00';

        if (selectedBeverage.type === 'preset') {
            await IntakeAPI.recordPreset(selectedBeverage.id, isoDateTime);
        } else {
            await IntakeAPI.recordCustom(selectedBeverage.id, isoDateTime);
        }

        // 모달 닫기
        closeAllModals();

        // 섭취 기록 새로고침
        await loadTodayIntakes();

    } catch (error) {
        alert('섭취 기록 실패: ' + error.message);
    }
}

// 섭취 기록 삭제
async function deleteIntake(intakeId) {
    if (!confirm('이 기록을 삭제하시겠습니까?')) {
        return;
    }

    try {
        await IntakeAPI.delete(intakeId);
        await loadTodayIntakes();
    } catch (error) {
        alert('삭제 실패: ' + error.message);
    }
}