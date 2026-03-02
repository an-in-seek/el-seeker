import { fetchWithAuthRetry } from '/js/common-util.js';

const API_BASE = '/api/v1/game/word-puzzles';

// ── State ──
const state = {
    puzzleId: null,
    attemptId: null,
    board: null,
    entries: [],
    cells: [],
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

// IME composition tracking for Korean input
let isComposing = false;
let compositionJustEnded = false;
let composingRow = null;
let composingCol = null;

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
const backButton = document.getElementById('topNavBackButton');
const pageTitleLabel = document.getElementById('pageTitleLabel');

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
    if (!puzzleId) {
        window.location.replace('/web/game/bible-word-puzzle');
        return;
    }
    state.puzzleId = parseInt(puzzleId, 10);

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
// Puzzle Play
// ══════════════════════════════════════════

function setupPlayListeners() {
    $('wpRevealBtn').addEventListener('click', onRevealLetter);
    $('wpCheckWordBtn').addEventListener('click', onCheckWord);
    submitBtn.addEventListener('click', onSubmit);
    $('wpBackToListBtn').addEventListener('click', () => {
        window.location.href = '/web/game/bible-word-puzzle';
    });

    // Keyboard
    document.addEventListener('keydown', onKeyDown);

    // Deselect when clicking outside the board
    document.addEventListener('click', (e) => {
        if (state.selectedRow == null) return;
        if (!playSection || playSection.classList.contains('d-none')) return;
        const clickedCell = e.target.closest('.wp-cell');
        if (clickedCell && !clickedCell.classList.contains('wp-cell-black')) return;
        if (!clickedCell && boardEl.contains(e.target)) return;
        if (e.target.closest('.wp-action-bar')) return;
        state.selectedRow = null;
        state.selectedCol = null;
        highlightCells();
        clueNumber.textContent = '';
        clueText.textContent = '칸을 선택하세요';
    });

    // Flush dirty cells on page unload (browser back, tab close)
    window.addEventListener('beforeunload', (e) => {
        if (state.dirtyCells.length > 0) {
            flushDirtyCells();
            e.preventDefault();
        }
    });

    // Mobile keyboard: keep selected cell visible when virtual keyboard resizes viewport
    if (window.visualViewport) {
        let prevViewportHeight = window.visualViewport.height;
        window.visualViewport.addEventListener('resize', () => {
            if (playSection.classList.contains('d-none')) return;
            if (state.selectedRow == null) return;
            const currentHeight = window.visualViewport.height;
            if (currentHeight < prevViewportHeight) {
                requestAnimationFrame(() => {
                    const selectedEl = getCellElement(state.selectedRow, state.selectedCol);
                    if (!selectedEl) return;
                    const rect = selectedEl.getBoundingClientRect();
                    if (rect.bottom > currentHeight - 10 || rect.top < 0) {
                        selectedEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                });
            }
            prevViewportHeight = currentHeight;
        });
    }
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
    state.cells = data.cells;
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

    // Build set of valid cell positions
    const validCells = new Set(Object.keys(state.cellMap));

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

            if (validCells.has(key)) {
                cellEl.className = 'wp-cell';
                const cellData = state.cellMap[key];

                // Clue number
                if (clueNumberMap[key]) {
                    const numEl = document.createElement('span');
                    numEl.className = 'wp-cell-number';
                    numEl.textContent = clueNumberMap[key];
                    cellEl.appendChild(numEl);
                }

                // Letter
                const letterEl = document.createElement('span');
                letterEl.className = 'wp-cell-letter';
                letterEl.textContent = cellData.inputLetter || '';
                cellEl.appendChild(letterEl);

                if (cellData.isRevealed) {
                    cellEl.classList.add('wp-cell-revealed');
                }

                cellEl.addEventListener('click', () => onCellClick(r, c));
            } else {
                cellEl.className = 'wp-cell wp-cell-black';
            }

            boardEl.appendChild(cellEl);
        }
    }

    // Hidden input for IME
    let hiddenInput = document.querySelector('.wp-hidden-input');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.className = 'wp-hidden-input';
        hiddenInput.type = 'text';
        hiddenInput.setAttribute('autocomplete', 'off');
        hiddenInput.setAttribute('autocorrect', 'off');
        hiddenInput.setAttribute('autocapitalize', 'off');
        hiddenInput.setAttribute('spellcheck', 'false');
        document.body.appendChild(hiddenInput);

        hiddenInput.addEventListener('compositionstart', () => {
            isComposing = true;
            composingRow = state.selectedRow;
            composingCol = state.selectedCol;
            const el = getCellElement(composingRow, composingCol);
            if (el) el.classList.add('wp-cell-composing');
        });

        hiddenInput.addEventListener('compositionupdate', (e) => {
            const row = composingRow ?? state.selectedRow;
            const col = composingCol ?? state.selectedCol;
            if (row == null) return;
            const key = `${row},${col}`;
            const cellData = state.cellMap[key];
            if (!cellData || cellData.isRevealed) return;
            const el = getCellElement(row, col);
            if (el) {
                const letterEl = el.querySelector('.wp-cell-letter');
                if (letterEl) letterEl.textContent = e.data ? e.data.charAt(e.data.length - 1) : '';
            }
        });

        hiddenInput.addEventListener('compositionend', (e) => {
            isComposing = false;
            compositionJustEnded = true;
            setTimeout(() => { compositionJustEnded = false; }, 0);

            const targetRow = composingRow;
            const targetCol = composingCol;
            composingRow = null;
            composingCol = null;

            const el = getCellElement(targetRow, targetCol);
            if (el) el.classList.remove('wp-cell-composing');

            const letter = e.data;
            if (letter && targetRow != null) {
                const finalChar = letter.charAt(letter.length - 1);
                const key = `${targetRow},${targetCol}`;
                const cellData = state.cellMap[key];
                if (cellData && !cellData.isRevealed) {
                    cellData.inputLetter = finalChar;
                    if (el) {
                        const letterEl = el.querySelector('.wp-cell-letter');
                        if (letterEl) letterEl.textContent = finalChar;
                    }
                    state.dirtyCells.push({ row: targetRow, col: targetCol, letter: finalChar });
                    scheduleSave();
                    updateSubmitButton();
                }
                // Only auto-advance if the user hasn't tapped a different cell
                if (state.selectedRow === targetRow && state.selectedCol === targetCol) {
                    moveToNextCell();
                }
            }
            hiddenInput.value = '';
        });

        hiddenInput.addEventListener('input', (e) => {
            if (e.isComposing || isComposing || compositionJustEnded) return;
            const val = hiddenInput.value;
            if (val) {
                setCurrentCellLetter(val.charAt(val.length - 1));
                moveToNextCell();
                hiddenInput.value = '';
            }
        });
    }
}

