import {LocalStore} from "/js/storage-util.js?v=2.1";

/**
 * Configuration constants
 */
const API_CONFIG = {
    BASE_URL: "/api/v1/game/bible-quiz",
    ENDPOINTS: {
        STAGES: "/stages"
    }
};

const STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "quiz_current_stage",
    LAST_COMPLETED_STAGE: "quiz_last_completed_stage",
    STAGE_COUNT: "quiz_stage_count",
    STAGE_SCORE_PREFIX: "quiz_stage_score",
    CURRENT_QUESTION_PREFIX: "quiz_current_question_stage",
    CURRENT_SCORE_PREFIX: "quiz_current_score_stage",
    CURRENT_REVIEW_TYPE_PREFIX: "quiz_current_review_type_stage",
    QUESTION_STATS_PREFIX: "quiz_question_stats_stage",
    REVIEW_COUNT_PREFIX: "quiz_review_count_stage",
});

const UI_CLASSES = {
    HIDDEN: "d-none",
    BUSY: "aria-busy",
    CORRECT: "is-correct",
    WRONG: "is-wrong",
    SELECTED: "is-selected"
};

/**
 * Service for LocalStorage operations
 */
const SafeLocalStore = {
    isAvailable: () => {
        try {
            return typeof localStorage !== "undefined";
        } catch (error) {
            return false;
        }
    },
    get: (key) => {
        if (!SafeLocalStore.isAvailable()) return null;
        try {
            return LocalStore.get(key);
        } catch (error) {
            return null;
        }
    },
    set: (key, value) => {
        if (!SafeLocalStore.isAvailable()) return;
        try {
            LocalStore.set(key, value);
        } catch (error) {
            // Ignore storage errors to keep quiz usable.
        }
    },
    remove: (key) => {
        if (!SafeLocalStore.isAvailable()) return;
        try {
            LocalStore.remove(key);
        } catch (error) {
            // Ignore storage errors to keep quiz usable.
        }
    }
};

