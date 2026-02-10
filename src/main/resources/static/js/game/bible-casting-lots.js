const STAGES = Object.freeze({
    SETUP: "SETUP",
    GAME: "GAME",
    RESULT: "RESULT"
});

const elements = {
    setupSection: document.getElementById("setupSection"),
    gameSection: document.getElementById("gameSection"),
    resultSection: document.getElementById("resultSection"),
    
    // Setup Phase
    countInput: document.getElementById("castingLotsCount"),
    btnIncrease: document.getElementById("btnIncrease"),
    btnDecrease: document.getElementById("btnDecrease"),
    inputList: document.getElementById("lotInputList"),
    foldButton: document.getElementById("foldLotsButton"),
    errorToast: document.getElementById("castingLotsError"),

    // Game Phase
    remaining: document.getElementById("remainingLots"),
    cardArena: document.getElementById("cardArena"),
    gameInstruction: document.getElementById("gameInstruction"),

    // Result Phase
    resetButton: document.getElementById("resetLotsButton")
};

let gameState = {
    stage: STAGES.SETUP,
    lots: [],
    count: 2
};

// --- Utils ---
const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const showError = (msg) => {
    elements.errorToast.textContent = msg;
    elements.errorToast.classList.remove("d-none");
    // Auto hide after 3s
    setTimeout(() => {
        elements.errorToast.classList.add("d-none");
    }, 3000);
};

const clearError = () => {
    elements.errorToast.classList.add("d-none");
};

// --- Setup Phase Functionality ---
const createInputRow = (index, value = "") => {
    const div = document.createElement("div");
    div.className = "lot-input-wrapper";
    div.innerHTML = `
        <input type="text" placeholder="제비 ${index} 내용 입력..." value="${value}" maxlength="20">
    `;
    return div;
};

const renderInputs = () => {
    const currentValues = Array.from(elements.inputList.querySelectorAll("input"))
        .map(input => input.value);
    
    elements.inputList.innerHTML = "";
    
    for (let i = 0; i < gameState.count; i++) {
        const value = currentValues[i] || "";
        const row = createInputRow(i + 1, value);
        elements.inputList.appendChild(row);
    }
    
    elements.countInput.value = gameState.count;
};

const updateCount = (delta) => {
    const newCount = gameState.count + delta;
    if (newCount >= 2 && newCount <= 12) {
        gameState.count = newCount;
        renderInputs();
    }
};

// --- Game Phase Functionality ---
const shuffleArray = (array) => {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
};

const renderCards = (lots) => {
    elements.cardArena.innerHTML = "";
    lots.forEach((lot, index) => {
        const btn = document.createElement("button");
        btn.className = "game-card";
        btn.dataset.id = lot.id;
        btn.dataset.index = index;
        btn.innerHTML = `
            <div class="card-inner">
                <div class="card-face card-front">
                    <div class="card-pattern"></div>
                </div>
                <div class="card-face card-back">${lot.text}</div>
            </div>
        `;
        // Stagger animation
        btn.style.opacity = "0";
        btn.style.animation = `slideUp 0.4s ease forwards ${index * 0.1}s`;
        
        btn.addEventListener("click", () => handleCardClick(btn, lot));
        elements.cardArena.appendChild(btn);
    });
};

const handleCardClick = (cardBtn, lot) => {
    if (lot.revealed) return;

    // Reveal logic
    lot.revealed = true;
    cardBtn.classList.add("flipped");
    
    // Update stats
    updateRemainingCount();

    // Check if game over
    if (gameState.lots.every(l => l.revealed)) {
        setTimeout(endGame, 800);
    }
};

const updateRemainingCount = () => {
    const remaining = gameState.lots.filter(l => !l.revealed).length;
    elements.remaining.textContent = remaining;
    
    if (remaining === 0) {
        elements.gameInstruction.textContent = "확인 완료!";
    } else {
        elements.gameInstruction.textContent = "카드를 선택하여 확인하세요";
    }
};

const startGame = async () => {
    clearError();
    const inputs = Array.from(elements.inputList.querySelectorAll("input"));
    const values = inputs.map(input => input.value.trim());

    if (values.some(v => !v)) {
        showError("모든 제비의 내용을 입력해주세요.");
        return;
    }

    // Prepare Data
    gameState.lots = values.map((text, i) => ({
        id: `lot-${Date.now()}-${i}`,
        text,
        revealed: false
    }));

    // Transition UI
    elements.setupSection.classList.add("game-phase-hidden"); // fade out setup
    await wait(500); 
    elements.setupSection.style.display = "none";
    
    elements.gameSection.style.display = "block";
    // Force reflow
    void elements.gameSection.offsetWidth;
    elements.gameSection.classList.remove("game-phase-hidden");

    // Shuffle Simulation
    elements.gameInstruction.textContent = "제비를 섞는 중...";
    gameState.lots = shuffleArray(gameState.lots);
    
    // Fake shuffle delay for tension
    await wait(1000);
    
    renderCards(gameState.lots);
    updateRemainingCount();
};

const endGame = () => {
    elements.resultSection.style.display = "block";
    void elements.resultSection.offsetWidth;
    elements.resultSection.classList.remove("result-phase-hidden");
    
    // Scroll to result if needed, or just show it (it's in view usually)
};

const resetGame = async () => {
    // Hide Result & Game
    elements.resultSection.classList.add("result-phase-hidden");
    elements.gameSection.classList.add("game-phase-hidden");
    
    await wait(500);
    elements.resultSection.style.display = "none";
    elements.gameSection.style.display = "none";
    elements.cardArena.innerHTML = "";

    // Show Setup
    elements.setupSection.style.display = "block";
    void elements.setupSection.offsetWidth;
    elements.setupSection.classList.remove("game-phase-hidden");
    
    gameState.lots = [];
};

// --- Event Listeners ---
elements.btnIncrease.addEventListener("click", () => updateCount(1));
elements.btnDecrease.addEventListener("click", () => updateCount(-1));

// Initial Input Render
renderInputs();

elements.foldButton.addEventListener("click", startGame);
elements.resetButton.addEventListener("click", resetGame);
