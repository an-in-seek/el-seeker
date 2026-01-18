import {buildLoginRedirectUrl, checkAuthStatus, showAuthError} from "/js/auth/auth-check.js";

document.addEventListener("DOMContentLoaded", () => {
    const authErrorMessage = document.getElementById("authErrorMessage");

    const redirectToLogin = (returnUrl) => {
        window.location.replace(buildLoginRedirectUrl(returnUrl));
    };

    const ensureAuthenticated = async (returnUrl, onAuthenticated) => {
        await checkAuthStatus({
            onAuthenticated,
            onUnauthenticated: () => {
                alert("로그인 후 사용할 수 있습니다.");
                redirectToLogin(returnUrl);
            },
            onError: () => showAuthError(authErrorMessage, "네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
        });
    };

    const gameList = document.querySelector(".game-list");
    if (gameList) {
        gameList.addEventListener("click", (event) => {
            const card = event.target.closest(".card");
            if (!card) {
                return;
            }
            if (card.classList.contains("coming-soon")) {
                event.preventDefault();
                return;
            }
            if (card.dataset.gameKey === "bible-quiz" && card.dataset.gameRoute) {
                const gameRoute = card.dataset.gameRoute;
                event.preventDefault();
                // 메뉴 클릭 시에도 서버 인증을 확인합니다.
                ensureAuthenticated(gameRoute, () => {
                    window.location.href = gameRoute;
                });
            }
        });
    }
    const comingSoonCards = document.querySelectorAll(".coming-soon");
    comingSoonCards.forEach((card) => {
        card.addEventListener("click", (event) => {
            event.preventDefault();
        });
    });
    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "게임";
        pageTitleLabel.classList.remove("d-none");
    }
});
