import { formatNumberWithComma } from "/js/common-util.js";

const API = {
    POSTS: "/api/v1/community/posts",
    TOP_POSTS: "/api/v1/community/posts/top",
};

const PAGE_SIZE = 20;

const CATEGORY_CONFIG = {
    all: { sort: "latest" },
    "인기": { sort: "popular" },
    "자유": { sort: "latest", type: "FREE" },
    "Q&A": { sort: "latest", type: "QUESTION" },
    "기도나눔": { sort: "latest", type: "PRAY" },
};

const TYPE_LABELS = {
    FREE: "자유",
    QUESTION: "Q&A",
    NOTICE: "공지",
    PRAY: "기도",
};

const DEFAULT_EMPTY_MESSAGE = "아직 등록된 글이 없습니다<br>첫 번째 글을 작성해보세요!";

const App = {
    state: {
        activeCategory: "all",
        abortController: null,
    },

    init() {
        App.setPageTitle();
        App.initComingSoon();
        App.initCategoryTabs();
        App.initTop3MoreLink();
        App.initScrollTop();
        App.loadTopPosts();

        const initialCategory = App.getInitialCategory();
        App.selectCategory(initialCategory);
    },

    setPageTitle() {
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "커뮤니티";
            pageTitleLabel.classList.remove("d-none");
        }
    },

    initComingSoon() {
        const comingSoonCards = document.querySelectorAll(".coming-soon");
        comingSoonCards.forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();
            });
        });
    },

    initCategoryTabs() {
        const tabs = document.querySelectorAll(".community-tab");
        if (tabs.length === 0) return;

        tabs.forEach(tab => {
            tab.addEventListener("click", () => {
                const category = tab.dataset.category || "all";
                App.selectCategory(category);
            });
        });
    },

    initTop3MoreLink() {
        const moreLinks = document.querySelectorAll(".top3-widget .widget-more");
        if (moreLinks.length === 0) return;

        moreLinks.forEach(link => {
            link.addEventListener("click", event => {
                event.preventDefault();
                App.selectCategory("인기");
            });
        });
    },

    initScrollTop() {
        const scrollTopBtn = document.getElementById("scrollTopBtn");
        if (!scrollTopBtn) return;

        window.addEventListener("scroll", () => {
            if (window.scrollY > 300) {
                scrollTopBtn.classList.add("show");
            } else {
                scrollTopBtn.classList.remove("show");
            }
        });

        scrollTopBtn.addEventListener("click", () => {
            window.scrollTo({
                top: 0,
                behavior: "smooth",
            });
        });
    },

    getInitialCategory() {
        const activeTab = document.querySelector(".community-tab.active");
        return activeTab?.dataset.category || "all";
    },

    selectCategory(category) {
        if (!category) return;

        App.state.activeCategory = category;
        App.updateTabState(category);
        App.toggleTop3(category === "all");

        const config = CATEGORY_CONFIG[category];
        if (config?.unsupported) {
            App.clearFeed();
            App.setEmptyState(true, "준비 중인 카테고리입니다.");
            return;
        }

        App.loadPosts(category);
    },

    updateTabState(category) {
        const tabs = document.querySelectorAll(".community-tab");
        tabs.forEach(tab => {
            const isActive = tab.dataset.category === category;
            tab.classList.toggle("active", isActive);
            tab.setAttribute("aria-selected", String(isActive));
        });
    },

    toggleTop3(show) {
        const feedTop3 = document.getElementById("feedTop3");
        const mobileTop3 = document.getElementById("mobileTop3");

        if (feedTop3) feedTop3.hidden = !show;
        if (mobileTop3) mobileTop3.hidden = !show;
    },

    async loadPosts(category) {
        const config = CATEGORY_CONFIG[category] || CATEGORY_CONFIG.all;

        if (App.state.abortController) {
            App.state.abortController.abort();
        }

        const params = new URLSearchParams();
        params.set("page", "0");
        params.set("size", String(PAGE_SIZE));
        params.set("order", config.sort || "latest");
        if (config.type) {
            params.set("type", config.type);
        }

        App.setEmptyState(true, "게시글을 불러오는 중입니다...");
        App.clearFeed();

        const controller = new AbortController();
        App.state.abortController = controller;

        try {
            const response = await fetch(`${API.POSTS}?${params.toString()}`, {
                signal: controller.signal,
            });

            if (!response.ok) {
                throw new Error(`게시글 조회 실패 (${response.status})`);
            }

            const payload = await response.json();
            App.renderPosts(payload?.content || []);
        } catch (error) {
            if (error.name === "AbortError") return;
            App.clearFeed();
            App.setEmptyState(true, "게시글을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    renderPosts(posts) {
        const { feedList, emptyState } = App.getFeedElements();
        if (!feedList || !emptyState) return;

        App.clearFeed();

        if (!posts || posts.length === 0) {
            App.setEmptyState(true, DEFAULT_EMPTY_MESSAGE);
            return;
        }

        const fragment = document.createDocumentFragment();
        posts.forEach(post => {
            fragment.appendChild(App.createPostCard(post));
        });

        feedList.insertBefore(fragment, emptyState);
        App.setEmptyState(false);
    },

    createPostCard(post) {
        const card = document.createElement("article");
        card.className = "feed-card";
        card.dataset.category = App.getTypeLabel(post.type);

        const topRow = document.createElement("div");
        topRow.className = "feed-top-row";

        const badges = document.createElement("div");
        badges.className = "feed-badges";

        const categoryBadge = document.createElement("span");
        categoryBadge.className = "feed-category-badge";
        categoryBadge.textContent = App.getTypeLabel(post.type);
        badges.appendChild(categoryBadge);

        if (post.isPopular) {
            const popularBadge = document.createElement("span");
            popularBadge.className = "feed-category-badge";
            popularBadge.textContent = "인기";
            badges.appendChild(popularBadge);
        }

        topRow.appendChild(badges);
        card.appendChild(topRow);

        const title = document.createElement("h4");
        title.className = "feed-title";
        title.textContent = post.title || "";
        card.appendChild(title);

        const previewText = post.preview || post.summary || "";
        if (previewText) {
            const preview = document.createElement("div");
            preview.className = "feed-preview";
            preview.textContent = previewText;
            card.appendChild(preview);
        }

        const footer = document.createElement("div");
        footer.className = "feed-footer";

        const userInfo = document.createElement("div");
        userInfo.className = "feed-user-info";

        const metaCol = document.createElement("div");
        metaCol.className = "feed-meta-col";

        const author = document.createElement("span");
        author.className = "feed-author";
        author.textContent = post.authorNickname || "익명";

        const time = document.createElement("span");
        time.className = "feed-time";
        time.textContent = App.formatRelativeTime(post.createdAt);

        metaCol.appendChild(author);
        metaCol.appendChild(time);
        userInfo.appendChild(metaCol);

        const actions = document.createElement("div");
        actions.className = "feed-actions";

        actions.appendChild(App.createActionPill("👀", post.viewCount));
        actions.appendChild(App.createActionPill("❤️", post.reactionCount));
        actions.appendChild(App.createActionPill("💬", post.commentCount));

        footer.appendChild(userInfo);
        footer.appendChild(actions);
        card.appendChild(footer);

        return card;
    },

    createActionPill(icon, value) {
        const pill = document.createElement("span");
        pill.className = "action-pill";

        const iconSpan = document.createElement("i");
        iconSpan.className = "icon";
        iconSpan.textContent = icon;

        const text = document.createTextNode(` ${formatNumberWithComma(value || 0)}`);

        pill.appendChild(iconSpan);
        pill.appendChild(text);
        return pill;
    },

    getTypeLabel(type) {
        return TYPE_LABELS[type] || type || "기타";
    },

    formatRelativeTime(isoString) {
        if (!isoString) return "";
        const target = new Date(isoString);
        if (Number.isNaN(target.getTime())) return "";

        const diffMs = Date.now() - target.getTime();
        const diffSeconds = Math.floor(diffMs / 1000);
        if (diffSeconds < 60) return `${Math.max(diffSeconds, 0)}초 전`;

        const diffMinutes = Math.floor(diffSeconds / 60);
        if (diffMinutes < 60) return `${diffMinutes}분 전`;

        const diffHours = Math.floor(diffMinutes / 60);
        if (diffHours < 24) return `${diffHours}시간 전`;

        const diffDays = Math.floor(diffHours / 24);
        if (diffDays < 7) return `${diffDays}일 전`;

        const diffWeeks = Math.floor(diffDays / 7);
        if (diffWeeks < 4) return `${diffWeeks}주 전`;

        const diffMonths = Math.floor(diffDays / 30);
        if (diffMonths < 12) return `${diffMonths}개월 전`;

        const diffYears = Math.floor(diffDays / 365);
        return `${diffYears}년 전`;
    },

    setEmptyState(visible, message) {
        const { emptyState, emptyText } = App.getFeedElements();
        if (!emptyState || !emptyText) return;

        if (message) {
            emptyText.innerHTML = message;
        }

        emptyState.style.display = visible ? "block" : "none";
    },

    clearFeed() {
        const { feedList, emptyState } = App.getFeedElements();
        if (!feedList || !emptyState) return;

        const cards = feedList.querySelectorAll(".feed-card");
        cards.forEach(card => card.remove());
    },

    getFeedElements() {
        const feedList = document.querySelector(".feed-list");
        const emptyState = feedList?.querySelector(".feed-empty") || null;
        const emptyText = emptyState?.querySelector(".empty-text") || null;
        return { feedList, emptyState, emptyText };
    },

    async loadTopPosts() {
        try {
            const response = await fetch(API.TOP_POSTS);
            if (!response.ok) {
                throw new Error(`인기글 조회 실패 (${response.status})`);
            }
            const posts = await response.json();
            App.renderTopPosts(posts || []);
        } catch (error) {
            // Fail silently; keep empty state text in the widget.
        }
    },

    renderTopPosts(posts) {
        const lists = document.querySelectorAll(".top3-list");
        if (lists.length === 0) return;

        lists.forEach(list => {
            const cardClass = list.dataset.cardClass || "";
            list.innerHTML = "";

            if (!posts || posts.length === 0) {
                const empty = document.createElement("div");
                empty.className = "top3-empty";
                empty.textContent = "인기글이 아직 없습니다.";
                list.appendChild(empty);
                return;
            }

            const fragment = document.createDocumentFragment();
            posts.slice(0, 3).forEach((post, index) => {
                fragment.appendChild(App.createTopPostCard(post, index + 1, cardClass));
            });

            list.appendChild(fragment);
        });
    },

    createTopPostCard(post, rank, cardClass) {
        const card = document.createElement("article");
        card.className = ["feed-card", cardClass].filter(Boolean).join(" ");
        card.dataset.rank = String(rank);

        const topRow = document.createElement("div");
        topRow.className = "feed-top-row";

        const badges = document.createElement("div");
        badges.className = "feed-badges";

        const categoryBadge = document.createElement("span");
        categoryBadge.className = "feed-category-badge";
        categoryBadge.textContent = App.getTypeLabel(post.type);
        badges.appendChild(categoryBadge);

        topRow.appendChild(badges);
        card.appendChild(topRow);

        const title = document.createElement("h4");
        title.className = "feed-title";
        title.textContent = post.title || "";
        card.appendChild(title);

        const footer = document.createElement("div");
        footer.className = "feed-footer";

        const actions = document.createElement("div");
        actions.className = "feed-actions";
        actions.appendChild(App.createActionPill("👀", post.viewCount));
        actions.appendChild(App.createActionPill("❤️", post.reactionCount));
        actions.appendChild(App.createActionPill("💬", post.commentCount));

        footer.appendChild(actions);
        card.appendChild(footer);

        return card;
    },
};

document.addEventListener("DOMContentLoaded", App.init);
