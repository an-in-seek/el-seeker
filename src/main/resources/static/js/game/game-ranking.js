import { fetchWithAuthRetry } from '/js/common-util.js';

const API_BASE = '/api/v1/game/ranking';
const MEDALS = ['', '\uD83E\uDD47', '\uD83E\uDD48', '\uD83E\uDD49']; // 🥇🥈🥉

// URL ?gameType= 파라미터로 초기 탭 설정
const VALID_TYPES = ['OX_QUIZ', 'MULTIPLE_CHOICE', 'WORD_PUZZLE', 'TYPING'];
const urlGameType = new URLSearchParams(location.search).get('gameType');
let currentGameType = VALID_TYPES.includes(urlGameType) ? urlGameType : 'OX_QUIZ';

const $tabs = document.querySelectorAll('.ranking-tab');
const $list = document.getElementById('rankingList');
const $empty = document.getElementById('rankingEmpty');
const $loading = document.getElementById('rankingLoading');
const $myCard = document.getElementById('myRankingCard');

// 탭 전환
$tabs.forEach(tab => {
    tab.addEventListener('click', () => {
        $tabs.forEach(t => {
            t.classList.remove('active');
            t.setAttribute('aria-selected', 'false');
        });
        tab.classList.add('active');
        tab.setAttribute('aria-selected', 'true');
        currentGameType = tab.dataset.gameType;
        loadRankings();
    });
});

async function loadRankings() {
    showLoading(true);
    hideEmpty();
    $list.innerHTML = '';
    $myCard.style.display = 'none';

    try {
        const res = await fetchWithAuthRetry(`${API_BASE}?gameType=${currentGameType}&limit=100`);
        if (!res.ok) {
            if (res.status === 401) {
                // 비로그인 — 일반 fetch로 재시도
                const publicRes = await fetch(`${API_BASE}?gameType=${currentGameType}&limit=100`);
                if (!publicRes.ok) throw new Error('Failed to load rankings');
                const data = await publicRes.json();
                renderRankings(data);
                return;
            }
            throw new Error('Failed to load rankings');
        }
        const data = await res.json();
        renderRankings(data);
    } catch (e) {
        // fetchWithAuthRetry가 없거나 실패 시 일반 fetch
        try {
            const publicRes = await fetch(`${API_BASE}?gameType=${currentGameType}&limit=100`);
            if (!publicRes.ok) throw new Error('Failed');
            const data = await publicRes.json();
            renderRankings(data);
        } catch {
            showEmpty();
        }
    } finally {
        showLoading(false);
    }
}

function renderRankings(data) {
    const { rankings, myRanking, totalParticipants } = data;

    if (!rankings || rankings.length === 0) {
        showEmpty();
        return;
    }

    // 내 순위 카드
    if (myRanking) {
        $myCard.style.display = '';
        document.getElementById('myRankNumber').textContent = myRanking.rank;
        document.getElementById('myRankTotal').textContent = `/ ${totalParticipants}명`;
        document.getElementById('myRankScore').textContent = formatScore(myRanking.rankingScore);
        document.getElementById('myRankCompleted').textContent = myRanking.completedCount;
        document.getElementById('myRankPerfect').textContent = myRanking.perfectCount;
        document.getElementById('myRankPercent').textContent =
            myRanking.topPercent != null ? `${myRanking.topPercent}%` : '-';
    }

    // 랭킹 목록
    const fragment = document.createDocumentFragment();
    rankings.forEach(item => {
        const isMe = myRanking && item.rank === myRanking.rank &&
            item.rankingScore === myRanking.rankingScore;
        const el = createRankingItem(item, isMe);
        fragment.appendChild(el);
    });
    $list.appendChild(fragment);
}

function createRankingItem(item, isMe) {
    const div = document.createElement('div');
    div.className = 'ranking-item' + (isMe ? ' is-me' : '');

    const medal = MEDALS[item.rank] || '';
    const rankDisplay = medal
        ? `<span class="ri-rank-medal">${medal}</span>`
        : item.rank;

    const avatarHtml = item.profileImageUrl
        ? `<img class="ri-avatar" src="${escapeHtml(item.profileImageUrl)}" alt="" loading="lazy" decoding="async"
               onerror="this.style.display='none'">`
        : '';

    div.innerHTML = `
        <span class="ri-rank">${rankDisplay}</span>
        <span class="ri-nickname">
            ${avatarHtml}
            <span class="ri-name">${escapeHtml(item.nickname)}</span>
        </span>
        <span class="ri-score">${formatScore(item.rankingScore)}</span>
        <span class="ri-completed">${item.completedCount}</span>
        <span class="ri-perfect">${item.perfectCount}</span>
    `;

    return div;
}

function formatScore(score) {
    if (score == null) return '-';
    const num = Number(score);
    return num % 1 === 0 ? num.toFixed(0) : num.toFixed(2);
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function showLoading(show) {
    $loading.style.display = show ? '' : 'none';
}

function showEmpty() {
    $empty.style.display = '';
}

function hideEmpty() {
    $empty.style.display = 'none';
}

// 초기 탭 활성화 및 로드
$tabs.forEach(t => {
    const isActive = t.dataset.gameType === currentGameType;
    t.classList.toggle('active', isActive);
    t.setAttribute('aria-selected', String(isActive));
});
loadRankings();