const StorageService = {
    getStageCount: () => {
        const count = parseInt(SafeLocalStore.get(STORAGE_KEYS.STAGE_COUNT), 10);
        return Number.isNaN(count) ? null : count;
    },
    getLastCompletedStage: () => {
        const stage = parseInt(SafeLocalStore.get(STORAGE_KEYS.LAST_COMPLETED_STAGE), 10);
        return Number.isNaN(stage) ? 0 : stage;
    },
    getCurrentStage: () => {
        const stage = parseInt(SafeLocalStore.get(STORAGE_KEYS.CURRENT_STAGE), 10);
        return Number.isNaN(stage) ? null : stage;
    },
    setLastCompletedStage: (stage) => SafeLocalStore.set(STORAGE_KEYS.LAST_COMPLETED_STAGE, stage),
    setCurrentStage: (stage) => SafeLocalStore.set(STORAGE_KEYS.CURRENT_STAGE, stage),
    setStageCount: (count) => SafeLocalStore.set(STORAGE_KEYS.STAGE_COUNT, count),
    setStageScore: (stage, score) => SafeLocalStore.set(`${STORAGE_KEYS.STAGE_SCORE_PREFIX}_${stage}`, score),
    getCurrentScore: (stage) => {
        const storedScore = parseInt(SafeLocalStore.get(`${STORAGE_KEYS.CURRENT_SCORE_PREFIX}_${stage}`), 10);
        return Number.isNaN(storedScore) ? null : storedScore;
    },
    setCurrentScore: (stage, score) => SafeLocalStore.set(`${STORAGE_KEYS.CURRENT_SCORE_PREFIX}_${stage}`, score),
    clearCurrentScore: (stage) => SafeLocalStore.remove(`${STORAGE_KEYS.CURRENT_SCORE_PREFIX}_${stage}`),
    getCurrentReviewType: (stage) => SafeLocalStore.get(`${STORAGE_KEYS.CURRENT_REVIEW_TYPE_PREFIX}_${stage}`),
    setCurrentReviewType: (stage, type) => SafeLocalStore.set(`${STORAGE_KEYS.CURRENT_REVIEW_TYPE_PREFIX}_${stage}`, type),
    clearCurrentReviewType: (stage) => SafeLocalStore.remove(`${STORAGE_KEYS.CURRENT_REVIEW_TYPE_PREFIX}_${stage}`),
    hasCurrentQuestionIndex: (stage) => {
        const key = `${STORAGE_KEYS.CURRENT_QUESTION_PREFIX}_${stage}`;
        return SafeLocalStore.get(key) !== null;
    },
    getCurrentQuestionIndex: (stage, questionCount) => {
        const key = `${STORAGE_KEYS.CURRENT_QUESTION_PREFIX}_${stage}`;
        const storedIndex = parseInt(SafeLocalStore.get(key), 10);
        if (Number.isNaN(storedIndex) || storedIndex < 1) return 0;
        if (questionCount && storedIndex > questionCount) return 0;
        return storedIndex - 1;
    },
    setCurrentQuestionIndex: (stage, index) => {
        const parsedIndex = parseInt(index, 10);
        if (Number.isNaN(parsedIndex)) return;
        const key = `${STORAGE_KEYS.CURRENT_QUESTION_PREFIX}_${stage}`;
        SafeLocalStore.set(key, parsedIndex + 1);
    },
    clearCurrentQuestionIndex: (stage) => {
        const key = `${STORAGE_KEYS.CURRENT_QUESTION_PREFIX}_${stage}`;
        SafeLocalStore.remove(key);
    },
    getQuestionStats: (stage) => {
        const raw = SafeLocalStore.get(`${STORAGE_KEYS.QUESTION_STATS_PREFIX}_${stage}`);
        if (!raw) return {};
        try {
            const parsed = JSON.parse(raw);
            return parsed && typeof parsed === "object" ? parsed : {};
        } catch (error) {
            return {};
        }
    },
    setQuestionStats: (stage, stats) => {
        SafeLocalStore.set(`${STORAGE_KEYS.QUESTION_STATS_PREFIX}_${stage}`, JSON.stringify(stats));
    },
    updateQuestionStats: (stage, questionId, isCorrect) => {
        const stats = StorageService.getQuestionStats(stage);
        const key = String(questionId);
        const current = stats[key] || {attempts: 0, correct: 0};
        current.attempts += 1;
        if (isCorrect) current.correct += 1;
        stats[key] = current;
        StorageService.setQuestionStats(stage, stats);
    },
    getReviewCount: (stage) => {
        const count = parseInt(SafeLocalStore.get(`${STORAGE_KEYS.REVIEW_COUNT_PREFIX}_${stage}`), 10);
        return Number.isNaN(count) ? 0 : count;
    },
    incrementReviewCount: (stage) => {
        const next = StorageService.getReviewCount(stage) + 1;
        SafeLocalStore.set(`${STORAGE_KEYS.REVIEW_COUNT_PREFIX}_${stage}`, next);
    }
};

/**
 * Service for API interactions
 */
