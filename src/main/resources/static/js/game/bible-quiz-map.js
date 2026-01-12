import {LocalStore} from "/js/storage-util.js?v=2.1";

// Module-scope script
// ==========================================
// Constants & Configuration
// ==========================================
const API_CONFIG = {
    BASE_URL: "/api/v1/game/bible-quiz",
    ENDPOINTS: {
        STAGES: "/stages"
    }
};

const STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "quiz_current_stage",
    LAST_COMPLETED_STAGE: "quiz_last_completed_stage",
    STAGE_SCORE_PREFIX: "quiz_stage_score",
    QUESTION_STATS_PREFIX: "quiz_question_stats_stage",
    REVIEW_COUNT_PREFIX: "quiz_review_count_stage",
    CURRENT_QUESTION_PREFIX: "quiz_current_question_stage",
    CURRENT_SCORE_PREFIX: "quiz_current_score_stage",
    CURRENT_REVIEW_TYPE_PREFIX: "quiz_current_review_type_stage",
    LAST_WRONG_IDS_PREFIX: "quiz_last_wrong_ids_stage",
});

const UI_CLASSES = {
    CARD: "stage-card",
    STATUS_BADGE: "stage-status",
    ICON: "stage-status-icon",
    CURRENT_LABEL: "stage-current-label",
    FLOW: {
        RIGHT: "flow-right",
        LEFT: "flow-left",
        DOWN: "flow-down",
        END: "flow-end"
    },
    STATE: {
        COMPLETED: "is-complete",
        CURRENT: "is-current",
        LOCKED: "is-locked",
        CLICKABLE: "is-clickable"
    }
};

// ==========================================
// Pure Utilities & Logic
// ==========================================

/**
 * Clamps a number between min and max.
 */
const clamp = (value, min, max) => Math.min(Math.max(value, min), max);

/**
 * Normalizes the current stage value.
 */
const normalizeStage = (stageValue, totalStages) => {
    const parsed = parseInt(stageValue, 10);
    if (Number.isNaN(parsed)) return 1;
    if (!totalStages) return Math.max(parsed, 1);
    return clamp(parsed, 1, totalStages);
};

/**
 * Determines the status of a stage relative to the user's current progress.
 * @returns {'completed' | 'active' | 'locked'}
 */
const calculateStageStatus = (stageNumber, currentStageNumber) => {
    if (stageNumber < currentStageNumber) return "completed";
    if (stageNumber === currentStageNumber) return "active";
    return "locked";
};

const calculateAccuracy = (stats) => {
    if (!stats || !stats.length) return 0;
    let totalAttempts = 0;
    let totalCorrect = 0;
    stats.forEach((entry) => {
        if (!entry || !entry.attempts) return;
        totalAttempts += entry.attempts;
        totalCorrect += entry.correct || 0;
    });
    if (totalAttempts === 0) return 0;
    return Math.round((totalCorrect / totalAttempts) * 100);
};

const getMasteryLevel = (accuracy, reviewCount) => {
    if (accuracy >= 90 && reviewCount >= 3) {
        return {label: "완성", class: "badge-gold"};
    }
    if (accuracy >= 80) {
        return {label: "숙련", class: "badge-blue"};
    }
    if (accuracy >= 65) {
        return {label: "기초", class: "badge-green"};
    }
    return {label: "입문", class: "badge-gray"};
};

/**
 * Generates the properties needed to render a stage card.
 */
const getStageCardProps = ({stage, questionCount, status, score, lastCompletedStage}) => {
    const isCompleted = status === "completed";
    const isActive = status === "active";
    const isLocked = status === "locked";

    let label = "잠김";
    let meta = "진행 전";
    let route = null;
    const cssClasses = [UI_CLASSES.CARD];

    if (isCompleted) {
        cssClasses.push(UI_CLASSES.STATE.COMPLETED);
        label = "완료";
        if (score !== null && questionCount > 0) {
            const percent = questionCount > 0 ? Math.round((score / questionCount) * 100) : 0;
            meta = `점수 ${score} / ${questionCount} (${percent}%)`;
        } else {
            meta = "완료";
        }
        route = `/web/game/bible-quiz?stage=${stage}`;
    } else if (isActive) {
        cssClasses.push(UI_CLASSES.STATE.CURRENT);
        label = "현재";
        meta = "진행 가능";
        route = `/web/game/bible-quiz?stage=${stage}`;
    } else {
        cssClasses.push(UI_CLASSES.STATE.LOCKED);
    }

    const isClickable = !isLocked;
    if (isClickable) {
        cssClasses.push(UI_CLASSES.STATE.CLICKABLE);
    }

    return {
        label,
        meta,
        route,
        isClickable,
        cssClasses: cssClasses.join(" "),
        statusIcon: isCompleted ? "✓" : (isActive ? "▶︎" : null)
    };
};

