import {fetchAdmin} from "/js/admin/admin-common.js";

const API_URL = "/api/v1/admin/bible/search-keywords/ranking";

const formatNumber = (n) => (n ?? 0).toLocaleString("ko-KR");

const escapeHtml = (s) => String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");

const state = {
    keywordLimit: 20,
};

const fetchRanking = (limit) => fetchAdmin(`${API_URL}?limit=${limit}`);

const renderTable = (data) => {
    const tbody = document.querySelector("[data-keyword-tbody]");
    const refreshedEl = document.querySelector("[data-keyword-refreshed]");
    if (!tbody) return;

    const items = data?.items ?? [];
    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">데이터가 없습니다.</td></tr>`;
        if (refreshedEl) refreshedEl.textContent = "";
        return;
    }
    tbody.innerHTML = items
        .map((item) => `
            <tr>
                <td>${item.rank}</td>
                <td>${escapeHtml(item.keyword)}</td>
                <td>${formatNumber(item.searchCount)}</td>
            </tr>
        `)
        .join("");
    if (refreshedEl && data.refreshedAt) {
        const localized = new Date(data.refreshedAt).toLocaleString("ko-KR");
        refreshedEl.textContent = `갱신 시각: ${localized}`;
    }
};

const load = async () => {
    try {
        const data = await fetchRanking(state.keywordLimit);
        renderTable(data);
    } catch (e) {
        console.error(e);
        const tbody = document.querySelector("[data-keyword-tbody]");
        if (tbody) {
            tbody.innerHTML =
                `<tr><td colspan="3" class="text-center text-danger">조회 실패: ${escapeHtml(e.message)}</td></tr>`;
        }
    }
};

export const initSearchKeywordRanking = () => {
    const form = document.querySelector("[data-keyword-form]");
    const limitInput = document.querySelector("[data-keyword-limit]");
    const tbody = document.querySelector("[data-keyword-tbody]");

    if (!form || !limitInput || !tbody) return;

    limitInput.value = String(state.keywordLimit);

    form.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        const next = Number(limitInput?.value ?? state.keywordLimit);
        state.keywordLimit = Number.isFinite(next) && next > 0 ? next : state.keywordLimit;

        const button = form.querySelector("button[type='submit']");
        if (button) button.disabled = true;
        try {
            await load();
        } finally {
            if (button) button.disabled = false;
        }
    });

    load();
};
