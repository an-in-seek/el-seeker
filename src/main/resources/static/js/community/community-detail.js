import { formatNumberWithComma } from "/js/common-util.js";

const API = {
    POST_DETAIL: "/api/v1/community/posts",
    COMMENTS: "/api/v1/community/posts",
};

const COMMENT_PAGE_SIZE = 20;

const TYPE_LABELS = {
    FREE: "자유",
    QUESTION: "Q&A",
    NOTICE: "공지",
    PRAY: "기도",
};

const UI_CLASSES = {
    HIDDEN: "d-none",
};

const App = {
    state: {
        postId: null,
        commentPage: 0,
        commentHasNext: true,
        commentLoading: false,
    },

    init() {
        App.state.postId = App.getPostId();
        if (!App.state.postId) {
            App.showContentError("유효하지 않은 게시글입니다.");
            return;
        }

        App.initNav();
        App.initScrollTop();
        App.loadPost();
        App.loadComments();
        App.bindCommentMore();
    },

    getPostId() {
        const body = document.body;
        const raw = body?.dataset?.postId;
        if (!raw) return null;
        const parsed = Number(raw);
        return Number.isNaN(parsed) ? null : parsed;
    },

    async loadPost() {
        try {
            const response = await fetch(`${API.POST_DETAIL}/${App.state.postId}`);
            if (!response.ok) {
                throw new Error(`게시글 조회 실패 (${response.status})`);
            }
            const post = await response.json();
            App.renderPost(post);
        } catch (error) {
            App.showContentError("게시글을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    renderPost(post) {
        App.setText("postTitle", post.title || "");
        App.setText("postAuthor", post.authorNickname || "익명");
        App.setText("postTime", App.formatRelativeTime(post.createdAt));
        App.setText("postViewCount", formatNumberWithComma(post.viewCount || 0));
        App.setText("postReactionCount", formatNumberWithComma(post.reactionCount || 0));
        App.setText("postCommentCount", formatNumberWithComma(post.commentCount || 0));
        App.setText("commentCountLabel", formatNumberWithComma(post.commentCount || 0));

        const typeLabel = TYPE_LABELS[post.type] || post.type || "기타";
        App.setText("postTypeBadge", typeLabel);

        const popularBadge = document.getElementById("postPopularBadge");
        if (popularBadge) {
            popularBadge.hidden = !post.isPopular;
        }

        const content = document.getElementById("postContent");
        if (content) {
            if (post.isHtml) {
                content.innerHTML = post.content || "";
            } else {
                content.textContent = post.content || "";
            }
        }
    },

    showContentError(message) {
        App.setText("postContent", message);
    },

    initNav() {
        const backButton = document.getElementById("topNavBackButton");
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "커뮤니티";
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }
        if (backButton) {
            backButton.classList.remove(UI_CLASSES.HIDDEN);
            backButton.addEventListener("click", () => {
                const backLink = document.body.dataset.backLink || "/web/community";
                window.location.href = backLink;
            });
        }
    },

    async loadComments() {
        if (App.state.commentLoading || !App.state.commentHasNext) return;

        App.state.commentLoading = true;
        const params = new URLSearchParams();
        params.set("page", String(App.state.commentPage));
        params.set("size", String(COMMENT_PAGE_SIZE));

        try {
            const response = await fetch(`${API.COMMENTS}/${App.state.postId}/comments?${params.toString()}`);
            if (!response.ok) {
                throw new Error(`댓글 조회 실패 (${response.status})`);
            }
            const payload = await response.json();
            App.state.commentHasNext = Boolean(payload?.hasNext);
            if (App.state.commentHasNext) {
                App.state.commentPage += 1;
            }
            App.renderComments(payload?.content || []);
        } catch (error) {
            App.toggleCommentEmpty(true, "댓글을 불러오지 못했습니다.");
            App.state.commentHasNext = false;
        } finally {
            App.state.commentLoading = false;
            App.toggleCommentMore(App.state.commentHasNext);
        }
    },

    renderComments(comments) {
        const list = document.getElementById("commentList");
        if (!list) return;

        if (!comments || comments.length === 0) {
            if (App.state.commentPage === 0) {
                App.toggleCommentEmpty(true, "등록된 댓글이 없습니다.");
            }
            return;
        }

        const fragment = document.createDocumentFragment();
        comments.forEach(comment => {
            fragment.appendChild(App.createCommentItem(comment));
        });

        list.appendChild(fragment);
        App.toggleCommentEmpty(false);
    },

    createCommentItem(comment) {
        const item = document.createElement("div");
        item.className = "comment-item";

        // Avatar
        const avatar = document.createElement("div");
        avatar.className = "comment-avatar";
        avatar.textContent = (comment.authorNickname || "익").charAt(0);
        item.appendChild(avatar);

        // Body (Meta + Content)
        const body = document.createElement("div");
        body.className = "comment-body";

        const meta = document.createElement("div");
        meta.className = "comment-meta";

        const author = document.createElement("span");
        author.className = "comment-author";
        author.textContent = comment.authorNickname || "익명";

        const time = document.createElement("span");
        time.className = "comment-time";
        time.textContent = App.formatRelativeTime(comment.createdAt);

        meta.appendChild(author);
        meta.appendChild(time);

        const content = document.createElement("div");
        content.className = "comment-content";
        content.textContent = comment.content || "";

        body.appendChild(meta);
        body.appendChild(content);

        item.appendChild(body);
        return item;
    },

    bindCommentMore() {
        const button = document.getElementById("commentMoreBtn");
        if (!button) return;

        button.addEventListener("click", () => {
            App.loadComments();
        });
    },

    toggleCommentMore(visible) {
        const button = document.getElementById("commentMoreBtn");
        if (!button) return;
        button.hidden = !visible;
    },

    toggleCommentEmpty(show, message) {
        const empty = document.getElementById("commentEmpty");
        if (!empty) return;
        if (message) {
            empty.textContent = message;
        }
        empty.hidden = !show;
    },

    setText(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
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

    initScrollTop() {
        const scrollTopBtn = document.getElementById("scrollToTopBtn");
        if (!scrollTopBtn) return;

        window.addEventListener("scroll", () => {
            if (window.scrollY > 300) {
                scrollTopBtn.classList.add("is-visible");
            } else {
                scrollTopBtn.classList.remove("is-visible");
            }
        });

        scrollTopBtn.addEventListener("click", () => {
            window.scrollTo({
                top: 0,
                behavior: "smooth",
            });
        });
    },
};

document.addEventListener("DOMContentLoaded", App.init);
