// ========================================
// CaFit - 주간 통계
// ========================================

let weeklyChart = null;

// 주간 통계 모달 열기
async function openWeeklyStatsModal() {
    const modal = document.getElementById('weeklyStatsModal');
    modal.classList.add('active');

    try {
        AppState.dailyStatsData = await StatisticsAPI.getDailyStatistics(7);
        AppState.topBeverages = await StatisticsAPI.getTopBeverages(7);
        renderWeeklyChart();
        renderWeeklySummary();
        renderTopBeverages();
    } catch (error) {
        console.error('주간 통계 로드 실패:', error);
    }
}

// 주간 차트 렌더링
function renderWeeklyChart() {
    const ctx = document.getElementById('weeklyChart');
    if (!ctx || !AppState.dailyStatsData) return;

    if (weeklyChart) {
        weeklyChart.destroy();
    }

    const { dailyStats, dailyLimit } = AppState.dailyStatsData;

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

// 주간 요약 렌더링
function renderWeeklySummary() {
    const container = document.getElementById('weeklySummary');
    if (!container || !AppState.dailyStatsData) return;

    const { dailyStats, periodAverage, dailyLimit } = AppState.dailyStatsData;

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
// top 음료
function renderTopBeverages() {
    const container = document.getElementById('topBeveragesContainer');
    if (!container || !AppState.topBeverages) return;

    if (AppState.topBeverages.length === 0) {
        container.innerHTML = `
            <div class="top-beverages-empty">
                <p>이번 주 섭취 기록이 없습니다</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <h4>🏆 이번 주 TOP 음료</h4>
        <div class="top-beverages-list">
            ${AppState.topBeverages.map((bev, index) => `
                <div class="top-beverage-item">
                    <span class="top-rank">${index + 1}</span>
                    <div class="top-beverage-info">
                        <span class="top-beverage-name">${bev.brandName ? bev.brandName + ' ' : ''}${bev.beverageName}</span>
                        <span class="top-beverage-detail">${bev.volumeMl}ml · ${bev.count}회</span>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}