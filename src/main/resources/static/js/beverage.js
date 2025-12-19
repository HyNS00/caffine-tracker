// ========================================
// CaFit - 음료 검색 및 커스텀 음료
// ========================================

// 카테고리 로드
async function loadCategories() {
    try {
        AppState.categories = await BeverageAPI.getCategories();
        populateCategorySelect();
    } catch (error) {
        console.error('카테고리 로드 실패:', error);
    }
}

function populateCategorySelect() {
    const select = document.getElementById('customCategory');
    if (!select) return;

    select.innerHTML = '<option value="">선택하세요</option>';

    AppState.categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.code;
        option.textContent = `${category.displayName} (기본: ${category.defaultServingSizeMl}ml, ${Math.round(category.caffeineMgPer100ml * category.defaultServingSizeMl / 100)}mg)`;
        select.appendChild(option);
    });
}

// ========================================
// 음료 검색
// ========================================
function setupSearchListener() {
    const searchInput = document.getElementById('beverageSearch');
    const searchBtn = document.getElementById('searchBtn');

    searchBtn?.addEventListener('click', performSearch);

    searchInput?.addEventListener('keypress', (e) => {
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
        AppState.beverages = await BeverageAPI.search(keyword);
        renderBeverages(AppState.beverages);
    } catch (error) {
        console.error('검색 실패:', error);
        alert('검색 중 오류가 발생했습니다');
    }
}

// 음료 그룹화 (같은 이름, 다른 사이즈)
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

// 음료 목록 렌더링
function renderBeverages(beverageList) {
    const grid = document.getElementById('beverageGrid');
    if (!grid) return;

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
        const isFavorite = checkIfFavorite('PRESET', firstSize.id);

        return `
            <div class="beverage-card-grouped" data-selected-id="${firstSize.id}">
                <div class="beverage-header">
                    <span class="beverage-brand">${group.brandName}</span>
                    <span class="beverage-name">${group.name}</span>
                </div>
                <div class="size-selector">
                    ${sizesHtml}
                </div>
                <div class="beverage-actions">
                    <button class="btn-favorite-add ${isFavorite ? 'active' : ''}" 
                            data-type="PRESET" 
                            data-beverage-id="${firstSize.id}"
                            onclick="event.stopPropagation(); toggleFavoriteFromCard(this.closest('.beverage-card-grouped'), 'PRESET')" 
                            title="즐겨찾기">
                        ${isFavorite ? '★' : '☆'}
                    </button>
                    <button class="btn-drink" onclick="onGroupedBeverageClick(this.closest('.beverage-card-grouped'))">
                        <span class="btn-drink-icon">☕</span>
                        <span>마시기</span>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function selectSize(btn, brandName, name) {
    const card = btn.closest('.beverage-card-grouped');
    card.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    card.dataset.selectedId = btn.dataset.id;

    // 별표 버튼의 beverageId도 업데이트
    const starBtn = card.querySelector('.btn-favorite-add');
    if (starBtn) {
        const newId = parseInt(btn.dataset.id);
        starBtn.dataset.beverageId = newId;

        // 즐겨찾기 상태 업데이트
        const isFavorite = checkIfFavorite('PRESET', newId);
        if (isFavorite) {
            starBtn.classList.add('active');
            starBtn.innerHTML = '★';
        } else {
            starBtn.classList.remove('active');
            starBtn.innerHTML = '☆';
        }
    }
}

async function onGroupedBeverageClick(card) {
    const beverageId = parseInt(card.dataset.selectedId);
    await onBeverageClick(beverageId, 'preset');
}

// ========================================
// 커스텀 음료
// ========================================
function setupCustomBeverageListeners() {
    document.getElementById('addCustomBtn')?.addEventListener('click', () => {
        openCustomBeverageModal();
    });

    document.getElementById('saveCustomBtn')?.addEventListener('click', saveCustomBeverage);

    document.getElementById('customCategory')?.addEventListener('change', (e) => {
        const selectedCode = e.target.value;
        if (!selectedCode) return;

        const category = AppState.categories.find(c => c.code === selectedCode);
        if (category) {
            document.getElementById('customVolume').value = category.defaultServingSizeMl;
            updateCaffeineEstimate();
        }
    });

    document.getElementById('customVolume')?.addEventListener('input', updateCaffeineEstimate);
}

function updateCaffeineEstimate() {
    const categoryCode = document.getElementById('customCategory').value;
    const volume = parseInt(document.getElementById('customVolume').value);

    if (!categoryCode || !volume) return;

    const category = AppState.categories.find(c => c.code === categoryCode);
    if (category) {
        const isDefaultVolume = volume === category.defaultServingSizeMl;

        if (isDefaultVolume) {
            document.getElementById('customCaffeine').value = category.defaultCaffeineMg.toFixed(1);
        } else {
            const estimatedCaffeine = (category.caffeineMgPer100ml * volume / 100).toFixed(1);
            document.getElementById('customCaffeine').value = estimatedCaffeine;
        }
    }
}

async function loadCustomBeverages() {
    try {
        AppState.customBeverages = await CustomBeverageAPI.getMyBeverages();
        renderCustomBeverages();
    } catch (error) {
        console.error('커스텀 음료 로드 실패:', error);
    }
}

function renderCustomBeverages() {
    const grid = document.getElementById('customBeverageGrid');
    if (!grid) return;

    if (AppState.customBeverages.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">☕</div>
                <p>나만의 음료를 추가해보세요</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = AppState.customBeverages.map(beverage => {
        const isFavorite = checkIfFavorite('CUSTOM', beverage.id);

        return `
        <div class="beverage-card-grouped custom-card" data-beverage-id="${beverage.id}">
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
            <div class="beverage-actions">
                <button class="btn-favorite-add ${isFavorite ? 'active' : ''}" 
                        data-type="CUSTOM" 
                        data-beverage-id="${beverage.id}"
                        onclick="event.stopPropagation(); toggleFavoriteFromCard(this.closest('.beverage-card-grouped'), 'CUSTOM')" 
                        title="즐겨찾기">
                    ${isFavorite ? '★' : '☆'}
                </button>
                <button class="btn-drink" onclick="onBeverageClick(${beverage.id}, 'custom')">
                    <span class="btn-drink-icon">☕</span>
                    <span>마시기</span>
                </button>
            </div>
        </div>
    `}).join('');
}

function openCustomBeverageModal(beverage = null) {
    AppState.editingCustomBeverage = beverage;

    if (beverage) {
        document.getElementById('customModalTitle').textContent = '음료 수정';
        document.getElementById('customName').value = beverage.name;

        const category = AppState.categories.find(c => c.displayName === beverage.category);
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
        if (AppState.editingCustomBeverage) {
            await CustomBeverageAPI.update(AppState.editingCustomBeverage.id, {
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
    const beverage = AppState.customBeverages.find(b => b.id === beverageId);
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