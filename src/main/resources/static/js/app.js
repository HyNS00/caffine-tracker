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
let pollingInterval = null;
let selectedIntakeForDetail = null;

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
    setupChartTabs();
    setupVisibilityChange();
    startPolling();
}

// ========================================
// 폴링 (1분마다 자동 갱신)
// ========================================
function startPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }

    pollingInterval = setInterval(async () => {
        console.log('폴링: 카페인 상태 갱신');
        await loadCaffeineStatus();
        await loadTimeline();
    }, 60000); // 60초 = 1분
}

function stopPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
    }
}

// 페이지 숨김/표시 시 처리
function setupVisibilityChange() {
    document.addEventListener('visibilitychange', async () => {
        if (document.hidden) {
            stopPolling();
        } else {
            // 다시 보이면 즉시 갱신 후 폴링 시작
            await loadCaffeineStatus();
            await loadTimeline();
            startPolling();
        }
    });
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
        stopPolling(); // 폴링 중지
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
            animation: {
                duration: 1500,
                easing: 'easeOutQuart'
            },
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
                    grid: { color: 'rgba(0, 0, 0, 0.05)' },
                    ticks: {
                        callback: function(value) {
                            return value + 'mg';
                        }
                    }
                },
                x: { grid: { display: false } }
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
// 카페인 상태 로드 + 커피컵 게이지
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

    // 커피컵 게이지 업데이트
    updateCoffeeCupGauge(status.currentMg, settings.dailyLimitMg);

    // 일일 권장량 표시
    const dailyLimitEl = document.getElementById('dailyLimit');
    if (dailyLimitEl) {
        dailyLimitEl.textContent = `${settings.dailyLimitMg}mg`;
    }

    // 취침 예측 업데이트
    updateBedtimePrediction(status.predictedAtBedtimeMg, settings.targetSleepCaffeineMg, status.hoursUntilBedtime);
}

