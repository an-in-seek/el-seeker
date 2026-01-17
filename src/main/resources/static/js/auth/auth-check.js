// /js/auth/auth-check.js

const AUTH_ME_ENDPOINT = "/api/auth/me";

const isOkStatus = (response) => response && response.status === 200;

const isUnauthorized = (response) => response && response.status === 401;

export const buildLoginRedirectUrl = (returnUrl) => {
    const loginUrl = new URL("/login", window.location.origin);
    if (returnUrl) {
        loginUrl.searchParams.set("returnUrl", returnUrl);
    }
    return loginUrl.toString();
};

export const showAuthError = (container, message) => {
    if (!container) {
        return;
    }
    container.textContent = message;
    container.classList.remove("d-none");
};

export const checkAuthStatus = async ({
    onAuthenticated,
    onUnauthenticated,
    onError,
} = {}) => {
    try {
        const response = await fetch(AUTH_ME_ENDPOINT, {
            method: "GET",
            credentials: "include",
            headers: {
                Accept: "application/json",
            },
        });

        if (isOkStatus(response)) {
            const data = await response.json().catch(() => null);
            // 인증 성공 시 사용자 요약 정보를 전달합니다.
            if (typeof onAuthenticated === "function") {
                onAuthenticated(data);
            }
            return;
        }

        if (isUnauthorized(response)) {
            // 인증 실패는 로그인으로 유도합니다.
            if (typeof onUnauthenticated === "function") {
                onUnauthenticated();
            }
            return;
        }

        if (typeof onError === "function") {
            onError(new Error("Unexpected auth response"));
        }
    } catch (error) {
        if (typeof onError === "function") {
            onError(error);
        }
    }
};
