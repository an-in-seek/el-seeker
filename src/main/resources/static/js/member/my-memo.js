import {buildLoginRedirectUrl, checkAuthStatus} from "/js/auth/auth-check.js";
import {fetchWithAuthRetry} from "/js/common-util.js?v=2.2";

const PAGE_SIZE = 20;

const state = {
    page: 0,
    hasNext: false,
    loading: false,
    translationFilter: null,
    bookFilter: null,
};

const formatMemoDate = (value) => {
    if (!value) {
        return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "";
    }
    return date.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    });
};

document.addEventListener("DOMContentLoaded", () => {
    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "나의 메모";
        pageTitleLabel.classList.remove("d-none");
    }

    const backButton = document.getElementById("topNavBackButton");
    if (backButton) {
        backButton.classList.remove("d-none");
        backButton.addEventListener("click", () => history.back());
    }
    
    const elements = {
        skeleton: document.getElementById("myMemoSkeleton"),
        list: document.getElementById("myMemoList"),
        empty: document.getElementById("myMemoEmpty"),
        more: document.getElementById("myMemoMore"),
        filter: document.getElementById("myMemoFilter"),
        translationSelect: document.getElementById("myMemoTranslationFilter"),
        bookSelect: document.getElementById("myMemoBookFilter"),
        countBadge: document.getElementById("myMemoCountBadge"),
    };

    const redirectToLogin = () => {
        window.location.replace(buildLoginRedirectUrl("/web/member/my-memo"));
    };

    const updateCountBadge = (count) => {
        if (!elements.countBadge) {
            return;
        }
        elements.countBadge.textContent = `총 ${count}개`;
        elements.countBadge.classList.remove("d-none");
    };

    const updateMoreButton = () => {
        if (!elements.more) {
            return;
        }
        elements.more.disabled = state.loading;
        elements.more.classList.toggle("d-none", !state.hasNext);
    };

    const createMemoCard = (memo) => {
        const card = document.createElement("a");
        card.className = "my-memo-card";
        card.href = `/web/bible/verse?translationId=${encodeURIComponent(memo.translationId)}&bookOrder=${encodeURIComponent(memo.bookOrder)}&chapterNumber=${encodeURIComponent(memo.chapterNumber)}&verseNumber=${encodeURIComponent(memo.verseNumber)}&from=mypage`;

        const header = document.createElement("div");
        header.className = "my-memo-card-header";

        const refSpan = document.createElement("span");
        refSpan.className = "my-memo-card-ref";
        refSpan.textContent = `${memo.bookName} ${memo.chapterNumber}:${memo.verseNumber}`;

        const dateSpan = document.createElement("span");
        dateSpan.className = "my-memo-card-date";
        dateSpan.textContent = formatMemoDate(memo.updatedAt);

        const content = document.createElement("div");
        content.className = "my-memo-card-content";
        content.textContent = memo.content || "";

        header.append(refSpan, dateSpan);
        card.append(header, content);
        return card;
    };

    const renderErrorState = () => {
        if (!elements.list) {
            return;
        }

        elements.list.innerHTML = "";
        elements.empty?.classList.add("d-none");

        const errorBlock = document.createElement("div");
        errorBlock.className = "my-memo-empty";

        const message = document.createElement("p");
        message.className = "mb-2";
        message.textContent = "메모를 불러오지 못했습니다.";

        const retryButton = document.createElement("button");
        retryButton.type = "button";
        retryButton.className = "btn btn-outline-primary btn-sm";
        retryButton.textContent = "다시 시도";
        retryButton.addEventListener("click", async () => {
            state.page = 0;
            await loadMyMemos(false);
        });

        errorBlock.append(message, retryButton);
        elements.list.appendChild(errorBlock);
    };

    const loadBooks = async (translationId) => {
        if (!elements.bookSelect) {
            return;
        }

        elements.bookSelect.innerHTML = '<option value="">전체 성경</option>';
        elements.bookSelect.disabled = true;
        state.bookFilter = null;

        if (!translationId) {
            return;
        }

        try {
            const response = await fetchWithAuthRetry(
                `/api/v1/bibles/my-memos/books?translationId=${translationId}`,
                {
                    credentials: "include",
                    headers: {Accept: "application/json"},
                }
            );
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (!response.ok) {
                return;
            }

            const books = await response.json().catch(() => []);
            if (!Array.isArray(books) || books.length === 0) {
                return;
            }

            books.forEach((book) => {
                const option = document.createElement("option");
                option.value = String(book.bookOrder);
                option.textContent = book.bookName;
                elements.bookSelect.appendChild(option);
            });
            elements.bookSelect.disabled = false;
        } catch {
            elements.bookSelect.disabled = true;
        }
    };

    const loadTranslations = async () => {
        if (!elements.translationSelect || !elements.filter) {
            return;
        }

        try {
            const response = await fetchWithAuthRetry("/api/v1/bibles/my-memos/translations", {
                credentials: "include",
                headers: {Accept: "application/json"},
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (!response.ok) {
                return;
            }

            const translations = await response.json().catch(() => []);
            if (!Array.isArray(translations) || translations.length === 0) {
                return;
            }

            elements.filter.classList.remove("d-none");
            elements.translationSelect.innerHTML = '<option value="">전체 번역본</option>';

            translations.forEach((translation) => {
                const option = document.createElement("option");
                option.value = String(translation.translationId);
                option.textContent = translation.translationName;
                elements.translationSelect.appendChild(option);
            });

            if (translations.length === 1) {
                const [translation] = translations;
                elements.translationSelect.value = String(translation.translationId);
                state.translationFilter = translation.translationId;
                await loadBooks(state.translationFilter);
            }
        } catch {
            // 필터 로드 실패 시 목록 조회는 계속 진행합니다.
        }
    };

    const loadMyMemos = async (append = false) => {
        if (state.loading) {
            return;
        }

        state.loading = true;
        updateMoreButton();
        if (!append) {
            elements.skeleton?.classList.remove("d-none");
        }

        try {
            let memoApiUrl = `/api/v1/bibles/my-memos?page=${state.page}&size=${PAGE_SIZE}`;
            if (state.translationFilter != null) {
                memoApiUrl += `&translationId=${state.translationFilter}`;
            }
            if (state.bookFilter != null) {
                memoApiUrl += `&bookOrder=${state.bookFilter}`;
            }

            const response = await fetchWithAuthRetry(memoApiUrl, {
                credentials: "include",
                headers: {Accept: "application/json"},
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (!response.ok) {
                throw new Error("Failed to load my memos");
            }

            const data = await response.json().catch(() => null);
            if (!data) {
                throw new Error("Invalid memo response");
            }

            if (!append && elements.list) {
                elements.list.innerHTML = "";
            }

            const content = Array.isArray(data.content) ? data.content : [];
            if (content.length > 0) {
                if (elements.list) {
                    const fragment = document.createDocumentFragment();
                    content.forEach((memo) => {
                        fragment.appendChild(createMemoCard(memo));
                    });
                    elements.list.appendChild(fragment);
                }
                elements.empty?.classList.add("d-none");
            } else if (!append) {
                elements.list?.replaceChildren();
                elements.empty?.classList.remove("d-none");
            }

            if (!append && data.totalCount != null) {
                updateCountBadge(data.totalCount);
            }

            state.hasNext = data.hasNext === true;
        } catch (error) {
            if (append && state.page > 0) {
                state.page -= 1;
            }
            state.hasNext = false;
            renderErrorState();
        } finally {
            elements.skeleton?.classList.add("d-none");
            state.loading = false;
            updateMoreButton();
        }
    };

    const resetAndReload = async () => {
        state.page = 0;
        await loadMyMemos(false);
    };

    elements.translationSelect?.addEventListener("change", async () => {
        const value = elements.translationSelect.value;
        state.translationFilter = value ? parseInt(value, 10) : null;
        await loadBooks(state.translationFilter);
        await resetAndReload();
    });

    elements.bookSelect?.addEventListener("change", async () => {
        const value = elements.bookSelect.value;
        state.bookFilter = value ? parseInt(value, 10) : null;
        await resetAndReload();
    });

    elements.more?.addEventListener("click", async () => {
        if (!state.hasNext || state.loading) {
            return;
        }
        state.page += 1;
        await loadMyMemos(true);
    });

    checkAuthStatus({
        onAuthenticated: async () => {
            await loadTranslations();
            await loadMyMemos(false);
        },
        onUnauthenticated: redirectToLogin,
        onError: renderErrorState,
    });
});
