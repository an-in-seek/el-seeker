(function () {
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
        CURRENT_STAGE: "currentStage",
        LAST_COMPLETED_STAGE: "lastCompletedStage",
        STAGE_SCORE_PREFIX: "quizStageScore",
    });

    const UI_CLASSES = {
        CARD: "stage-card",
        STATUS_BADGE: "stage-status",
        ICON: "stage-status-icon",
        ACTION: "stage-action",
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
        let action = null;
        const cssClasses = [UI_CLASSES.CARD];

        if (isCompleted) {
            cssClasses.push(UI_CLASSES.STATE.COMPLETED);
            label = "완료";
            if (score !== null && questionCount > 0) {
                const percent = Math.round((score / questionCount) * 100);
                meta = `점수 ${score} / ${questionCount} (${percent}%)`;
            } else {
                meta = "완료";
            }
            if (stage === lastCompletedStage) {
                action = "재도전 가능";
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
            action,
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
        const store = window.LocalStore ?? window.localStorage;
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
            return (stageList && quizMapNote) ? {stageList, quizMapNote} : null;
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

            const meta = document.createElement("div");
            meta.className = "stage-meta";
            meta.textContent = props.meta;

            button.appendChild(header);
            button.appendChild(meta);

            if (status === "active") {
                const currentLabel = document.createElement("span");
                currentLabel.className = UI_CLASSES.CURRENT_LABEL;
                currentLabel.textContent = "현재 스테이지";
                button.appendChild(currentLabel);
            }

            if (props.action) {
                const action = document.createElement("span");
                action.className = UI_CLASSES.ACTION;
                action.textContent = props.action;
                button.appendChild(action);
            }

            return button;
        },

        updateFlowDirections: (stageList) => {
            const cards = Array.from(stageList.querySelectorAll(`.${UI_CLASSES.CARD}`));
            if (!cards.length) return;

            // Reset classes
            const flowClasses = Object.values(UI_CLASSES.FLOW);
            cards.forEach(card => card.classList.remove(...flowClasses));

            // Calculate grid columns
            const gridStyle = window.getComputedStyle(stageList).gridTemplateColumns;
            const columns = (gridStyle && gridStyle !== "none")
                ? gridStyle.split(" ").filter(Boolean).length
                : 1;

            // Apply new classes
            cards.forEach((card, index) => {
                const directionClass = calculateFlowDirection(index, cards.length, columns);
                card.classList.add(directionClass);
            });
        },

        render: (elements, stageSummaries, context) => {
            const fragment = document.createDocumentFragment();
            stageSummaries.forEach(summary => {
                fragment.appendChild(DomHelper.createCardElement(summary, context));
            });
            elements.stageList.replaceChildren(fragment);

            DomHelper.updateFlowDirections(elements.stageList);
        },

        bindEvents: (elements) => {
            elements.stageList.addEventListener("click", (event) => {
                const target = event.target.closest("[data-route]");
                if (!target || !elements.stageList.contains(target)) return;
                window.location.href = target.dataset.route;
            });
        },

        showError: (elements) => {
            elements.quizMapNote.textContent = "퀴즈 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.";
        },

        showIntro: (elements) => {
            elements.quizMapNote.textContent = "스테이지를 선택하면 퀴즈 화면에서 모드를 고를 수 있습니다.";
        }
    };

    // ==========================================
    // Main Application Logic
    // ==========================================

    const App = {
        init: async () => {
            const elements = DomHelper.getElements();
            if (!elements) return;

            StorageService.initDefaults();

            const context = {
                currentStage: StorageService.getCurrentStage(),
                lastCompletedStage: StorageService.getLastCompletedStage()
            };

            const stages = await ApiService.fetchStages();
            if (!stages || !stages.length) {
                DomHelper.showError(elements);
                return;
            }

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
                    DomHelper.updateFlowDirections(elements.stageList);
                }, 120);
            });
        }
    };

    // Bootstrap
    document.addEventListener("DOMContentLoaded", App.init);
})();
