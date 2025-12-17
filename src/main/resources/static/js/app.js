// 전역 상태
let beverages = [];
let customBeverages = [];
let categories = [];
let todayIntakes = [];
let selectedBeverage = null;
let editingCustomBeverage = null;
let currentCaffeineStatus = null;

// 앱 초기화
async function initApp() {
    updateTodayDate();
    await loadCategories();
    await loadCaffeineStatus();
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

// 카페인 상태 로드
async function loadCaffeineStatus() {
    try {
        currentCaffeineStatus = await CaffeineAPI.getStatus();
        updateCaffeineStatusUI();
    } catch (error) {
        console.error('카페인 상태 로드 실패:', error);
    }
}

// 카페인 상태 UI 업데이트
function updateCaffeineStatusUI() {
    if (!currentCaffeineStatus) return;

    const { status, settings, recommendation } = currentCaffeineStatus;

    // 현재 체내 카페인 표시
    document.getElementById('totalCaffeine').textContent = Math.round(status.currentMg);

    // 프로그레스 링 업데이트
    updateProgressRing(status.todayTotalMg, settings.dailyLimitMg);

    // 취침 시 예상 표시
    updateBedtimePrediction(status.predictedAtBedtimeMg, settings.targetSleepCaffeineMg, status.hoursUntilBedtime);

    // 상태 배지 업데이트
    updateRecommendationBadge(recommendation);
}

// 프로그레스 링 업데이트
function updateProgressRing(currentMg, limitMg) {
    const percentage = Math.min((currentMg / limitMg) * 100, 100);
    const circumference = 534.07;
    const offset = circumference - (circumference * percentage / 100);

    const progressRing = document.getElementById('caffeineProgress');
    progressRing.style.strokeDashoffset = offset;

    // 색상 변경
    if (percentage > 100) {
        progressRing.style.stroke = '#E57373';
    } else if (percentage > 80) {
        progressRing.style.stroke = '#FF9800';
    } else if (percentage > 50) {
        progressRing.style.stroke = '#FFC857';
    } else {
        progressRing.style.stroke = '#4CAF50';
    }
}

// 취침 시 예상 카페인 표시
function updateBedtimePrediction(predictedMg, targetMg, hoursUntilBedtime) {
    const element = document.getElementById('bedtimePrediction');
    if (element) {
        const isOver = predictedMg > targetMg;
        const hours = Math.floor(hoursUntilBedtime);
        const minutes = Math.round((hoursUntilBedtime - hours) * 60);

        element.innerHTML = `
            <div class="bedtime-card ${isOver ? 'warning' : 'safe'}">
                <div class="bedtime-icon">${isOver ? '🌙' : '😴'}</div>
                <div class="bedtime-info">
                    <span class="bedtime-label">취침까지 ${hours}시간 ${minutes}분</span>
                    <span class="bedtime-value">${Math.round(predictedMg)}mg <span class="bedtime-target">/ ${targetMg}mg</span></span>
                </div>
            </div>
        `;
    }
}

// 상태 배지 업데이트
function updateRecommendationBadge(recommendation) {
    const badge = document.getElementById('recommendationBadge');
    if (!badge) return;

    const config = {
        'SAFE': { class: 'badge-safe', text: '안전', icon: '✓' },
        'WARNING': { class: 'badge-warning', text: '주의', icon: '!' },
        'DANGER': { class: 'badge-danger', text: '위험', icon: '✕' }
    };

    const { class: badgeClass, text, icon } = config[recommendation] || config['SAFE'];
    badge.className = `recommendation-badge ${badgeClass}`;
    badge.innerHTML = `<span class="badge-icon">${icon}</span><span class="badge-text">${text}</span>`;
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

// 음료 그룹핑 (같은 브랜드+이름끼리)
function groupBeverages(beverageList) {
    const groups = {};

    beverageList.forEach(beverage => {
        const key = `${beverage.brandName}-${beverage.name}`;
        if (!groups[key]) {
            groups[key] = {
                brandName: beverage.brandName,
                name: beverage.name,
                sizes: []
            };
        }
        groups[key].sizes.push({
            id: beverage.id,
            volumeMl: beverage.volumeMl,
            caffeineMg: beverage.caffeineMg
        });
    });

    // 용량순 정렬
    Object.values(groups).forEach(group => {
        group.sizes.sort((a, b) => a.volumeMl - b.volumeMl);
    });

    return Object.values(groups);
}

// 음료 목록 렌더링 (그룹핑 적용)
function renderBeverages(beverageList) {
    const grid = document.getElementById('beverageGrid');

    if (beverageList.length === 0) {
        grid.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🔍</div><p>검색 결과가 없습니다</p></div>';
        return;
    }

    const groups = groupBeverages(beverageList);

    grid.innerHTML = groups.map(group => {
        const sizesHtml = group.sizes.map((size, index) => `
            <button class="size-btn ${index === 0 ? 'active' : ''}" 
                    data-id="${size.id}" 
                    data-volume="${size.volumeMl}" 
                    data-caffeine="${size.caffeineMg}"
                    onclick="selectSize(this, '${group.brandName}', '${group.name}')">
                <span class="size-volume">${size.volumeMl}ml</span>
                <span class="size-caffeine">${Math.round(size.caffeineMg)}mg</span>
            </button>
        `).join('');

        const firstSize = group.sizes[0];

        return `
            <div class="beverage-card-grouped" data-selected-id="${firstSize.id}">
                <div class="beverage-header">
                    <span class="beverage-brand">${group.brandName}</span>
                    <span class="beverage-name">${group.name}</span>
                </div>
                <div class="size-selector">
                    ${sizesHtml}
                </div>
                <button class="btn-drink" onclick="onGroupedBeverageClick(this.closest('.beverage-card-grouped'))">
                    <span class="btn-drink-icon">☕</span>
                    <span>마시기</span>
                </button>
            </div>
        `;
    }).join('');
}

// 사이즈 선택
function selectSize(btn, brandName, name) {
    const card = btn.closest('.beverage-card-grouped');

    // 활성화 상태 변경
    card.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    // 선택된 ID 업데이트
    card.dataset.selectedId = btn.dataset.id;
}

// 그룹화된 음료 클릭
async function onGroupedBeverageClick(card) {
    const beverageId = parseInt(card.dataset.selectedId);
    await onBeverageClick(beverageId, 'preset');
}

// 음료 클릭 시 체크 후 모달 표시
async function onBeverageClick(beverageId, type) {
    try {
        const checkResult = type === 'preset'
            ? await CaffeineAPI.checkPreset(beverageId)
            : await CaffeineAPI.checkCustom(beverageId);

        showDrinkCheckModal(beverageId, type, checkResult);
    } catch (error) {
        console.error('음료 체크 실패:', error);
        openIntakeModal(beverageId, type);
    }
}

// 음료 체크 결과 모달 표시 (현대적 UI)
function showDrinkCheckModal(beverageId, type, result) {
    selectedBeverage = { id: beverageId, type };

    const modal = document.getElementById('intakeModal');
    const beverageInfo = document.getElementById('selectedBeverageInfo');

    const recommendationConfig = {
        'SAFE': { class: 'result-safe', icon: '✓', message: '안전하게 마실 수 있어요' },
        'WARNING': { class: 'result-warning', icon: '!', message: '수면에 영향을 줄 수 있어요' },
        'DANGER': { class: 'result-danger', icon: '✕', message: '오늘은 그만 마시는 게 좋아요' }
    };

    const config = recommendationConfig[result.recommendation];

    beverageInfo.innerHTML = `
        <div class="drink-check-result">
            <div class="drink-header">
                <h4 class="drink-name">${result.beverage.name}</h4>
                <span class="drink-caffeine">${result.beverage.caffeineMg}mg</span>
            </div>
            
            <div class="caffeine-comparison">
                <div class="comparison-item before">
                    <div class="comparison-label">현재</div>
                    <div class="comparison-main">
                        <span class="comparison-value">${result.before.currentMg}</span>
                        <span class="comparison-unit">mg</span>
                    </div>
                    <div class="comparison-bedtime">
                        <span class="bedtime-icon-small">🌙</span>
                        <span>${result.before.predictedAtBedtimeMg}mg</span>
                    </div>
                </div>
                
                <div class="comparison-arrow">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                
                <div class="comparison-item after ${result.recommendation.toLowerCase()}">
                    <div class="comparison-label">마신 후</div>
                    <div class="comparison-main">
                        <span class="comparison-value">${result.after.currentMg}</span>
                        <span class="comparison-unit">mg</span>
                    </div>
                    <div class="comparison-bedtime">
                        <span class="bedtime-icon-small">🌙</span>
                        <span>${result.after.predictedAtBedtimeMg}mg</span>
                    </div>
                </div>
            </div>
            
            <div class="result-message ${config.class}">
                <span class="result-icon">${config.icon}</span>
                <span class="result-text">${config.message}</span>
            </div>
        </div>
    `;

    // 현재 시간으로 기본값 설정
    const now = new Date();
    const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
    document.getElementById('consumedAtInput').value = localDateTime;

    modal.classList.add('active');
}

// 검색 리스너 설정
function setupSearchListener() {
    const searchInput = document.getElementById('beverageSearch');
    const searchBtn = document.getElementById('searchBtn');

    searchBtn.addEventListener('click', performSearch);

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
        updateIntakeCount();
    } catch (error) {
        console.error('섭취 기록 로드 실패:', error);
    }
}

// 섭취 횟수 업데이트
function updateIntakeCount() {
    document.getElementById('intakeCount').textContent = `${todayIntakes.length}회`;
}

// 탭 리스너 설정
function setupTabListeners() {
    document.querySelectorAll('.beverage-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;

            document.querySelectorAll('.beverage-tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            document.querySelectorAll('.beverage-tab-content').forEach(content => content.classList.remove('active'));
            document.getElementById(`${tab}Tab`).classList.add('active');
        });
    });
}