/**
 * Determines the flow direction class for a card at a given index in the grid.
 * Used to draw the connecting lines (snake layout).
 */
const calculateFlowDirection = (index, totalItems, columns) => {
    // Last item always ends the flow
    if (index === totalItems - 1) return UI_CLASSES.FLOW.END;

    // Single column layout always flows down
    if (columns === 1) return UI_CLASSES.FLOW.DOWN;

    const row = Math.floor(index / columns);
    const col = index % columns;
    const isRowEven = row % 2 === 0;
    const maxRow = Math.floor((totalItems - 1) / columns);

    // Even Rows (Left -> Right)
    if (isRowEven) {
        const isLastInRow = col === columns - 1;
        // If not last in row, go right.
        // If last in row, go down (unless it's the very last row)
        if (!isLastInRow) return UI_CLASSES.FLOW.RIGHT;
        return (row < maxRow) ? UI_CLASSES.FLOW.DOWN : UI_CLASSES.FLOW.END;
    }

    // Odd Rows (Right -> Left)
    const isFirstInRow = col === 0;
    if (!isFirstInRow) return UI_CLASSES.FLOW.LEFT;
    return (row < maxRow) ? UI_CLASSES.FLOW.DOWN : UI_CLASSES.FLOW.END;
};

// ==========================================
// Data Access & Storage
// ==========================================

const Storage = (() => {
    const store = LocalStore;
    return {
        get: (key) => (store.get ? store.get(key) : store.getItem(key)),
        set: (key, value) => (store.set ? store.set(key, value) : store.setItem(key, String(value))),
        remove: (key) => (store.remove ? store.remove(key) : store.removeItem(key))
    };
})();

const StorageService = {
    getStageScore: (stage) => {
        const key = `${STORAGE_KEYS.STAGE_SCORE_PREFIX}_${stage}`;
        const val = parseInt(Storage.get(key), 10);
        return Number.isNaN(val) ? null : val;
    },
    getStageStats: (stage) => {
        const raw = Storage.get(`${STORAGE_KEYS.QUESTION_STATS_PREFIX}_${stage}`);
        if (!raw) return [];
        try {
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== "object") return [];
            return Object.values(parsed);
        } catch (error) {
            return [];
        }
    },
    getReviewCount: (stage) => {
        const count = parseInt(Storage.get(`${STORAGE_KEYS.REVIEW_COUNT_PREFIX}_${stage}`), 10);
        return Number.isNaN(count) ? 0 : count;
    },
    getCurrentStage: () => {
        const val = parseInt(Storage.get(STORAGE_KEYS.CURRENT_STAGE), 10);
        return (Number.isNaN(val) || val < 1) ? 1 : val;
    },
    setCurrentStage: (stage) => {
        Storage.set(STORAGE_KEYS.CURRENT_STAGE, stage);
    },
    getLastCompletedStage: () => {
        const val = parseInt(Storage.get(STORAGE_KEYS.LAST_COMPLETED_STAGE), 10);
        return Number.isNaN(val) ? 0 : val;
    },
    initDefaults: () => {
        if (Storage.get(STORAGE_KEYS.LAST_COMPLETED_STAGE) === null) {
            Storage.set(STORAGE_KEYS.LAST_COMPLETED_STAGE, 0);
        }
        // Ensure current stage is valid
        const current = StorageService.getCurrentStage();
        const stored = parseInt(Storage.get(STORAGE_KEYS.CURRENT_STAGE), 10);
        if (stored !== current) {
            StorageService.setCurrentStage(current);
        }
    },
    resetProgress: () => {
        Storage.set(STORAGE_KEYS.CURRENT_STAGE, 1);
        Storage.set(STORAGE_KEYS.LAST_COMPLETED_STAGE, 0);

        if (typeof localStorage === "undefined") return;

        const prefixes = [
            STORAGE_KEYS.CURRENT_QUESTION_PREFIX,
            STORAGE_KEYS.CURRENT_SCORE_PREFIX,
            STORAGE_KEYS.CURRENT_REVIEW_TYPE_PREFIX,
            STORAGE_KEYS.LAST_WRONG_IDS_PREFIX
        ];
        const keysToRemove = [];
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (!key) continue;
            if (prefixes.some((prefix) => key.startsWith(`${prefix}_`))) {
                keysToRemove.push(key);
            }
        }
        keysToRemove.forEach((key) => Storage.remove(key));
    }
};

