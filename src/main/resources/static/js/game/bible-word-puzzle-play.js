import { fetchWithAuthRetry } from '/js/common-util.js';

const API_BASE = '/api/v1/game/word-puzzles';

// ── State ──
const state = {
    puzzleId: null,
    attemptId: null,
    board: null,
    entries: [],
    cellMap: {},          // "row,col" -> cell
    selectedRow: null,
    selectedCol: null,
    direction: 'ACROSS',
    elapsedSeconds: 0,
    timerInterval: null,
    // dirty cells for auto-save
    dirtyCells: [],
    saveTimeout: null
};

// IME composition tracking (cell 단위)
let composingCellKey = null;
let compositionEndCellKey = null;  // compositionend 직후 후속 input/blur 이벤트의 덮어쓰기 방지
let pendingMove = null;  // IME 조합 중 Enter/Space 입력 시 보류된 이동
let compositionEndMoveTimeout = null;  // 모바일 IME에서 Enter 키 이벤트 미발생 시 fallback 이동

// ── DOM refs ──
const $ = (id) => document.getElementById(id);

const playSection = $('wpPlaySection');
const resultSection = $('wpResultSection');
const playLoading = $('wpPlayLoading');

const boardEl = $('wpBoard');
const clueNumber = $('wpClueNumber');
const clueText = $('wpClueText');
const timerEl = $('wpTimer');
const titleEl = $('wpPlayTitle');
const submitBtn = $('wpSubmitBtn');
const saveBanner = $('wpSaveBanner');
const errorEl = $('wpError');

// Top nav
const backButton = $('topNavBackButton');
const pageTitleLabel = $('pageTitleLabel');

// ── Init ──
document.addEventListener('DOMContentLoaded', () => {
    initNav();
    setupPlayListeners();
    initPuzzle();
});

function initNav() {
    if (backButton) {
        backButton.classList.remove('d-none');
        backButton.onclick = () => {
            stopTimer();
            flushDirtyCells();
            window.location.href = '/web/game/bible-word-puzzle';
        };
    }
    if (pageTitleLabel) {
        pageTitleLabel.textContent = '성경 단어 퍼즐';
        pageTitleLabel.classList.remove('d-none');
    }
}

