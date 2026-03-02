import { fetchWithAuthRetry } from '/js/common-util.js';

const API_BASE = '/api/v1/game/word-puzzles';

// ── State ──
const state = {
    currentPage: 0,
    pageSize: 20
};

// ── DOM refs ──
const $ = (id) => document.getElementById(id);

const listLoading = $('wpListLoading');
const listEmpty = $('wpListEmpty');
const listCards = $('wpListCards');
const pagination = $('wpPagination');
const difficultyFilter = $('wpDifficultyFilter');
const errorEl = $('wpError');

// Top nav
const backButton = document.getElementById('topNavBackButton');
const pageTitleLabel = document.getElementById('pageTitleLabel');

// ── Init ──
document.addEventListener('DOMContentLoaded', () => {
    initNav();
    loadPuzzleList();
    setupFilterListeners();
});

function initNav() {
    if (backButton) {
        backButton.classList.remove('d-none');
        backButton.onclick = () => { window.location.href = '/web/game'; };
    }
    if (pageTitleLabel) {
        pageTitleLabel.textContent = '성경 단어 퍼즐';
        pageTitleLabel.classList.remove('d-none');
    }
}

function setupFilterListeners() {
    difficultyFilter.addEventListener('change', () => { state.currentPage = 0; loadPuzzleList(); });
}

async function loadPuzzleList() {
    errorEl.classList.add('d-none');
    listLoading.classList.remove('d-none');
    listCards.classList.add('d-none');
    listEmpty.classList.add('d-none');
    pagination.classList.add('d-none');

    const params = new URLSearchParams();
    params.set('page', state.currentPage);
    params.set('size', state.pageSize);
    const difficulty = difficultyFilter.value;
    if (difficulty) params.set('difficulty', difficulty);

    try {
        const res = await fetchWithAuthRetry(`${API_BASE}?${params}`);
        if (!res.ok) throw new Error('목록 로딩 실패');
        const data = await res.json();
        renderPuzzleList(data);
    } catch (e) {
        showError('퍼즐 목록을 불러올 수 없습니다. 다시 시도해 주세요.');
    } finally {
        listLoading.classList.add('d-none');
    }
}

function renderPuzzleList(data) {
    const items = data.content;
    if (!items || items.length === 0) {
        listEmpty.classList.remove('d-none');
        return;
    }

    listCards.innerHTML = '';
    items.forEach(puzzle => {
        const isInProgress = puzzle.inProgressAttemptId != null;
        const badgeClass = isInProgress ? 'wp-puzzle-badge-progress' : 'wp-puzzle-badge-new';
        const badgeText = isInProgress ? '진행 중' : '새로운 퍼즐';
        const difficultyLabel = { EASY: '쉬움', NORMAL: '보통', HARD: '어려움' }[puzzle.difficultyCode] || puzzle.difficultyCode;

        const col = document.createElement('div');
        col.className = 'col-12 col-md-6 col-lg-4';
        col.innerHTML = `
            <div class="card wp-puzzle-card p-3" data-puzzle-id="${puzzle.puzzleId}">
                <div class="wp-puzzle-card-title">${escapeHtml(puzzle.title)}</div>
                <div class="wp-puzzle-card-meta mt-2">
                    <span class="badge rounded-pill wp-puzzle-badge ${badgeClass}">${badgeText}</span>
                    <span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis wp-puzzle-badge">${difficultyLabel}</span>
                    <span class="text-muted" style="font-size:0.75rem">${puzzle.boardWidth}x${puzzle.boardHeight}</span>
                </div>
            </div>
        `;
        col.querySelector('.wp-puzzle-card').addEventListener('click', () => {
            window.location.href = `/web/game/bible-word-puzzle/play?puzzleId=${puzzle.puzzleId}`;
        });
        listCards.appendChild(col);
    });

    listCards.classList.remove('d-none');

    // Pagination
    if (data.totalPages > 1) {
        renderPagination(data.totalPages, data.number ?? state.currentPage);
    }
}

function renderPagination(totalPages, currentPage) {
    const ul = pagination.querySelector('ul');
    ul.innerHTML = '';
    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<button class="page-link" type="button">${i + 1}</button>`;
        li.querySelector('button').addEventListener('click', () => {
            state.currentPage = i;
            loadPuzzleList();
        });
        ul.appendChild(li);
    }
    pagination.classList.remove('d-none');
}

// ── Utilities ──

function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove('d-none');
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