// 커피컵 게이지 업데이트
function updateCoffeeCupGauge(currentMg, limitMg) {
    const fillElement = document.getElementById('coffeeFill');
    const valueElement = document.getElementById('coffeeValue');
    const percentElement = document.getElementById('coffeePercent');

    if (!fillElement || !valueElement) return;

    // 퍼센트 계산 (최대 100%)
    const percentage = Math.min((currentMg / limitMg) * 100, 100);

    // 커피 채우기
    fillElement.style.height = `${percentage}%`;

    // 색상 변경 - 커피 톤으로 (진할수록 많이 마신 것)
    if (percentage > 80) {
        fillElement.style.setProperty('--coffee-color-start', '#3E2723');
        fillElement.style.setProperty('--coffee-color-end', '#1B0000');
    } else if (percentage > 50) {
        fillElement.style.setProperty('--coffee-color-start', '#5D4037');
        fillElement.style.setProperty('--coffee-color-end', '#3E2723');
    } else {
        fillElement.style.setProperty('--coffee-color-start', '#8D6E63');
        fillElement.style.setProperty('--coffee-color-end', '#5D4037');
    }

    // 값 표시
    valueElement.textContent = Math.round(currentMg);

    if (percentElement) {
        percentElement.textContent = `${Math.round(percentage)}%`;
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
// 차트 탭 (꺾은선 / 히트맵)
// ========================================
function setupChartTabs() {
    const lineTabBtn = document.getElementById('lineChartTab');
    const heatmapTabBtn = document.getElementById('heatmapTab');
    const lineChartContainer = document.getElementById('lineChartContainer');
    const heatmapContainer = document.getElementById('heatmapContainer');

    lineTabBtn?.addEventListener('click', () => {
        lineTabBtn.classList.add('active');
        heatmapTabBtn.classList.remove('active');
        lineChartContainer.classList.add('active');
        heatmapContainer.classList.remove('active');
    });

    heatmapTabBtn?.addEventListener('click', () => {
        heatmapTabBtn.classList.add('active');
        lineTabBtn.classList.remove('active');
        heatmapContainer.classList.add('active');
        lineChartContainer.classList.remove('active');
        renderHeatmap();
    });
}

// ========================================
// 타임라인 차트 (애니메이션 + 400mg 한계선)
// ========================================
async function loadTimeline() {
    try {
        timelineData = await StatisticsAPI.getTimeline(12);
        renderCaffeineChart();
        renderHeatmap();
    } catch (error) {
        console.error('타임라인 로드 실패:', error);
    }
}

function renderCaffeineChart() {
    const canvas = document.getElementById('caffeineChart');
    if (!canvas || !timelineData || !currentCaffeineStatus) return;

    const { dataPoints, targetSleepCaffeine } = timelineData;
    const dailyLimit = currentCaffeineStatus.settings.dailyLimitMg;

    const labels = dataPoints.map(p => `${new Date(p.time).getHours()}시`);
    const caffeineValues = dataPoints.map(p => Math.round(p.caffeineMg * 10) / 10);
    const targetLine = Array(dataPoints.length).fill(targetSleepCaffeine);
    const limitLine = Array(dataPoints.length).fill(dailyLimit);

    const ctx2d = canvas.getContext('2d');
    const gradient = ctx2d.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(44, 110, 73, 0.4)');
    gradient.addColorStop(1, 'rgba(44, 110, 73, 0.0)');

    // 1) 최초 1회 생성: "0에서 천천히 자라기"
    if (!caffeineChart) {
        const zeros = Array(dataPoints.length).fill(0);

        caffeineChart = new Chart(canvas, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: '체내 카페인',
                        data: zeros,  // 처음엔 0으로 시작
                        borderColor: '#2C6E49',
                        backgroundColor: gradient,
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 5,
                        pointBackgroundColor: '#fff',
                        pointBorderColor: '#2C6E49',
                        pointBorderWidth: 2,
                        pointHoverRadius: 8,
                        pointHoverBackgroundColor: '#2C6E49',
                        pointHoverBorderColor: '#fff',
                        pointHoverBorderWidth: 2,
                    },
                    {
                        label: '목표 수면 카페인',
                        data: targetLine,
                        borderColor: '#E57373',
                        borderWidth: 2,
                        borderDash: [5, 5],
                        pointRadius: 0,
                        fill: false,
                    },
                    {
                        label: '일일 한계량',
                        data: limitLine,
                        borderColor: '#FF9800',
                        borderWidth: 2,
                        borderDash: [10, 5],
                        pointRadius: 0,
                        fill: false,
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: {
                    duration: 3000,  // 3초 동안 천천히 올라옴
                    easing: 'easeOutCubic',
                },
                interaction: {
                    intersect: false,
                    mode: 'index',
                },
                plugins: {
                    legend: { display: false },
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
                                } else if (context.datasetIndex === 1) {
                                    return `목표: ${context.raw}mg`;
                                } else {
                                    return `한계: ${context.raw}mg`;
                                }
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0,0,0,0.05)' },
                        ticks: {
                            callback: function(value) {
                                return value + 'mg';
                            }
                        }
                    },
                    x: {
                        grid: { display: false },
                        ticks: {
                            maxRotation: 0,
                            autoSkip: true,
                            maxTicksLimit: 8,
                        }
                    }
                }
            }
        });

        // 생성 직후 실제 값으로 업데이트 → 0에서 천천히 올라감!
        requestAnimationFrame(() => {
            caffeineChart.data.datasets[0].data = caffeineValues;
            caffeineChart.update();
        });
        return;
    }

    // 2) 이후 갱신(폴링/추가/삭제): 데이터만 교체하고 부드럽게 이동
    const prev = caffeineChart.data.datasets[0].data;
    const same =
        prev.length === caffeineValues.length &&
        prev.every((v, i) => v === caffeineValues[i]) &&
        caffeineChart.data.labels.length === labels.length &&
        caffeineChart.data.labels.every((v, i) => v === labels[i]);

    if (same) return;  // 데이터 동일하면 스킵 (불필요한 애니메이션 방지)

    // 폴링 시에는 짧은 애니메이션으로 부드럽게
    caffeineChart.options.animation = {
        duration: 800,
        easing: 'easeInOutCubic',
    };
    caffeineChart.data.labels = labels;
    caffeineChart.data.datasets[0].data = caffeineValues;
    caffeineChart.data.datasets[1].data = targetLine;
    caffeineChart.data.datasets[2].data = limitLine;
    caffeineChart.update();
}

