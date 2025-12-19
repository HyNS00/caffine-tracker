// ========================================
// CaFit 메인 앱 - 전역 상태 및 초기화
// ========================================

// 카테고리별 아이콘 매핑
const CategoryIcons = {
    // 커피류
    '에스프레소': '☕',
    '아메리카노': '☕',
    '라떼': '🥛',
    '카푸치노': '☕',
    '모카': '🍫',
    '콜드브루': '🧊',

    // 스무디
    '커피 스무디': '🍨',
    '과일 스무디': '🍹',

    // 에너지/차류
    '에너지 음료': '⚡',
    '홍차': '🍵',
    '녹차': '🍃',
    '밀크티': '🧋',

    // 기타
    '아이스티': '🍑',

    // 기본값
    'default': '☕'
};

// 카테고리 아이콘 가져오기
function getCategoryIcon(category) {
    return CategoryIcons[category] || CategoryIcons['default'];
}

// 전역 상태
const AppState = {
    beverages: [],
    customBeverages: [],
    categories: [],
    todayIntakes: [],
    favorites: [],
    selectedBeverage: null,
    editingCustomBeverage: null,
    currentCaffeineStatus: null,
    timelineData: null,
    dailyStatsData: null,
    selectedIntakeForDetail: null,
    pollingInterval: null
};

// 앱 초기화
async function initApp() {
    updateTodayDate();
    await loadCategories();
    await loadCaffeineStatus();
    await loadCustomBeverages();
    await loadTodayIntakes();
    await loadTimeline();
    await loadFavorites();
    setupEventListeners();
    startPolling();
}

// 오늘 날짜 표시
function updateTodayDate() {
    const today = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    const dateEl = document.getElementById('todayDate');
    if (dateEl) {
        dateEl.textContent = today.toLocaleDateString('ko-KR', options);
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    setupTabListeners();
    setupSearchListener();
    setupCustomBeverageListeners();
    setupModalListeners();
    setupChartTabs();
    setupVisibilityChange();
    setupFavoriteListeners();
}

// 탭 전환 리스너
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

// 모달 리스너
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

    document.getElementById('confirmIntakeBtn')?.addEventListener('click', confirmIntake);

    // 상세 모달에서 삭제 버튼
    document.getElementById('deleteIntakeFromDetail')?.addEventListener('click', async () => {
        if (!AppState.selectedIntakeForDetail) return;

        if (!confirm('이 기록을 삭제하시겠습니까?')) return;

        try {
            await IntakeAPI.delete(AppState.selectedIntakeForDetail.id);
            closeAllModals();
            AppState.selectedIntakeForDetail = null;

            await loadTodayIntakes();
            await loadCaffeineStatus();
            await loadTimeline();
        } catch (error) {
            alert('삭제 실패: ' + error.message);
        }
    });

    // 섭취 기록 모달에서 즐겨찾기 토글 버튼
    document.getElementById('toggleFavoriteFromIntake')?.addEventListener('click', async () => {
        if (!AppState.selectedBeverage) return;

        const { type, id } = AppState.selectedBeverage;
        const typeUpper = type.toUpperCase();

        await toggleFavoriteAndUpdateButton(typeUpper, id, 'toggleFavoriteFromIntake');
    });

    // 상세 모달에서 즐겨찾기 토글 버튼
    document.getElementById('addFavoriteFromDetail')?.addEventListener('click', async () => {
        if (!AppState.selectedIntakeForDetail) return;

        const intake = AppState.selectedIntakeForDetail;
        const btn = document.getElementById('addFavoriteFromDetail');

        try {
            // 프리셋 음료 검색
            if (intake.brandName) {
                const searchResult = await BeverageAPI.search(intake.beverageName);
                if (searchResult && searchResult.length > 0) {
                    const matched = searchResult.find(b =>
                        b.name === intake.beverageName &&
                        b.brandName === intake.brandName &&
                        b.volumeMl === intake.volumeMl
                    ) || searchResult.find(b =>
                        b.name === intake.beverageName &&
                        b.brandName === intake.brandName
                    ) || searchResult[0];

                    await toggleFavoriteAndUpdateButton('PRESET', matched.id, 'addFavoriteFromDetail');
                    return;
                }
            }

            // 커스텀 음료
            const customMatch = AppState.customBeverages.find(c =>
                c.name === intake.beverageName
            );

            if (customMatch) {
                await toggleFavoriteAndUpdateButton('CUSTOM', customMatch.id, 'addFavoriteFromDetail');
            } else {
                showToast('즐겨찾기에 추가할 수 없는 음료입니다');
            }
        } catch (error) {
            console.error('즐겨찾기 처리 실패:', error);
            showToast('즐겨찾기 처리 실패: ' + error.message);
        }
    });
}

