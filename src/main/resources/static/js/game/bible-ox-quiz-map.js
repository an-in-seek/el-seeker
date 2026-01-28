/**
 * 성경 O/X 퀴즈 스테이지 맵
 * - 로그인 필수
 */

import { checkAuthStatus, buildLoginRedirectUrl } from "/js/auth/auth-check.js";

const API_BASE = "/api/v1/game/bible-ox-quiz";

class BibleOxQuizMap {
    constructor() {
        this.initElements();
        this.init();
    }

    initElements() {
        this.loadingEl = document.getElementById("oxQuizLoading");
        this.errorEl = document.getElementById("oxQuizError");
        this.backButton = document.getElementById("topNavBackButton");
        this.stageListSection = document.getElementById("oxStageList");
        this.stageGrid = document.getElementById("oxStageGrid");
    }

    async init() {
        this.initNav();
        await checkAuthStatus({
            onAuthenticated: () => this.loadStageList(),
            onUnauthenticated: () => {
                alert("로그인 후 사용할 수 있습니다.");
                window.location.replace(buildLoginRedirectUrl());
            },
            onError: () => {
                this.showError("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            }
        });
    }

    initNav() {
        if (!this.backButton) {
            return;
        }
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 O/X 퀴즈 맵";
            pageTitleLabel.classList.remove("d-none");
        }
        this.backButton.classList.remove("d-none");
        this.backButton.addEventListener("click", () => {
            const isSameOrigin = document.referrer && new URL(document.referrer).origin === window.location.origin;
            if (isSameOrigin) {
                window.history.back();
                return;
            }
            window.location.href = "/web/game";
        });
    }

    async loadStageList() {
        try {
            const response = await this.fetchApi(`${API_BASE}/stages`);
            if (!response.ok) {
                throw new Error("스테이지 목록을 불러올 수 없습니다.");
            }

            const data = await response.json();
            this.renderStageList(data);
            this.hideLoading();
            this.stageListSection.classList.remove("d-none");
        } catch (error) {
            this.showError(error.message);
        }
    }

    renderStageList(data) {
        this.stageGrid.innerHTML = "";

        data.stages.forEach((stage) => {
            const card = document.createElement("article");
            card.className = "ox-stage-card";
            if (stage.isCompleted) {
                card.classList.add("is-completed");
            }
            if (stage.hasInProgress) {
                card.classList.add("has-progress");
            }

            const statusBadge = stage.isCompleted
                ? `<span class="ox-stage-badge completed">완료</span>`
                : stage.hasInProgress
                    ? `<span class="ox-stage-badge in-progress">진행중</span>`
                    : "";

            const scoreInfo = stage.bestScore !== null
                ? `<span class="ox-stage-score">최고 ${stage.bestScore}/${stage.totalQuestions}</span>`
                : "";

            card.innerHTML = `
                <div class="ox-stage-number">STAGE ${stage.stageNumber}</div>
                <div class="ox-stage-book">${stage.bookName}</div>
                <div class="ox-stage-info">
                    ${statusBadge}
                    ${scoreInfo}
                </div>
            `;

            card.addEventListener("click", () => {
                window.location.href = `/web/game/bible-ox-quiz?stage=${stage.stageNumber}`;
            });

            card.setAttribute("tabindex", "0");
            card.setAttribute("role", "button");
            card.setAttribute("aria-label", `${stage.bookName} 스테이지 ${stage.stageNumber} 시작`);
            card.addEventListener("keydown", (e) => {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    window.location.href = `/web/game/bible-ox-quiz?stage=${stage.stageNumber}`;
                }
            });

            this.stageGrid.appendChild(card);
        });
    }

    async fetchApi(url, options = {}) {
        const defaultOptions = {
            credentials: "include",
            headers: {
                Accept: "application/json",
                ...options.headers
            }
        };
        return fetch(url, { ...defaultOptions, ...options });
    }

    hideLoading() {
        this.loadingEl.classList.add("d-none");
    }

    showError(message) {
        this.hideLoading();
        this.errorEl.textContent = message;
        this.errorEl.classList.remove("d-none");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    new BibleOxQuizMap();
});
