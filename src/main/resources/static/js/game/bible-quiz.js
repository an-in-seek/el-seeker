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
    CURRENT_STAGE: "quizCurrentStage",
    LAST_COMPLETED_STAGE: "quizLastCompletedStage",
    STAGE_COUNT: "quizStageCount",
    STAGE_SCORE_PREFIX: "quizStageScore",
    CURRENT_QUESTION_PREFIX: "quizCurrentQuestionStage",
    CURRENT_SCORE_PREFIX: "quizCurrentScoreStage",
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
        const { storedStageCount, lastCompletedStage, rawCurrentStage } = storedData;
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
        const requiresModeSelection = activeStage === lastCompletedStage && lastCompletedStage > 0;
        const isPracticeOnly = activeStage < lastCompletedStage;
        const isBlocked = activeStage > boundedCurrentStage;

        return {
            activeStage,
            boundedCurrentStage,
            storedCurrentStage,
            isCompletedStage,
            requiresModeSelection,
            isPracticeOnly,
            isBlocked,
            canRetry: requiresModeSelection
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
            quizModeSelect: get("quizModeSelect"),
            quizModeRetryButton: get("quizModeRetryButton"),
            quizModePracticeButton: get("quizModePracticeButton"),
            quizModeNote: get("quizModeNote"),
            quizPanel: get("quizPanel"),
            quizComplete: get("quizComplete"),
            quizStage: get("quizStage"),
            quizQuestionProgress: get("quizQuestionProgress"),
            quizStageProgress: get("quizStageProgress"),
            quizQuestionProgressBar: get("quizQuestionProgressBar"),
            quizQuestion: get("quizQuestion"),
            quizOptions: get("quizOptions"),
            quizFeedback: get("quizFeedback"),
            quizNext: get("quizNext"),
            quizScore: get("quizScore"),
            quizModeSelectButton: get("quizModeSelectButton"),
            quizHeroLead: get("quizHeroLead"),
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
        mode: 'record' // 'record', 'practice', 'retry'
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
        } else if (context.requiresModeSelection && !hasInProgressQuiz) {
            App.showModeSelection(context);
        } else {
            let mode = 'record';
            if (context.isPracticeOnly) {
                mode = 'practice';
            } else if (context.requiresModeSelection && hasInProgressQuiz) {
                const storedScore = StorageService.getCurrentScore(context.activeStage);
                mode = storedScore === null ? 'practice' : 'retry';
            }
            App.startQuiz(mode);
        }

        App.bindEvents();
    },

    initNav: () => {
        const { backButton, pageTitleLabel } = App.elements;
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 퀴즈";
            DomHelper.toggleVisibility(pageTitleLabel, true);
        }
        if (backButton) {
            DomHelper.toggleVisibility(backButton, true);
            backButton.addEventListener("click", () => window.location.href = "/web/game");
        }
    },

    updateHeroLead: (context) => {
        const { quizHeroLead } = App.elements;
        if (!quizHeroLead) return;

        let message = "선택한 스테이지 퀴즈를 시작합니다.";
        if (context.isBlocked) message = "아직 열리지 않은 스테이지입니다.";
        else if (context.requiresModeSelection) message = "재도전 또는 연습 모드를 선택하세요.";
        else if (context.isPracticeOnly) message = "기록에 반영되지 않는 연습 모드입니다.";
        else if (context.isCompletedStage) message = "완료된 스테이지입니다.";

        DomHelper.setElementText(quizHeroLead, message);
    },

    showModeSelection: (context) => {
        const { quizModeSelect, quizModeRetryButton, quizModePracticeButton, quizModeNote, quizPanel } = App.elements;
        
        DomHelper.toggleVisibility(quizModeSelect, true);
        DomHelper.toggleVisibility(quizPanel, false);

        if (quizModeRetryButton) {
            DomHelper.toggleVisibility(quizModeRetryButton, context.canRetry);
            quizModeRetryButton.disabled = !context.canRetry;
            quizModeRetryButton.onclick = () => App.startQuiz('retry');
        }

        if (quizModePracticeButton) {
            quizModePracticeButton.onclick = () => App.startQuiz('practice');
        }

        if (quizModeNote) {
            DomHelper.setElementText(quizModeNote, context.canRetry
                ? "재도전은 기록에 반영되고, 연습은 기록에 반영되지 않습니다."
                : "연습은 기록에 반영되지 않습니다.");
        }
    },

    startQuiz: (mode) => {
        App.state.mode = mode;
        
        const { quizHeroLead, quizModeSelect, quizPanel } = App.elements;
        DomHelper.toggleVisibility(quizModeSelect, false);
        DomHelper.toggleVisibility(quizPanel, true);

        if (mode === 'practice') DomHelper.setElementText(quizHeroLead, "기록에 반영되지 않는 연습 모드입니다.");
        if (mode === 'retry') DomHelper.setElementText(quizHeroLead, "마지막 완료 스테이지를 다시 도전합니다.");

        App.loadStageData();
    },

    loadStageData: async () => {
        DomHelper.setBusy(App.elements.quizPanel, true);
        
        const data = await ApiService.fetchStageData(App.state.stage);
        
        if (!data || !data.questions || data.questions.length === 0) {
            App.showError("퀴즈를 불러올 수 없습니다", "잠시 후 다시 시도해주세요.");
            return;
        }

        if (data.stageCount) {
            App.state.stageCount = data.stageCount;
            StorageService.setStageCount(data.stageCount);
        }

        App.state.questions = data.questions;
        const hasProgress = StorageService.hasCurrentQuestionIndex(App.state.stage);
        App.state.index = StorageService.getCurrentQuestionIndex(App.state.stage, data.questions.length);
        App.state.score = 0;
        if (App.state.mode !== "practice") {
            const storedScore = hasProgress ? StorageService.getCurrentScore(App.state.stage) : null;
            App.state.score = storedScore ?? 0;
            StorageService.setCurrentScore(App.state.stage, App.state.score);
        }
        App.state.answered = false;
        App.state.selectedIndex = null;

        App.renderQuestion();
    },

    persistQuestionIndex: () => {
        StorageService.setCurrentQuestionIndex(App.state.stage, App.state.index);
    },

    renderQuestion: () => {
        const { questions, index, stage, stageCount } = App.state;
        const currentQuestion = questions[index];
        const { quizQuestion, quizOptions, quizNext, quizStage, quizQuestionProgress, quizStageProgress, quizQuestionProgressBar, quizPanel, quizFeedback } = App.elements;

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
        const { questions, index, selectedIndex } = App.state;
        const currentQuestion = questions[index];
        const isCorrect = selectedIndex === currentQuestion.answerIndex;
        
        App.state.answered = true;
        if (isCorrect && App.state.mode !== 'practice') {
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

        const { quizFeedback, quizNext } = App.elements;
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
        const { index, questions } = App.state;
        if (index < questions.length - 1) {
            App.state.index++;
            App.renderQuestion();
        } else {
            App.finishQuiz();
        }
    },

    finishQuiz: () => {
        const { stage, score, questions, mode, stageCount } = App.state;
        const { quizPanel, quizComplete, quizScore, quizModeSelectButton } = App.elements;

        StorageService.clearCurrentQuestionIndex(stage);
        StorageService.setCurrentScore(stage, score);

        if (mode !== 'practice') {
            const nextStage = QuizLogic.calculateNextStage(stage, stageCount);
            StorageService.setCurrentStage(nextStage);
            StorageService.setLastCompletedStage(stage);
            StorageService.setStageScore(stage, score);
        }

        DomHelper.setBusy(quizPanel, false);
        DomHelper.toggleVisibility(quizPanel, false);
        DomHelper.toggleVisibility(quizComplete, true);

        if (mode !== 'practice') {
            DomHelper.setElementText(quizScore, `점수 ${score} / ${questions.length}`);
        } else {
            DomHelper.setElementText(quizScore, "");
        }

        if (mode !== 'practice' && quizModeSelectButton) {
            DomHelper.toggleVisibility(quizModeSelectButton, true);
            quizModeSelectButton.onclick = () => {
                window.location.href = `/web/game/bible-quiz?stage=${stage}`;
            };
        }
    },

    showError: (title, message) => {
        const { quizQuestion, quizOptions, quizNext, quizPanel, quizStageProgress, quizQuestionProgressBar, quizStage, quizQuestionProgress: quizQuestionProgressText } = App.elements;
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
        const { quizPanel, quizComplete, quizQuestion, quizOptions, quizNext, quizStageProgress, quizQuestionProgressBar, quizStage, quizQuestionProgress: quizQuestionProgressText } = App.elements;
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
        const { quizNext } = App.elements;
        if (quizNext) {
            quizNext.addEventListener("click", App.handleNext);
        }
    }
};

document.addEventListener("DOMContentLoaded", App.init);
