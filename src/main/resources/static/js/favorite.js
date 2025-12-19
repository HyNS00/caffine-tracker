// ========================================
// CaFit - 즐겨찾기
// ========================================

const FAVORITE_MAX_COUNT = 8;

// ========================================
// 즐겨찾기 로드 / UI 동기화
// ========================================

// 즐겨찾기 로드
async function loadFavorites() {
    try {
        AppState.favorites = await FavoriteAPI.getAll();
        renderFavorites();
        // 모든 UI 동기화
        syncAllFavoriteButtons();
    } catch (error) {
        console.error('즐겨찾기 로드 실패:', error);
        AppState.favorites = [];
        renderFavorites();
    }
}

// 모든 즐겨찾기 버튼 상태 동기화
function syncAllFavoriteButtons() {
    // 1. 마신 기록 리스트 다시 렌더링
    if (typeof renderTodayIntakes === 'function') {
        renderTodayIntakes();
    }

    // 2. 음료 검색 결과의 별표 동기화
    updateBeverageGridStars();

    // 3. 커스텀 음료의 별표 동기화
    updateCustomBeverageGridStars();
}

// 음료 검색 결과 별표 동기화
function updateBeverageGridStars() {
    const grid = document.getElementById('beverageGrid');
    if (!grid) return;

    grid.querySelectorAll('.btn-favorite-add').forEach(btn => {
        const type = btn.dataset.type;
        const beverageId = parseInt(btn.dataset.beverageId, 10);

        if (type && beverageId) {
            const isFavorite = checkIfFavorite(type, beverageId);
            if (isFavorite) {
                btn.classList.add('active');
                btn.innerHTML = '★';
            } else {
                btn.classList.remove('active');
                btn.innerHTML = '☆';
            }
        }
    });
}

// 커스텀 음료 별표 동기화
function updateCustomBeverageGridStars() {
    const grid = document.getElementById('customBeverageGrid');
    if (!grid) return;

    grid.querySelectorAll('.btn-favorite-add').forEach(btn => {
        const type = btn.dataset.type;
        const beverageId = parseInt(btn.dataset.beverageId, 10);

        if (type && beverageId) {
            const isFavorite = checkIfFavorite(type, beverageId);
            if (isFavorite) {
                btn.classList.add('active');
                btn.innerHTML = '★';
            } else {
                btn.classList.remove('active');
                btn.innerHTML = '☆';
            }
        }
    });
}

// ========================================
// 즐겨찾기 렌더링
// ========================================