function onCellClick(row, col) {
    if (state.selectedRow === row && state.selectedCol === col) {
        // Toggle direction at same cell
        state.direction = state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS';
    }
    selectCell(row, col);
    focusHiddenInput();
}

function selectCell(row, col) {
    state.selectedRow = row;
    state.selectedCol = col;

    // Find matching entry for clue bar
    const entry = findEntryForCell(row, col, state.direction)
        || findEntryForCell(row, col, state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS');

    if (entry) {
        state.direction = entry.directionCode;
        clueNumber.textContent = `${entry.clueNumber}${entry.directionCode === 'ACROSS' ? '가로' : '세로'}`;
        clueText.textContent = entry.clueText;
    }

    highlightCells();
    focusHiddenInput();
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
    // Clear all highlights
    boardEl.querySelectorAll('.wp-cell').forEach(el => {
        el.classList.remove('wp-cell-selected', 'wp-cell-word-highlight', 'wp-cell-wrong');
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

function setCurrentCellLetter(letter) {
    const { selectedRow: r, selectedCol: c } = state;
    if (r == null || c == null) return;
    const key = `${r},${c}`;
    const cellData = state.cellMap[key];
    if (!cellData || cellData.isRevealed) return;

    cellData.inputLetter = letter || null;

    // Update DOM
    const el = getCellElement(r, c);
    if (el) {
        const letterEl = el.querySelector('.wp-cell-letter');
        if (letterEl) letterEl.textContent = letter || '';
    }

    // Mark dirty
    state.dirtyCells.push({ row: r, col: c, letter: letter || null });
    scheduleSave();
    updateSubmitButton();
}

function moveToNextCell() {
    const entry = getCurrentEntry();
    if (!entry) return;

    const { selectedRow: r, selectedCol: c } = state;
    let nextR = r, nextC = c;

    if (state.direction === 'ACROSS') {
        nextC = c + 1;
    } else {
        nextR = r + 1;
    }

    const key = `${nextR},${nextC}`;
    if (state.cellMap[key]) {
        selectCell(nextR, nextC);
    }
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

function onKeyDown(e) {
    if (playSection.classList.contains('d-none')) return;
    if (state.selectedRow == null) return;

    switch (e.key) {
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
            setCurrentCellLetter(null);
            moveToPrevCell();
            break;
        case 'Tab':
            e.preventDefault();
            moveToNextEntry(e.shiftKey);
            break;
        case ' ':
            e.preventDefault();
            state.direction = state.direction === 'ACROSS' ? 'DOWN' : 'ACROSS';
            selectCell(state.selectedRow, state.selectedCol);
            break;
        default:
            break;
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

function focusHiddenInput() {
    const input = document.querySelector('.wp-hidden-input');
    if (!input) return;

    const selectedEl = getCellElement(state.selectedRow, state.selectedCol);
    if (selectedEl) {
        const rect = selectedEl.getBoundingClientRect();
        input.style.top = `${rect.top + window.scrollY}px`;
        input.style.left = `${rect.left + window.scrollX}px`;
    }

    input.value = '';
    input.focus({ preventScroll: true });
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
        state.dirtyCells.push(...cellsToSave);
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
            el.querySelector('.wp-cell-letter').textContent = data.letter;
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
        data.words.forEach((word, i) => {
            const item = document.createElement('div');
            item.className = 'wp-word-item';

            const refsHtml = word.references.map(ref => `
                <div class="wp-word-ref">
                    <span class="wp-word-ref-verse">${escapeHtml(ref.verseReference)}</span>
                    <span>${escapeHtml(ref.verseExcerpt)}</span>
                </div>
            `).join('');

            item.innerHTML = `
                <button class="wp-word-header" type="button" aria-expanded="false" data-idx="${i}">
                    <span class="wp-word-surface">${escapeHtml(word.surfaceForm)}</span>
                    ${word.originalLexeme ? `<span class="wp-word-original">${escapeHtml(word.originalLexeme)} (${word.originalLanguageCode === 'HEBREW' ? '히브리어' : '헬라어'})</span>` : ''}
                </button>
                <div class="wp-word-body d-none" data-body-idx="${i}">
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
    toast.style.cssText = 'position:fixed;bottom:80px;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.8);color:#fff;padding:0.5rem 1rem;border-radius:0.5rem;font-size:0.85rem;z-index:9999;';
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