// 즐겨찾기 토글 및 버튼 업데이트
async function toggleFavoriteAndUpdateButton(type, beverageId, buttonId) {
    const btn = document.getElementById(buttonId);
    if (!btn) return;

    const isFavorite = checkIfFavorite(type, beverageId);

    try {
        if (isFavorite) {
            // 즐겨찾기 삭제
            const favorite = AppState.favorites.find(f => f.type === type && f.beverageId === beverageId);
            if (favorite) {
                await FavoriteAPI.delete(favorite.id);
                await loadFavorites();
                btn.classList.remove('active');
                btn.innerHTML = '☆ 즐겨찾기';
                showToast('즐겨찾기에서 삭제되었습니다');
            }
        } else {
            // 즐겨찾기 추가
            await addToFavorite(type, beverageId);
            btn.classList.add('active');
            btn.innerHTML = '★ 즐겨찾기됨';
        }
    } catch (error) {
        console.error('즐겨찾기 토글 실패:', error);
        showToast(error.message);
    }
}

// 모든 모달 닫기
function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    AppState.selectedBeverage = null;
    AppState.editingCustomBeverage = null;
    AppState.selectedIntakeForDetail = null;
}

// ========================================
// 폴링 (1분마다 자동 갱신)
// ========================================
function startPolling() {
    if (AppState.pollingInterval) {
        clearInterval(AppState.pollingInterval);
    }

    AppState.pollingInterval = setInterval(async () => {
        console.log('폴링: 카페인 상태 갱신');
        await loadCaffeineStatus();
        await loadTimeline();
    }, 60000);
}

function stopPolling() {
    if (AppState.pollingInterval) {
        clearInterval(AppState.pollingInterval);
        AppState.pollingInterval = null;
    }
}

// 페이지 숨김/표시 시 처리
function setupVisibilityChange() {
    document.addEventListener('visibilitychange', async () => {
        if (document.hidden) {
            stopPolling();
        } else {
            await loadCaffeineStatus();
            await loadTimeline();
            startPolling();
        }
    });
}

// ========================================
// 토스트 메시지
// ========================================
function showToast(message, options = {}) {
    const { duration = 3000, action = null, actionText = '' } = options;

    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast-message');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'toast-message';

    if (action) {
        toast.innerHTML = `
            <span class="toast-text">${message}</span>
            <button class="toast-action">${actionText}</button>
        `;
        toast.querySelector('.toast-action').addEventListener('click', () => {
            action();
            toast.remove();
        });
    } else {
        toast.textContent = message;
    }

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastOut 0.3s ease-in forwards';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// 토스트 애니메이션 CSS 추가
const toastStyle = document.createElement('style');
toastStyle.textContent = `
    .toast-message {
        position: fixed;
        bottom: 30px;
        left: 50%;
        transform: translateX(-50%);
        background: var(--primary);
        color: var(--bg-main);
        padding: 12px 24px;
        border-radius: 50px;
        font-weight: 600;
        z-index: 9999;
        animation: toastIn 0.3s ease-out;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .toast-action {
        background: rgba(0,0,0,0.2);
        border: none;
        color: inherit;
        padding: 6px 12px;
        border-radius: 20px;
        cursor: pointer;
        font-weight: 600;
        font-size: 0.85rem;
        transition: background 0.2s;
    }
    .toast-action:hover {
        background: rgba(0,0,0,0.3);
    }
    @keyframes toastIn {
        from { opacity: 0; transform: translateX(-50%) translateY(20px); }
        to { opacity: 1; transform: translateX(-50%) translateY(0); }
    }
    @keyframes toastOut {
        from { opacity: 1; transform: translateX(-50%) translateY(0); }
        to { opacity: 0; transform: translateX(-50%) translateY(20px); }
    }
`;
document.head.appendChild(toastStyle);