const ApiService = {
    fetchStageData: async (stageNumber) => {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.STAGES}/${stageNumber}`);
            if (!response.ok) return null;
            return await response.json();
        } catch (error) {
            console.error("Failed to fetch stage data:", error);
            return null;
        }
    }
};

const ReviewModes = Object.freeze({
    FULL: "full"
});

/**
 * Pure functions for business logic
 */
const QuizLogic = {
    clamp: (value, min, max) => Math.min(Math.max(value, min), max),

    normalizeStage: (stageValue, stageCount) => {
        const parsed = parseInt(stageValue, 10);
        if (Number.isNaN(parsed)) return 1;
        if (!stageCount) return Math.max(parsed, 1);
        return QuizLogic.clamp(parsed, 1, stageCount);
    },

    /**
     * Determines the initial state and mode of the quiz based on storage and URL
     */
    determineContext: (storedData, urlParams) => {
        const {storedStageCount, lastCompletedStage, rawCurrentStage} = storedData;
        const requestedStage = parseInt(urlParams.get("stage"), 10);

        const storedCurrentStage = QuizLogic.normalizeStage(rawCurrentStage, storedStageCount);
        const normalizedRequestedStage = Number.isNaN(requestedStage)
            ? null
            : QuizLogic.normalizeStage(requestedStage, storedStageCount);

        // Calculate current stage logic
        const currentStage = lastCompletedStage >= storedCurrentStage
            ? lastCompletedStage + 1
            : storedCurrentStage;

        const boundedCurrentStage = storedStageCount
            ? QuizLogic.clamp(currentStage, 1, storedStageCount)
            : Math.max(currentStage, 1);

        const activeStage = normalizedRequestedStage || boundedCurrentStage;

        const isCompletedStage = activeStage <= lastCompletedStage && lastCompletedStage > 0;
        const isReviewOnly = isCompletedStage;
        const isBlocked = activeStage > boundedCurrentStage;

        return {
            activeStage,
            boundedCurrentStage,
            storedCurrentStage,
            isCompletedStage,
            isReviewOnly,
            isBlocked
        };
    },

    calculateNextStage: (currentStage, stageCount) => {
        return stageCount
            ? Math.min(currentStage + 1, stageCount)
            : currentStage + 1;
    }
};

/**
 * DOM manipulation helper
 */
const DomHelper = {
    getElements: () => {
        const get = (id) => document.getElementById(id);
        return {
            quizPanel: get("quizPanel"),
            quizComplete: get("quizComplete"),
            quizStage: get("quizStage"),
            quizQuestionProgress: get("quizQuestionProgress"),
            quizStageProgress: get("quizStageProgress"),
            quizQuestionProgressBar: get("quizQuestionProgressBar"),
            quizTitle: get("quizTitle"),
            quizQuestion: get("quizQuestion"),
            quizOptions: get("quizOptions"),
            quizFeedback: get("quizFeedback"),
            quizNext: get("quizNext"),
            quizScore: get("quizScore"),
            quizSummary: get("quizSummary"),
            summaryAccuracy: get("summaryAccuracy"),
            summaryCount: get("summaryCount"),
            summaryMastery: get("summaryMastery"),
            quizHeroLead: get("quizHeroLead"),
            quizReviewSelect: get("quizReviewSelect"),
            quizReviewNote: get("quizReviewNote"),
            quizReviewFullButton: get("quizReviewFullButton"),
            quizModeLabel: get("quizModeLabel"),
            quizNextStageButton: get("quizNextStageButton"),
            backButton: get("topNavBackButton"),
            pageTitleLabel: get("pageTitleLabel")
        };
    },

    setElementText: (element, text) => {
        if (element) element.textContent = text;
    },

    toggleVisibility: (element, isVisible) => {
        if (!element) return;
        if (isVisible) element.classList.remove(UI_CLASSES.HIDDEN);
        else element.classList.add(UI_CLASSES.HIDDEN);
    },

    setBusy: (element, isBusy) => {
        if (element) element.setAttribute(UI_CLASSES.BUSY, isBusy ? "true" : "false");
    },

    updateProgressBar: (element, value, max, min = 1) => {
        if (!element) return;
        element.max = max;
        element.value = value;
        element.setAttribute("aria-valuemin", String(min));
        element.setAttribute("aria-valuemax", String(max));
        element.setAttribute("aria-valuenow", String(value));
    },

    createOptionButton: (text, onClick) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "quiz-option";
        button.textContent = text;
        button.addEventListener("click", onClick);
        return button;
    }
};

/**
 * Main Application Logic
 */
const App = {
    elements: null,
    state: {
        stage: 0,
        stageCount: 0,
        questions: [],
        index: 0,
        score: 0,
        answered: false,
        selectedIndex: null,
        mode: 'record', // 'record', 'review'
        reviewType: ReviewModes.FULL,
        cachedStageData: null
    },

    init: () => {
        App.elements = DomHelper.getElements();
        if (!App.elements.quizPanel) return;

        App.initNav();

        const storedData = {
            storedStageCount: StorageService.getStageCount(),
            lastCompletedStage: StorageService.getLastCompletedStage(),
            rawCurrentStage: StorageService.getCurrentStage()
        };
        const urlParams = new URLSearchParams(window.location.search);

        const context = QuizLogic.determineContext(storedData, urlParams);

        // Sync Storage if needed
        if (storedData.rawCurrentStage === null || context.boundedCurrentStage !== context.storedCurrentStage) {
            StorageService.setCurrentStage(context.boundedCurrentStage);
        }
        if (SafeLocalStore.get(STORAGE_KEYS.LAST_COMPLETED_STAGE) === null) {
            StorageService.setLastCompletedStage(storedData.lastCompletedStage);
        }

        App.state.stage = context.activeStage;
        App.state.stageCount = storedData.storedStageCount;

        App.updateHeroLead(context);

        const hasInProgressQuiz = StorageService.hasCurrentQuestionIndex(context.activeStage);

        if (context.isBlocked) {
            App.showAccessBlocked();
        } else if (context.isReviewOnly && !hasInProgressQuiz) {
            App.showReviewSelection();
        } else {
            const mode = context.isReviewOnly ? "review" : "record";
            const storedReviewType = context.isReviewOnly
                ? StorageService.getCurrentReviewType(context.activeStage)
                : null;
            App.startQuiz(mode, storedReviewType);
        }

        App.bindEvents();
    },

    initNav: () => {
        const {backButton, pageTitleLabel} = App.elements;
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 퀴즈";
            DomHelper.toggleVisibility(pageTitleLabel, true);
        }
        if (backButton) {
            DomHelper.toggleVisibility(backButton, true);
            backButton.addEventListener("click", () => window.location.href = "/web/game/bible-quiz/map");
        }
    },

    updateHeroLead: (context) => {
        const {quizHeroLead, quizTitle} = App.elements;
        if (quizTitle) {
            DomHelper.setElementText(quizTitle, `STAGE ${context.activeStage}`);
        }
        if (!quizHeroLead) return;

        if (context.isBlocked) {
            DomHelper.setElementText(quizHeroLead, "아직 진행할 수 없는 스테이지입니다.");
            quizHeroLead.classList.remove(UI_CLASSES.HIDDEN);
        } else {
            DomHelper.setElementText(quizHeroLead, "");
            quizHeroLead.classList.add(UI_CLASSES.HIDDEN);
        }
    },

    setModeLabel: (mode) => {
        const {quizModeLabel} = App.elements;
        if (!quizModeLabel) return;

        if (mode === "review") {
            quizModeLabel.textContent = "복습 모드";
            quizModeLabel.classList.add("is-review");
        } else if (mode === "record") {
            quizModeLabel.textContent = "기록 중";
            quizModeLabel.classList.remove("is-review");
        } else {
            quizModeLabel.textContent = "";
            quizModeLabel.classList.remove("is-review");
        }
    },

    showReviewSelection: () => {
        const {
            quizReviewSelect,
            quizReviewNote,
            quizReviewFullButton,
            quizPanel
        } = App.elements;

        DomHelper.toggleVisibility(quizReviewSelect, true);
        DomHelper.toggleVisibility(quizPanel, false);
        App.setModeLabel("review");

        if (quizReviewNote) {
            DomHelper.setElementText(quizReviewNote, "복습 모드는 기록과 랭킹에 반영되지 않습니다.");
        }

        if (quizReviewFullButton) {
            quizReviewFullButton.onclick = () => App.startQuiz("review", ReviewModes.FULL);
        }

        App.updateReviewButtons();
    },

    startQuiz: (mode, reviewType = ReviewModes.FULL) => {
        App.state.mode = mode;
        App.state.reviewType = reviewType || ReviewModes.FULL;

        const {quizHeroLead, quizPanel, quizReviewSelect} = App.elements;
        DomHelper.toggleVisibility(quizReviewSelect, false);
        DomHelper.toggleVisibility(quizPanel, true);
        App.setModeLabel(mode);

        if (mode === "review") {
            StorageService.setCurrentReviewType(App.state.stage, App.state.reviewType);
            DomHelper.setElementText(quizHeroLead, "기록에 반영되지 않는 복습 모드입니다.");
        } else {
            StorageService.clearCurrentReviewType(App.state.stage);
        }

        App.loadStageData();
    },

    updateReviewButtons: async () => {
        const {
            quizReviewFullButton
        } = App.elements;

        if (!quizReviewFullButton) return;

        DomHelper.setElementText(quizReviewFullButton, "확인 중...");
        quizReviewFullButton.setAttribute("aria-busy", "true");

        if (App.state.cachedStageData && App.state.cachedStageData.stage !== App.state.stage) {
            App.state.cachedStageData = null;
        }

        const data = App.state.cachedStageData
            ? App.state.cachedStageData.data
            : await ApiService.fetchStageData(App.state.stage);
        if (!data || !data.questions || data.questions.length === 0) {
            quizReviewFullButton.setAttribute("aria-busy", "false");
            return;
        }

        if (!App.state.cachedStageData) {
            App.state.cachedStageData = {stage: App.state.stage, data};
        }

        const totalCount = data.questions.length;
        DomHelper.setElementText(quizReviewFullButton, `복습 (${totalCount}문제)`);

        quizReviewFullButton.setAttribute("aria-busy", "false");
    },

    loadStageData: async () => {
        DomHelper.setBusy(App.elements.quizPanel, true);

        if (App.state.cachedStageData && App.state.cachedStageData.stage !== App.state.stage) {
            App.state.cachedStageData = null;
        }

        const data = App.state.cachedStageData
            ? App.state.cachedStageData.data
            : await ApiService.fetchStageData(App.state.stage);

        if (!data || !data.questions || data.questions.length === 0) {
            App.showError("퀴즈를 불러올 수 없습니다", "잠시 후 다시 시도해주세요.");
            return;
        }

        if (!App.state.cachedStageData) {
            App.state.cachedStageData = {stage: App.state.stage, data};
        }

        if (data.stageCount) {
            App.state.stageCount = data.stageCount;
            StorageService.setStageCount(data.stageCount);
        }

        let reviewedQuestions = App.state.mode === "review"
            ? App.applyReviewQuestions(data.questions)
            : data.questions;

        if (reviewedQuestions.length === 0) {
            App.state.reviewType = ReviewModes.FULL;
            reviewedQuestions = data.questions;
        }

        App.state.questions = reviewedQuestions;
        const hasProgress = StorageService.hasCurrentQuestionIndex(App.state.stage);
        App.state.index = StorageService.getCurrentQuestionIndex(App.state.stage, reviewedQuestions.length);
        App.state.score = 0;
        if (App.state.mode !== "review") {
            const storedScore = hasProgress ? StorageService.getCurrentScore(App.state.stage) : null;
            App.state.score = storedScore ?? 0;
            StorageService.setCurrentScore(App.state.stage, App.state.score);
        }
        App.state.answered = false;
        App.state.selectedIndex = null;

        App.renderQuestion();
    },

    applyReviewQuestions: (questions) => {
        return questions;
    },

    getStageAccuracy: (questions) => {
        const stats = StorageService.getQuestionStats(App.state.stage);
        let totalAttempts = 0;
        let totalCorrect = 0;

        questions.forEach((question) => {
            const entry = stats[String(question.id)];
            if (!entry || !entry.attempts) return;
            totalAttempts += entry.attempts;
            totalCorrect += entry.correct;
        });

        if (totalAttempts === 0) return null;
        return Math.round((totalCorrect / totalAttempts) * 100);
    },

    getMasteryLabel: (accuracy, reviewCount) => {
        if (accuracy === null) return "입문";
        if (accuracy >= 90 && reviewCount >= 3) return "완성";
        if (accuracy >= 80) return "숙련";
        if (accuracy >= 65) return "기초";
        return "입문";
    },

    persistQuestionIndex: () => {
        StorageService.setCurrentQuestionIndex(App.state.stage, App.state.index);
    },

    renderQuestion: () => {
        const {questions, index, stage, stageCount} = App.state;
        const currentQuestion = questions[index];
        const {
            quizQuestion,
            quizOptions,
            quizNext,
            quizStage,
            quizQuestionProgress,
            quizStageProgress,
            quizQuestionProgressBar,
            quizPanel,
            quizFeedback
        } = App.elements;

        DomHelper.setBusy(quizPanel, true);

        DomHelper.setElementText(quizQuestion, currentQuestion.question);

        quizOptions.innerHTML = "";
        currentQuestion.options.forEach((opt, idx) => {
            const btn = DomHelper.createOptionButton(opt, () => App.selectOption(idx));
            quizOptions.appendChild(btn);
        });

        DomHelper.setElementText(quizFeedback, "");
        quizFeedback.classList.remove(UI_CLASSES.CORRECT, UI_CLASSES.WRONG);

        quizNext.disabled = true;
        DomHelper.setElementText(quizNext, "정답 확인");

        DomHelper.setElementText(quizStage, `${stage} / ${stageCount || stage}`);
        DomHelper.setElementText(quizQuestionProgress, `${index + 1} / ${questions.length}`);
        DomHelper.updateProgressBar(quizStageProgress, stage, stageCount || stage);
        DomHelper.updateProgressBar(quizQuestionProgressBar, index + 1, questions.length);

        App.state.answered = false;
        App.state.selectedIndex = null;

        App.persistQuestionIndex();

        DomHelper.setBusy(quizPanel, false);
    },

    selectOption: (index) => {
        if (App.state.answered) return;

        App.state.selectedIndex = index;
        const buttons = App.elements.quizOptions.querySelectorAll(".quiz-option");
        buttons.forEach((btn, idx) => {
            if (idx === index) btn.classList.add(UI_CLASSES.SELECTED);
            else btn.classList.remove(UI_CLASSES.SELECTED);
        });

        App.elements.quizNext.disabled = false;
    },

    handleNext: () => {
        if (!App.state.answered) {
            App.gradeAnswer();
        } else {
            App.nextQuestion();
        }
    },

    gradeAnswer: () => {
        const {questions, index, selectedIndex} = App.state;
        const currentQuestion = questions[index];
        const isCorrect = selectedIndex === currentQuestion.answerIndex;

        App.state.answered = true;
        StorageService.updateQuestionStats(App.state.stage, currentQuestion.id, isCorrect);
        if (isCorrect && App.state.mode !== 'review') {
            App.state.score++;
            StorageService.setCurrentScore(App.state.stage, App.state.score);
        }

        const buttons = App.elements.quizOptions.querySelectorAll(".quiz-option");
        buttons.forEach((btn, idx) => {
            btn.disabled = true;
            if (idx === currentQuestion.answerIndex) {
                btn.classList.add(UI_CLASSES.CORRECT);
                btn.textContent += " (정답)";
            } else if (idx === selectedIndex && !isCorrect) {
                btn.classList.add(UI_CLASSES.WRONG);
                btn.textContent += " (오답)";
            }
        });

        const {quizFeedback, quizNext} = App.elements;
        if (isCorrect) {
            DomHelper.setElementText(quizFeedback, "😊 잘하셨어요 정답입니다!");
            quizFeedback.classList.add(UI_CLASSES.CORRECT);
        } else {
            DomHelper.setElementText(quizFeedback, "🥲 아쉽지만 오답입니다. 말씀을 다시 읽어보면 도움이 될 거예요!");
            quizFeedback.classList.add(UI_CLASSES.WRONG);
        }

        DomHelper.setElementText(quizNext, index === questions.length - 1 ? "완료" : "다음 문제");

        App.persistQuestionIndex();
    },

    nextQuestion: () => {
        const {index, questions} = App.state;
        if (index < questions.length - 1) {
            App.state.index++;
            App.renderQuestion();
        } else {
            App.finishQuiz();
        }
    },

    finishQuiz: () => {
        const {stage, score, questions, mode, stageCount} = App.state;
        const {
            quizPanel,
            quizComplete,
            quizScore,
            quizSummary,
            summaryAccuracy,
            summaryCount,
            summaryMastery,
            quizNextStageButton
        } = App.elements;

        StorageService.clearCurrentQuestionIndex(stage);
        StorageService.clearCurrentReviewType(stage);

        if (mode === "review") {
            StorageService.incrementReviewCount(stage);
        }

        if (mode !== 'review') {
            StorageService.setCurrentScore(stage, score);
            const nextStage = QuizLogic.calculateNextStage(stage, stageCount);
            StorageService.setCurrentStage(nextStage);
            StorageService.setLastCompletedStage(stage);
            StorageService.setStageScore(stage, score);
        }

        if (quizNextStageButton) {
            const nextStage = QuizLogic.calculateNextStage(stage, stageCount);
            quizNextStageButton.href = `/web/game/bible-quiz?stage=${nextStage}`;
        }

        DomHelper.setBusy(quizPanel, false);
        DomHelper.toggleVisibility(quizPanel, false);
        DomHelper.toggleVisibility(quizComplete, true);

        if (mode !== 'review') {
            DomHelper.setElementText(quizScore, `점수 ${score} / ${questions.length}`);
        } else {
            DomHelper.setElementText(quizScore, "");
        }

        if (quizSummary && summaryAccuracy && summaryCount && summaryMastery) {
            const accuracy = App.getStageAccuracy(questions);
            const reviewCount = StorageService.getReviewCount(stage);
            const mastery = App.getMasteryLabel(accuracy, reviewCount);
            const accuracyText = accuracy === null ? "-%" : `${accuracy}%`;

            DomHelper.setElementText(summaryAccuracy, accuracyText);
            DomHelper.setElementText(summaryCount, `${reviewCount}회`);
            DomHelper.setElementText(summaryMastery, mastery);

            summaryMastery.classList.remove("badge-gray", "badge-green", "badge-blue", "badge-gold");
            if (mastery === "완성") summaryMastery.classList.add("badge-gold");
            else if (mastery === "숙련") summaryMastery.classList.add("badge-blue");
            else if (mastery === "기초") summaryMastery.classList.add("badge-green");
            else summaryMastery.classList.add("badge-gray");
        }
    },

    showError: (title, message) => {
        const {
            quizQuestion,
            quizOptions,
            quizNext,
            quizPanel,
            quizStageProgress,
            quizQuestionProgressBar,
            quizStage,
            quizQuestionProgress: quizQuestionProgressText
        } = App.elements;
        DomHelper.setBusy(quizPanel, false);
        DomHelper.setElementText(quizQuestion, title);
        DomHelper.setElementText(quizOptions, message);
        quizNext.disabled = true;

        DomHelper.setElementText(quizStage, "0 / 0");
        DomHelper.setElementText(quizQuestionProgressText, "0 / 0");
        DomHelper.updateProgressBar(quizStageProgress, 0, 0, 0);
        DomHelper.updateProgressBar(quizQuestionProgressBar, 0, 0, 0);
    },

    showAccessBlocked: () => {
        const {
            quizPanel,
            quizComplete,
            quizQuestion,
            quizOptions,
            quizNext,
            quizStageProgress,
            quizQuestionProgressBar,
            quizStage,
            quizQuestionProgress: quizQuestionProgressText
        } = App.elements;
        DomHelper.setBusy(quizPanel, false);
        DomHelper.toggleVisibility(quizPanel, true);
        DomHelper.toggleVisibility(quizComplete, false);

        DomHelper.setElementText(quizQuestion, "아직 진행할 수 없는 스테이지입니다.");
        DomHelper.setElementText(quizOptions, "현재 진행 가능한 스테이지를 선택해주세요.");
        quizNext.disabled = true;

        DomHelper.setElementText(quizStage, "0 / 0");
        DomHelper.setElementText(quizQuestionProgressText, "0 / 0");
        DomHelper.updateProgressBar(quizStageProgress, 0, 0, 0);
        DomHelper.updateProgressBar(quizQuestionProgressBar, 0, 0, 0);
    },

    bindEvents: () => {
        const {quizNext} = App.elements;
        if (quizNext) {
            quizNext.addEventListener("click", App.handleNext);
        }
    }
};

document.addEventListener("DOMContentLoaded", App.init);
