document.addEventListener("DOMContentLoaded", () => {
    const dom = {
        backButton: document.getElementById("topNavBackButton"),
        pageTitleLabel: document.getElementById("pageTitleLabel"),
        keywordInput: document.getElementById("dictionaryKeywordInput"),
        searchForm: document.getElementById("dictionarySearchForm"),
        searchBtn: document.getElementById("dictionarySearchBtn"),
        clearBtn: document.getElementById("dictionaryClearBtn"),
        resultCount: document.getElementById("dictionaryResultCount"),
        emptyState: document.getElementById("dictionaryEmptyState"),
        listContainer: document.getElementById("dictionaryList"),
        scrollToTopBtn: document.getElementById("scrollToTopBtn"),
    };

    const config = {
        scrollThreshold: 300,
        scrollLoadOffset: 200,
        pageSize: 50,
    };

    const urlParams = new URLSearchParams(window.location.search);
    const initialKeyword = urlParams.get("keyword") ?? "";

    const state = {
        currentPage: 0,
        hasNext: false,
        isLoading: false,
        activeKeyword: "",
        totalCount: null,
    };

    const init = () => {
        updatePageTitle();
        initBackButton();
        initFormControls();
        initScrollToTop();

        window.addEventListener("scroll", maybeLoadNextPage, {passive: true});

        if (dom.keywordInput) {
            dom.keywordInput.value = initialKeyword;
        }
        startSearch(initialKeyword);
    };

    const updatePageTitle = () => {
        if (!dom.pageTitleLabel) {
            return;
        }
        dom.pageTitleLabel.textContent = "성경 사전";
        dom.pageTitleLabel.classList.remove("d-none");
    };

    const initBackButton = () => {
        if (!dom.backButton) {
            return;
        }
        dom.backButton.classList.remove("d-none");
        dom.backButton.addEventListener("click", () => {
            window.location.href = "/web/study";
        });
    };

    const initFormControls = () => {
        if (dom.keywordInput) {
            dom.keywordInput.focus();
            dom.keywordInput.addEventListener("input", () => {
                if (dom.clearBtn) {
                    dom.clearBtn.disabled = dom.keywordInput.value.trim().length === 0;
                }
            });
        }
        if (dom.clearBtn) {
            dom.clearBtn.disabled = dom.keywordInput?.value.trim().length === 0;
            dom.clearBtn.addEventListener("click", () => {
                if (dom.keywordInput) {
                    dom.keywordInput.value = "";
                    dom.keywordInput.focus();
                }
                clearResults();
                startSearch("");
            });
        }
        if (dom.searchForm) {
            dom.searchForm.addEventListener("submit", event => {
                event.preventDefault();
                const keyword = dom.keywordInput?.value ?? "";
                startSearch(keyword);
            });
        }
    };

    const initScrollToTop = () => {
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
    };

    const setEmptyState = (message, detail) => {
        if (dom.emptyState) {
            dom.emptyState.innerHTML = "";
            const title = document.createElement("p");
            title.className = detail ? "mb-1" : "mb-0";
            title.textContent = message;
            dom.emptyState.appendChild(title);
            if (detail) {
                const body = document.createElement("p");
                body.className = "small mb-0";
                body.textContent = detail;
                dom.emptyState.appendChild(body);
            }
            dom.emptyState.classList.remove("d-none");
        }
        if (dom.listContainer) {
            dom.listContainer.classList.add("d-none");
        }
    };

    const setResultsVisible = () => {
        if (dom.emptyState) {
            dom.emptyState.classList.add("d-none");
        }
        if (dom.listContainer) {
            dom.listContainer.classList.remove("d-none");
        }
    };

    const hideEmptyState = () => {
        if (dom.emptyState) {
            dom.emptyState.classList.add("d-none");
        }
    };

    const setLoading = loading => {
        if (dom.searchBtn) {
            dom.searchBtn.disabled = loading;
        }
        if (dom.keywordInput) {
            dom.keywordInput.disabled = loading;
        }
        if (dom.clearBtn) {
            dom.clearBtn.disabled = loading;
        }
    };

    const setLoadingState = message => {
        if (!dom.resultCount) {
            return;
        }
        hideEmptyState();
        dom.resultCount.innerHTML = `
                <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                ${message}
            `;
    };

    const resetSearchState = () => {
        state.currentPage = 0;
        state.hasNext = false;
        state.isLoading = false;
        state.activeKeyword = "";
        state.totalCount = null;
    };

    const updateResultCount = () => {
        if (!dom.resultCount) {
            return;
        }
        if (state.totalCount === null) {
            dom.resultCount.textContent = "";
            return;
        }
        if (state.activeKeyword) {
            dom.resultCount.textContent = `"${state.activeKeyword}"에 대한 결과 ${state.totalCount}건`;
        } else {
            dom.resultCount.textContent = `성경 사전 ${state.totalCount}건`;
        }
    };

    const createSummary = description => {
        const normalized = (description ?? "").trim();
        if (!normalized) {
            return "설명이 등록되지 않았습니다.";
        }
        if (normalized.length <= 120) {
            return normalized;
        }
        return `${normalized.substring(0, 120).trimEnd()}...`;
    };

    const buildDetailLink = id => {
        const params = new URLSearchParams();
        if (state.activeKeyword) {
            params.set("keyword", state.activeKeyword);
        }
        const queryString = params.toString();
        return queryString ? `/web/study/dictionary/${id}?${queryString}` : `/web/study/dictionary/${id}`;
    };

    const appendResults = items => {
        if (!dom.listContainer) {
            return;
        }
        items.forEach(item => {
            const link = document.createElement("a");
            link.className = "list-group-item list-group-item-action";
            link.href = buildDetailLink(item.id);

            const header = document.createElement("div");
            header.className = "d-flex justify-content-between align-items-center";

            const title = document.createElement("h2");
            title.className = "h6 fw-semibold mb-1";
            title.textContent = item.term ?? "";

            const badge = document.createElement("span");
            badge.className = "badge bg-light text-secondary";
            badge.textContent = "보기";

            header.appendChild(title);
            header.appendChild(badge);

            const summary = document.createElement("p");
            summary.className = "text-muted small mb-0";
            summary.textContent = createSummary(item.description);

            link.appendChild(header);
            link.appendChild(summary);
            dom.listContainer.appendChild(link);
        });
    };

    const updateUrl = () => {
        const params = new URLSearchParams();
        if (state.activeKeyword) {
            params.set("keyword", state.activeKeyword);
        }
        const queryString = params.toString();
        const newUrl = queryString ? `${window.location.pathname}?${queryString}` : window.location.pathname;
        history.replaceState(null, "", newUrl);
    };

    const fetchDictionaryPage = async page => {
        if (state.isLoading || !state.hasNext) {
            return;
        }
        state.isLoading = true;
        if (page === 0) {
            setLoading(true);
            setLoadingState("검색 중...");
        }
        try {
            const url = new URL("/api/v1/study/dictionaries", window.location.origin);
            if (state.activeKeyword) {
                url.searchParams.set("keyword", state.activeKeyword);
            }
            url.searchParams.set("page", String(page));
            url.searchParams.set("size", String(config.pageSize));
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error("사전 조회 실패");
            }
            const data = await response.json();
            const items = Array.isArray(data.content) ? data.content : [];
            if (page === 0) {
                if (typeof data.totalCount === "number") {
                    state.totalCount = data.totalCount;
                }
                if (items.length === 0) {
                    if (state.activeKeyword) {
                        setEmptyState("다른 용어로 다시 시도해 보세요.");
                    } else {
                        setEmptyState("등록된 사전 항목이 없습니다.");
                    }
                    updateResultCount();
                    state.hasNext = false;
                    return;
                }
            }
            setResultsVisible();
            appendResults(items);
            const sliceHasNext = data.hasNext === true;
            const pageHasNext = typeof data.last === "boolean" ? !data.last : false;
            state.hasNext = sliceHasNext || pageHasNext || items.length === config.pageSize;
            updateResultCount();
        } catch (error) {
            console.error(error);
            if (page === 0) {
                if (dom.resultCount) {
                    dom.resultCount.textContent = "검색 중 오류가 발생했습니다.";
                }
                setEmptyState("잠시 후 다시 시도해 주세요.");
            }
        } finally {
            state.isLoading = false;
            if (page === 0) {
                setLoading(false);
            }
        }
    };

    const maybeLoadNextPage = () => {
        if (!state.hasNext || state.isLoading) {
            return;
        }
        const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - config.scrollLoadOffset;
        if (nearBottom) {
            state.currentPage += 1;
            fetchDictionaryPage(state.currentPage);
        }
    };

    const normalizeKeyword = keyword => (keyword ?? "").trim();

    const clearResults = () => {
        if (dom.listContainer) {
            dom.listContainer.innerHTML = "";
        }
        if (dom.resultCount) {
            dom.resultCount.textContent = "";
        }
    };

    const startSearch = async keyword => {
        clearResults();
        resetSearchState();
        state.activeKeyword = normalizeKeyword(keyword);
        state.hasNext = true;
        state.currentPage = 0;
        updateUrl();
        setLoadingState("목록을 불러오는 중입니다.");
        await fetchDictionaryPage(0);
        maybeLoadNextPage();
    };

    init();
});
