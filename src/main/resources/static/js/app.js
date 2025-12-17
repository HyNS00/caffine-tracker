// 전역 상태
let beverages = [];
let customBeverages = [];
let categories = [];
let todayIntakes = [];
let selectedBeverage = null;
let editingCustomBeverage = null;
let currentCaffeineStatus = null;
let timelineData = null;
let dailyStatsData = null;
let caffeineChart = null;
let weeklyChart = null;

// 앱 초기화
async function initApp() {
    updateTodayDate();
    await loadCategories();
    await loadCaffeineStatus();
    await loadCustomBeverages();
    await loadTodayIntakes();
    await loadTimeline();
    setupTabListeners();
    setupSearchListener();
    setupCustomBeverageListeners();
    setupModalListeners();
    setupSidebar();
}

// 오늘 날짜 표시
function updateTodayDate() {
    const today = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    document.getElementById('todayDate').textContent = today.toLocaleDateString('ko-KR', options);
}

// ========================================
// 사이드바 설정
// ========================================
function setupSidebar() {
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const menuBtn = document.getElementById('menuBtn');
    const sidebarClose = document.getElementById('sidebarClose');
    const sidebarLogout = document.getElementById('sidebarLogout');

    function openSidebar() {
        sidebar.classList.add('active');
        sidebarOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeSidebar() {
        sidebar.classList.remove('active');
        sidebarOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    menuBtn?.addEventListener('click', openSidebar);
    sidebarClose?.addEventListener('click', closeSidebar);
    sidebarOverlay?.addEventListener('click', closeSidebar);

    // 사이드바 로그아웃
    sidebarLogout?.addEventListener('click', async () => {
        try {
            await AuthAPI.logout();
            sessionStorage.removeItem('user');
            closeSidebar();
            showLoginScreen();
        } catch (error) {
            console.error('로그아웃 실패:', error);
        }
    });

    // 주간 통계 메뉴
    document.getElementById('menuStats')?.addEventListener('click', async (e) => {
        e.preventDefault();
        closeSidebar();
        await openWeeklyStatsModal();
    });

    // 프로필 메뉴
    document.getElementById('menuProfile')?.addEventListener('click', (e) => {
        e.preventDefault();
        closeSidebar();
        alert('프로필 기능은 준비 중입니다.');
    });

    // 설정 메뉴
    document.getElementById('menuSettings')?.addEventListener('click', (e) => {
        e.preventDefault();
        closeSidebar();
        alert('설정 기능은 준비 중입니다.');
    });
}

// ========================================
// 주간 통계 모달
// ========================================
async function openWeeklyStatsModal() {
    const modal = document.getElementById('weeklyStatsModal');
    modal.classList.add('active');

    try {
        dailyStatsData = await StatisticsAPI.getDailyStatistics(7);
        renderWeeklyChart();
        renderWeeklySummary();
    } catch (error) {
        console.error('주간 통계 로드 실패:', error);
    }
}

function renderWeeklyChart() {
    const ctx = document.getElementById('weeklyChart');
    if (!ctx || !dailyStatsData) return;

    if (weeklyChart) {
        weeklyChart.destroy();
    }

    const { dailyStats, dailyLimit } = dailyStatsData;

    const labels = dailyStats.map(stat => {
        const date = new Date(stat.date);
        return date.toLocaleDateString('ko-KR', { weekday: 'short', month: 'numeric', day: 'numeric' });
    });

    const data = dailyStats.map(stat => Math.round(stat.totalCaffeineMg));

    weeklyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '일일 섭취량',
                    data: data,
                    backgroundColor: data.map(v => v > dailyLimit ? 'rgba(229, 115, 115, 0.8)' : 'rgba(44, 110, 73, 0.8)'),
                    borderColor: data.map(v => v > dailyLimit ? '#E57373' : '#2C6E49'),
                    borderWidth: 2,
                    borderRadius: 8,
                },
                {
                    label: '권장량',
                    data: Array(dailyStats.length).fill(dailyLimit),
                    type: 'line',
                    borderColor: '#FF9800',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    pointRadius: 0,
                    fill: false,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.dataset.label}: ${context.raw}mg`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                    },
                    ticks: {
                        callback: function(value) {
                            return value + 'mg';
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                    }
                }
            }
        }
    });
}

function renderWeeklySummary() {
    const container = document.getElementById('weeklySummary');
    if (!container || !dailyStatsData) return;

    const { dailyStats, periodAverage, dailyLimit } = dailyStatsData;

    const totalIntakes = dailyStats.reduce((sum, s) => sum + s.intakeCount, 0);
    const maxDay = dailyStats.reduce((max, s) => s.totalCaffeineMg > max.totalCaffeineMg ? s : max, dailyStats[0]);
    const overLimitDays = dailyStats.filter(s => s.totalCaffeineMg > dailyLimit).length;

    container.innerHTML = `
        <div class="summary-grid">
            <div class="summary-item">
                <div class="summary-value">${Math.round(periodAverage)}<span>mg</span></div>
                <div class="summary-label">일평균 섭취량</div>
            </div>
            <div class="summary-item">
                <div class="summary-value">${totalIntakes}<span>회</span></div>
                <div class="summary-label">총 섭취 횟수</div>
            </div>
            <div class="summary-item ${overLimitDays > 0 ? 'warning' : ''}">
                <div class="summary-value">${overLimitDays}<span>일</span></div>
                <div class="summary-label">권장량 초과일</div>
            </div>
            <div class="summary-item">
                <div class="summary-value">${Math.round(maxDay.totalCaffeineMg)}<span>mg</span></div>
                <div class="summary-label">최대 섭취일</div>
            </div>
        </div>
    `;
}

// ========================================
// 카페인 상태 로드
// ========================================
async function loadCaffeineStatus() {
    try {
        currentCaffeineStatus = await CaffeineAPI.getStatus();
        updateCaffeineStatusUI();
    } catch (error) {
        console.error('카페인 상태 로드 실패:', error);
    }
}

function updateCaffeineStatusUI() {
    if (!currentCaffeineStatus) return;

    const { status, settings, recommendation } = currentCaffeineStatus;

    document.getElementById('totalCaffeine').textContent = Math.round(status.currentMg);
    document.getElementById('dailyLimit').textContent = `${settings.dailyLimitMg}mg`;

    updateProgressRing(status.todayTotalMg, settings.dailyLimitMg);
    updateBedtimePrediction(status.predictedAtBedtimeMg, settings.targetSleepCaffeineMg, status.hoursUntilBedtime);
}

function updateProgressRing(currentMg, limitMg) {
    const percentage = Math.min((currentMg / limitMg) * 100, 100);
    const circumference = 534.07;
    const offset = circumference - (circumference * percentage / 100);

    const progressRing = document.getElementById('caffeineProgress');
    progressRing.style.strokeDashoffset = offset;

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

// ========================================
// 타임라인 차트 (Chart.js 꺾은선 그래프)
// ========================================
async function loadTimeline() {
    try {
        timelineData = await StatisticsAPI.getTimeline(12);
        renderCaffeineChart();
    } catch (error) {
        console.error('타임라인 로드 실패:', error);
    }
}

function renderCaffeineChart() {
    const ctx = document.getElementById('caffeineChart');
    if (!ctx || !timelineData) return;

    if (caffeineChart) {
        caffeineChart.destroy();
    }

    const { dataPoints, targetSleepCaffeine, bedtime } = timelineData;

    const labels = dataPoints.map(point => {
        const time = new Date(point.time);
        return `${time.getHours()}시`;
    });

    const caffeineValues = dataPoints.map(point => Math.round(point.caffeineMg * 10) / 10);
    const targetLine = Array(dataPoints.length).fill(targetSleepCaffeine);

    // 취침 시간 인덱스 찾기
    const bedtimeHour = new Date(bedtime).getHours();
    const bedtimeIndex = dataPoints.findIndex(point => {
        const hour = new Date(point.time).getHours();
        return hour === bedtimeHour;
    });

    caffeineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '체내 카페인',
                    data: caffeineValues,
                    borderColor: '#2C6E49',
                    backgroundColor: 'rgba(44, 110, 73, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: '#2C6E49',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointHoverRadius: 6,
                },
                {
                    label: '목표 수면 카페인',
                    data: targetLine,
                    borderColor: '#E57373',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    pointRadius: 0,
                    fill: false,
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'index',
            },
            plugins: {
                legend: {
                    display: false,
                },
                tooltip: {
                    backgroundColor: 'rgba(45, 48, 71, 0.9)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12,
                    cornerRadius: 8,
                    callbacks: {
                        label: function(context) {
                            if (context.datasetIndex === 0) {
                                return `카페인: ${context.raw}mg`;
                            }
                            return `목표: ${context.raw}mg`;
                        }
                    }
                },
                annotation: bedtimeIndex >= 0 ? {
                    annotations: {
                        bedtimeLine: {
                            type: 'line',
                            xMin: bedtimeIndex,
                            xMax: bedtimeIndex,
                            borderColor: '#9C27B0',
                            borderWidth: 2,
                            borderDash: [3, 3],
                            label: {
                                display: true,
                                content: '취침',
                                position: 'start',
                            }
                        }
                    }
                } : {}
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                    },
                    ticks: {
                        callback: function(value) {
                            return value + 'mg';
                        }
                    }
                },
                x: {
                    grid: {
                        display: false,
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 8,
                    }
                }
            }
        }
    });
}

// ========================================
// 기존 기능들
// ========================================

async function loadCategories() {
    try {
        categories = await BeverageAPI.getCategories();
        populateCategorySelect();
    } catch (error) {
        console.error('카테고리 로드 실패:', error);
    }
}

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

    Object.values(groups).forEach(group => {
        group.sizes.sort((a, b) => a.volumeMl - b.volumeMl);
    });

    return Object.values(groups);
}

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

function selectSize(btn, brandName, name) {
    const card = btn.closest('.beverage-card-grouped');
    card.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    card.dataset.selectedId = btn.dataset.id;
}

async function onGroupedBeverageClick(card) {
    const beverageId = parseInt(card.dataset.selectedId);
    await onBeverageClick(beverageId, 'preset');
}

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

    const now = new Date();
    const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);
    document.getElementById('consumedAtInput').value = localDateTime;

    modal.classList.add('active');
}

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

async function loadTodayIntakes() {
    try {
        todayIntakes = await IntakeAPI.getTodayIntakes();
        renderTodayIntakes();
        updateIntakeCount();
    } catch (error) {
        console.error('섭취 기록 로드 실패:', error);
    }
}

function updateIntakeCount() {
    document.getElementById('intakeCount').textContent = `${todayIntakes.length}회`;
}

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

function setupModalListeners() {
    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllModals();
        });
    });

    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeAllModals();
            }
        });
    });

    document.getElementById('confirmIntakeBtn').addEventListener('click', confirmIntake);
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    selectedBeverage = null;
    editingCustomBeverage = null;
}

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

async function loadCustomBeverages() {
    try {
        customBeverages = await CustomBeverageAPI.getMyBeverages();
        renderCustomBeverages();
    } catch (error) {
        console.error('커스텀 음료 로드 실패:', error);
    }
}

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

async function editCustomBeverage(beverageId) {
    const beverage = customBeverages.find(b => b.id === beverageId);
    if (beverage) {
        openCustomBeverageModal(beverage);
    }
}

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
        await loadTimeline();

    } catch (error) {
        alert('섭취 기록 실패: ' + error.message);
    }
}

async function deleteIntake(intakeId) {
    if (!confirm('이 기록을 삭제하시겠습니까?')) {
        return;
    }

    try {
        await IntakeAPI.delete(intakeId);
        await loadTodayIntakes();
        await loadCaffeineStatus();
        await loadTimeline();
    } catch (error) {
        alert('삭제 실패: ' + error.message);
    }
}