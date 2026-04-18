import {fetchAdmin} from "/js/admin/admin-common.js";

const API_BASE = "/api/v1/admin/analytics/visitors";

const toIsoDate = (d) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
};

const addDays = (d, n) => {
    const copy = new Date(d);
    copy.setDate(copy.getDate() + n);
    return copy;
};

const formatNumber = (n) => (n ?? 0).toLocaleString("ko-KR");

const fetchDaily = (from, to) =>
    fetchAdmin(`${API_BASE}/summary?from=${from}&to=${to}`);

const fetchOverview = (from, to) =>
    fetchAdmin(`${API_BASE}/overview?from=${from}&to=${to}`);

const fetchPages = (date, page = 0, size = 20) =>
    fetchAdmin(`${API_BASE}/pages?date=${date}&page=${page}&size=${size}`);

const renderKpi = (selector, value) => {
    const el = document.querySelector(`[data-kpi="${selector}"]`);
    if (el) el.textContent = formatNumber(value);
};

const renderDailyTable = (items) => {
    const tbody = document.querySelector("[data-daily-tbody]");
    if (!tbody) return;
    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">데이터가 없습니다.</td></tr>`;
        return;
    }
    tbody.innerHTML = items
        .map((item) => `
            <tr>
                <td>${escapeHtml(item.date)}</td>
                <td>${formatNumber(item.pageViewCount)}</td>
                <td>${formatNumber(item.uniqueVisitorCount)}</td>
            </tr>
        `)
        .join("");
};

const renderChart = (items) => {
    const wrapper = document.querySelector(".analytics-chart-wrapper");
    const line = document.querySelector("[data-chart-line]");
    const grid = document.querySelector("[data-chart-grid]");
    const pointsGroup = document.querySelector("[data-chart-points]");
    if (!wrapper || !line || !grid || !pointsGroup) return;

    if (!items.length) {
        wrapper.classList.add("is-empty");
        line.setAttribute("points", "");
        grid.innerHTML = "";
        pointsGroup.innerHTML = "";
        return;
    }
    wrapper.classList.remove("is-empty");

    const width = 800;
    const height = 260;
    const padding = {top: 20, right: 20, bottom: 30, left: 40};
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const max = Math.max(...items.map((i) => i.pageViewCount), 1);
    const step = items.length > 1 ? chartW / (items.length - 1) : 0;
    const singleOffset = items.length === 1 ? chartW / 2 : 0;

    const coords = items.map((item, i) => {
        const x = padding.left + singleOffset + step * i;
        const y = padding.top + chartH - (item.pageViewCount / max) * chartH;
        return {x, y, item};
    });

    line.setAttribute("points", coords.map((c) => `${c.x},${c.y}`).join(" "));

    const gridLines = [0, 0.25, 0.5, 0.75, 1].map((r) => {
        const y = padding.top + chartH * (1 - r);
        const label = Math.round(max * r);
        return `
            <line class="analytics-chart-grid-line" x1="${padding.left}" y1="${y}" x2="${width - padding.right}" y2="${y}"></line>
            <text class="analytics-chart-axis-label" x="${padding.left - 6}" y="${y + 3}" text-anchor="end">${label}</text>
        `;
    }).join("");

    const xLabels = coords
        .filter((_, i) => i === 0 || i === coords.length - 1 || i % Math.ceil(coords.length / 6) === 0)
        .map((c) => `<text class="analytics-chart-axis-label" x="${c.x}" y="${height - 10}" text-anchor="middle">${c.item.date.slice(5)}</text>`)
        .join("");

    grid.innerHTML = gridLines + xLabels;

    pointsGroup.innerHTML = coords
        .map((c) => `<circle class="analytics-chart-point" cx="${c.x}" cy="${c.y}" r="3">
            <title>${c.item.date} / PV ${formatNumber(c.item.pageViewCount)} / UV ${formatNumber(c.item.uniqueVisitorCount)}</title>
        </circle>`)
        .join("");
};

