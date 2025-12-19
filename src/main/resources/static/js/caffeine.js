// ========================================
// CaFit - 카페인 상태 및 차트
// ========================================

let caffeineChart = null;

// 카페인 상태 로드
async function loadCaffeineStatus() {
    try {
        AppState.currentCaffeineStatus = await CaffeineAPI.getStatus();
        updateCaffeineStatusUI();
    } catch (error) {
        console.error('카페인 상태 로드 실패:', error);
    }
}

// UI 업데이트
function updateCaffeineStatusUI() {
    if (!AppState.currentCaffeineStatus) return;

    const { status, settings, recommendation } = AppState.currentCaffeineStatus;

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

// 취침 예측 업데이트
function updateBedtimePrediction(predictedMg, targetMg, hoursUntilBedtime) {
    const element = document.getElementById('bedtimePrediction');
    if (!element) return;

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
// 타임라인 차트
// ========================================
async function loadTimeline() {
    try {
        AppState.timelineData = await StatisticsAPI.getTimeline(12);
        renderCaffeineChart();
        renderHeatmap();
    } catch (error) {
        console.error('타임라인 로드 실패:', error);
    }
}

function renderCaffeineChart() {
    const canvas = document.getElementById('caffeineChart');
    if (!canvas || !AppState.timelineData || !AppState.currentCaffeineStatus) return;

    const { dataPoints, targetSleepCaffeine } = AppState.timelineData;
    const dailyLimit = AppState.currentCaffeineStatus.settings.dailyLimitMg;

    const labels = dataPoints.map(p => `${new Date(p.time).getHours()}시`);
    const caffeineValues = dataPoints.map(p => Math.round(p.caffeineMg * 10) / 10);
    const targetLine = Array(dataPoints.length).fill(targetSleepCaffeine);
    const limitLine = Array(dataPoints.length).fill(dailyLimit);

    const ctx2d = canvas.getContext('2d');
    const gradient = ctx2d.createLinearGradient(0, 0, 0, 250);
    gradient.addColorStop(0, 'rgba(212, 163, 115, 0.5)');
    gradient.addColorStop(1, 'rgba(212, 163, 115, 0.0)');

    // 최초 1회 생성
    if (!caffeineChart) {
        const zeros = Array(dataPoints.length).fill(0);

        caffeineChart = new Chart(canvas, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: '체내 카페인',
                        data: zeros,
                        borderColor: '#D4A373',
                        backgroundColor: gradient,
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointRadius: 5,
                        pointBackgroundColor: '#1E1E1E',
                        pointBorderColor: '#D4A373',
                        pointBorderWidth: 2,
                        pointHoverRadius: 8,
                        pointHoverBackgroundColor: '#D4A373',
                        pointHoverBorderColor: '#fff',
                        pointHoverBorderWidth: 2,
                    },
                    {
                        label: '목표 수면 카페인',
                        data: targetLine,
                        borderColor: '#66BB6A',
                        borderWidth: 2,
                        borderDash: [5, 5],
                        pointRadius: 0,
                        fill: false,
                    },
                    {
                        label: '일일 한계량',
                        data: limitLine,
                        borderColor: '#EF5350',
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
                    duration: 3000,
                    easing: 'easeOutCubic',
                },
                interaction: {
                    intersect: false,
                    mode: 'index',
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(30, 30, 30, 0.95)',
                        titleColor: '#D4A373',
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
                        grid: { color: 'rgba(255,255,255,0.1)' },
                        ticks: {
                            color: '#B0B0B0',
                            callback: function(value) {
                                return value + 'mg';
                            }
                        }
                    },
                    x: {
                        grid: { display: false },
                        ticks: {
                            color: '#B0B0B0',
                            maxRotation: 0,
                            autoSkip: true,
                            maxTicksLimit: 8,
                        }
                    }
                }
            }
        });

        // 생성 직후 실제 값으로 업데이트
        requestAnimationFrame(() => {
            caffeineChart.data.datasets[0].data = caffeineValues;
            caffeineChart.update();
        });
        return;
    }

    // 이후 갱신
    const prev = caffeineChart.data.datasets[0].data;
    const same =
        prev.length === caffeineValues.length &&
        prev.every((v, i) => v === caffeineValues[i]) &&
        caffeineChart.data.labels.length === labels.length &&
        caffeineChart.data.labels.every((v, i) => v === labels[i]);

    if (same) return;

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
    if (!container || !AppState.timelineData) return;

    const { dataPoints, targetSleepCaffeine } = AppState.timelineData;

    container.innerHTML = dataPoints.map(point => {
        const time = new Date(point.time);
        const hour = time.getHours();
        const value = Math.round(point.caffeineMg);

        let color;
        if (value > targetSleepCaffeine * 3) {
            color = '#EF5350';
        } else if (value > targetSleepCaffeine * 2) {
            color = '#FFA726';
        } else if (value > targetSleepCaffeine) {
            color = '#FFEE58';
        } else {
            color = '#66BB6A';
        }

        return `
            <div class="heatmap-cell" style="background: ${color};">
                <span class="heatmap-time">${hour}시</span>
                <span class="heatmap-value">${value}mg</span>
            </div>
        `;
    }).join('');
}