// CustomBeverage 리스너 설정
function setupCustomBeverageListeners() {
    document.getElementById('addCustomBtn').addEventListener('click', () => {
        openCustomBeverageModal();
    });

    document.getElementById('saveCustomBtn').addEventListener('click', saveCustomBeverage);

    document.getElementById('customCategory').addEventListener('change', (e) => {
        const selectedCode = e.target.value;
        if (!selectedCode) return;

        const category = categories.find(c => c.code === selectedCode);
        if (category) {
            document.getElementById('customVolume').value = category.defaultServingSizeMl;
            updateCaffeineEstimate();
        }
    });

    document.getElementById('customVolume').addEventListener('input', updateCaffeineEstimate);
}

// 모달 리스너 설정
function setupModalListeners() {
    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllModals();
        });
    });

    document.getElementById('intakeModal').addEventListener('click', (e) => {
        if (e.target.id === 'intakeModal') {
            closeAllModals();
        }
    });

    document.getElementById('customBeverageModal').addEventListener('click', (e) => {
        if (e.target.id === 'customBeverageModal') {
            closeAllModals();
        }
    });

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
        <div class="beverage-card-grouped custom-card">
            <div class="beverage-card-actions">
                <button class="btn-icon-modern" onclick="event.stopPropagation(); editCustomBeverage(${beverage.id})" title="수정">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                </button>
                <button class="btn-icon-modern delete" onclick="event.stopPropagation(); deleteCustomBeverage(${beverage.id})" title="삭제">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3,6 5,6 21,6"/>
                        <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2v2"/>
                    </svg>
                </button>
            </div>
            <div class="beverage-header">
                <span class="beverage-brand custom-badge">내 음료</span>
                <span class="beverage-name">${beverage.name}</span>
            </div>
            <div class="custom-info">
                <div class="info-item">
                    <span class="info-value">${beverage.volumeMl}</span>
                    <span class="info-label">ml</span>
                </div>
                <div class="info-divider"></div>
                <div class="info-item">
                    <span class="info-value">${Math.round(beverage.caffeineMg)}</span>
                    <span class="info-label">mg</span>
                </div>
            </div>
            <button class="btn-drink" onclick="onBeverageClick(${beverage.id}, 'custom')">
                <span class="btn-drink-icon">☕</span>
                <span>마시기</span>
            </button>
        </div>
    `).join('');
}

// CustomBeverage 모달 열기
function openCustomBeverageModal(beverage = null) {
    editingCustomBeverage = beverage;

    if (beverage) {
        document.getElementById('customModalTitle').textContent = '음료 수정';
        document.getElementById('customName').value = beverage.name;

        const category = categories.find(c => c.displayName === beverage.category);
        if (category) {
            document.getElementById('customCategory').value = category.code;
        }

        document.getElementById('customVolume').value = beverage.volumeMl;
        document.getElementById('customCaffeine').value = beverage.caffeineMg;
    } else {
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
            await CustomBeverageAPI.update(editingCustomBeverage.id, {
                name,
                volumeMl,
                caffeineMg
            });
        } else {
            await CustomBeverageAPI.create({
                name,
                category,
                volumeMl,
                caffeineMg
            });
        }

        closeAllModals();
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
            <div class="intake-item-modern">
                <div class="intake-time-badge">${time}</div>
                <div class="intake-content">
                    <div class="intake-name">${intake.displayName}</div>
                    <div class="intake-meta">${intake.category} · ${intake.volumeMl}ml</div>
                </div>
                <div class="intake-caffeine-badge">${Math.round(intake.caffeineMg)}mg</div>
                <button class="btn-delete-modern" onclick="deleteIntake(${intake.id})">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3,6 5,6 21,6"/>
                        <path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a2,2,0,0,1,2-2h4a2,2,0,0,1,2,2v2"/>
                    </svg>
                </button>
            </div>
        `;
    }).join('');
}

// 기존 섭취 모달 열기 (폴백용)
function openIntakeModal(beverageId, type) {
    selectedBeverage = { id: beverageId, type };

    let beverage;
    if (type === 'preset') {
        beverage = beverages.find(b => b.id === beverageId);
    } else {
        beverage = customBeverages.find(b => b.id === beverageId);
    }

    if (!beverage) return;

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

    const now = new Date();
    const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
    document.getElementById('consumedAtInput').value = localDateTime;

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
        const isoDateTime = consumedAt + ':00';

        if (selectedBeverage.type === 'preset') {
            await IntakeAPI.recordPreset(selectedBeverage.id, isoDateTime);
        } else {
            await IntakeAPI.recordCustom(selectedBeverage.id, isoDateTime);
        }

        closeAllModals();

        await loadTodayIntakes();
        await loadCaffeineStatus();

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
        await loadCaffeineStatus();
    } catch (error) {
        alert('삭제 실패: ' + error.message);
    }
}