/**
 * 성경 O/X 퀴즈 게임
 * - API 연동 버전
 * - 로그인 필수
 * - 스테이지별 문제 10개
 */

import { checkAuthStatus, buildLoginRedirectUrl } from "/js/auth/auth-check.js";

const API_BASE = "/api/v1/game/bible-ox-quiz";
const MAX_STAGE = 66;

class BibleOxQuiz {
    constructor() {
        this.stageNumber = null;
        this.stageAttemptId = null;
        this.questions = [];
        this.currentIndex = 0;
        this.correctCount = 0;
        this.wrongCount = 0;
        this.isAnswered = false;
        this.bookName = "";

        this.initElements();
        this.init();
    }

    initElements() {
        // 공통
        this.loadingEl = document.getElementById("oxQuizLoading");
        this.errorEl = document.getElementById("oxQuizError");
        this.backButton = document.getElementById("topNavBackButton");

        // 퀴즈 게임
        this.gameSection = document.getElementById("oxQuizGame");
        this.stageLabelEl = document.getElementById("oxQuizStageLabel");
        this.titleEl = document.getElementById("oxQuizTitle");
        this.quizPanel = document.getElementById("oxQuizPanel");
        this.quizComplete = document.getElementById("oxQuizComplete");
        this.progressBar = document.getElementById("oxQuizProgressBar");
        this.progressText = document.getElementById("oxQuizProgress");
        this.questionEl = document.getElementById("oxQuizQuestion");
        this.optionO = document.getElementById("oxQuizOptionO");
        this.optionX = document.getElementById("oxQuizOptionX");
        this.feedback = document.getElementById("oxQuizFeedback");
        this.nextBtn = document.getElementById("oxQuizNext");
        this.correctCountEl = document.getElementById("oxQuizCorrectCount");
        this.wrongCountEl = document.getElementById("oxQuizWrongCount");
        this.nextStageBtn = document.getElementById("oxQuizNextStageBtn");
        this.summaryCorrect = document.getElementById("summaryCorrect");
        this.summaryWrong = document.getElementById("summaryWrong");
        this.summaryAccuracy = document.getElementById("summaryAccuracy");
        this.resultText = document.getElementById("oxQuizResultText");
        this.completeIcon = document.getElementById("oxQuizCompleteIcon");
        this.completeTitle = document.getElementById("oxQuizCompleteTitle");
    }

    async init() {
        this.initNav();
        // 인증 체크
        await checkAuthStatus({
            onAuthenticated: () => this.onAuthenticated(),
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
            pageTitleLabel.textContent = "성경 O/X 퀴즈";
            pageTitleLabel.classList.remove("d-none");
        }
        this.backButton.classList.remove("d-none");
        this.backButton.addEventListener("click", () => {
            window.location.href = "/web/game/bible-ox-quiz/map";
            }
        });
    }

    async onAuthenticated() {
        const urlParams = new URLSearchParams(window.location.search);
        const stageParam = urlParams.get("stage");

        if (stageParam) {
            this.stageNumber = parseInt(stageParam, 10);
            if (isNaN(this.stageNumber) || this.stageNumber < 1 || this.stageNumber > MAX_STAGE) {
                this.showError("잘못된 스테이지 번호입니다. (1~66)");
                return;
            }
            await this.loadQuizGame();
        } else {
            window.location.replace("/web/game/bible-ox-quiz/map");
        }
    }

    async loadQuizGame() {
        try {
            // 1. 스테이지 정보 로드
            const stageResponse = await this.fetchApi(`${API_BASE}/stages/${this.stageNumber}`);
            if (!stageResponse.ok) {
                const errorData = await stageResponse.json().catch(() => ({}));
                throw new Error(errorData.message || "스테이지를 불러올 수 없습니다.");
            }

            const stageData = await stageResponse.json();
            this.questions = stageData.questions;
            this.bookName = stageData.bookName;

            // 2. 스테이지 시작
            const startResponse = await this.fetchApi(`${API_BASE}/stages/${this.stageNumber}/start`, {
                method: "POST"
            });
            if (!startResponse.ok) {
                throw new Error("스테이지를 시작할 수 없습니다.");
            }

            const startData = await startResponse.json();
            this.stageAttemptId = startData.stageAttemptId;
            this.currentIndex = startData.answeredCount;
            this.correctCount = startData.currentScore;
            this.wrongCount = startData.answeredCount - startData.currentScore;

            // UI 초기화
            this.stageLabelEl.textContent = `STAGE ${this.stageNumber}`;
            this.titleEl.textContent = this.bookName;
            this.correctCountEl.textContent = this.correctCount;
            this.wrongCountEl.textContent = this.wrongCount;

            this.bindEvents();
            this.hideLoading();
            this.gameSection.classList.remove("d-none");

            if (this.currentIndex >= this.questions.length) {
                // 이미 완료된 경우
                this.showComplete();
            } else {
                this.renderQuestion();
            }
        } catch (error) {
            this.showError(error.message);
        }
    }

