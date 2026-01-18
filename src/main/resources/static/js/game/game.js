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

    const handleGameCardAction = (card) => {
        if (card.classList.contains("coming-soon")) {
            return;
        }
        const gameRoute = card.dataset.gameRoute;
        if (!gameRoute) return;

        if (gameRoute.startsWith("#")) {
            const target = document.querySelector(gameRoute);
            if (target) {
                target.scrollIntoView({behavior: "smooth", block: "start"});
            }
            return;
        }

        // 메뉴 클릭 시에도 서버 인증을 확인합니다.
        ensureAuthenticated(gameRoute, () => {
            window.location.href = gameRoute;
        });
    };

    const gameList = document.querySelector(".game-list");
    if (gameList) {
        // Mouse Click Event
        gameList.addEventListener("click", (event) => {
            const card = event.target.closest(".card");
            if (!card) return;
            
            event.preventDefault();
            handleGameCardAction(card);
        });

        // Keyboard Event (Enter or Space)
        gameList.addEventListener("keydown", (event) => {
            const card = event.target.closest(".card");
            if (!card) return;

            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                handleGameCardAction(card);
            }
        });
    }

    const comingSoonCards = document.querySelectorAll(".coming-soon");
    comingSoonCards.forEach((card) => {
        card.addEventListener("click", (event) => {
            event.preventDefault();
        });
        // Remove tabindex from coming soon cards if present, or ensure they are not focusable
        card.removeAttribute("tabindex");
        card.removeAttribute("role");
    });

    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "게임";
        pageTitleLabel.classList.remove("d-none");
    }
});
