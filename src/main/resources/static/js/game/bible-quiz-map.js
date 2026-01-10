const QUIZ_API_BASE = "/api/v1/game/bible-quiz";

const QUIZ_STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "currentStage",
    LAST_COMPLETED_DATE: "lastCompletedDate",
    STAGE_SCORE_PREFIX: "quizStageScore",
});

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

const getStageScoreKey = stage => `${QUIZ_STORAGE_KEYS.STAGE_SCORE_PREFIX}_${stage}`;

const getStoredStageScore = stage => {
    const score = parseInt(LocalStore.get(getStageScoreKey(stage)), 10);
    return Number.isNaN(score) ? null : score;
};

const fetchStageSummaries = async () => {
    try {
        const response = await fetch(`${QUIZ_API_BASE}/stages`);
        if (!response.ok) {
            return null;
        }
        return await response.json();
    } catch (error) {
        return null;
    }
};

const getQuizMapElements = () => {
    const elements = {
        stageList: document.getElementById("stageList"),
        quizMapDate: document.getElementById("quizMapDate"),
        quizMapNote: document.getElementById("quizMapNote")
    };
    const missingRequired = Object.values(elements).some(element => !element);
    return missingRequired ? null : elements;
};

const buildContext = elements => {
    const today = getLocalDateString();
    const storedDate = LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE);
    const currentStage = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE), 10);

    return {
        elements,
        today,
        storedDate,
        currentStage,
        canPlayToday: storedDate !== today
    };
};

const updateSummary = context => {
    context.elements.quizMapDate.textContent = `오늘 ${context.today}`;
    if (context.storedDate === context.today) {
        context.elements.quizMapNote.textContent = "오늘의 퀴즈를 완료했습니다. 현재 스테이지는 내일부터 시작할 수 있습니다.";
        return;
    }
    context.elements.quizMapNote.textContent = "현재 스테이지는 오늘의 퀴즈에서 진행됩니다.";
};

const resolveStageStatus = (stage, currentStage) => {
    if (stage < currentStage) {
        return "complete";
    }
    if (stage === currentStage) {
        return "current";
    }
    return "locked";
};

const resolveStageCardProps = (status, questionCount, canPlayToday, stage, stageScore) => {
    let statusLabel = "진행 전";
    let metaText = `${questionCount}문제`;
    let route = null;
    let isClickable = false;
    const classList = ["stage-card"];

    if (status === "complete") {
        classList.push("is-complete", "is-clickable");
        statusLabel = "완료";
        if (stageScore !== null && questionCount > 0) {
            const percentage = Math.round((stageScore / questionCount) * 100);
            metaText = `점수 ${stageScore} / ${questionCount} (${percentage}%)`;
        } else {
            metaText = "복습 가능";
        }
        route = `/web/game/bible-quiz?stage=${stage}&mode=review`;
        isClickable = true;
    } else if (status === "current") {
        classList.push("is-current");
        statusLabel = "현재";
        if (canPlayToday) {
            classList.push("is-clickable");
            metaText = "오늘 진행";
            route = "/web/game/bible-quiz";
            isClickable = true;
        } else {
            metaText = "내일 시작 가능";
        }
    } else {
        classList.push("is-locked");
    }

    return { statusLabel, metaText, route, isClickable, classList };
};

const buildStageCardMarkup = (summary, context) => {
    const stage = summary.stage;
    const status = resolveStageStatus(stage, context.currentStage);
    const stageScore = status === "complete" ? getStoredStageScore(stage) : null;
    const { statusLabel, metaText, route, isClickable, classList } = resolveStageCardProps(
        status,
        summary.questionCount,
        context.canPlayToday,
        stage,
        stageScore
    );
    const badgeClass = [
        "stage-status",
        status === "complete" ? "is-complete" : "",
        status === "current" ? "is-current" : ""
    ].filter(Boolean).join(" ");
    const disabledAttr = isClickable ? "" : "disabled";
    const routeAttr = route ? `data-route="${route}"` : "";

    return `
        <button type="button" class="${classList.join(" ")}" ${disabledAttr} ${routeAttr}>
            <div class="stage-card-header">
                <span class="stage-number">${stage} 스테이지</span>
                <span class="${badgeClass}">${statusLabel}</span>
            </div>
            <div class="stage-meta">${metaText}</div>
        </button>
    `;
};

const renderStages = (context, stageSummaries) => {
    context.elements.stageList.innerHTML = stageSummaries
        .map(summary => buildStageCardMarkup(summary, context))
        .join("");
    context.elements.stageList.querySelectorAll("[data-route]").forEach(card => {
        card.addEventListener("click", () => {
            window.location.href = card.dataset.route;
        });
    });
};

const showLoadError = context => {
    context.elements.quizMapNote.textContent = "퀴즈 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.";
};

const initializeQuizMap = async context => {
    const stageSummaries = await fetchStageSummaries();
    if (!Array.isArray(stageSummaries) || stageSummaries.length === 0) {
        showLoadError(context);
        return;
    }
    context.currentStage = normalizeStage(context.currentStage, stageSummaries.length);
    updateSummary(context);
    renderStages(context, stageSummaries);
};

document.addEventListener("DOMContentLoaded", () => {
    const elements = getQuizMapElements();
    if (!elements) {
        return;
    }
    const context = buildContext(elements);
    initializeQuizMap(context).catch(() => showLoadError(context));
});
