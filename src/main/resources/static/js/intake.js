// ========================================
// CaFit - 섭취 기록
// ========================================

// 오늘 섭취 기록 로드
async function loadTodayIntakes() {
    try {
        AppState.todayIntakes = await IntakeAPI.getTodayIntakes();
        renderTodayIntakes();
        updateIntakeCount();
    } catch (error) {
        console.error('섭취 기록 로드 실패:', error);
    }
}

function updateIntakeCount() {
    const countEl = document.getElementById('intakeCount');
    if (countEl) {
        countEl.textContent = `${AppState.todayIntakes.length}회`;
    }
}

// 오늘 섭취 기록 렌더링
function renderTodayIntakes() {
    const timeline = document.getElementById('intakesTimeline');
    if (!timeline) return;

    if (AppState.todayIntakes.length === 0) {
        timeline.innerHTML = `
            <div class="empty-state-small">
                <p>아직 섭취 기록이 없습니다</p>
            </div>
        `;
        return;
    }

    timeline.innerHTML = AppState.todayIntakes.map(intake => {
        const time = new Date(intake.consumedAt).toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit'
        });

        // 즐겨찾기 상태 확인 (즐겨찾기 목록에서 이름으로 매칭)
        const isFavorite = checkIntakeFavoriteStatus(intake);

        return `
            <div class="intake-item-compact" onclick="showIntakeDetail(${intake.id})">
                <div class="intake-time">${time}</div>
                <div class="intake-details">
                    <span class="intake-name">${intake.beverageName}</span>
                    <span class="intake-caffeine">${Math.round(intake.caffeineMg)}mg</span>
                </div>
                <button class="btn-favorite-mini ${isFavorite ? 'active' : ''}" 
                        onclick="event.stopPropagation(); toggleFavoriteFromIntakeList(${intake.id})"
                        data-intake-id="${intake.id}"
                        title="즐겨찾기">${isFavorite ? '★' : '☆'}</button>
                <button class="btn-delete-small" onclick="event.stopPropagation(); deleteIntake(${intake.id})">×</button>
            </div>
        `;
    }).join('');
}

// intake가 즐겨찾기에 있는지 확인
function checkIntakeFavoriteStatus(intake) {
    if (intake.sourceType && intake.sourceBeverageId) {
        return checkIfFavorite(intake.sourceType, intake.sourceBeverageId);
    }
    return false;
}

// 마신 기록 리스트의 즐겨찾기 버튼 상태 업데이트
async function updateIntakeListFavoriteButtons() {
    for (const intake of AppState.todayIntakes) {
        const btn = document.querySelector(`[data-intake-id="${intake.id}"]`);
        if (!btn) continue;

        try {
            let isFavorite = false;

            if (intake.brandName) {
                // 프리셋 음료 - 검색해서 찾기
                const searchResult = await BeverageAPI.search(intake.beverageName);
                if (searchResult && searchResult.length > 0) {
                    const matched = searchResult.find(b =>
                        b.name === intake.beverageName &&
                        b.brandName === intake.brandName
                    );
                    if (matched) {
                        isFavorite = checkIfFavorite('PRESET', matched.id);
                    }
                }
            } else {
                // 커스텀 음료
                const customMatch = AppState.customBeverages.find(c =>
                    c.name === intake.beverageName
                );
                if (customMatch) {
                    isFavorite = checkIfFavorite('CUSTOM', customMatch.id);
                }
            }

            if (isFavorite) {
                btn.classList.add('active');
                btn.innerHTML = '★';
            }
        } catch (error) {
            // 조용히 실패
        }
    }
}

// 마신 기록 리스트에서 즐겨찾기 토글
async function toggleFavoriteFromIntakeList(intakeId) {
    const intake = AppState.todayIntakes.find(i => i.id === intakeId);
    if (!intake) return;

    const btn = document.querySelector(`[data-intake-id="${intakeId}"]`);

    try {
        if (intake.brandName) {
            // 프리셋 음료
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

                const isFavorite = checkIfFavorite('PRESET', matched.id);

                if (isFavorite) {
                    const favorite = AppState.favorites.find(f => f.type === 'PRESET' && f.beverageId === matched.id);
                    if (favorite) {
                        await FavoriteAPI.delete(favorite.id);
                        await loadFavorites();
                        if (btn) {
                            btn.classList.remove('active');
                            btn.innerHTML = '☆';
                        }
                        showToast('즐겨찾기에서 삭제되었습니다');
                    }
                } else {
                    await addToFavorite('PRESET', matched.id);
                    if (btn) {
                        btn.classList.add('active');
                        btn.innerHTML = '★';
                    }
                }
                return;
            }
        }

        // 커스텀 음료
        const customMatch = AppState.customBeverages.find(c =>
            c.name === intake.beverageName
        );

        if (customMatch) {
            const isFavorite = checkIfFavorite('CUSTOM', customMatch.id);

            if (isFavorite) {
                const favorite = AppState.favorites.find(f => f.type === 'CUSTOM' && f.beverageId === customMatch.id);
                if (favorite) {
                    await FavoriteAPI.delete(favorite.id);
                    await loadFavorites();
                    if (btn) {
                        btn.classList.remove('active');
                        btn.innerHTML = '☆';
                    }
                    showToast('즐겨찾기에서 삭제되었습니다');
                }
            } else {
                await addToFavorite('CUSTOM', customMatch.id);
                if (btn) {
                    btn.classList.add('active');
                    btn.innerHTML = '★';
                }
            }
        } else {
            showToast('즐겨찾기에 추가할 수 없는 음료입니다');
        }
    } catch (error) {
        console.error('즐겨찾기 토글 실패:', error);
        showToast(error.message);
    }
}

