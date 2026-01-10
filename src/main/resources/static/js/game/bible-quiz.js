const QUIZ_API_BASE = "/api/v1/game/bible-quiz";
const QUIZ_STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "currentStage",
    LAST_COMPLETED_DATE: "lastCompletedDate",
    LAST_STAGE_SCORE: "lastStageScore",
    LAST_STAGE_QUESTION_COUNT: "lastStageQuestionCount",
    LAST_COMPLETED_STAGE: "lastCompletedStage",
    STAGE_COUNT: "quizStageCount",
    STAGE_SCORE_PREFIX: "quizStageScore",
});

const fetchStageData = async stageNumber => {
    try {
        const response = await fetch(`${QUIZ_API_BASE}/stages/${stageNumber}`);
        if (!response.ok) {
            return null;
        }
        return await response.json();
    } catch (error) {
        return null;
    }
};

const clampNumber = (value, min, max) => Math.min(Math.max(value, min), max);

const normalizeStage = (stageValue, stageCount) => {
    const parsed = parseInt(stageValue, 10);
    if (Number.isNaN(parsed)) {
        return 1;
    }
    if (!stageCount) {
        return Math.max(parsed, 1);
    }
    return clampNumber(parsed, 1, stageCount);
};

const getStoredDate = () => LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE);

const getStoredStageCount = () => {
    const count = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.STAGE_COUNT), 10);
    return Number.isNaN(count) ? null : count;
};

const getStoredScore = () => {
    const score = LocalStore.get(QUIZ_STORAGE_KEYS.LAST_STAGE_SCORE);
    const parsed = parseInt(score, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const getStoredQuestionCount = () => {
    const count = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.LAST_STAGE_QUESTION_COUNT), 10);
    return Number.isNaN(count) ? null : count;
};

const getStoredLastCompletedStage = () => {
    const stage = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_STAGE), 10);
    return Number.isNaN(stage) ? null : stage;
};

const getStageScoreKey = stage => `${QUIZ_STORAGE_KEYS.STAGE_SCORE_PREFIX}_${stage}`;

const resolveRetryStage = stageCount => {
    const storedStage = getStoredLastCompletedStage();
    if (storedStage) {
        return storedStage;
    }
    const currentStage = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE), 10);
    if (Number.isNaN(currentStage)) {
        return null;
    }
    const retryStage = Math.max(currentStage - 1, 1);
    if (stageCount && retryStage > stageCount) {
        return stageCount;
    }
    return retryStage;
};

const resetForRetry = stage => {
    if (!stage) {
        return;
    }
    LocalStore.set(QUIZ_STORAGE_KEYS.CURRENT_STAGE, stage);
    LocalStore.remove(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE);
    LocalStore.remove(QUIZ_STORAGE_KEYS.LAST_STAGE_SCORE);
    LocalStore.remove(QUIZ_STORAGE_KEYS.LAST_STAGE_QUESTION_COUNT);
};

const showCompletion = (quizPanel, quizComplete, quizScore, score, questionCount) => {
    quizPanel.setAttribute("aria-busy", "false");
    quizPanel.classList.add("d-none");
    if (score !== null && score !== undefined && questionCount) {
        quizScore.textContent = `오늘 점수 ${score} / ${questionCount}`;
    } else {
        quizScore.textContent = "";
    }
    quizComplete.classList.remove("d-none");
};

const showReviewButton = context => {
    const {quizModeSelectButton} = context.elements;
    if (!quizModeSelectButton) {
        return;
    }
    const reviewStage = context.state?.stage || getStoredLastCompletedStage();
    if (!reviewStage) {
        return;
    }
    quizModeSelectButton.classList.remove("d-none");
    quizModeSelectButton.addEventListener("click", () => {
        window.location.href = `/web/game/bible-quiz?stage=${reviewStage}`;
    });
};

