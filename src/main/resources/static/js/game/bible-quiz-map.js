const QUIZ_API_BASE = "/api/v1/game/bible-quiz";

const QUIZ_STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "currentStage",
    LAST_COMPLETED_STAGE: "lastCompletedStage",
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
        quizMapNote: document.getElementById("quizMapNote")
    };
    const missingRequired = [
        "stageList",
        "quizMapNote"
    ].some(key => !elements[key]);
    return missingRequired ? null : elements;
};

const buildContext = elements => {
    const rawCurrentStage = LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE);
    const currentStage = parseInt(rawCurrentStage, 10);
    const normalizedCurrentStage = Number.isNaN(currentStage) || currentStage < 1 ? 1 : currentStage;
    if (normalizedCurrentStage !== currentStage) {
        LocalStore.set(QUIZ_STORAGE_KEYS.CURRENT_STAGE, 1);
    }
    const rawLastCompletedStage = LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_STAGE);
    if (rawLastCompletedStage === null) {
        LocalStore.set(QUIZ_STORAGE_KEYS.LAST_COMPLETED_STAGE, 0);
    }
    const lastCompletedStage = parseInt(LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_STAGE), 10);
    return {
        elements,
        currentStage: normalizedCurrentStage,
        lastCompletedStage: Number.isNaN(lastCompletedStage) ? 0 : lastCompletedStage
    };
};

const updateSummary = context => {
    context.elements.quizMapNote.textContent = "스테이지를 선택하면 퀴즈 화면에서 모드를 고를 수 있습니다.";
};

const resolveStageStatus = (stage, currentStage) => {
    if (stage < currentStage) {
        return "completed";
    }
    if (stage === currentStage) {
        return "active";
    }
    return "locked";
};

const resolveStageCardProps = (status, questionCount, stage, stageScore, lastCompletedStage) => {
    let statusLabel = "진행 전";
    let metaText = `${questionCount}문제`;
    let route = `/web/game/bible-quiz?stage=${stage}`;
    let isClickable = status !== "locked";
    let actionLabel = null;
    const classList = ["stage-card"];

    if (status === "completed") {
        classList.push("is-complete");
        statusLabel = "완료";
        if (stageScore !== null && questionCount > 0) {
            const percentage = Math.round((stageScore / questionCount) * 100);
            metaText = `점수 ${stageScore} / ${questionCount} (${percentage}%)`;
        } else {
            metaText = "완료";
        }
        if (stage === lastCompletedStage) {
            actionLabel = "재도전 가능";
        }
    } else if (status === "active") {
        classList.push("is-current");
        statusLabel = "현재";
        metaText = "진행 가능";
    } else {
        classList.push("is-locked");
        statusLabel = "잠김";
        metaText = "진행 전";
        route = null;
    }

    if (isClickable) {
        classList.push("is-clickable");
    }

    return {statusLabel, metaText, route, isClickable, actionLabel, classList};
};

const buildStageCardMarkup = (summary, context) => {
    const stage = summary.stage;
    const status = resolveStageStatus(stage, context.currentStage);
    const stageScore = status === "completed" ? getStoredStageScore(stage) : null;
    const {statusLabel, metaText, route, isClickable, actionLabel, classList} = resolveStageCardProps(
        status,
        summary.questionCount,
        stage,
        stageScore,
        context.lastCompletedStage
    );
    const isCurrentStage = status === "active";
    const badgeClass = [
        "stage-status",
        status === "completed" ? "is-complete" : "",
        status === "active" ? "is-current" : ""
    ].filter(Boolean).join(" ");
    const statusIcon = status === "completed" ? "✓" : status === "active" ? "▶︎" : "";
    const statusIconMarkup = statusIcon
        ? `<span class="stage-status-icon" aria-hidden="true">${statusIcon}</span>`
        : "";
    const disabledAttr = isClickable ? "" : "disabled";
    const routeAttr = route ? `data-route="${route}"` : "";
    const ariaCurrentAttr = isCurrentStage ? 'aria-current="step"' : "";
    const actionMarkup = actionLabel ? `<span class="stage-action">${actionLabel}</span>` : "";
    const currentLabelMarkup = isCurrentStage
        ? `<span class="stage-current-label">현재 스테이지</span>`
        : "";

    return `
        <button type="button" class="${classList.join(" ")}" ${disabledAttr} ${routeAttr} ${ariaCurrentAttr}>
            <div class="stage-card-header">
                <span class="stage-number">${stage} 스테이지</span>
                <span class="${badgeClass}">${statusIconMarkup}${statusLabel}</span>
            </div>
            <div class="stage-meta">${metaText}</div>
            ${currentLabelMarkup}
            ${actionMarkup}
        </button>
    `;
};

const getGridColumnCount = stageList => {
    const template = window.getComputedStyle(stageList).gridTemplateColumns;
    if (!template || template === "none") {
        return 1;
    }
    const columns = template.split(" ").filter(Boolean).length;
    return columns > 0 ? columns : 1;
};

const clearFlowClasses = stageCards => {
    stageCards.forEach(card => {
        card.classList.remove("flow-right", "flow-left", "flow-down", "flow-end");
    });
};

const applyFlowDirections = stageList => {
    const stageCards = Array.from(stageList.querySelectorAll(".stage-card"));
    if (stageCards.length === 0) {
        return;
    }
    clearFlowClasses(stageCards);
    const columns = getGridColumnCount(stageList);
    const total = stageCards.length;
    const maxRow = Math.floor((total - 1) / columns);

    stageCards.forEach((card, index) => {
        if (index === total - 1) {
            card.classList.add("flow-end");
            return;
        }
        if (columns === 1) {
            card.classList.add("flow-down");
            return;
        }

        const row = Math.floor(index / columns);
        const col = index % columns;
        const isRowEven = row % 2 === 0;

        if (isRowEven) {
            const hasNextInRow = index + 1 < total && Math.floor((index + 1) / columns) === row;
            if (hasNextInRow) {
                card.classList.add("flow-right");
                return;
            }
            if (row < maxRow) {
                card.classList.add("flow-down");
                return;
            }
            card.classList.add("flow-end");
            return;
        }

        const hasPrevInRow = col > 0;
        if (hasPrevInRow) {
            card.classList.add("flow-left");
            return;
        }
        if (row < maxRow) {
            card.classList.add("flow-down");
            return;
        }
        card.classList.add("flow-end");
    });
};

const renderStages = (context, stageSummaries) => {
    context.elements.stageList.innerHTML = stageSummaries
        .map(summary => buildStageCardMarkup(summary, context))
        .join("");
    applyFlowDirections(context.elements.stageList);
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
    const stageCount = stageSummaries.length;
    context.currentStage = normalizeStage(context.currentStage, stageCount);
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
    let resizeTimer = null;
    window.addEventListener("resize", () => {
        if (resizeTimer) {
            window.clearTimeout(resizeTimer);
        }
        resizeTimer = window.setTimeout(() => {
            applyFlowDirections(elements.stageList);
        }, 120);
    });
});