function renderFavorites() {
    const container = document.getElementById('favoritesContainer');
    if (!container) return;

    // 즐겨찾기 개수 표시
    const countEl = document.getElementById('favoritesCount');
    if (countEl) {
        countEl.textContent =
            AppState.favorites.length > 0
                ? `${AppState.favorites.length}/${FAVORITE_MAX_COUNT}`
                : '';
    }

    if (AppState.favorites.length === 0) {
        container.innerHTML = `
            <div class="favorites-empty">
                <div class="favorites-empty-icon">⭐</div>
                <p>자주 마시는 음료를 즐겨찾기에 추가해보세요!</p>
                <span class="favorites-empty-hint">음료 검색 후 별표를 클릭하거나, 마신 기록에서 추가할 수 있어요</span>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="favorites-grid" id="favoritesGrid">
            ${AppState.favorites
        .map(fav => {
            const icon = getCategoryIcon(fav.category);
            const brandLabel = fav.brandName || '내 음료';
            return `
                        <div class="favorite-item"
                             data-favorite-id="${fav.id}"
                             data-type="${fav.type}"
                             data-beverage-id="${fav.beverageId}">
                            <button class="favorite-remove" onclick="event.stopPropagation(); removeFavorite(${fav.id})" title="삭제">×</button>
                            <div class="favorite-icon">${icon}</div>
                            <div class="favorite-info">
                                <span class="favorite-brand">${brandLabel}</span>
                                <span class="favorite-name">${fav.name}</span>
                                <span class="favorite-caffeine">${Math.round(fav.caffeineMg)}mg</span>
                            </div>
                            <button class="favorite-drink-btn" onclick="drinkFavorite(${fav.id})">
                                마시기
                            </button>
                        </div>
                    `;
        })
        .join('')}
        </div>
    `;

    // 드래그앤드롭 설정
    setupFavoriteDragAndDrop();
}

// 즐겨찾기 이벤트 리스너 설정 (필요 시 확장)
function setupFavoriteListeners() {
    // 추가적인 전역 리스너가 필요하면 여기에
}

// ========================================
// 즐겨찾기 추가/삭제
// ========================================

// 즐겨찾기 여부 체크
function checkIfFavorite(type, beverageId) {
    const typeUpper = String(type).toUpperCase();
    return AppState.favorites.some(
        f => f.type === typeUpper && f.beverageId === beverageId
    );
}

// 즐겨찾기 추가
async function addToFavorite(type, beverageId) {
    if (AppState.favorites.length >= FAVORITE_MAX_COUNT) {
        showToast(`즐겨찾기는 최대 ${FAVORITE_MAX_COUNT}개까지 가능합니다`);
        return;
    }

    if (checkIfFavorite(type, beverageId)) {
        showToast('이미 즐겨찾기에 추가된 음료입니다');
        return;
    }

    try {
        await FavoriteAPI.add(type, beverageId);
        await loadFavorites();
        showToast('⭐ 즐겨찾기에 추가되었습니다');
    } catch (error) {
        console.error('즐겨찾기 추가 실패:', error);
        showToast('즐겨찾기 추가 실패: ' + error.message);
    }
}

// 즐겨찾기 삭제
async function removeFavorite(favoriteId) {
    try {
        await FavoriteAPI.delete(favoriteId);
        await loadFavorites();
        showToast('즐겨찾기에서 삭제되었습니다');
    } catch (error) {
        console.error('즐겨찾기 삭제 실패:', error);
        showToast('즐겨찾기 삭제 실패: ' + error.message);
    }
}

// 카드에서 즐겨찾기 토글
async function toggleFavoriteFromCard(card, type) {
    const beverageId = parseInt(
        card.dataset.selectedId || card.dataset.beverageId,
        10
    );
    if (!beverageId) return;

    const typeUpper = String(type).toUpperCase();
    const isFav = checkIfFavorite(typeUpper, beverageId);

    if (isFav) {
        const fav = AppState.favorites.find(
            f => f.type === typeUpper && f.beverageId === beverageId
        );
        if (fav) {
            await removeFavorite(fav.id);
        }
    } else {
        await addToFavorite(typeUpper, beverageId);
    }
}

// ========================================
// 즐겨찾기에서 마시기
// ========================================

async function drinkFavorite(favoriteId) {
    const favorite = AppState.favorites.find(f => f.id === favoriteId);
    if (!favorite) return;

    const type = String(favorite.type || '').toLowerCase();
    const beverageId = favorite.beverageId;

    await onBeverageClick(beverageId, type);
}

// ========================================
// 드래그앤드롭 순서 변경 (부드럽게 개선 / 마우스+터치 지원)
// - 핵심: move 이벤트에서는 좌표만 기록하고 requestAnimationFrame에서 DOM 업데이트
// ========================================

function setupFavoriteDragAndDrop() {
    const grid = document.getElementById('favoritesGrid');
    if (!grid) return;

    let draggedItem = null;
    let placeholder = null;
    let offsetX = 0;
    let offsetY = 0;
    let isDragging = false;
    let startX = 0;
    let startY = 0;

    // rAF 기반 업데이트용
    let rafId = null;
    let latestPoint = null;

    // 카드 전체에 이벤트 바인딩
    grid.querySelectorAll('.favorite-item').forEach(item => {
        item.addEventListener('mousedown', startDrag);
        item.addEventListener('touchstart', startDrag, { passive: false });
    });

    function startDrag(e) {
        // 버튼 클릭은 무시
        if (e.target.closest('button')) return;

        const point = e.touches ? e.touches[0] : e;
        startX = point.clientX;
        startY = point.clientY;

        draggedItem = e.currentTarget;

        // 이벤트 리스너 추가 (실제 드래그는 움직이면 시작)
        document.addEventListener('mousemove', onMove, { passive: false });
        document.addEventListener('mouseup', endDrag);
        document.addEventListener('touchmove', onMove, { passive: false });
        document.addEventListener('touchend', endDrag);
        document.addEventListener('touchcancel', endDrag);
    }

    function onMove(e) {
        if (!draggedItem) return;

        const point = e.touches ? e.touches[0] : e;
        latestPoint = point;

        if (!isDragging) {
            const diffX = Math.abs(point.clientX - startX);
            const diffY = Math.abs(point.clientY - startY);

            // 클릭과 드래그 구분 임계값(부드럽게: 3px)
            if (diffX < 3 && diffY < 3) return;

            // 드래그 시작
            isDragging = true;
            e.preventDefault();

            const rect = draggedItem.getBoundingClientRect();
            offsetX = startX - rect.left;
            offsetY = startY - rect.top;

            // placeholder 생성
            placeholder = document.createElement('div');
            placeholder.className = 'favorite-item-placeholder';
            placeholder.style.width = rect.width + 'px';
            placeholder.style.height = rect.height + 'px';
            draggedItem.parentNode.insertBefore(placeholder, draggedItem);

            // 드래그 중 스타일
            draggedItem.classList.add('dragging');
            draggedItem.style.position = 'fixed';
            draggedItem.style.width = rect.width + 'px';
            draggedItem.style.zIndex = '1000';
            draggedItem.style.pointerEvents = 'none';
        }

        e.preventDefault();

        // rAF로 DOM 업데이트(프레임당 1회)
        if (!rafId) {
            rafId = requestAnimationFrame(updateDragPosition);
        }
    }

    function updateDragPosition() {
        if (!draggedItem || !latestPoint) {
            rafId = null;
            return;
        }

        // 드래그 중인 아이템 위치 업데이트
        draggedItem.style.left = (latestPoint.clientX - offsetX) + 'px';
        draggedItem.style.top = (latestPoint.clientY - offsetY) + 'px';

        // 드롭 위치 계산 (가로 기준)
        const afterElement = getDragAfterElement(grid, latestPoint.clientX);

        if (afterElement == null) {
            grid.appendChild(placeholder);
        } else if (afterElement !== placeholder) {
            grid.insertBefore(placeholder, afterElement);
        }

        rafId = null;
    }

    function endDrag() {
        // 이벤트 리스너 제거
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', endDrag);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', endDrag);
        document.removeEventListener('touchcancel', endDrag);

        // rAF 정리
        if (rafId) {
            cancelAnimationFrame(rafId);
            rafId = null;
        }

        if (!draggedItem) return;

        if (isDragging) {
            // 스타일 복원
            draggedItem.classList.remove('dragging');
            draggedItem.style.position = '';
            draggedItem.style.width = '';
            draggedItem.style.zIndex = '';
            draggedItem.style.left = '';
            draggedItem.style.top = '';
            draggedItem.style.pointerEvents = '';

            // placeholder 위치에 삽입
            if (placeholder && placeholder.parentNode) {
                placeholder.parentNode.insertBefore(draggedItem, placeholder);
                placeholder.remove();
            }

            // 순서 저장
            saveFavoriteOrder();
        }

        placeholder = null;
        draggedItem = null;
        isDragging = false;
        latestPoint = null;
    }
}