const getQuizElements = () => {
    const getElement = id => document.getElementById(id);
    const elements = {
        quizModeSelect: getElement("quizModeSelect"),
        quizModeRetryButton: getElement("quizModeRetryButton"),
        quizModeReviewButton: getElement("quizModeReviewButton"),
        quizPanel: getElement("quizPanel"),
        quizComplete: getElement("quizComplete"),
        quizStage: getElement("quizStage"),
        quizQuestionProgress: getElement("quizQuestionProgress"),
        quizStageProgress: getElement("quizStageProgress"),
        quizQuestionProgressBar: getElement("quizQuestionProgressBar"),
        quizQuestion: getElement("quizQuestion"),
        quizOptions: getElement("quizOptions"),
        quizFeedback: getElement("quizFeedback"),
        quizNext: getElement("quizNext"),
        quizScore: getElement("quizScore"),
        quizModeSelectButton: getElement("quizModeSelectButton"),
        quizHeroLead: getElement("quizHeroLead")
    };
    const missingRequired = [
        "quizPanel",
        "quizComplete",
        "quizStage",
        "quizQuestionProgress",
        "quizStageProgress",
        "quizQuestionProgressBar",
        "quizQuestion",
        "quizOptions",
        "quizFeedback",
        "quizNext",
        "quizScore"
    ].some(key => !elements[key]);

    return missingRequired ? null : elements;
};

const buildContext = elements => {
    const today = getLocalDateString();
    const storedDate = getStoredDate();
    const storedScore = getStoredScore();
    const storedQuestionCount = getStoredQuestionCount();
    const storedStageCount = getStoredStageCount();
    const lastCompletedStage = getStoredLastCompletedStage();
    const queryParams = new URLSearchParams(window.location.search);
    const requestedStage = parseInt(queryParams.get("stage"), 10);
    const currentStage = normalizeStage(LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE), storedStageCount);
    const normalizedRequestedStage = Number.isNaN(requestedStage)
        ? null
        : normalizeStage(requestedStage, storedStageCount);
    const activeStage = normalizedRequestedStage || currentStage;
    const requiresModeSelection = normalizedRequestedStage !== null && normalizedRequestedStage < currentStage;
    const canRetry = normalizedRequestedStage !== null && normalizedRequestedStage === lastCompletedStage;

    return {
        elements,
        today,
        storedDate,
        storedScore,
        storedQuestionCount,
        stageCount: storedStageCount,
        isReviewMode: false,
        isRetryMode: false,
        requiresModeSelection,
        canRetry,
        hasResetForRetry: false,
        activeStage,
        state: null
    };
};

const updateHeroLead = context => {
    const {quizHeroLead} = context.elements;
    if (!quizHeroLead) {
        return;
    }
    if (context.requiresModeSelection && !context.isReviewMode && !context.isRetryMode) {
        quizHeroLead.textContent = "재도전 또는 연습 모드를 선택하세요.";
        return;
    }
    if (context.isReviewMode) {
        quizHeroLead.textContent = "기록에 반영되지 않는 연습 모드입니다.";
        return;
    }
    if (context.isRetryMode) {
        quizHeroLead.textContent = "마지막 완료 스테이지를 다시 도전합니다.";
    }
};

const setBusy = (context, isBusy) => {
    context.elements.quizPanel.setAttribute("aria-busy", isBusy ? "true" : "false");
};

const showModeSelection = context => {
    const {quizModeSelect, quizModeRetryButton, quizModeReviewButton, quizPanel} = context.elements;
    if (!context.requiresModeSelection || !quizModeSelect || !quizModeRetryButton || !quizModeReviewButton || !quizPanel) {
        return false;
    }
    quizModeSelect.classList.remove("d-none");
    quizPanel.classList.add("d-none");
    quizModeRetryButton.disabled = !context.canRetry;
    quizModeRetryButton.textContent = context.canRetry ? "재도전" : "재도전 (불가능)"
    quizModeRetryButton.addEventListener("click", () => {
        if (!context.canRetry) {
            return;
        }
        startQuizWithMode(context, "retry");
    });
    quizModeReviewButton.addEventListener("click", () => {
        startQuizWithMode(context, "review");
    });
    return true;
};

