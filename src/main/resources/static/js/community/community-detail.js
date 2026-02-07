import { formatNumberWithComma, fetchWithAuthRetry } from "/js/common-util.js";
import { buildLoginRedirectUrl, checkAuthStatus } from "/js/auth/auth-check.js";

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
        postAuthorNickname: null,
        commentPage: 0,
        commentHasNext: true,
        commentLoading: false,
        auth: {
            checked: false,
            allowed: false,
            checking: false,
            user: null,
        },
    },

    init() {
        App.state.postId = App.getPostId();
        if (!App.state.postId) {
            App.showContentError("유효하지 않은 게시글입니다.");
            return;
        }

        App.initAuth();
        App.initNav();
        App.initScrollTop();
        App.bindCommentForm();
        App.bindCommentActions();
        App.bindReportPost();
        App.bindPostOwnerActions();
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

    initAuth() {
        if (App.state.auth.checked || App.state.auth.checking) return;
        App.resolveAuth();
    },

    resolveAuth() {
        if (App.state.auth.checked) {
            return Promise.resolve(App.state.auth.allowed);
        }
        if (App.state.auth.checking) {
            return new Promise(resolve => {
                const timer = setInterval(() => {
                    if (!App.state.auth.checking) {
                        clearInterval(timer);
                        resolve(App.state.auth.allowed);
                    }
                }, 50);
            });
        }
        App.state.auth.checking = true;
        return new Promise(resolve => {
            checkAuthStatus({
                onAuthenticated: (data) => {
                    App.setAuthState(true, data);
                    resolve(true);
                },
                onUnauthenticated: () => {
                    App.setAuthState(false, null);
                    resolve(false);
                },
                onError: () => {
                    App.setAuthState(false, null);
                    resolve(false);
                },
            });
        });
    },

    setAuthState(allowed, user) {
        App.state.auth.checked = true;
        App.state.auth.allowed = allowed;
        App.state.auth.user = user;
        App.state.auth.checking = false;
        App.refreshCommentActions();
        App.refreshPostOwnerActions();
    },

    async ensureAuth() {
        const allowed = await App.resolveAuth();
        if (!allowed) {
            App.redirectToLogin();
            return false;
        }
        return true;
    },

    redirectToLogin() {
        alert("로그인이 필요합니다.");
        window.location.href = buildLoginRedirectUrl();
    },

    async loadPost() {
        try {
            const response = await fetch(`${API.POST_DETAIL}/${App.state.postId}`, {
                credentials: "include",
                headers: {
                    Accept: "application/json",
                },
            });
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
        App.state.postAuthorNickname = post.authorNickname || null;
        App.setText("postTitle", post.title || "");
        App.setText("postAuthor", post.authorNickname || "익명");
        App.setText("postTime", App.formatRelativeTime(post.createdAt));
        App.refreshPostOwnerActions();
        App.setText("postViewCount", formatNumberWithComma(post.viewCount || 0));
        App.setText("postReactionCount", formatNumberWithComma(post.reactionCount || 0));
        App.setText("postCommentCount", formatNumberWithComma(post.commentCount || 0));
        App.setText("commentCountLabel", formatNumberWithComma(post.commentCount || 0));
        App.setText("likeCountLabel", formatNumberWithComma(post.reactionCount || 0));

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
            const response = await fetch(`${API.COMMENTS}/${App.state.postId}/comments?${params.toString()}`, {
                credentials: "include",
                headers: {
                    Accept: "application/json",
                },
            });
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
        App.refreshCommentActions();
    },

    createCommentItem(comment) {
        const item = document.createElement("div");
        item.className = "comment-item";
        item.dataset.commentId = String(comment.id || "");
        item.dataset.authorNickname = comment.authorNickname || "";

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
        meta.appendChild(App.createCommentActions(comment));

        const content = document.createElement("div");
        content.className = "comment-content";
        content.textContent = comment.content || "";

        body.appendChild(meta);
        body.appendChild(content);

        item.appendChild(body);
        return item;
    },

    createCommentActions(comment) {
        const actions = document.createElement("div");
        actions.className = "comment-actions";

        const editBtn = App.createCommentAction("edit", "수정");
        editBtn.classList.add("comment-action-owner");
        actions.appendChild(editBtn);

        const deleteBtn = App.createCommentAction("delete", "삭제");
        deleteBtn.classList.add("comment-action-owner");
        actions.appendChild(deleteBtn);

        const reportBtn = App.createCommentAction("report", "신고");
        actions.appendChild(reportBtn);

        App.applyOwnerActionVisibility(actions, comment);
        return actions;
    },

    createCommentAction(action, label) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "comment-action";
        button.dataset.action = action;
        button.textContent = label;
        return button;
    },

    applyOwnerActionVisibility(container, comment) {
        const canManage = App.canManageComment(comment);
        container.querySelectorAll(".comment-action-owner").forEach(button => {
            button.hidden = !canManage;
        });
    },

    canManageComment(comment) {
        const user = App.state.auth.user;
        if (!user) return false;
        if (user.role === "ADMIN") return true;
        const nickname = (comment.authorNickname || "").trim();
        return nickname && nickname === user.nickname;
    },

    refreshCommentActions() {
        const list = document.getElementById("commentList");
        if (!list) return;
        list.querySelectorAll(".comment-item").forEach(item => {
            const nickname = item.dataset.authorNickname || "";
            const canManage = App.canManageComment({ authorNickname: nickname });
            item.querySelectorAll(".comment-action-owner").forEach(button => {
                button.hidden = !canManage;
            });
        });
    },

    canManagePost() {
        const user = App.state.auth.user;
        if (!user) return false;
        if (user.role === "ADMIN") return true;
        const authorNickname = (App.state.postAuthorNickname || "").trim();
        return authorNickname && authorNickname === user.nickname;
    },

    refreshPostOwnerActions() {
        const container = document.getElementById("postOwnerActions");
        if (!container) return;
        container.hidden = !App.canManagePost();
    },

    bindPostOwnerActions() {
        const editBtn = document.getElementById("btnEditPost");
        const deleteBtn = document.getElementById("btnDeletePost");

        if (editBtn) {
            editBtn.addEventListener("click", () => {
                App.handleEditPost();
            });
        }
        if (deleteBtn) {
            deleteBtn.addEventListener("click", () => {
                App.handleDeletePost();
            });
        }
    },

    handleEditPost() {
        window.location.href = `/web/community/write?postId=${App.state.postId}`;
    },

    async handleDeletePost() {
        if (!window.confirm("게시글을 삭제하시겠습니까?")) return;

        const allowed = await App.ensureAuth();
        if (!allowed) return;

        try {
            const response = await fetchWithAuthRetry(
                `${API.POST_DETAIL}/${App.state.postId}`,
                {
                    method: "DELETE",
                    credentials: "include",
                    headers: {
                        Accept: "application/json",
                    },
                }
            );

            if (response.status === 401) {
                App.redirectToLogin();
                return;
            }

            if (!response.ok) {
                throw new Error(`게시글 삭제 실패 (${response.status})`);
            }

            window.location.href = "/web/community";
        } catch (error) {
            alert("게시글 삭제에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    bindCommentActions() {
        const list = document.getElementById("commentList");
        if (!list) return;

        list.addEventListener("click", (event) => {
            const actionButton = event.target.closest("button[data-action]");
            if (!actionButton) return;
            const item = actionButton.closest(".comment-item");
            if (!item) return;

            const action = actionButton.dataset.action;
            if (action === "edit") {
                App.startEditComment(item);
                return;
            }
            if (action === "delete") {
                App.deleteComment(item);
                return;
            }
            if (action === "report") {
                App.reportComment(item);
            }
        });
    },

    startEditComment(item) {
        if (item.classList.contains("is-editing")) return;
        const contentEl = item.querySelector(".comment-content");
        if (!contentEl) return;

        const original = contentEl.textContent || "";
        item.dataset.originalContent = original;
        item.classList.add("is-editing");

        const editor = App.createCommentEditor(original, item);
        contentEl.replaceWith(editor);
    },

    createCommentEditor(original, item) {
        const editor = document.createElement("div");
        editor.className = "comment-editor";

        const textarea = document.createElement("textarea");
        textarea.value = original;
        editor.appendChild(textarea);

        const actions = document.createElement("div");
        actions.className = "comment-editor-actions";

        const saveBtn = document.createElement("button");
        saveBtn.type = "button";
        saveBtn.className = "primary";
        saveBtn.textContent = "저장";
        saveBtn.addEventListener("click", () => {
            App.saveCommentEdit(item, textarea.value);
        });

        const cancelBtn = document.createElement("button");
        cancelBtn.type = "button";
        cancelBtn.textContent = "취소";
        cancelBtn.addEventListener("click", () => {
            App.cancelEditComment(item);
        });

        actions.appendChild(saveBtn);
        actions.appendChild(cancelBtn);
        editor.appendChild(actions);
        return editor;
    },

    cancelEditComment(item) {
        const original = item.dataset.originalContent || "";
        const editor = item.querySelector(".comment-editor");
        if (!editor) return;

        const content = document.createElement("div");
        content.className = "comment-content";
        content.textContent = original;
        editor.replaceWith(content);
        item.classList.remove("is-editing");
    },

    async saveCommentEdit(item, content) {
        const commentId = item.dataset.commentId;
        if (!commentId) return;
        const trimmed = (content || "").trim();
        if (!trimmed) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }

        const allowed = await App.ensureAuth();
        if (!allowed) return;

        try {
            const response = await fetchWithAuthRetry(
                `${API.COMMENTS}/${App.state.postId}/comments/${commentId}`,
                {
                    method: "PUT",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                    },
                    body: JSON.stringify({ content: trimmed }),
                }
            );

            if (response.status === 401) {
                App.redirectToLogin();
                return;
            }

            if (!response.ok) {
                throw new Error(`댓글 수정 실패 (${response.status})`);
            }

            const updated = await response.json();
            const editor = item.querySelector(".comment-editor");
            if (!editor) return;
            const contentEl = document.createElement("div");
            contentEl.className = "comment-content";
            contentEl.textContent = updated.content || trimmed;
            editor.replaceWith(contentEl);
            item.classList.remove("is-editing");
            item.dataset.originalContent = updated.content || trimmed;
        } catch (error) {
            alert("댓글 수정에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    async deleteComment(item) {
        const commentId = item.dataset.commentId;
        if (!commentId) return;

        const confirmed = window.confirm("댓글을 삭제하시겠습니까?");
        if (!confirmed) return;

        const allowed = await App.ensureAuth();
        if (!allowed) return;

        try {
            const response = await fetchWithAuthRetry(
                `${API.COMMENTS}/${App.state.postId}/comments/${commentId}`,
                {
                    method: "DELETE",
                    credentials: "include",
                    headers: {
                        Accept: "application/json",
                    },
                }
            );

            if (response.status === 401) {
                App.redirectToLogin();
                return;
            }

            if (!response.ok) {
                throw new Error(`댓글 삭제 실패 (${response.status})`);
            }

            item.remove();
            App.updateCommentCountBy(-1);
            const list = document.getElementById("commentList");
            if (list && list.children.length === 0) {
                App.toggleCommentEmpty(true, "등록된 댓글이 없습니다.");
            }
        } catch (error) {
            alert("댓글 삭제에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    async reportComment(item) {
        const commentId = item.dataset.commentId;
        if (!commentId) return;

        const allowed = await App.ensureAuth();
        if (!allowed) return;

        const reason = App.promptReportReason();
        if (!reason) return;

        try {
            const response = await fetchWithAuthRetry(
                `${API.COMMENTS}/${App.state.postId}/comments/${commentId}/reports`,
                {
                    method: "POST",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                    },
                    body: JSON.stringify({ reason }),
                }
            );

            if (response.status === 401) {
                App.redirectToLogin();
                return;
            }

            if (!response.ok) {
                throw new Error(`댓글 신고 실패 (${response.status})`);
            }
            alert("신고가 접수되었습니다.");
        } catch (error) {
            alert("댓글 신고에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    bindReportPost() {
        const button = document.getElementById("btnReportPost");
        if (!button) return;

        button.addEventListener("click", async () => {
            const allowed = await App.ensureAuth();
            if (!allowed) return;

            const reason = App.promptReportReason();
            if (!reason) return;

            try {
                const response = await fetchWithAuthRetry(
                    `${API.POST_DETAIL}/${App.state.postId}/reports`,
                    {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                        },
                        body: JSON.stringify({ reason }),
                    }
                );

                if (response.status === 401) {
                    App.redirectToLogin();
                    return;
                }

                if (!response.ok) {
                    throw new Error(`게시글 신고 실패 (${response.status})`);
                }
                alert("신고가 접수되었습니다.");
            } catch (error) {
                alert("게시글 신고에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        });
    },

    promptReportReason() {
        const reasons = ["SPAM", "ABUSE", "HATE", "ADULT", "ETC"];
        const value = window.prompt(
            "신고 사유를 입력해주세요. (SPAM, ABUSE, HATE, ADULT, ETC)",
            "SPAM"
        );
        if (!value) return null;
        const normalized = value.trim().toUpperCase();
        if (!reasons.includes(normalized)) {
            alert("유효하지 않은 신고 사유입니다.");
            return null;
        }
        return normalized;
    },

    updateCommentCountBy(delta) {
        const targets = ["postCommentCount", "commentCountLabel"];
        targets.forEach(id => {
            const element = document.getElementById(id);
            if (!element) return;
            const current = App.parseNumber(element.textContent);
            const next = Math.max(current + delta, 0);
            element.textContent = formatNumberWithComma(next);
        });
    },

    parseNumber(value) {
        if (!value) return 0;
        const numeric = Number(String(value).replace(/,/g, ""));
        return Number.isNaN(numeric) ? 0 : numeric;
    },

    bindCommentMore() {
        const button = document.getElementById("commentMoreBtn");
        if (!button) return;

        button.addEventListener("click", () => {
            App.loadComments();
        });
    },

    bindCommentForm() {
        const form = document.getElementById("commentForm");
        const input = document.getElementById("commentInput");
        if (!form || !input) return;

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const content = input.value.trim();
            if (!content) return;
            const allowed = await App.ensureAuth();
            if (!allowed) return;
            App.submitComment(content);
        });
    },

    async submitComment(content) {
        const url = `${API.COMMENTS}/${App.state.postId}/comments`;
        try {
            const response = await fetchWithAuthRetry(url, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify({ content }),
            });

            if (response.status === 401) {
                App.redirectToLogin();
                return;
            }

            if (!response.ok) {
                throw new Error(`댓글 작성 실패 (${response.status})`);
            }

            const saved = await response.json();
            App.appendNewComment(saved);
            App.updateCommentCountBy(1);
            const input = document.getElementById("commentInput");
            if (input) input.value = "";
        } catch (error) {
            alert("댓글 작성에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    },

    appendNewComment(comment) {
        const list = document.getElementById("commentList");
        if (!list) return;
        const item = App.createCommentItem(comment);
        list.appendChild(item);
        App.toggleCommentEmpty(false);
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
