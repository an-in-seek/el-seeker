import { fetchWithAuthRetry } from "/js/common-util.js";
import { buildLoginRedirectUrl } from "/js/auth/auth-check.js";

const API_POSTS = "/api/v1/community/posts";

const App = {
    state: {
        submitting: false,
    },

    init() {
        App.setPageTitle();
        App.bindForm();
        App.bindCancel();
        App.bindTitleCount();
    },

    setPageTitle() {
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "글쓰기";
            pageTitleLabel.classList.remove("d-none");
        }
    },

    bindForm() {
        const form = document.getElementById("writeForm");
        if (!form) return;

        form.addEventListener("submit", (e) => {
            e.preventDefault();
            App.handleSubmit();
        });
    },

    bindCancel() {
        const btn = document.getElementById("btnCancel");
        if (!btn) return;

        btn.addEventListener("click", () => {
            window.location.href = "/web/community";
        });
    },

    bindTitleCount() {
        const input = document.getElementById("postTitle");
        const counter = document.getElementById("titleCount");
        if (!input || !counter) return;

        input.addEventListener("input", () => {
            counter.textContent = `${input.value.length} / 200`;
        });
    },

    validate() {
        let valid = true;

        const type = document.getElementById("postType");
        const typeError = document.getElementById("postTypeError");
        if (!type.value) {
            type.classList.add("is-invalid");
            typeError.textContent = "카테고리를 선택하세요.";
            valid = false;
        } else {
            type.classList.remove("is-invalid");
            typeError.textContent = "";
        }

        const title = document.getElementById("postTitle");
        const titleError = document.getElementById("postTitleError");
        if (!title.value.trim()) {
            title.classList.add("is-invalid");
            titleError.textContent = "제목을 입력하세요.";
            valid = false;
        } else if (title.value.trim().length > 200) {
            title.classList.add("is-invalid");
            titleError.textContent = "제목은 200자 이내여야 합니다.";
            valid = false;
        } else {
            title.classList.remove("is-invalid");
            titleError.textContent = "";
        }

        const content = document.getElementById("postContent");
        const contentError = document.getElementById("postContentError");
        if (!content.value.trim()) {
            content.classList.add("is-invalid");
            contentError.textContent = "내용을 입력하세요.";
            valid = false;
        } else {
            content.classList.remove("is-invalid");
            contentError.textContent = "";
        }

        return valid;
    },

    async handleSubmit() {
        if (App.state.submitting) return;
        if (!App.validate()) return;

        const submitBtn = document.getElementById("btnSubmit");
        App.state.submitting = true;
        submitBtn.disabled = true;
        submitBtn.textContent = "등록 중...";

        const body = {
            type: document.getElementById("postType").value,
            language: "ko",
            country: "KR",
            title: document.getElementById("postTitle").value.trim(),
            content: document.getElementById("postContent").value.trim(),
        };

        try {
            const response = await fetchWithAuthRetry(API_POSTS, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            });

            if (response.status === 401) {
                alert("로그인이 필요합니다.");
                window.location.href = buildLoginRedirectUrl("/web/community");
                return;
            }

            if (!response.ok) {
                const error = await response.json().catch(() => null);
                throw new Error(error?.message || "게시글 등록에 실패했습니다.");
            }

            const post = await response.json();
            window.location.href = `/web/community/${post.id}`;
        } catch (error) {
            alert(error.message || "게시글 등록에 실패했습니다. 잠시 후 다시 시도해주세요.");
        } finally {
            App.state.submitting = false;
            submitBtn.disabled = false;
            submitBtn.textContent = "등록하기";
        }
    },
};

document.addEventListener("DOMContentLoaded", App.init);
