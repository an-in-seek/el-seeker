import {fetchAdmin} from "/js/admin/admin-common.js";

const SECTIONS = [
    {
        key: "bible",
        apiUrl: "/api/v1/admin/bible/search-keywords/ranking",
    },
    {
        key: "dictionary",
        apiUrl: "/api/v1/admin/dictionaries/search-keywords/ranking",
    },
];

const formatNumber = (n) => (n ?? 0).toLocaleString("ko-KR");

const escapeHtml = (s) => String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");

const createInitialState = () => Object.fromEntries(
    SECTIONS.map(section => [section.key, { limit: 20 }])
);

const state = createInitialState();

const fetchRanking = (apiUrl, limit) => fetchAdmin(`${apiUrl}?limit=${limit}`);

const getSectionElements = key => ({
    form: document.querySelector(`[data-keyword-form="${key}"]`),
    limitInput: document.querySelector(`[data-keyword-limit="${key}"]`),
    tbody: document.querySelector(`[data-keyword-tbody="${key}"]`),
    refreshedEl: document.querySelector(`[data-keyword-refreshed="${key}"]`),
});

const renderTable = (key, data) => {
    const { tbody, refreshedEl } = getSectionElements(key);
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

const load = async section => {
    const { key, apiUrl } = section;
    const { tbody } = getSectionElements(key);
    if (!tbody) return;

    try {
        const data = await fetchRanking(apiUrl, state[key].limit);
        renderTable(key, data);
    } catch (e) {
        console.error(e);
        if (tbody) {
            tbody.innerHTML =
                `<tr><td colspan="3" class="text-center text-danger">조회 실패: ${escapeHtml(e.message)}</td></tr>`;
        }
    }
};

export const initSearchKeywordRanking = () => {
    SECTIONS.forEach(section => {
        const { key } = section;
        const { form, limitInput, tbody } = getSectionElements(key);

        if (!form || !limitInput || !tbody) return;

        limitInput.value = String(state[key].limit);

        form.addEventListener("submit", async (ev) => {
            ev.preventDefault();
            const next = Number(limitInput?.value ?? state[key].limit);
            state[key].limit = Number.isFinite(next) && next > 0 ? next : state[key].limit;

            const button = form.querySelector("button[type='submit']");
            if (button) button.disabled = true;
            try {
                await load(section);
            } finally {
                if (button) button.disabled = false;
            }
        });

        load(section);
    });
};