const ApiService = {
    fetchStages: async () => {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000);
        try {
            const response = await fetch(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.STAGES}`,
                {signal: controller.signal}
            );
            return response.ok ? await response.json() : null;
        } catch (e) {
            console.error("Failed to fetch stages", e);
            return null;
        } finally {
            clearTimeout(timeoutId);
        }
    }
};

// ==========================================
// DOM Manipulation & Rendering
// ==========================================

const DomHelper = {
    getElements: () => {
        const stageList = document.getElementById("stageList");
        const quizMapNote = document.getElementById("quizMapNote");
        const resetProgressButton = document.getElementById("resetProgressButton");
        return (stageList && quizMapNote) ? {stageList, quizMapNote, resetProgressButton} : null;
    },

    getGridColumns: (stageList) => {
        const gridStyle = window.getComputedStyle(stageList).gridTemplateColumns;
        return (gridStyle && gridStyle !== "none")
            ? gridStyle.split(" ").filter(Boolean).length
            : 1;
    },

    getOrderedStages: (stageSummaries, columns, minColumns) => {
        const useSnakeLayout = columns >= minColumns;
        if (!useSnakeLayout) return stageSummaries;

        const ordered = [];
        for (let i = 0; i < stageSummaries.length; i += columns) {
            const row = stageSummaries.slice(i, i + columns);
            const rowIndex = Math.floor(i / columns);
            ordered.push(...(rowIndex % 2 === 0 ? row : row.reverse()));
        }
        return ordered;
    },

    createCardElement: (summary, context) => {
        const {stage, questionCount} = summary;
        const status = calculateStageStatus(stage, context.currentStage);
        const score = status === "completed" ? StorageService.getStageScore(stage) : null;

        const props = getStageCardProps({
            stage,
            questionCount,
            status,
            score,
            lastCompletedStage: context.lastCompletedStage
        });

        const statusBadgeClass = [
            UI_CLASSES.STATUS_BADGE,
            status === "completed" ? UI_CLASSES.STATE.COMPLETED : "",
            status === "active" ? UI_CLASSES.STATE.CURRENT : ""
        ].join(" ").trim();

        const button = document.createElement("button");
        button.type = "button";
        button.className = props.cssClasses;
        button.disabled = !props.isClickable;
        if (props.route) button.dataset.route = props.route;
        if (status === "active") button.setAttribute("aria-current", "step");

        const header = document.createElement("div");
        header.className = "stage-card-header";

        const number = document.createElement("span");
        number.className = "stage-number";
        number.textContent = `${stage} 스테이지`;

        const badge = document.createElement("span");
        badge.className = statusBadgeClass;
        if (props.statusIcon) {
            const icon = document.createElement("span");
            icon.className = UI_CLASSES.ICON;
            icon.setAttribute("aria-hidden", "true");
            icon.textContent = props.statusIcon;
            badge.appendChild(icon);
        }
        const labelText = document.createTextNode(props.label);
        badge.appendChild(labelText);

        header.appendChild(number);
        header.appendChild(badge);

        let masteryBadge = null;
        if (status === "completed") {
            const accuracy = calculateAccuracy(StorageService.getStageStats(stage));
            const mastery = getMasteryLevel(accuracy, StorageService.getReviewCount(stage));
            masteryBadge = document.createElement("span");
            masteryBadge.className = `stage-mastery-badge ${mastery.class}`;
            masteryBadge.textContent = mastery.label;
        }

        const meta = document.createElement("div");
        meta.className = "stage-meta-row";
        const metaText = document.createElement("span");
        metaText.textContent = props.meta;
        meta.appendChild(metaText);
        if (masteryBadge) {
            meta.appendChild(masteryBadge);
        }

        button.appendChild(header);
        button.appendChild(meta);

        return button;
    },

    updateFlowDirections: (stageList, columns) => {
        const cards = Array.from(stageList.querySelectorAll(`.${UI_CLASSES.CARD}`));
        if (!cards.length) return;

        // Reset classes
        const flowClasses = Object.values(UI_CLASSES.FLOW);
        cards.forEach(card => card.classList.remove(...flowClasses));

        // Calculate grid columns
        const resolvedColumns = Number.isInteger(columns) ? columns : DomHelper.getGridColumns(stageList);

        // Apply new classes
        cards.forEach((card, index) => {
            const directionClass = calculateFlowDirection(index, cards.length, resolvedColumns);
            card.classList.add(directionClass);
        });
    },

    render: (elements, stageSummaries, context) => {
        const columns = DomHelper.getGridColumns(elements.stageList);
        const minColumns = parseInt(elements.stageList.dataset.snakeColumns, 10) || 4;
        const orderedStages = DomHelper.getOrderedStages(stageSummaries, columns, minColumns);
        const fragment = document.createDocumentFragment();
        orderedStages.forEach(summary => {
            fragment.appendChild(DomHelper.createCardElement(summary, context));
        });
        elements.stageList.replaceChildren(fragment);

        DomHelper.updateFlowDirections(elements.stageList, columns);
    },

    bindEvents: (elements) => {
        elements.stageList.addEventListener("click", (event) => {
            const target = event.target.closest("[data-route]");
            if (!target || !elements.stageList.contains(target)) return;
            window.location.href = target.dataset.route;
        });
        if (elements.resetProgressButton) {
            elements.resetProgressButton.addEventListener("click", () => {
                StorageService.resetProgress();
                window.location.href = "/web/game/bible-quiz?stage=1";
            });
        }
    },

    showError: (elements) => {
        elements.quizMapNote.textContent = "퀴즈 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.";
    },

    showIntro: (elements) => {
        elements.quizMapNote.textContent = "스테이지를 선택하면 복습 또는 진행을 시작할 수 있습니다.";
    }
};

// ==========================================
// Main Application Logic
// ==========================================

const initNav = () => {
    const backButton = document.getElementById("topNavBackButton");
    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "성경 퀴즈";
        pageTitleLabel.classList.remove("d-none");
    }
    if (backButton) {
        backButton.classList.remove("d-none");
        backButton.addEventListener("click", () => {
            window.location.href = "/web/game";
        });
    }
};

const App = {
    init: async () => {
        initNav();
        const elements = DomHelper.getElements();
        if (!elements) return;

        StorageService.initDefaults();

        const context = {
            currentStage: StorageService.getCurrentStage(),
            lastCompletedStage: StorageService.getLastCompletedStage()
        };

        if (elements.resetProgressButton) {
            elements.resetProgressButton.classList.toggle("d-none", context.lastCompletedStage < 1);
        }

        const stages = await ApiService.fetchStages();
        if (!stages || !stages.length) {
            DomHelper.showError(elements);
            return;
        }

        const state = {
            columns: DomHelper.getGridColumns(elements.stageList),
            stages
        };

        // Validate current stage against actual stage count
        const normalizedStage = normalizeStage(context.currentStage, stages.length);
        if (normalizedStage !== context.currentStage) {
            context.currentStage = normalizedStage;
            StorageService.setCurrentStage(normalizedStage);
        }

        DomHelper.showIntro(elements);
        DomHelper.bindEvents(elements);
        DomHelper.render(elements, stages, context);

        // Handle Resize for Flow Lines
        let resizeTimer;
        window.addEventListener("resize", () => {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                const nextColumns = DomHelper.getGridColumns(elements.stageList);
                if (nextColumns !== state.columns) {
                    state.columns = nextColumns;
                    DomHelper.render(elements, state.stages, context);
                } else {
                    DomHelper.updateFlowDirections(elements.stageList, nextColumns);
                }
            }, 120);
        });
    }
};

// Bootstrap
document.addEventListener("DOMContentLoaded", App.init);
