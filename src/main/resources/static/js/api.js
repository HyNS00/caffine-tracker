// API Base URL
const API_BASE = '/api';

// API 호출 헬퍼 함수
async function fetchAPI(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    };

    const response = await fetch(url, { ...defaultOptions, ...options });

    if (response.status === 401) {
        sessionStorage.removeItem('user');
        window.location.href = '/';
        throw new Error('로그인이 필요합니다');
    }

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || '요청 실패');
    }

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

    me: () => fetchAPI(`${API_BASE}/auth/me`),
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
    getStatus: () => fetchAPI(`${API_BASE}/caffeine/status`),

    checkPreset: (beverageId) => fetchAPI(`${API_BASE}/caffeine/check/preset/${beverageId}`, {
        method: 'POST',
    }),

    checkCustom: (beverageId) => fetchAPI(`${API_BASE}/caffeine/check/custom/${beverageId}`, {
        method: 'POST',
    }),
};

// Statistics API (신규 추가)
const StatisticsAPI = {
    getTimeline: (hours = 12) => fetchAPI(`${API_BASE}/statistics/timeline?hours=${hours}`),

    getDailyStatistics: (days = 7) => fetchAPI(`${API_BASE}/statistics/daily?days=${days}`),
};