async function initPuzzle() {
    const puzzleId = new URLSearchParams(window.location.search).get('puzzleId');
    const parsed = parseInt(puzzleId, 10);
    if (!puzzleId || isNaN(parsed)) {
        window.location.replace('/web/game/bible-word-puzzle');
        return;
    }
    state.puzzleId = parsed;

    try {
        // POST /attempts — 서버가 기존 attempt 있으면 자동 이어하기
        const res = await fetchWithAuthRetry(`${API_BASE}/${state.puzzleId}/attempts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!res.ok) throw new Error('퍼즐을 시작할 수 없습니다.');
        const data = await res.json();
        startPlay(data, data.title);
    } catch (e) {
        playLoading.classList.add('d-none');
        showError(e.message || '퍼즐을 시작할 수 없습니다.');
    }
}

// ══════════════════════════════════════════
// Per-cell Input helpers
// ══════════════════════════════════════════

function syncCellFromInput(row, col) {
    const key = `${row},${col}`;
    const cellData = state.cellMap[key];
    if (!cellData || cellData.isRevealed) return;

    const cellEl = getCellElement(row, col);
    if (!cellEl) return;
    const input = cellEl.querySelector('.wp-cell-input');
    if (!input) return;

    const value = input.value || null;
    cellData.inputLetter = value;
    const existingIdx = state.dirtyCells.findIndex(c => c.row === row && c.col === col);
    if (existingIdx !== -1) {
        state.dirtyCells[existingIdx].letter = value;
    } else {
        state.dirtyCells.push({ row, col, letter: value });
    }
    scheduleSave();
    updateSubmitButton();
}

// ══════════════════════════════════════════
// Puzzle Play
// ══════════════════════════════════════════

function setupPlayListeners() {
    $('wpRevealBtn').addEventListener('click', onRevealLetter);
    $('wpCheckWordBtn').addEventListener('click', onCheckWord);
    submitBtn.addEventListener('click', onSubmit);
    $('wpBackToListBtn').addEventListener('click', () => {
        window.location.href = '/web/game/bible-word-puzzle';
    });

    // 셀 클릭 시 blur 방지 — focus 전환 없이 셀 선택만 처리
    boardEl.addEventListener('pointerdown', (e) => {
        const cell = e.target.closest('.wp-cell');
        if (cell && !cell.classList.contains('wp-cell-black')) {
            e.preventDefault();
        }
    });

    // Deselect when clicking outside the board
    document.addEventListener('click', (e) => {
        if (state.selectedRow == null) return;
        if (!playSection || playSection.classList.contains('d-none')) return;
        const clickedCell = e.target.closest('.wp-cell');
        if (clickedCell && !clickedCell.classList.contains('wp-cell-black')) return;
        if (!clickedCell && boardEl.contains(e.target)) return;
        if (e.target.closest('.wp-action-bar')) return;

        // Blur current cell input
        const currentCellEl = getCellElement(state.selectedRow, state.selectedCol);
        if (currentCellEl) {
            const input = currentCellEl.querySelector('.wp-cell-input');
            if (input) input.blur();
        }

        state.selectedRow = null;
        state.selectedCol = null;
        highlightCells();
        clueNumber.textContent = '';
        clueText.textContent = '칸을 선택하세요';
    });

    // Flush dirty cells on page unload (browser back, tab close)
    window.addEventListener('beforeunload', (e) => {
        stopTimer();
        if (state.dirtyCells.length > 0) {
            const cellsToSave = [...state.dirtyCells];
            state.dirtyCells = [];
            const url = `${API_BASE}/${state.puzzleId}/attempts/${state.attemptId}/cells`;
            const payload = JSON.stringify({ cells: cellsToSave, elapsedSeconds: state.elapsedSeconds });
            // keepalive: true로 페이지 언로드 후에도 요청이 완료될 수 있게 보장
            fetch(url, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: payload,
                keepalive: true
            }).catch(() => {});
            e.preventDefault();
        }
    });
}

function startPlay(data, title) {
    state.attemptId = data.attemptId;
    state.board = data.board;
    state.entries = data.entries;
    state.elapsedSeconds = data.elapsedSeconds;
    state.dirtyCells = [];
    state.selectedRow = null;
    state.selectedCol = null;
    state.direction = 'ACROSS';

    // Build cell map
    state.cellMap = {};
    data.cells.forEach(c => {
        state.cellMap[`${c.row},${c.col}`] = { ...c };
    });

    titleEl.textContent = title || '퍼즐';
    hideError();
    saveBanner.classList.add('d-none');
    playLoading.classList.add('d-none');
    showSection('play');
    renderBoard();
    startTimer();
    updateSubmitButton();

    // Select first cell
    if (data.cells.length > 0) {
        const firstEntry = data.entries[0];
        if (firstEntry) {
            selectCell(firstEntry.startRow, firstEntry.startCol);
        }
    }
}

function renderBoard() {
    const { width, height } = state.board;
    boardEl.style.gridTemplateColumns = `repeat(${width}, 1fr)`;
    boardEl.innerHTML = '';

    // Build clue number map from entries
    const clueNumberMap = {};
    state.entries.forEach(e => {
        const key = `${e.startRow},${e.startCol}`;
        if (!clueNumberMap[key]) clueNumberMap[key] = e.clueNumber;
    });

    for (let r = 0; r < height; r++) {
        for (let c = 0; c < width; c++) {
            const key = `${r},${c}`;
            const cellEl = document.createElement('div');
            cellEl.dataset.row = r;
            cellEl.dataset.col = c;

            if (state.cellMap[key]) {
                cellEl.className = 'wp-cell';
                const cellData = state.cellMap[key];

                // Clue number
                if (clueNumberMap[key]) {
                    const numEl = document.createElement('span');
                    numEl.className = 'wp-cell-number';
                    numEl.textContent = clueNumberMap[key];
                    cellEl.appendChild(numEl);
                }

                if (cellData.isRevealed) {
                    // Revealed cell — read-only span
                    const letterEl = document.createElement('span');
                    letterEl.className = 'wp-cell-letter';
                    letterEl.textContent = cellData.inputLetter || '';
                    cellEl.appendChild(letterEl);
                    cellEl.classList.add('wp-cell-revealed');
                } else {
                    // Editable cell — native input
                    const input = createCellInput(r, c, cellData);
                    cellEl.appendChild(input);
                }

                cellEl.addEventListener('click', () => onCellClick(r, c));
            } else {
                cellEl.className = 'wp-cell wp-cell-black';
            }

            boardEl.appendChild(cellEl);
        }
    }
}

function isMoveTriggerKey(e) {
    return e.key === 'Enter' || e.key === ' ' || e.code === 'Enter' || e.code === 'Space'
        || e.keyCode === 13 || e.keyCode === 32;
}

function createCellInput(row, col, cellData) {
    const cellKey = `${row},${col}`;
    const input = document.createElement('input');
    input.type = 'text';
    input.maxLength = 1;
    input.className = 'wp-cell-input';
    input.setAttribute('autocomplete', 'off');
    input.setAttribute('autocorrect', 'off');
    input.setAttribute('autocapitalize', 'off');
    input.setAttribute('spellcheck', 'false');
    input.setAttribute('aria-label', `${row + 1}행 ${col + 1}열`);
    input.value = cellData.inputLetter || '';

    // ── IME Composition ──
    input.addEventListener('compositionstart', () => {
        composingCellKey = cellKey;
        if (compositionEndMoveTimeout) {
            clearTimeout(compositionEndMoveTimeout);
            compositionEndMoveTimeout = null;
        }
    });

    input.addEventListener('compositionend', (e) => {
        if (composingCellKey === cellKey) composingCellKey = null;

        // blur()로 조합이 종료되면 브라우저가 input.value를 비울 수 있으므로
        // compositionend 시점에 조합된 글자를 즉시 보존한다.
        const composedChar = (e.data || input.value || '').slice(-1) || null;
        compositionEndCellKey = cellKey;

        const currentCellData = state.cellMap[`${row},${col}`];
        if (currentCellData && !currentCellData.isRevealed && composedChar) {
            input.value = composedChar;
            currentCellData.inputLetter = composedChar;
        }

        // 다음 틱에서 최종 동기화 및 보류된 이동 실행
        setTimeout(() => {
            // 브라우저가 값을 지웠다면 복원
            if (compositionEndCellKey === cellKey && composedChar && !input.value) {
                input.value = composedChar;
            }
            compositionEndCellKey = null;

            syncCellFromInput(row, col);
            if (pendingMove) {
                const action = pendingMove;
                pendingMove = null;
                if (state.selectedRow === row && state.selectedCol === col) {
                    action();
                }
            }
        }, 0);

        // 모바일 fallback: Android 등에서 IME가 Enter 키 이벤트를 소비하여
        // 별도의 keydown/keyup이 발생하지 않는 경우, 일정 시간 후 자동 이동.
        // 데스크톱에서는 compositionend 후 별도의 Enter keydown이 발생하므로
        // 해당 keydown에서 이 timeout을 취소한다.
        if (compositionEndMoveTimeout) clearTimeout(compositionEndMoveTimeout);
        if (composedChar) {
            compositionEndMoveTimeout = setTimeout(() => {
                compositionEndMoveTimeout = null;
                if (state.selectedRow === row && state.selectedCol === col
                    && !composingCellKey && input.value) {
                    moveToNextCell();
                }
            }, 100);
        }
    });

    input.addEventListener('blur', () => {
        // blur 시 모바일 fallback 이동 타이머 취소 (셀 이탈이므로 자동 이동 불필요)
        if (compositionEndMoveTimeout) {
            clearTimeout(compositionEndMoveTimeout);
            compositionEndMoveTimeout = null;
        }

        if (composingCellKey === cellKey) {
            // 한글 IME 조합 직후 blur가 먼저 오면 최종 글자가 아직 input.value에 반영되지 않을 수 있음
            setTimeout(() => {
                if (composingCellKey === cellKey) composingCellKey = null;
                syncCellFromInput(row, col);
            }, 0);
            // 조합 중 셀 전환을 위한 pendingMove는 유지
            return;
        }
        // compositionend가 이미 값을 저장했으면 blur에서 다시 동기화하지 않음 (브라우저가 값을 비운 경우 방지)
        if (compositionEndCellKey === cellKey) return;
        pendingMove = null;
        syncCellFromInput(row, col);
    });

    // ── Input ──
    input.addEventListener('input', () => {
        if (composingCellKey === cellKey) return;
        // compositionend 직후 브라우저가 발생시키는 input 이벤트에서 빈 값으로 덮어쓰기 방지
        if (compositionEndCellKey === cellKey) return;
        syncCellFromInput(row, col);
    });

    // ── Keydown (이동 제어) ──
    input.addEventListener('keydown', (e) => {
        const composingNow = composingCellKey === cellKey || e.isComposing || e.keyCode === 229;

        // compositionend 후 별도 keydown이 발생하면 모바일 fallback 이동을 취소.
        // 단, IME 유령 이벤트(composingNow)에서는 취소하지 않는다.
        // 모바일에서 compositionend 직후 keyCode 229인 keydown이 올 수 있는데,
        // 이 이벤트는 Enter를 실제로 처리하지 못하므로 fallback 타이머를 유지해야 한다.
        if (compositionEndMoveTimeout && !composingNow) {
            clearTimeout(compositionEndMoveTimeout);
            compositionEndMoveTimeout = null;
        }

        if (composingNow) {
            // IME 조합 중에도 이동 키는 보류 등록
            if (isMoveTriggerKey(e)) {
                e.preventDefault();
                pendingMove = moveToNextCell;
            }
            return;
        }

        // compositionend의 setTimeout(0)/pendingMove 또는 fallback timeout이
        // 이미 이동을 실행했을 수 있으므로 이 셀이 여전히 선택 상태인지 확인
        pendingMove = null;
        const isStillHere = state.selectedRow === row && state.selectedCol === col;

        switch (e.key) {
            case 'Enter':
            case ' ':
                e.preventDefault();
                if (isStillHere) moveToNextCell();
                break;
            case 'Tab':
                e.preventDefault();
                moveToNextEntry(e.shiftKey);
                break;
            case 'ArrowLeft':
                e.preventDefault();
                moveInDirection(0, -1);
                break;
            case 'ArrowRight':
                e.preventDefault();
                moveInDirection(0, 1);
                break;
            case 'ArrowUp':
                e.preventDefault();
                moveInDirection(-1, 0);
                break;
            case 'ArrowDown':
                e.preventDefault();
                moveInDirection(1, 0);
                break;
            case 'Backspace':
                e.preventDefault();
                if (input.value) {
                    input.value = '';
                    syncCellFromInput(row, col);
                } else {
                    moveToPrevCell();
                }
                break;
        }
    });

    return input;
}

function onCellClick(row, col) {
    if (state.selectedRow === row && state.selectedCol === col) {
        // Toggle direction at same cell
        state.direction = state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS';
    } else {
        // Prefer the direction of the entry that starts at this cell
        const acrossStart = state.entries.find(e => e.directionCode === 'ACROSS' && e.startRow === row && e.startCol === col);
        const downStart = state.entries.find(e => e.directionCode === 'DOWN' && e.startRow === row && e.startCol === col);
        if (acrossStart && !downStart) {
            state.direction = 'ACROSS';
        } else if (downStart && !acrossStart) {
            state.direction = 'DOWN';
        }
        // Both or neither start here → keep current direction
    }
    selectCell(row, col);
}

function selectCell(row, col) {
    const prevRow = state.selectedRow;
    const prevCol = state.selectedCol;

    // 조합 중에는 즉시 포커스를 옮기지 않고 조합 완료 후 이동
    if (prevRow != null && prevCol != null && (prevRow !== row || prevCol !== col)) {
        const prevKey = `${prevRow},${prevCol}`;
        if (composingCellKey === prevKey) {
            pendingMove = () => selectCell(row, col);
            const prevCellEl = getCellElement(prevRow, prevCol);
            const prevInput = prevCellEl?.querySelector('.wp-cell-input');
            if (prevInput) prevInput.blur();
            return;
        }
    }

    state.selectedRow = row;
    state.selectedCol = col;

    // 다른 셀로 이동 시 기존 입력을 종료해 IME 조합이 이전 셀에 남지 않게 처리
    if (prevRow != null && prevCol != null && (prevRow !== row || prevCol !== col)) {
        const prevCellEl = getCellElement(prevRow, prevCol);
        const prevInput = prevCellEl?.querySelector('.wp-cell-input');
        if (prevInput) prevInput.blur();
        pendingMove = null;
    }

    // Find matching entry for clue bar
    const entry = findEntryForCell(row, col, state.direction)
        || findEntryForCell(row, col, state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS');

    if (entry) {
        state.direction = entry.directionCode;
        clueNumber.textContent = `${entry.clueNumber}${entry.directionCode === 'ACROSS' ? '가로' : '세로'}`;
        clueText.textContent = entry.clueText;
    }

    highlightCells();
    focusCellInput(row, col);
}

function findEntryForCell(row, col, direction) {
    return state.entries.find(e => {
        if (e.directionCode !== direction) return false;
        for (let i = 0; i < e.length; i++) {
            const r = direction === 'ACROSS' ? e.startRow : e.startRow + i;
            const c = direction === 'ACROSS' ? e.startCol + i : e.startCol;
            if (r === row && c === col) return true;
        }
        return false;
    });
}

function getCurrentEntry() {
    return findEntryForCell(state.selectedRow, state.selectedCol, state.direction)
        || findEntryForCell(state.selectedRow, state.selectedCol, state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS');
}

function highlightCells() {
    // Clear selection/word highlights only (wp-cell-wrong은 setTimeout에서 자체 제거)
    boardEl.querySelectorAll('.wp-cell').forEach(el => {
        el.classList.remove('wp-cell-selected', 'wp-cell-word-highlight');
    });

    const entry = getCurrentEntry();
    if (entry) {
        // Highlight word
        for (let i = 0; i < entry.length; i++) {
            const r = entry.directionCode === 'ACROSS' ? entry.startRow : entry.startRow + i;
            const c = entry.directionCode === 'ACROSS' ? entry.startCol + i : entry.startCol;
            const el = getCellElement(r, c);
            if (el) el.classList.add('wp-cell-word-highlight');
        }
    }

    // Highlight selected
    const selEl = getCellElement(state.selectedRow, state.selectedCol);
    if (selEl) {
        selEl.classList.remove('wp-cell-word-highlight');
        selEl.classList.add('wp-cell-selected');
    }
}

function getCellElement(row, col) {
    return boardEl.querySelector(`[data-row="${row}"][data-col="${col}"]`);
}

function moveToNextCell() {
    const entry = getCurrentEntry();
    if (!entry) return;

    // 현재 셀이 entry 내 몇 번째인지 계산
    const offsetInEntry = state.direction === 'ACROSS'
        ? state.selectedCol - entry.startCol
        : state.selectedRow - entry.startRow;
    const remaining = entry.length - offsetInEntry - 1;

    let nextR = state.selectedRow;
    let nextC = state.selectedCol;

    // 현재 방향으로 entry 내 남은 셀만 탐색
    for (let i = 0; i < remaining; i++) {
        if (state.direction === 'ACROSS') {
            nextC++;
        } else {
            nextR++;
        }
        const key = `${nextR},${nextC}`;
        const cell = state.cellMap[key];
        if (!cell) break;
        if (!cell.isRevealed) {
            selectCell(nextR, nextC);
            return;
        }
    }

    // 현재 단어 끝이면 다음 단어의 첫 셀로 이동
    moveToNextEntry(false);
}

function moveToPrevCell() {
    const { selectedRow: r, selectedCol: c } = state;
    let prevR = r, prevC = c;

    if (state.direction === 'ACROSS') {
        prevC = c - 1;
    } else {
        prevR = r - 1;
    }

    const key = `${prevR},${prevC}`;
    if (state.cellMap[key]) {
        selectCell(prevR, prevC);
    }
}

function moveInDirection(dr, dc) {
    const newR = state.selectedRow + dr;
    const newC = state.selectedCol + dc;
    if (state.cellMap[`${newR},${newC}`]) {
        selectCell(newR, newC);
    }
}

function moveToNextEntry(reverse) {
    const currentEntry = getCurrentEntry();
    if (!currentEntry) return;
    const idx = state.entries.indexOf(currentEntry);
    const nextIdx = reverse
        ? (idx - 1 + state.entries.length) % state.entries.length
        : (idx + 1) % state.entries.length;
    const next = state.entries[nextIdx];
    if (next) {
        state.direction = next.directionCode;
        selectCell(next.startRow, next.startCol);
    }
}

function focusCellInput(row, col) {
    const key = `${row},${col}`;
    const cellData = state.cellMap[key];
    if (!cellData || cellData.isRevealed) return;

    const cellEl = getCellElement(row, col);
    if (!cellEl) return;
    const input = cellEl.querySelector('.wp-cell-input');
    if (input) input.focus({ preventScroll: true });
}

// ── Timer ──

function startTimer() {
    stopTimer();
    updateTimerDisplay();
    state.timerInterval = setInterval(() => {
        state.elapsedSeconds++;
        updateTimerDisplay();
    }, 1000);
}

function stopTimer() {
    if (state.timerInterval) {
        clearInterval(state.timerInterval);
        state.timerInterval = null;
    }
}

function updateTimerDisplay() {
    const mins = Math.floor(state.elapsedSeconds / 60);
    const secs = state.elapsedSeconds % 60;
    timerEl.textContent = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

// ── Auto-Save ──

function scheduleSave() {
    if (state.saveTimeout) clearTimeout(state.saveTimeout);
    state.saveTimeout = setTimeout(() => flushDirtyCells(), 2000);
}

async function flushDirtyCells() {
    if (state.dirtyCells.length === 0) return;
    const cellsToSave = [...state.dirtyCells];
    state.dirtyCells = [];

    try {
        const res = await fetchWithAuthRetry(
            `${API_BASE}/${state.puzzleId}/attempts/${state.attemptId}/cells`,
            {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ cells: cellsToSave, elapsedSeconds: state.elapsedSeconds })
            }
        );
        if (!res.ok) throw new Error();
        saveBanner.classList.add('d-none');
    } catch {
        saveBanner.classList.remove('d-none');
        // 실패한 셀을 복원하되 이미 존재하는 셀은 중복 추가하지 않음
        cellsToSave.forEach(saved => {
            if (!state.dirtyCells.some(c => c.row === saved.row && c.col === saved.col)) {
                state.dirtyCells.push(saved);
            }
        });
    }
}

// ── Submit Button ──

function updateSubmitButton() {
    const allFilled = Object.values(state.cellMap).every(c => c.inputLetter != null || c.isRevealed);
    submitBtn.disabled = !allFilled;
}

// ── Hints ──

async function onRevealLetter() {
    if (state.selectedRow == null) return;
    const entry = getCurrentEntry();
    if (!entry) return;

    const key = `${state.selectedRow},${state.selectedCol}`;
    if (state.cellMap[key]?.isRevealed) {
        showToast('이미 공개된 칸입니다.');
        return;
    }

    if (!confirm('힌트를 사용하면 점수가 감점됩니다. 사용하시겠습니까?')) return;

    await flushDirtyCells();

    try {
        const res = await fetchWithAuthRetry(
            `${API_BASE}/${state.puzzleId}/attempts/${state.attemptId}/hints/reveal-letter`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    entryId: entry.entryId,
                    row: state.selectedRow,
                    col: state.selectedCol,
                    elapsedSeconds: state.elapsedSeconds
                })
            }
        );
        if (!res.ok) throw new Error();
        const data = await res.json();

        // Update cell
        const cellData = state.cellMap[key];
        cellData.inputLetter = data.letter;
        cellData.isRevealed = true;

        const el = getCellElement(data.row, data.col);
        if (el) {
            // Replace input with revealed span
            const input = el.querySelector('.wp-cell-input');
            if (input) input.remove();

            const letterEl = document.createElement('span');
            letterEl.className = 'wp-cell-letter';
            letterEl.textContent = data.letter;
            el.appendChild(letterEl);

            el.classList.add('wp-cell-revealed');
        }
        updateSubmitButton();
    } catch {
        showToast('힌트를 불러올 수 없습니다.');
    }
}

async function onCheckWord() {
    const entry = getCurrentEntry();
    if (!entry) return;

    if (!confirm('힌트를 사용하면 점수가 감점됩니다. 사용하시겠습니까?')) return;

    await flushDirtyCells();

    try {
        const res = await fetchWithAuthRetry(
            `${API_BASE}/${state.puzzleId}/attempts/${state.attemptId}/hints/check-word`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    entryId: entry.entryId,
                    elapsedSeconds: state.elapsedSeconds
                })
            }
        );
        if (!res.ok) throw new Error();
        const data = await res.json();

        // Highlight results
        data.results.forEach(r => {
            const el = getCellElement(r.row, r.col);
            if (el) {
                if (r.correct) {
                    el.classList.add('wp-cell-correct-flash');
                    setTimeout(() => el.classList.remove('wp-cell-correct-flash'), 600);
                } else {
                    el.classList.add('wp-cell-wrong');
                    setTimeout(() => el.classList.remove('wp-cell-wrong'), 600);
                }
            }
        });
    } catch {
        showToast('단어 확인에 실패했습니다.');
    }
}

// ── Submit ──

async function onSubmit() {
    if (!confirm('제출하시겠습니까?')) return;

    await flushDirtyCells();
    submitBtn.disabled = true;

    try {
        const res = await fetchWithAuthRetry(
            `${API_BASE}/${state.puzzleId}/attempts/${state.attemptId}/submit`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ elapsedSeconds: state.elapsedSeconds })
            }
        );
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            if (err.error === 'EMPTY_CELLS_EXIST') {
                showToast('아직 채워지지 않은 칸이 있습니다.');
            } else {
                showToast('제출에 실패했습니다. 다시 시도해 주세요.');
            }
            submitBtn.disabled = false;
            return;
        }

        const data = await res.json();

        if (data.result === 'WRONG') {
            // Highlight wrong cells
            data.wrongCells.forEach(wc => {
                const el = getCellElement(wc.row, wc.col);
                if (el) {
                    el.classList.add('wp-cell-wrong');
                    setTimeout(() => el.classList.remove('wp-cell-wrong'), 1500);
                }
            });
            showToast(`오답입니다. (오답 횟수: ${data.wrongSubmissionCount})`);
            submitBtn.disabled = false;
        } else {
            // CORRECT
            stopTimer();
            if (state.saveTimeout) {
                clearTimeout(state.saveTimeout);
                state.saveTimeout = null;
            }
            showResult(data);
        }
    } catch {
        showToast('제출에 실패했습니다. 다시 시도해 주세요.');
        submitBtn.disabled = false;
    }
}

// ══════════════════════════════════════════
// Result
// ══════════════════════════════════════════

function showResult(data) {
    $('wpResultScore').textContent = data.score;
    $('wpResultTime').textContent = formatTime(data.elapsedSeconds);
    $('wpResultHints').textContent = `${data.hintUsageCount}회`;
    $('wpResultWrong').textContent = `${data.wrongSubmissionCount}회`;

    // Word study
    const wordList = $('wpWordList');
    wordList.innerHTML = '';
    if (data.words && data.words.length > 0) {
        data.words.forEach((word) => {
            const item = document.createElement('div');
            item.className = 'wp-word-item';

            const refsHtml = word.references.map(ref => `
                <div class="wp-word-ref">
                    <span class="wp-word-ref-verse">${escapeHtml(ref.verseReference)}</span>
                    <span>${escapeHtml(ref.verseExcerpt)}</span>
                </div>
            `).join('');

            item.innerHTML = `
                <button class="wp-word-header" type="button" aria-expanded="false">
                    <span class="wp-word-surface">${escapeHtml(word.surfaceForm)}</span>
                    ${word.originalLexeme ? `<span class="wp-word-original">${escapeHtml(word.originalLexeme)} (${word.originalLanguageCode === 'HEBREW' ? '히브리어' : '헬라어'})</span>` : ''}
                </button>
                <div class="wp-word-body d-none">
                    <p class="wp-word-definition">${escapeHtml(word.dictionaryDefinition)}</p>
                    ${refsHtml}
                </div>
            `;

            item.querySelector('.wp-word-header').addEventListener('click', (e) => {
                const body = item.querySelector('.wp-word-body');
                const expanded = body.classList.toggle('d-none');
                e.currentTarget.setAttribute('aria-expanded', !expanded);
            });

            wordList.appendChild(item);
        });
        $('wpWordStudy').classList.remove('d-none');
    } else {
        $('wpWordStudy').classList.add('d-none');
    }

    showSection('result');
}

// ── Utilities ──

function showSection(name) {
    playLoading.classList.add('d-none');
    playSection.classList.toggle('d-none', name !== 'play');
    resultSection.classList.toggle('d-none', name !== 'result');
}

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove('d-none');
}

function hideError() {
    errorEl.classList.add('d-none');
}

function showToast(msg) {
    const existing = document.querySelector('.wp-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'wp-toast';
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}분 ${secs}초`;
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
