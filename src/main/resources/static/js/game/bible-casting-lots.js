const STAGES = Object.freeze({
    SETUP: "SETUP",
    SHUFFLED: "SHUFFLED",
    DRAWING: "DRAWING",
    RESULT: "RESULT",
});

// 상태 흐름: SETUP(입력) → SHUFFLED(섞기) → DRAWING(뽑기) → RESULT(결과)
const state = {
    stage: STAGES.SETUP,
    lots: [],
};

const DEFAULT_RESULT_MESSAGE = "제비 결과는 카드가 뒤집히면서 바로 확인됩니다.";

const elements = {
    page: document.querySelector(".casting-lots-page"),
    steps: document.querySelectorAll(".casting-lots-step"),
    error: document.getElementById("castingLotsError"),
    countInput: document.getElementById("castingLotsCount"),
    inputList: document.getElementById("lotInputList"),
    foldButton: document.getElementById("foldLotsButton"),
    deckSection: document.getElementById("castingDeckSection"),
    deckMessage: document.getElementById("castingDeckMessage"),
    remaining: document.getElementById("remainingLots"),
    cardGrid: document.getElementById("lotCardGrid"),
    resultSection: document.getElementById("castingResultSection"),
    resultMessage: document.getElementById("castingResultMessage"),
    resultComplete: document.getElementById("resultComplete"),
    resetButton: document.getElementById("resetLotsButton"),
    setupSection: document.getElementById("castingSetupSection"),
};

const buildInputRows = (count) => {
    const safeCount = Math.max(2, Math.min(12, Number(count) || 2));
    const previousValues = Array.from(elements.inputList.querySelectorAll("input")).map((input) => input.value);

    elements.inputList.innerHTML = "";
    for (let i = 0; i < safeCount; i += 1) {
        const wrapper = document.createElement("label");
        wrapper.className = "lot-input-item";
        wrapper.innerHTML = `
            <span class="field-label">제비 ${i + 1}</span>
            <input type="text" class="form-control" maxlength="30" placeholder="예: 사도행전 맛디아"
                   aria-label="제비 ${i + 1} 내용" />
        `;
        const input = wrapper.querySelector("input");
        if (previousValues[i]) {
            input.value = previousValues[i];
        }
        elements.inputList.appendChild(wrapper);
    }

    elements.countInput.value = String(safeCount);
};

const setStage = (nextStage) => {
    state.stage = nextStage;
    if (elements.page) {
        elements.page.dataset.stage = nextStage;
    }

    elements.setupSection.classList.toggle("d-none", nextStage !== STAGES.SETUP);
    elements.deckSection.classList.toggle("d-none", nextStage === STAGES.SETUP);
    elements.resultSection.classList.toggle("d-none", nextStage === STAGES.SETUP);

    elements.steps.forEach((step) => {
        const stepStage = step.dataset.stage;
        step.classList.toggle("is-active", stepStage === nextStage);
        step.classList.toggle("is-complete", stepStage !== nextStage && isStageComplete(stepStage));
    });
};

const isStageComplete = (stage) => {
    const order = [STAGES.SETUP, STAGES.SHUFFLED, STAGES.DRAWING, STAGES.RESULT];
    return order.indexOf(stage) < order.indexOf(state.stage);
};

const showError = (message) => {
    elements.error.textContent = message;
    elements.error.classList.remove("d-none");
};

const clearError = () => {
    elements.error.textContent = "";
    elements.error.classList.add("d-none");
};

const shuffleLots = (lots) => {
    const copy = [...lots];
    for (let i = copy.length - 1; i > 0; i -= 1) {
        const j = Math.floor(Math.random() * (i + 1));
        [copy[i], copy[j]] = [copy[j], copy[i]];
    }
    return copy;
};

const renderCards = () => {
    elements.cardGrid.innerHTML = "";
    state.lots.forEach((lot) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "lot-card";
        button.dataset.lotId = lot.id;
        button.disabled = lot.revealed;
        button.setAttribute("aria-label", "접힌 제비");
        button.innerHTML = `
            <span class="lot-card-inner">
                <span class="lot-card-face lot-card-front"><span>제비</span></span>
                <span class="lot-card-face lot-card-back">${lot.text}</span>
            </span>
        `;
        if (lot.revealed) {
            button.classList.add("is-revealed");
            button.setAttribute("aria-label", "펼쳐진 제비");
        }
        elements.cardGrid.appendChild(button);
    });

    elements.remaining.textContent = String(state.lots.filter((lot) => !lot.revealed).length);
};

const startShuffle = () => {
    elements.deckMessage.textContent = "제비를 섞고 있어요...";
    setStage(STAGES.SHUFFLED);
    renderCards();

    window.setTimeout(() => {
        elements.deckMessage.textContent = "카드를 눌러 제비를 펼쳐 보세요.";
        setStage(STAGES.DRAWING);
    }, 700);
};

const handleFoldLots = () => {
    clearError();
    const count = Number(elements.countInput.value) || 0;
    const inputs = Array.from(elements.inputList.querySelectorAll("input"));
    if (count < 2) {
        showError("제비는 최소 2개가 필요합니다.");
        return;
    }

    const values = inputs.map((input) => input.value.trim());
    if (values.some((value) => value.length === 0)) {
        showError("모든 제비 내용을 입력해 주세요.");
        return;
    }

    state.lots = shuffleLots(
        values.map((text, index) => ({
            id: `lot-${index + 1}`,
            text,
            revealed: false,
        }))
    );
    elements.resultMessage.textContent = DEFAULT_RESULT_MESSAGE;
    elements.resultComplete.classList.add("d-none");
    startShuffle();
};

const handleCardClick = (event) => {
    const card = event.target.closest(".lot-card");
    if (!card || state.stage !== STAGES.DRAWING) return;

    const lotId = card.dataset.lotId;
    const lot = state.lots.find((item) => item.id === lotId);
    if (!lot || lot.revealed) return;

    lot.revealed = true;

    card.classList.add("is-revealed");
    card.disabled = true;
    card.setAttribute("aria-label", "펼쳐진 제비");

    elements.remaining.textContent = String(state.lots.filter((item) => !item.revealed).length);
    elements.resultComplete.classList.toggle("d-none", state.lots.some((item) => !item.revealed));

    if (state.lots.every((item) => item.revealed)) {
        elements.deckMessage.textContent = "모든 제비를 펼쳤습니다.";
        elements.resultMessage.textContent = "제비뽑기가 완료되었습니다. 결과를 확인해 주세요.";
        setStage(STAGES.RESULT);
    }
};

const resetGame = () => {
    state.stage = STAGES.SETUP;
    state.lots = [];
    elements.cardGrid.innerHTML = "";
    elements.resultComplete.classList.add("d-none");
    elements.resultMessage.textContent = DEFAULT_RESULT_MESSAGE;
    buildInputRows(elements.countInput.value);
    setStage(STAGES.SETUP);
};

const handleCountChange = (event) => {
    clearError();
    const value = Number(event.target.value) || 2;
    buildInputRows(value);
};

buildInputRows(elements.countInput.value);
setStage(STAGES.SETUP);

if (elements.countInput) {
    elements.countInput.addEventListener("change", handleCountChange);
}

if (elements.foldButton) {
    elements.foldButton.addEventListener("click", handleFoldLots);
}

if (elements.cardGrid) {
    elements.cardGrid.addEventListener("click", handleCardClick);
}

if (elements.resetButton) {
    elements.resetButton.addEventListener("click", resetGame);
}