const startQuizWithMode = (context, mode) => {
    context.isReviewMode = mode === "review";
    context.isRetryMode = mode === "retry";
    context.hasResetForRetry = false;
    context.elements.quizModeSelect.classList.add("d-none");
    context.elements.quizPanel.classList.remove("d-none");
    updateHeroLead(context);
    initializeQuiz(context).catch(() => showLoadError(context));
};

const resetFeedback = context => {
    context.elements.quizFeedback.textContent = "";
    context.elements.quizFeedback.classList.remove("is-correct", "is-wrong");
};

const updateProgressBar = (element, value, max, min) => {
    element.max = max;
    element.value = value;
    element.setAttribute("aria-valuemin", String(min));
    element.setAttribute("aria-valuemax", String(max));
    element.setAttribute("aria-valuenow", String(value));
};

const updateProgress = context => {
    const {state} = context;
    if (!state) {
        return;
    }
    const stageCount = context.stageCount || state.stage;
    const questionNumber = state.index + 1;
    context.elements.quizStage.textContent = `${state.stage} / ${stageCount}`;
    context.elements.quizQuestionProgress.textContent = `${questionNumber} / ${state.questions.length}`;
    updateProgressBar(context.elements.quizStageProgress, state.stage, stageCount, 1);
    updateProgressBar(context.elements.quizQuestionProgressBar, questionNumber, state.questions.length, 1);
};

const showLoadError = context => {
    const stageCount = context.stageCount || 0;
    const questionCount = context.storedQuestionCount || 0;
    setBusy(context, false);
    context.elements.quizQuestion.textContent = "오늘의 퀴즈를 불러올 수 없습니다";
    context.elements.quizOptions.textContent = "잠시 후 다시 시도해주세요.";
    context.elements.quizNext.disabled = true;
    resetFeedback(context);
    context.elements.quizStage.textContent = `0 / ${stageCount}`;
    context.elements.quizQuestionProgress.textContent = `0 / ${questionCount}`;
    updateProgressBar(context.elements.quizStageProgress, 0, stageCount, 0);
    updateProgressBar(context.elements.quizQuestionProgressBar, 0, questionCount, 0);
};

const renderQuestion = context => {
    setBusy(context, true);
    const current = context.state.questions[context.state.index];
    if (!current) {
        showLoadError(context);
        return;
    }

    const {quizQuestion, quizOptions, quizNext} = context.elements;
    quizQuestion.textContent = current.question;
    quizOptions.innerHTML = "";
    resetFeedback(context);
    quizNext.disabled = true;
    quizNext.textContent = "정답 확인";
    context.state.answered = false;
    context.state.selectedIndex = null;

    current.options.forEach((option, optionIndex) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "quiz-option";
        button.textContent = option;
        button.addEventListener("click", () => selectOption(context, button, optionIndex));
        quizOptions.appendChild(button);
    });

    updateProgress(context);
    setBusy(context, false);
};

const selectOption = (context, selectedButton, selectedIndex) => {
    if (context.state.answered) {
        return;
    }
    context.state.selectedIndex = selectedIndex;
    context.elements.quizOptions.querySelectorAll(".quiz-option").forEach(button => {
        button.classList.remove("is-selected");
    });
    selectedButton.classList.add("is-selected");
    context.elements.quizNext.disabled = false;
};