const renderPageTable = (pageData) => {
    const tbody = document.querySelector("[data-page-tbody]");
    const pagination = document.querySelector("[data-page-pagination]");
    if (!tbody) return;

    const items = pageData?.content ?? [];
    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">데이터가 없습니다.</td></tr>`;
        if (pagination) pagination.innerHTML = "";
        return;
    }
    tbody.innerHTML = items
        .map((item) => `
            <tr>
                <td>${escapeHtml(item.pageKey)}</td>
                <td>${formatNumber(item.pageViewCount)}</td>
                <td>${formatNumber(item.uniqueVisitorCount)}</td>
            </tr>
        `)
        .join("");

    if (!pagination) return;
    const totalPages = pageData.totalPages ?? 0;
    const current = pageData.page ?? 0;
    if (totalPages <= 1) {
        pagination.innerHTML = "";
        return;
    }
    pagination.innerHTML = Array.from({length: totalPages}, (_, i) => {
        if (i === current) return `<span class="active">${i + 1}</span>`;
        return `<a href="#" data-page-go="${i}">${i + 1}</a>`;
    }).join("");
    pagination.querySelectorAll("[data-page-go]").forEach((el) => {
        el.addEventListener("click", (ev) => {
            ev.preventDefault();
            const nextPage = Number(el.dataset.pageGo);
            loadPages(nextPage);
        });
    });
};

const escapeHtml = (s) => String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");

const state = {
    from: null,
    to: null,
    pageDate: null,
};

const KPI_KEYS = [
    "todayPageView",
    "todayUniqueVisitor",
    "last7PageView",
    "last30UniqueVisitor",
    "last30UniqueMember",
];

const markKpisError = () => {
    KPI_KEYS.forEach((key) => {
        const el = document.querySelector(`[data-kpi="${key}"]`);
        if (el) el.textContent = "오류";
    });
};

const loadKpis = async () => {
    const today = toIsoDate(new Date());
    const last7From = toIsoDate(addDays(new Date(), -6));
    const last30From = toIsoDate(addDays(new Date(), -29));

    try {
        const [todayOverview, last7Overview, last30Overview] = await Promise.all([
            fetchOverview(today, today),
            fetchOverview(last7From, today),
            fetchOverview(last30From, today),
        ]);
        renderKpi("todayPageView", todayOverview?.totalPageViewCount);
        renderKpi("todayUniqueVisitor", todayOverview?.periodUniqueVisitorCount);
        renderKpi("last7PageView", last7Overview?.totalPageViewCount);
        renderKpi("last30UniqueVisitor", last30Overview?.periodUniqueVisitorCount);
        renderKpi("last30UniqueMember", last30Overview?.periodAuthenticatedUniqueMemberCount);
    } catch (e) {
        console.error(e);
        markKpisError();
    }
};

const loadDaily = async () => {
    try {
        const data = await fetchDaily(state.from, state.to);
        const items = data?.items ?? [];
        renderDailyTable(items);
        renderChart(items);
    } catch (e) {
        console.error(e);
        document.querySelector("[data-daily-tbody]").innerHTML =
            `<tr><td colspan="3" class="text-center text-danger">조회 실패: ${escapeHtml(e.message)}</td></tr>`;
    }
};

const loadPages = async (page = 0) => {
    try {
        const data = await fetchPages(state.pageDate, page, 20);
        renderPageTable(data);
    } catch (e) {
        console.error(e);
        document.querySelector("[data-page-tbody]").innerHTML =
            `<tr><td colspan="3" class="text-center text-danger">조회 실패: ${escapeHtml(e.message)}</td></tr>`;
    }
};

export const initAnalyticsVisitors = () => {
    const today = toIsoDate(new Date());
    const thirtyAgo = toIsoDate(addDays(new Date(), -29));

    state.from = thirtyAgo;
    state.to = today;
    state.pageDate = today;

    const rangeForm = document.querySelector("[data-range-form]");
    const fromInput = document.querySelector("[data-range-from]");
    const toInput = document.querySelector("[data-range-to]");
    const pageForm = document.querySelector("[data-page-form]");
    const pageDateInput = document.querySelector("[data-page-date]");

    if (fromInput) fromInput.value = state.from;
    if (toInput) toInput.value = state.to;
    if (pageDateInput) pageDateInput.value = state.pageDate;

    const withBusyButton = async (form, action) => {
        const button = form.querySelector("button[type='submit']");
        if (button) button.disabled = true;
        try {
            await action();
        } finally {
            if (button) button.disabled = false;
        }
    };

    rangeForm?.addEventListener("submit", (ev) => {
        ev.preventDefault();
        state.from = fromInput.value;
        state.to = toInput.value;
        withBusyButton(rangeForm, loadDaily);
    });

    pageForm?.addEventListener("submit", (ev) => {
        ev.preventDefault();
        state.pageDate = pageDateInput.value;
        withBusyButton(pageForm, () => loadPages(0));
    });

    loadKpis();
    loadDaily();
    loadPages(0);
};
