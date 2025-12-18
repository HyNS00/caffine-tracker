// 탭 전환
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const tab = btn.dataset.tab;

        // 탭 버튼 활성화
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        // 폼 전환
        document.querySelectorAll('.auth-form').forEach(form => form.classList.remove('active'));
        document.getElementById(`${tab}Form`).classList.add('active');
    });
});

// 회원가입
document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('signupName').value;
    const email = document.getElementById('signupEmail').value;
    const password = document.getElementById('signupPassword').value;
    const errorEl = document.getElementById('signupError');

    try {
        await AuthAPI.signup({ name, email, password });

        // 회원가입 성공 - 로그인 탭으로 전환
        alert('회원가입이 완료되었습니다. 로그인해주세요.');

        // 로그인 탭으로 전환
        document.querySelector('[data-tab="login"]').click();

        // 폼 초기화
        document.getElementById('signupForm').reset();

    } catch (error) {
        errorEl.textContent = error.message;
        errorEl.classList.add('show');

        setTimeout(() => {
            errorEl.classList.remove('show');
        }, 3000);
    }
});

// 로그인
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorEl = document.getElementById('loginError');

    try {
        const response = await AuthAPI.login({ email, password });

        // 사용자 정보 저장
        sessionStorage.setItem('user', JSON.stringify(response));

        // 앱 화면으로 전환
        showAppScreen();

    } catch (error) {
        errorEl.textContent = error.message;
        errorEl.classList.add('show');

        setTimeout(() => {
            errorEl.classList.remove('show');
        }, 3000);
    }
});

// 로그아웃 (헤더 버튼 - 없을 수도 있음)
document.getElementById('logoutBtn')?.addEventListener('click', async () => {
    try {
        await AuthAPI.logout();
        sessionStorage.removeItem('user');
        showLoginScreen();
    } catch (error) {
        console.error('로그아웃 실패:', error);
    }
});

// 화면 전환 함수
function showLoginScreen() {
    document.getElementById('loginScreen').classList.add('active');
    document.getElementById('appScreen').classList.remove('active');
}

function showAppScreen() {
    document.getElementById('loginScreen').classList.remove('active');
    document.getElementById('appScreen').classList.add('active');

    // 사용자 정보 표시
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (user) {
        document.getElementById('userName').textContent = user.name;
    }

    // 앱 초기화
    if (typeof initApp === 'function') {
        initApp();
    }
}

// 페이지 로드 시 세션 체크
window.addEventListener('DOMContentLoaded', async () => {
    try {
        // 서버에 세션 유효성 확인
        const user = await AuthAPI.me();
        sessionStorage.setItem('user', JSON.stringify(user));
        showAppScreen();
    } catch (error) {
        // 세션 없음 → 로그인 화면
        sessionStorage.removeItem('user');
        showLoginScreen();
    }
});