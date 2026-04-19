const ENDPOINTS = {
    bible: "/api/v1/bibles/search-keywords/ranking?limit=5",
    dictionary: "/api/v1/study/dictionaries/search-keywords/ranking?limit=5",
};

const TIMEOUT_MS = 5000;

export const initHomePopularSearch = () => {
    document.querySelectorAll("[data-keyword-ranking]").forEach(renderCard);
};

const renderCard = async (card) => {
    const type = card.dataset.keywordRanking;
    const endpoint = ENDPOINTS[type];
    if (!endpoint) return;

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);

    try {
        const res = await fetch(endpoint, {signal: controller.signal, credentials: "omit"});
        if (!res.ok) return;
        const data = await res.json();
        const items = Array.isArray(data?.items) ? data.items : [];
        if (items.length === 0) return;
        paint(card, items);
        card.hidden = false;
    } catch (_) {
        // network error / timeout → keep card hidden
    } finally {
        clearTimeout(timer);
    }
};

const paint = (card, items) => {
    const list = card.querySelector("[data-keyword-list]");
    if (!list) return;
    const linkTpl = card.dataset.linkTemplate;
    const ariaTpl = card.dataset.ariaTemplate;

    list.replaceChildren(...items.map((item) => {
        const li = document.createElement("li");
        li.className = "popular-search-item";

        const a = document.createElement("a");
        a.className = "popular-search-link";
        a.href = linkTpl.replace("{kw}", encodeURIComponent(item.keyword));
        if (ariaTpl) {
            a.setAttribute(
                "aria-label",
                ariaTpl.replace("{rank}", item.rank).replace("{keyword}", item.keyword),
            );
        }
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
    }));
};
