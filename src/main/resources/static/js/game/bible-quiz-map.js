// Module-scope script
// ==========================================
// Constants & Configuration
// ==========================================
const API_CONFIG = {
    BASE_URL: "/api/v1/game/bible-quiz",
    ENDPOINTS: {
        STAGES: "/stages",
        RESET: "/progress/reset"
    }
};

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
 * Generates the properties needed to render a stage card.
 */
const getStageCardProps = ({stage, questionCount, status, score}) => {
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
    },
    resetProgress: async () => {
        try {
            const response = await fetch(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.RESET}`,
                {method: "POST"}
            );
            return response.ok;
        } catch (e) {
            console.error("Failed to reset progress", e);
            return false;
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
        const quizMapTotal = document.getElementById("quizMapTotal");
        const quizMapProgress = document.getElementById("quizMapProgress");
        const quizMapProgressBar = document.getElementById("quizMapProgressBar");
        const quizMapProgressFill = document.getElementById("quizMapProgressFill");
        const resetProgressButton = document.getElementById("resetProgressButton");
        return (stageList && quizMapNote)
            ? {
                stageList,
                quizMapNote,
                quizMapTotal,
                quizMapProgress,
                quizMapProgressBar,
                quizMapProgressFill,
                resetProgressButton
            }
            : null;
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

    createCardElement: (summary) => {
        const {stage, questionCount, status, score} = summary;

        const props = getStageCardProps({
            stage,
            questionCount,
            status,
            score
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

        const meta = document.createElement("div");
        meta.className = "stage-meta-row";
        const metaText = document.createElement("span");
        metaText.textContent = props.meta;
        meta.appendChild(metaText);

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

    render: (elements, stageSummaries) => {
        const columns = DomHelper.getGridColumns(elements.stageList);
        const minColumns = parseInt(elements.stageList.dataset.snakeColumns, 10) || 4;
        const orderedStages = DomHelper.getOrderedStages(stageSummaries, columns, minColumns);
        const fragment = document.createDocumentFragment();
        orderedStages.forEach(summary => {
            fragment.appendChild(DomHelper.createCardElement(summary));
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
            elements.resetProgressButton.addEventListener("click", async () => {
                if (!confirm("모든 스테이지 정보가 초기화됩니다. 정말 진행하시겠습니까?")) return;
                const ok = await ApiService.resetProgress();
                if (ok) {
                    window.location.href = "/web/game/bible-quiz?stage=1";
                }
            });
        }
    },

    showError: (elements) => {
        elements.quizMapNote.textContent = "퀴즈 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.";
    },

    showIntro: (elements) => {
        elements.quizMapNote.textContent = "스테이지를 선택하면 복습 또는 진행을 시작할 수 있습니다.";
    },

    updateSummary: (elements, context, totalStages) => {
        const completed = Math.max(0, Math.min(context.lastCompletedStage, totalStages));
        const progressPercent = totalStages > 0 ? Math.round((completed / totalStages) * 100) : 0;

        if (elements.quizMapTotal) {
            elements.quizMapTotal.textContent = `총 ${totalStages} 스테이지`;
        }

        if (elements.quizMapProgress) {
            elements.quizMapProgress.textContent = `진행 ${completed} / ${totalStages} (${progressPercent}%)`;
        }

        if (elements.quizMapProgressBar) {
            elements.quizMapProgressBar.setAttribute("aria-valuenow", String(progressPercent));
        }

        if (elements.quizMapProgressFill) {
            elements.quizMapProgressFill.style.width = `${progressPercent}%`;
        }
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

        const response = await ApiService.fetchStages();
        if (!response || !response.stages || !response.stages.length) {
            DomHelper.showError(elements);
            return;
        }

        const context = {
            currentStage: response.currentStage,
            lastCompletedStage: response.lastCompletedStage
        };

        if (elements.resetProgressButton) {
            elements.resetProgressButton.classList.toggle("d-none", context.lastCompletedStage < 1);
        }

        DomHelper.updateSummary(elements, context, response.totalStages);

        const state = {
            columns: DomHelper.getGridColumns(elements.stageList),
            stages: response.stages
        };

        DomHelper.showIntro(elements);
        DomHelper.bindEvents(elements);
        DomHelper.render(elements, response.stages);

        // Handle Resize for Flow Lines
        let resizeTimer;
        window.addEventListener("resize", () => {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                const nextColumns = DomHelper.getGridColumns(elements.stageList);
                if (nextColumns !== state.columns) {
                    state.columns = nextColumns;
                    DomHelper.render(elements, state.stages);
                } else {
                    DomHelper.updateFlowDirections(elements.stageList, nextColumns);
                }
            }, 120);
        });
    }
};

// Bootstrap
document.addEventListener("DOMContentLoaded", App.init);