function getDragAfterElement(container, x) {
    const draggableElements = [
        ...container.querySelectorAll('.favorite-item:not(.dragging)')
    ];

    return draggableElements.reduce(
        (closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = x - box.left - box.width / 2;

            if (offset < 0 && offset > closest.offset) {
                return { offset, element: child };
            }
            return closest;
        },
        { offset: Number.NEGATIVE_INFINITY, element: null }
    ).element;
}

// ========================================
// 순서 저장 (디바운스)
// ========================================

let saveOrderTimeout = null;

function saveFavoriteOrder() {
    if (saveOrderTimeout) clearTimeout(saveOrderTimeout);

    // 500ms 후에 저장 (연속 드래그 시 마지막만 실행)
    saveOrderTimeout = setTimeout(async () => {
        const grid = document.getElementById('favoritesGrid');
        if (!grid) return;

        const items = grid.querySelectorAll('.favorite-item');
        const favoriteIds = Array.from(items).map(item =>
            parseInt(item.dataset.favoriteId, 10)
        );

        try {
            await FavoriteAPI.updateOrder(favoriteIds);

            // 로컬 상태 업데이트
            const reorderedFavorites = favoriteIds
                .map(id => AppState.favorites.find(f => f.id === id))
                .filter(Boolean);

            AppState.favorites = reorderedFavorites;
        } catch (error) {
            console.error('순서 저장 실패:', error);
            // 실패 시 원래대로 복구
            await loadFavorites();
        }
    }, 500);
}

// ========================================
// 즐겨찾기 CSS 추가
// ========================================