const gradeAnswer = context => {
    if (context.state.answered || context.state.selectedIndex === null) {
        return;
    }
    if (context.isRetryMode && !context.hasResetForRetry && context.state.index === 0) {
        resetForRetry(context.activeStage);
        context.hasResetForRetry = true;
    }
    context.state.answered = true;
    const current = context.state.questions[context.state.index];
    const isCorrect = context.state.selectedIndex === current.answerIndex;

    context.elements.quizOptions.querySelectorAll(".quiz-option")
        .forEach((button, index) => {
            button.disabled = true;
            if (index === current.answerIndex) {
                button.classList.add("is-correct");
                button.textContent = `${button.textContent} (정답)`;
            }
            if (index === context.state.selectedIndex && !isCorrect) {
                button.classList.add("is-wrong");
                button.textContent = `${button.textContent} (오답)`;
            }
        });

    if (isCorrect) {
        if (!context.state.isReview) {
            context.state.score += 1;
        }
        context.elements.quizFeedback.textContent = "😊 잘하셨어요 정답입니다!";
        context.elements.quizFeedback.classList.add("is-correct");
    } else {
        context.elements.quizFeedback.textContent = "🥲 아쉽지만 오답입니다. 말씀을 다시 읽어보면 도움이 될 거예요!";
        context.elements.quizFeedback.classList.add("is-wrong");
    }

    context.elements.quizNext.disabled = false;
    context.elements.quizNext.textContent = context.state.index === context.state.questions.length - 1
        ? "완료"
        : "다음 문제";
};

const handleNext = context => {
    if (!context.state.answered) {
        if (context.state.selectedIndex === null) {
            return;
        }
        gradeAnswer(context);
        return;
    }
    if (context.state.index === context.state.questions.length - 1) {
        if (!context.state.isReview) {
            const nextStage = context.stageCount
                ? Math.min(context.state.stage + 1, context.stageCount)
                : context.state.stage + 1;
            LocalStore.set(QUIZ_STORAGE_KEYS.CURRENT_STAGE, nextStage);
            LocalStore.set(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE, context.today);
            LocalStore.set(QUIZ_STORAGE_KEYS.LAST_STAGE_SCORE, context.state.score);
            LocalStore.set(QUIZ_STORAGE_KEYS.LAST_STAGE_QUESTION_COUNT, context.state.questions.length);
            LocalStore.set(QUIZ_STORAGE_KEYS.LAST_COMPLETED_STAGE, context.state.stage);
            LocalStore.set(getStageScoreKey(context.state.stage), context.state.score);
        }
        showCompletion(
            context.elements.quizPanel,
            context.elements.quizComplete,
            context.elements.quizScore,
            context.state.isReview ? null : context.state.score,
            context.state.questions.length
        );
        if (!context.state.isReview) {
            showReviewButton(context);
        }
        return;
    }
    context.state.index += 1;
    renderQuestion(context);
};

const initializeQuiz = async context => {
    if (!context.isReviewMode && !context.isRetryMode && context.storedDate === context.today) {
        showCompletion(
            context.elements.quizPanel,
            context.elements.quizComplete,
            context.elements.quizScore,
            context.storedScore,
            context.storedQuestionCount
        );
        showReviewButton(context);
        return;
    }

    setBusy(context, true);
    const stageData = await fetchStageData(context.activeStage);

    if (!stageData || !Array.isArray(stageData.questions) || stageData.questions.length === 0) {
        showLoadError(context);
        return;
    }
    if (Number.isInteger(stageData.stageCount) && stageData.stageCount > 0) {
        context.stageCount = stageData.stageCount;
        LocalStore.set(QUIZ_STORAGE_KEYS.STAGE_COUNT, stageData.stageCount);
    }

    context.state = {
        index: 0,
        answered: false,
        score: 0,
        stage: context.activeStage,
        questions: stageData.questions,
        isReview: context.isReviewMode,
        selectedIndex: null
    };

    renderQuestion(context);
};

const bindQuizEvents = context => {
    context.elements.quizNext.addEventListener("click", () => {
        handleNext(context);
    });
};

document.addEventListener("DOMContentLoaded", () => {
    const elements = getQuizElements();
    if (!elements) {
        return;
    }

    const context = buildContext(elements);
    updateHeroLead(context);
    bindQuizEvents(context);
    if (!showModeSelection(context)) {
        initializeQuiz(context).catch(() => showLoadError(context));
    }
});
