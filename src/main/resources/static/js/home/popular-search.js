const TRIGGERS = {
    bible: {
        title: "성경 구절 인기 검색어",
        endpoint: "/api/v1/bibles/search-keywords/ranking",
        linkTemplate: "/web/bible/search?keyword={kw}",
        ariaTemplate: "순위 {rank}위, {keyword} 구절 검색",
    },
    dictionary: {
        title: "성경 사전 인기 검색어",
        endpoint: "/api/v1/study/dictionaries/search-keywords/ranking",
        linkTemplate: "/web/study/dictionary?keyword={kw}",
        ariaTemplate: "순위 {rank}위, {keyword} 사전 검색",
    },
};

const INITIAL_LIMIT = 5;
const DIALOG_LIMIT = 50;
const TIMEOUT_MS = 5000;

export const initPopularSearch = () => {
    document.querySelectorAll("[data-keyword-ranking]").forEach(renderCard);
    initDialog();
};

const fetchRanking = async (type, limit) => {
    const trigger = TRIGGERS[type];
    if (!trigger) return null;
    const url = new URL(trigger.endpoint, window.location.origin);
    url.searchParams.set("limit", String(limit));

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
    try {
        const res = await fetch(url, {signal: controller.signal, credentials: "omit"});
        if (!res.ok) return null;
        const data = await res.json();
        return Array.isArray(data?.items) ? data.items : [];
    } catch (_) {
        return null;
    } finally {
        clearTimeout(timer);
    }
};

const renderCard = async (card) => {
    const type = card.dataset.keywordRanking;
    if (!TRIGGERS[type]) return;
    const items = await fetchRanking(type, INITIAL_LIMIT);
    if (!items || items.length === 0) return;
    paintList(card.querySelector("[data-keyword-list]"), items, type);
    card.hidden = false;
};

const paintList = (list, items, type) => {
    if (!list) return;
    const {linkTemplate, ariaTemplate} = TRIGGERS[type];
    list.replaceChildren(...items.map((item) => buildItem(item, linkTemplate, ariaTemplate)));
};

const buildItem = (item, linkTemplate, ariaTemplate) => {
    const li = document.createElement("li");
    li.className = "popular-search-item";

    const a = document.createElement("a");
    a.className = "popular-search-link";
    a.href = linkTemplate.replace("{kw}", encodeURIComponent(item.keyword));
    a.setAttribute(
        "aria-label",
        ariaTemplate.replace("{rank}", item.rank).replace("{keyword}", item.keyword),
    );
    if (item.rank <= 3) {
        a.classList.add(`top-rank-${item.rank}`);
    }

    const rank = document.createElement("span");
    rank.className = "popular-search-rank";
    rank.textContent = String(item.rank);

    const keyword = document.createElement("span");
    keyword.className = "popular-search-keyword";
    keyword.textContent = item.keyword;

    a.append(rank, keyword);
    li.appendChild(a);
    return li;
};

const initDialog = () => {
    const dialog = document.getElementById("popularSearchDialog");
    if (!dialog || typeof dialog.showModal !== "function") return;

    const titleEl = dialog.querySelector("#popularSearchDialogTitle");
    const listEl = dialog.querySelector("[data-dialog-keyword-list]");
    const closeBtn = dialog.querySelector("[data-dialog-close]");

    const openFor = async (type) => {
        const trigger = TRIGGERS[type];
        if (!trigger || !titleEl || !listEl) return;
        titleEl.textContent = trigger.title;
        listEl.replaceChildren();
        dialog.showModal();

        const items = await fetchRanking(type, DIALOG_LIMIT);
        if (!items || items.length === 0) {
            const li = document.createElement("li");
            li.className = "popular-search-item text-muted small";
            li.textContent = "표시할 검색어가 없습니다.";
            listEl.appendChild(li);
            return;
        }
        paintList(listEl, items, type);
    };

    document.querySelectorAll("[data-ranking-more]").forEach((btn) => {
        btn.addEventListener("click", () => openFor(btn.dataset.rankingMore));
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", () => dialog.close());
    }

    dialog.addEventListener("click", (e) => {
        if (e.target === dialog) {
            dialog.close();
        }
    });
};