// 섭취 상세 정보 모달
function showIntakeDetail(intakeId) {
    const intake = AppState.todayIntakes.find(i => i.id === intakeId);
    if (!intake) return;

    AppState.selectedIntakeForDetail = intake;

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

    // 카테고리 아이콘
    const icon = getCategoryIcon(intake.category);

    content.innerHTML = `
        <div class="intake-detail-header">
            <div class="intake-detail-icon">${icon}</div>
            <div class="intake-detail-title">
                <div class="intake-detail-name">${intake.beverageName}</div>
                ${intake.brandName ? `<div class="intake-detail-brand">${intake.brandName}</div>` : '<div class="intake-detail-brand custom-label">내 음료</div>'}
            </div>
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
                <div class="intake-detail-value">${icon} ${intake.category}</div>
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

    // 즐겨찾기 버튼 상태 업데이트
    updateFavoriteButtonState(intake);
}

// 즐겨찾기 버튼 상태 업데이트
async function updateFavoriteButtonState(intake) {
    const btn = document.getElementById('addFavoriteFromDetail');
    if (!btn) return;

    // 기본 상태로 리셋
    btn.classList.remove('active');
    btn.innerHTML = '☆ 즐겨찾기';

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
                );

                if (matched && checkIfFavorite('PRESET', matched.id)) {
                    btn.classList.add('active');
                    btn.innerHTML = '★ 즐겨찾기됨';
                }
            }
        } else {
            // 커스텀 음료
            const customMatch = AppState.customBeverages.find(c =>
                c.name === intake.beverageName
            );

            if (customMatch && checkIfFavorite('CUSTOM', customMatch.id)) {
                btn.classList.add('active');
                btn.innerHTML = '★ 즐겨찾기됨';
            }
        }
    } catch (error) {
        console.log('즐겨찾기 상태 확인 실패:', error);
    }
}

// ========================================
// 음료 마시기 (체크 모달)
// ========================================
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
    AppState.selectedBeverage = { id: beverageId, type, name: result.beverage.name, caffeineMg: result.beverage.caffeineMg };

    const modal = document.getElementById('intakeModal');
    const beverageInfo = document.getElementById('selectedBeverageInfo');

    const recommendationConfig = {
        'SAFE': { class: 'result-safe', icon: '✔', message: '안전하게 마실 수 있어요' },
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

    // 즐겨찾기 버튼 상태 업데이트
    updateIntakeModalFavoriteButton(type.toUpperCase(), beverageId);

    modal.classList.add('active');
}

// 섭취 기록 모달의 즐겨찾기 버튼 상태 업데이트
function updateIntakeModalFavoriteButton(type, beverageId) {
    const btn = document.getElementById('toggleFavoriteFromIntake');
    if (!btn) return;

    const isFavorite = checkIfFavorite(type, beverageId);

    if (isFavorite) {
        btn.classList.add('active');
        btn.innerHTML = '★ 즐겨찾기됨';
    } else {
        btn.classList.remove('active');
        btn.innerHTML = '☆ 즐겨찾기';
    }
}

function openIntakeModal(beverageId, type) {
    AppState.selectedBeverage = { id: beverageId, type };

    let beverage;
    if (type === 'preset') {
        beverage = AppState.beverages.find(b => b.id === beverageId);
    } else {
        beverage = AppState.customBeverages.find(b => b.id === beverageId);
    }

    if (!beverage) return;

    AppState.selectedBeverage.name = beverage.name;
    AppState.selectedBeverage.caffeineMg = beverage.caffeineMg;

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

    // 즐겨찾기 버튼 상태 업데이트
    updateIntakeModalFavoriteButton(type.toUpperCase(), beverageId);

    document.getElementById('intakeModal').classList.add('active');
}

async function confirmIntake() {
    if (!AppState.selectedBeverage) return;

    const consumedAt = document.getElementById('consumedAtInput').value;

    if (!consumedAt) {
        alert('섭취 시간을 선택해주세요');
        return;
    }

    try {
        const isoDateTime = consumedAt + ':00';

        if (AppState.selectedBeverage.type === 'preset') {
            await IntakeAPI.recordPreset(AppState.selectedBeverage.id, isoDateTime);
        } else {
            await IntakeAPI.recordCustom(AppState.selectedBeverage.id, isoDateTime);
        }

        const beverageName = AppState.selectedBeverage.name;
        const caffeineMg = AppState.selectedBeverage.caffeineMg;
        const beverageId = AppState.selectedBeverage.id;
        const beverageType = AppState.selectedBeverage.type;

        closeAllModals();

        // 즉시 갱신
        await loadTodayIntakes();
        await loadCaffeineStatus();
        await loadTimeline();

        // 즐겨찾기 유도 토스트
        const isFavorite = checkIfFavorite(beverageType, beverageId);
        if (!isFavorite) {
            showToast(`☕ ${beverageName} ${Math.round(caffeineMg)}mg 기록 완료!`, {
                duration: 5000,
                action: () => addToFavorite(beverageType.toUpperCase(), beverageId),
                actionText: '⭐ 즐겨찾기'
            });
        } else {
            showToast(`☕ ${beverageName} ${Math.round(caffeineMg)}mg 기록 완료!`);
        }

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