const favoriteStyle = document.createElement('style');
favoriteStyle.textContent = `
    .favorites-section {
        margin-bottom: 1.5rem;
    }

    .favorites-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
    }

    .favorites-header h3 {
        color: var(--primary);
        font-size: 1.1rem;
    }

    .favorites-count {
        font-size: 0.85rem;
        color: var(--text-secondary);
    }

    .favorites-empty {
        background: var(--bg-card);
        border: 2px dashed var(--border);
        border-radius: 16px;
        padding: 2rem;
        text-align: center;
        color: var(--text-secondary);
    }

    .favorites-empty-icon {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
        opacity: 0.5;
    }

    .favorites-empty p {
        margin-bottom: 0.5rem;
    }

    .favorites-empty-hint {
        font-size: 0.8rem;
        color: var(--text-light);
    }

    .favorites-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 1rem;
    }

    @media (max-width: 768px) {
        .favorites-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    .favorite-item {
        background: var(--bg-card);
        border: 1px solid var(--border);
        border-radius: 16px;
        padding: 1rem;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
        transition: all 0.2s ease;
        position: relative;
        user-select: none;
        cursor: grab;
        touch-action: none; /* 터치 드래그 시 스크롤/줌 간섭 줄임 */
        will-change: transform; /* 브라우저 최적화 힌트 */
    }

    .favorite-item:hover {
        border-color: var(--primary);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }

    .favorite-item.dragging {
        opacity: 0.9;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
        border-color: var(--primary);
        cursor: grabbing;
        will-change: left, top;
    }

    .favorite-item-placeholder {
        background: var(--bg-card-hover);
        border: 2px dashed var(--primary);
        border-radius: 16px;
        opacity: 0.5;
    }

    .favorite-remove {
        position: absolute;
        top: 6px;
        right: 6px;
        width: 20px;
        height: 20px;
        border: none;
        background: var(--bg-card-hover);
        color: var(--text-light);
        border-radius: 50%;
        cursor: pointer;
        font-size: 14px;
        line-height: 1;
        opacity: 0;
        transition: all 0.2s;
    }

    .favorite-item:hover .favorite-remove {
        opacity: 1;
    }

    .favorite-remove:hover {
        background: var(--danger);
        color: white;
    }

    .favorite-icon {
        font-size: 1.5rem;
    }

    .favorite-info {
        text-align: center;
    }

    .favorite-brand {
        display: block;
        font-size: 0.7rem;
        color: var(--text-light);
        margin-bottom: 2px;
    }

    .favorite-name {
        display: block;
        font-weight: 600;
        font-size: 0.9rem;
        color: var(--text-primary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100px;
    }

    .favorite-caffeine {
        font-size: 0.8rem;
        color: var(--primary);
        font-weight: 600;
    }

    .favorite-drink-btn {
        width: 100%;
        background: transparent;
        border: 1px solid var(--primary);
        color: var(--primary);
        padding: 0.5rem;
        border-radius: 8px;
        font-size: 0.85rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .favorite-drink-btn:hover {
        background: var(--primary);
        color: var(--bg-main);
    }

    /* 음료 카드 즐겨찾기 버튼 */
    .beverage-actions {
        display: flex;
        gap: 0.5rem;
        align-items: center;
    }

    .btn-favorite-add {
        width: 40px;
        height: 40px;
        background: var(--bg-card-hover);
        border: 1px solid var(--border);
        border-radius: 10px;
        color: var(--text-light);
        font-size: 1.2rem;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .btn-favorite-add:hover {
        border-color: var(--primary);
        color: var(--primary);
    }

    .btn-favorite-add.active {
        background: var(--primary);
        border-color: var(--primary);
        color: var(--bg-main);
    }

    .beverage-actions .btn-drink {
        flex: 1;
    }

    /* 섭취 상세 모달 헤더 개선 */
    .intake-detail-header {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid var(--border);
        margin-bottom: 0.5rem;
    }

    .intake-detail-icon {
        font-size: 2.5rem;
    }

    .intake-detail-title {
        flex: 1;
    }

    /* 3버튼 모달 푸터 */
    .modal-footer-three {
        display: flex;
        gap: 0.75rem;
    }

    .modal-footer-three button {
        flex: 1;
    }

    .btn-favorite {
        padding: 1rem 1.5rem;
        background: rgba(212, 163, 115, 0.15);
        color: var(--primary);
        border: 1px solid var(--primary);
        border-radius: 12px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .btn-favorite:hover {
        background: var(--primary);
        color: var(--bg-main);
    }

    .btn-favorite.active {
        background: var(--primary);
        color: var(--bg-main);
    }

    .btn-favorite.disabled {
        opacity: 0.5;
        cursor: not-allowed;
        pointer-events: none;
    }

    /* 마신 기록 리스트의 미니 즐겨찾기 버튼 */
    .btn-favorite-mini {
        background: none;
        border: none;
        color: var(--text-light);
        cursor: pointer;
        font-size: 1rem;
        padding: 4px 6px;
        border-radius: 6px;
        transition: all 0.2s;
    }

    .btn-favorite-mini:hover {
        color: var(--primary);
        background: rgba(212, 163, 115, 0.1);
    }

    .btn-favorite-mini.active {
        color: var(--primary);
    }
`;
document.head.appendChild(favoriteStyle);
