document.addEventListener("DOMContentLoaded", () => {
    
    const dom = {
        backButton: document.getElementById("topNavBackButton"),
        translationLink: document.getElementById("topNavTranslationLink"),
        translationTypeLabel: document.getElementById("translationTypeLabel"),
        pageTitleLabel: document.getElementById("pageTitleLabel"),
        searchLink: document.getElementById("topNavSearchLink"),
        keywordInput: document.getElementById("keywordInput"),
        searchForm: document.getElementById("searchForm"),
        clearBtn: document.getElementById("clearBtn"),
        searchBtn: document.getElementById("searchBtn"),
        resultCount: document.getElementById("resultCount"),
        emptyState: document.getElementById("searchEmptyState"),
        searchResultTable: document.getElementById("searchResultTable"),
        searchResultBody: document.getElementById("searchResultBody"),
        scrollToTopBtn: document.getElementById("scrollToTopBtn"),
    };

    const config = {
        scrollThreshold: 300,
        pageSize: 50,
        scrollLoadOffset: 200,
    };

    const state = {
        translationId: getTranslationId(),
        translationType: null,
        currentPage: 0,
        hasNext: false,
        isLoading: false,
        totalCount: null,
        activeKeyword: "",
        initialKeyword: getInitialKeyword(),
    };

    const init = async () => {
        if (!state.translationId) {
            redirectToTranslation();
            return;
        }

        initNav();

        const translationInfo = await ensureTranslationInfo(state.translationId);
        state.translationType = translationInfo.type;
        updateTranslationTypeLabel();

        initFormControls();
        initResultHandlers();
        initScrollToTop();
        window.addEventListener("scroll", maybeLoadNextPage, {passive: true});

        if (state.initialKeyword) {
            if (dom.keywordInput) {
                dom.keywordInput.value = state.initialKeyword;
            }
            startSearch(state.initialKeyword);
        } else {
            updateUrl();
        }
    };

    function getTranslationId() {
        const urlParams = new URLSearchParams(window.location.search);
        const translationIdParam = parseInt(urlParams.get("translationId"), 10);
        return Number.isNaN(translationIdParam)
            ? TranslationStore.getCurrentTranslationId()
            : translationIdParam;
    }

    function getInitialKeyword() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get("keyword") ?? "";
    }

    function initNav() {
        if (dom.backButton) {
            dom.backButton.classList.remove("d-none");
            dom.backButton.addEventListener("click", () => {
                history.back();
            });
        }
        if (dom.translationLink) {
            dom.translationLink.classList.remove("d-none");
            dom.translationLink.classList.add("nav-placeholder");
            dom.translationLink.setAttribute("aria-hidden", "true");
            dom.translationLink.setAttribute("tabindex", "-1");
        }
        if (dom.searchLink) {
            dom.searchLink.classList.remove("d-none");
            dom.searchLink.classList.add("nav-placeholder");
        }
        if (dom.pageTitleLabel) {
            dom.pageTitleLabel.textContent = "성경 검색";
            dom.pageTitleLabel.classList.remove("d-none");
        }
    }

    function updateTranslationTypeLabel() {
        if (dom.translationTypeLabel) {
            dom.translationTypeLabel.textContent = state.translationType ?? "";
        }
    }

    function initFormControls() {
        if (dom.clearBtn && dom.keywordInput) {
            dom.clearBtn.addEventListener("click", () => {
                dom.keywordInput.value = "";
                dom.keywordInput.focus();
                clearResults();
                setEmptyState("검색어를 입력하고 검색을 시작하세요.");
                resetSearchState();
                updateUrl();
            });

            dom.keywordInput.addEventListener("input", () => {
                dom.clearBtn.disabled = dom.keywordInput.value.trim().length === 0;
            });
            dom.keywordInput.focus();
            dom.clearBtn.disabled = dom.keywordInput.value.trim().length === 0;
        }

        if (dom.searchForm && dom.keywordInput) {
            dom.searchForm.addEventListener("submit", async event => {
                event.preventDefault();
                await startSearch(dom.keywordInput.value);
            });
        }

    }

    function initResultHandlers() {
        if (dom.searchResultBody) {
            dom.searchResultBody.addEventListener("click", handleResultClick);
        }
    }

    function initScrollToTop() {
        if (!dom.scrollToTopBtn) {
            return;
        }

        const updateScrollToTopVisibility = () => {
            if (window.scrollY >= config.scrollThreshold) {
                dom.scrollToTopBtn.classList.add("is-visible");
            } else {
                dom.scrollToTopBtn.classList.remove("is-visible");
            }
        };

        dom.scrollToTopBtn.addEventListener("click", () => {
            window.scrollTo({top: 0, behavior: "smooth"});
        });

        window.addEventListener("scroll", updateScrollToTopVisibility, {passive: true});
        updateScrollToTopVisibility();
    }

    function setEmptyState(message) {
        if (dom.emptyState) {
            dom.emptyState.textContent = message;
            dom.emptyState.classList.remove("d-none");
        }
        if (dom.searchResultTable) {
            dom.searchResultTable.classList.add("d-none");
        }
    }

    function setResultsVisible() {
        if (dom.emptyState) {
            dom.emptyState.classList.add("d-none");
        }
        if (dom.searchResultTable) {
            dom.searchResultTable.classList.remove("d-none");
        }
    }

    function hideEmptyState() {
        if (dom.emptyState) {
            dom.emptyState.classList.add("d-none");
        }
    }

    function setLoading(loading) {
        if (dom.searchBtn) {
            dom.searchBtn.disabled = loading;
        }
        if (dom.keywordInput) {
            dom.keywordInput.disabled = loading;
        }
        if (dom.clearBtn) {
            dom.clearBtn.disabled = loading;
        }
    }

    function setLoadingState(message) {
        if (!dom.resultCount) {
            return;
        }
        hideEmptyState();
        dom.resultCount.innerHTML = `
                <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                ${message}
            `;
    }

    function resetSearchState() {
        state.currentPage = 0;
        state.hasNext = false;
        state.totalCount = null;
        state.activeKeyword = "";
        state.isLoading = false;
    }

    function buildSearchUrl() {
        const params = new URLSearchParams();
        params.set("translationId", state.translationId);
        if (state.activeKeyword) {
            params.set("keyword", state.activeKeyword);
        }
        const queryString = params.toString();
        return `${window.location.pathname}?${queryString}`;
    }

    function updateUrl() {
        history.replaceState(null, "", buildSearchUrl());
    }

    function updateResultCount() {
        if (dom.resultCount && state.totalCount !== null && state.activeKeyword) {
            dom.resultCount.textContent = `"${state.activeKeyword}"에 대한 결과 ${state.totalCount}건`;
        }
    }

    function clearResults() {
        if (dom.searchResultBody) {
            dom.searchResultBody.innerHTML = "";
        }
        if (dom.resultCount) {
            dom.resultCount.textContent = "";
        }
    }

    function appendResults(items) {
        if (!dom.searchResultBody) {
            return;
        }
        items.forEach(item => {
            const row = document.createElement("tr");
            const highlightedText = item.text.replace(
                new RegExp(`(${state.activeKeyword})`, "gi"),
                "<span class=\"highlight-keyword\">$1</span>"
            );
            row.innerHTML = `
                  <td class="verse-link" style="cursor:pointer"
                      data-book-id="${item.bookId}"
                      data-book-order="${item.bookOrder}"
                      data-book-name="${item.bookName}"
                      data-chapter-id="${item.chapterId}"
                      data-chapter-number="${item.chapterNumber}"
                      data-verse-id="${item.verseId}"
                      data-verse-number="${item.verseNumber}">
                      ${highlightedText} (${item.bookName} ${item.chapterNumber}:${item.verseNumber})
                  </td>
                `;
            dom.searchResultBody.appendChild(row);
        });
    }

    async function fetchSearchPage(page) {
        if (!state.activeKeyword || state.isLoading || !state.hasNext) {
            return;
        }
        state.isLoading = true;
        if (page === 0) {
            setLoading(true);
            hideEmptyState();
            setLoadingState("검색 중...");
        }
        try {
            const response = await fetch(`/api/v1/bibles/translations/${state.translationId}/search?keyword=${encodeURIComponent(state.activeKeyword)}&page=${page}&size=${config.pageSize}`);
            if (!response.ok) {
                throw new Error("검색 실패");
            }
            const data = await response.json();
            if (page === 0) {
                state.totalCount = data.totalCount ?? null;
                if (state.totalCount === 0) {
                    setEmptyState("다른 검색어로 다시 시도해 보세요.");
                    state.hasNext = false;
                }
                updateResultCount();
            }

            if (!data.content || data.content.length === 0) {
                if (page === 0) {
                    setEmptyState("다른 검색어로 다시 시도해 보세요.");
                }
                state.hasNext = false;
                return;
            }

            setResultsVisible();
            appendResults(data.content);
            state.hasNext = data.hasNext === true;
        } catch (error) {
            if (page === 0) {
                if (dom.resultCount) {
                    dom.resultCount.textContent = "검색 중 오류가 발생했습니다.";
                }
                setEmptyState("잠시 후 다시 시도해 주세요.");
            }
            console.error(error);
        } finally {
            state.isLoading = false;
            if (page === 0) {
                setLoading(false);
            }
        }
    }

    async function startSearch(keyword) {
        const normalizedKeyword = keyword.trim();
        clearResults();
        resetSearchState();
        if (!normalizedKeyword) {
            setEmptyState("검색어를 입력하고 검색을 시작하세요.");
            updateUrl();
            return;
        }
        hideEmptyState();
        state.activeKeyword = normalizedKeyword;
        state.hasNext = true;
        state.currentPage = 0;
        updateUrl();
        await fetchSearchPage(0);
        maybeLoadNextPage();
    }

    function maybeLoadNextPage() {
        if (!state.hasNext || state.isLoading) {
            return;
        }
        const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - config.scrollLoadOffset;
        if (nearBottom) {
            state.currentPage += 1;
            fetchSearchPage(state.currentPage);
        }
    }

    function handleResultClick(event) {
        const td = event.target.closest(".verse-link");
        if (!td) {
            return;
        }
        const selectedBookOrder = parseInt(td.dataset.bookOrder, 10);
        const selectedChapterNumber = parseInt(td.dataset.chapterNumber, 10);
        BookStore.saveCurrentBook({
            bookOrder: selectedBookOrder,
            bookName: td.dataset.bookName,
        });
        ChapterStore.saveNumber(selectedChapterNumber);
        VerseStore.saveNumber(td.dataset.verseNumber);
        const targetUrl = new URL("/web/bible/verse", window.location.origin);
        targetUrl.searchParams.set("translationId", state.translationId);
        targetUrl.searchParams.set("bookOrder", selectedBookOrder);
        targetUrl.searchParams.set("chapterNumber", selectedChapterNumber);
        targetUrl.searchParams.set("verseNumber", td.dataset.verseNumber);
        targetUrl.searchParams.set("from", "search");
        window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
    }

    function getStoredTranslation() {
        return {
            id: TranslationStore.getCurrentTranslationId(),
            type: TranslationStore.getCurrentTranslationType(),
            name: TranslationStore.getCurrentTranslationName(),
            language: TranslationStore.getCurrentTranslationLanguage(),
        };
    }

    function hasCompleteTranslation(stored, targetId) {
        return stored.id === targetId && stored.type && stored.name && stored.language;
    }

    async function ensureTranslationInfo(targetId) {
        const stored = getStoredTranslation();
        if (hasCompleteTranslation(stored, targetId)) {
            return stored;
        }
        try {
            const response = await fetch("/api/v1/bibles/translations");
            if (!response.ok) {
                throw new Error("번역본 정보를 불러오는 중 오류가 발생했습니다.");
            }
            const translations = await response.json();
            const match = translations.find(item => item.translationId === targetId);
            if (match) {
                const translation = {
                    id: match.translationId,
                    name: match.translationName,
                    type: match.translationType,
                    language: match.translationLanguage,
                };
                TranslationStore.saveCurrentTranslation(translation);
                return translation;
            }
        } catch (error) {
            console.warn(error.message);
        }
        return stored;
    }

    function redirectToTranslation() {
        window.location.href = "/web/bible/translation";
    }

    init();
});