// 히트맵 렌더링
function renderHeatmap() {
    const container = document.getElementById('heatmapGrid');
    if (!container || !timelineData) return;

    const { dataPoints, targetSleepCaffeine } = timelineData;

    container.innerHTML = dataPoints.map(point => {
        const time = new Date(point.time);
        const hour = time.getHours();
        const value = Math.round(point.caffeineMg);

        // 색상 결정
        let color;
        if (value > targetSleepCaffeine * 3) {
            color = '#C62828'; // 높음
        } else if (value > targetSleepCaffeine * 2) {
            color = '#FF9800'; // 주의
        } else if (value > targetSleepCaffeine) {
            color = '#FFC107'; // 보통
        } else {
            color = '#4CAF50'; // 안전
        }

        return `
            <div class="heatmap-cell" style="background: ${color};">
                <span class="heatmap-time">${hour}시</span>
                <span class="heatmap-value">${value}mg</span>
            </div>
        `;
    }).join('');
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

    // 상세 모달에서 삭제 버튼
    document.getElementById('deleteIntakeFromDetail')?.addEventListener('click', async () => {
        if (!selectedIntakeForDetail) return;

        if (!confirm('이 기록을 삭제하시겠습니까?')) return;

        try {
            await IntakeAPI.delete(selectedIntakeForDetail.id);
            closeAllModals();
            selectedIntakeForDetail = null;

            await loadTodayIntakes();
            await loadCaffeineStatus();
            await loadTimeline();
        } catch (error) {
            alert('삭제 실패: ' + error.message);
        }
    });
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    selectedBeverage = null;
    editingCustomBeverage = null;
    selectedIntakeForDetail = null;
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

// ========================================
// 오늘 섭취 기록 (클릭 시 상세보기)
// ========================================
function renderTodayIntakes() {
    const timeline = document.getElementById('intakesTimeline');

    if (todayIntakes.length === 0) {
        timeline.innerHTML = `
            <div class="empty-state-small">
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
            <div class="intake-item-compact" onclick="showIntakeDetail(${intake.id})">
                <div class="intake-time">${time}</div>
                <div class="intake-details">
                    <span class="intake-name">${intake.beverageName}</span>
                    <span class="intake-caffeine">${Math.round(intake.caffeineMg)}mg</span>
                </div>
                <button class="btn-delete-small" onclick="event.stopPropagation(); deleteIntake(${intake.id})">×</button>
            </div>
        `;
    }).join('');
}

// 섭취 상세 정보 모달 열기
function showIntakeDetail(intakeId) {
    const intake = todayIntakes.find(i => i.id === intakeId);
    if (!intake) return;

    selectedIntakeForDetail = intake;

    const content = document.getElementById('intakeDetailContent');
    const consumedDate = new Date(intake.consumedAt);

    const timeStr = consumedDate.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    const dateStr = consumedDate.toLocaleDateString('ko-KR', {
        month: 'long',
        day: 'numeric',
        weekday: 'short'
    });

    content.innerHTML = `
        <div class="intake-detail-header">
            <div class="intake-detail-name">${intake.beverageName}</div>
            ${intake.brandName ? `<div class="intake-detail-brand">${intake.brandName}</div>` : '<div class="intake-detail-brand custom-label">내 음료</div>'}
        </div>
        
        <div class="intake-detail-grid">
            <div class="intake-detail-item">
                <div class="intake-detail-label">카페인</div>
                <div class="intake-detail-value highlight">${Math.round(intake.caffeineMg)}mg</div>
            </div>
            <div class="intake-detail-item">
                <div class="intake-detail-label">용량</div>
                <div class="intake-detail-value">${intake.volumeMl}ml</div>
            </div>
            <div class="intake-detail-item">
                <div class="intake-detail-label">카테고리</div>
                <div class="intake-detail-value">${intake.category}</div>
            </div>
            <div class="intake-detail-item">
                <div class="intake-detail-label">섭취 시간</div>
                <div class="intake-detail-value">${timeStr}</div>
            </div>
            <div class="intake-detail-item full-width">
                <div class="intake-detail-label">섭취 날짜</div>
                <div class="intake-detail-time">
                    <span>📅</span>
                    <span>${dateStr}</span>
                </div>
            </div>
        </div>
    `;

    document.getElementById('intakeDetailModal').classList.add('active');
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

        // 즉시 갱신
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
        // 즉시 갱신
        await loadTodayIntakes();
        await loadCaffeineStatus();
        await loadTimeline();
    } catch (error) {
        alert('삭제 실패: ' + error.message);
    }
}