// API Base URL
const API_BASE = '/api';

// API 호출 헬퍼 함수
async function fetchAPI(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // 세션 쿠키 포함
    };

    const response = await fetch(url, { ...defaultOptions, ...options });

    // 401 Unauthorized - 로그인 화면으로 이동
    if (response.status === 401) {
        sessionStorage.removeItem('user');
        window.location.href = '/';
        throw new Error('로그인이 필요합니다');
    }

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || '요청 실패');
    }

    // 204 No Content는 빈 응답
    if (response.status === 204) {
        return null;
    }

    return response.json();
}

// Auth API
const AuthAPI = {
    signup: (data) => fetchAPI(`${API_BASE}/auth/signup`, {
        method: 'POST',
        body: JSON.stringify(data),
    }),

    login: (data) => fetchAPI(`${API_BASE}/auth/login`, {
        method: 'POST',
        body: JSON.stringify(data),
    }),

    logout: () => fetchAPI(`${API_BASE}/auth/logout`, {
        method: 'POST',
    }),
};

// Beverage API
const BeverageAPI = {
    getAll: () => fetchAPI(`${API_BASE}/beverages`),

    search: (keyword) => fetchAPI(`${API_BASE}/beverages?keyword=${encodeURIComponent(keyword)}`),

    getCategories: () => fetchAPI(`${API_BASE}/beverages/categories`),
};

// Intake API
const IntakeAPI = {
    recordPreset: (beverageId, consumedAt) => fetchAPI(`${API_BASE}/intakes/preset/${beverageId}`, {
        method: 'POST',
        body: JSON.stringify({ consumedAt }),
    }),

    recordCustom: (beverageId, consumedAt) => fetchAPI(`${API_BASE}/intakes/custom/${beverageId}`, {
        method: 'POST',
        body: JSON.stringify({ consumedAt }),
    }),

    getTodayIntakes: () => fetchAPI(`${API_BASE}/intakes/today`),

    delete: (intakeId) => fetchAPI(`${API_BASE}/intakes/${intakeId}`, {
        method: 'DELETE',
    }),
};

// Custom Beverage API
const CustomBeverageAPI = {
    create: (data) => fetchAPI(`${API_BASE}/beverages/custom`, {
        method: 'POST',
        body: JSON.stringify(data),
    }),

    getMyBeverages: () => fetchAPI(`${API_BASE}/beverages/custom`),

    update: (beverageId, data) => fetchAPI(`${API_BASE}/beverages/custom/${beverageId}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    }),

    delete: (beverageId) => fetchAPI(`${API_BASE}/beverages/custom/${beverageId}`, {
        method: 'DELETE',
    }),
};

// Caffeine Check API
const CaffeineAPI = {
    // 현재 카페인 상태 조회
    getStatus: () => fetchAPI(`${API_BASE}/caffeine/status`),

    // PresetBeverage 마셔도 되는지 체크
    checkPreset: (beverageId) => fetchAPI(`${API_BASE}/caffeine/check/preset/${beverageId}`, {
        method: 'POST',
    }),

    // CustomBeverage 마셔도 되는지 체크
    checkCustom: (beverageId) => fetchAPI(`${API_BASE}/caffeine/check/custom/${beverageId}`, {
        method: 'POST',
    }),
};