    bindEvents() {
        this.optionO.addEventListener("click", () => this.handleAnswer(true));
        this.optionX.addEventListener("click", () => this.handleAnswer(false));
        this.nextBtn.addEventListener("click", () => this.nextQuestion());
        this.nextStageBtn.addEventListener("click", () => this.goToNextStage());

        document.addEventListener("keydown", (e) => {
            if (this.quizComplete.classList.contains("d-none") === false) {
                return; // 완료 화면에서는 키보드 단축키 비활성화
            }

            if (this.isAnswered) {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    this.nextQuestion();
                }
            } else {
                if (e.key === "o" || e.key === "O" || e.key === "1") {
                    e.preventDefault();
                    this.handleAnswer(true);
                } else if (e.key === "x" || e.key === "X" || e.key === "2") {
                    e.preventDefault();
                    this.handleAnswer(false);
                }
            }
        });
    }

    renderQuestion() {
        const question = this.questions[this.currentIndex];
        const total = this.questions.length;
        const current = this.currentIndex + 1;

        this.progressBar.max = total;
        this.progressBar.value = current;
        this.progressBar.setAttribute("aria-valuenow", current);
        this.progressBar.setAttribute("aria-valuemax", total);
        this.progressText.textContent = `${current} / ${total}`;

        this.questionEl.textContent = question.questionText;
        this.feedback.textContent = "";
        this.feedback.className = "ox-quiz-feedback text-center";

        this.optionO.disabled = false;
        this.optionX.disabled = false;
        this.optionO.classList.remove("is-selected", "is-correct", "is-wrong");
        this.optionX.classList.remove("is-selected", "is-correct", "is-wrong");

        this.nextBtn.disabled = true;
        this.isAnswered = false;
    }

    async handleAnswer(userAnswer) {
        if (this.isAnswered) return;

        this.isAnswered = true;
        this.optionO.disabled = true;
        this.optionX.disabled = true;

        const question = this.questions[this.currentIndex];
        const selectedBtn = userAnswer ? this.optionO : this.optionX;
        selectedBtn.classList.add("is-selected");

        try {
            const response = await this.fetchApi(
                `${API_BASE}/stages/${this.stageNumber}/questions/${question.questionId}/answer`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ selectedAnswer: userAnswer })
                }
            );

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || "답안 제출에 실패했습니다.");
            }

            const result = await response.json();
            const correctBtn = result.correctAnswer ? this.optionO : this.optionX;

            if (result.isCorrect) {
                this.correctCount++;
                this.correctCountEl.textContent = this.correctCount;
                selectedBtn.classList.add("is-correct");
                this.feedback.textContent = "정답입니다!";
                this.feedback.classList.add("is-correct");
            } else {
                this.wrongCount++;
                this.wrongCountEl.textContent = this.wrongCount;
                selectedBtn.classList.add("is-wrong");
                correctBtn.classList.add("is-correct");
                this.feedback.textContent = `오답입니다. 정답은 ${result.correctAnswer ? "O" : "X"}입니다.`;
                this.feedback.classList.add("is-wrong");
            }

            if (this.currentIndex < this.questions.length - 1) {
                this.nextBtn.textContent = "다음 문제";
            } else {
                this.nextBtn.textContent = "결과 보기";
            }
            this.nextBtn.disabled = false;

        } catch (error) {
            this.feedback.textContent = error.message;
            this.feedback.classList.add("is-wrong");
            this.isAnswered = false;
            this.optionO.disabled = false;
            this.optionX.disabled = false;
            selectedBtn.classList.remove("is-selected");
        }
    }

    async nextQuestion() {
        if (this.currentIndex < this.questions.length - 1) {
            this.currentIndex++;
            this.renderQuestion();
        } else {
            await this.completeStage();
        }
    }

    async completeStage() {
        try {
            const response = await this.fetchApi(`${API_BASE}/stages/${this.stageNumber}/complete`, {
                method: "POST"
            });

            if (!response.ok) {
                throw new Error("스테이지 완료 처리에 실패했습니다.");
            }

            const result = await response.json();
            this.showComplete(result);
        } catch (error) {
            this.showError(error.message);
        }
    }

    showComplete(result) {
        this.quizPanel.classList.add("d-none");
        this.quizComplete.classList.remove("d-none");

        const total = this.questions.length;
        const accuracy = result ? result.accuracyPercent : Math.round((this.correctCount / total) * 100);

        this.summaryCorrect.textContent = this.correctCount;
        this.summaryWrong.textContent = this.wrongCount;
        this.summaryAccuracy.textContent = `${accuracy}%`;

        let message = "";
        let icon = "🎉";
        if (accuracy === 100) {
            message = "완벽합니다! 성경 지식이 뛰어나시네요.";
            icon = "🏆";
        } else if (accuracy >= 80) {
            message = "훌륭해요! 조금만 더 공부하면 만점입니다.";
            icon = "🎉";
        } else if (accuracy >= 60) {
            message = "좋아요! 꾸준히 성경을 읽으면 더 잘할 수 있어요.";
            icon = "👍";
        } else {
            message = "괜찮아요! 함께 성경을 더 알아가 봐요.";
            icon = "💪";
        }

        this.completeIcon.textContent = icon;
        this.resultText.textContent = message;

        // 다음 스테이지 버튼 처리
        if (this.stageNumber >= MAX_STAGE) {
            this.nextStageBtn.disabled = true;
            this.nextStageBtn.textContent = "마지막 스테이지";
        } else {
            this.nextStageBtn.disabled = false;
            this.nextStageBtn.textContent = "다음 스테이지로";
        }
    }

    goToNextStage() {
        if (this.stageNumber < MAX_STAGE) {
            window.location.href = `/web/game/bible-ox-quiz?stage=${this.stageNumber + 1}`;
        }
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
    new BibleOxQuiz();
});
