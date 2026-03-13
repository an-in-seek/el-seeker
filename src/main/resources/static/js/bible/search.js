import {BookStore, ChapterStore, TranslationStore, VerseStore} from "/js/storage-util.js?v=2.3";
import {formatNumberWithComma} from "/js/common-util.js?v=2.2";

const UI_CLASSES = {
    HIDDEN: "d-none",
    VISIBLE: "is-visible"
};

const API_CONFIG = {
    TRANSLATIONS: "/api/v1/bibles/translations"
};

const ROUTES = {
    TRANSLATION_LIST: "/web/bible/translation",
    VERSE_LIST: "/web/bible/verse"
};

const CONFIG = {
    SCROLL_THRESHOLD: 300,
    PAGE_SIZE: 50,
    SCROLL_LOAD_OFFSET: 200
};

const DomHelper = {
    getElements: () => {
        const get = id => document.getElementById(id);
        return {
            backButton: get("topNavBackButton"),
            translationLink: get("topNavTranslationLink"),
            translationTypeLabel: get("translationTypeLabel"),
            pageTitleLabel: get("pageTitleLabel"),
            searchLink: get("topNavSearchLink"),
            keywordInput: get("keywordInput"),
            searchForm: get("searchForm"),
            clearBtn: get("clearBtn"),
            searchBtn: get("searchBtn"),
            resultCount: get("resultCount"),
            emptyState: get("searchEmptyState"),
            searchLoading: get("searchLoading"),
            searchLoadingMessage: get("searchLoadingMessage"),
            searchResultList: get("searchResultList"),
            scrollToTopBtn: get("scrollToTopBtn")
        };
    }
};

const App = {
    elements: null,
    state: {
        translationId: null,
        translationType: null,
        currentPage: 0,
        hasNext: false,
        isLoading: false,
        totalCount: null,
        activeKeyword: "",
        initialKeyword: ""
    },

    init: async () => {
        App.elements = DomHelper.getElements();
        App.state.translationId = App.getTranslationId();
        App.state.initialKeyword = App.getInitialKeyword();

        if (!App.state.translationId) {
            App.redirectToTranslation();
            return;
        }

        App.initNav();

        const translationInfo = await App.ensureTranslationInfo(App.state.translationId);
        App.state.translationType = translationInfo.type;
        App.updateTranslationTypeLabel();

        App.initFormControls();
        App.initResultHandlers();
        App.initScrollToTop();
        window.addEventListener("scroll", App.maybeLoadNextPage, {passive: true});

        if (App.state.initialKeyword) {
            if (App.elements.keywordInput) {
                App.elements.keywordInput.value = App.state.initialKeyword;
            }
            App.startSearch(App.state.initialKeyword);
        } else {
            App.updateUrl();
        }
    },

    getTranslationId: () => {
        const urlParams = new URLSearchParams(window.location.search);
        const translationIdParam = parseInt(urlParams.get("translationId"), 10);
        return Number.isNaN(translationIdParam)
            ? TranslationStore.getCurrentTranslationId()
            : translationIdParam;
    },

    getInitialKeyword: () => {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get("keyword") ?? "";
    },

    initNav: () => {
        const {backButton, translationLink, searchLink, pageTitleLabel} = App.elements;
        if (backButton) {
            backButton.classList.remove(UI_CLASSES.HIDDEN);
            backButton.addEventListener("click", () => {
                history.back();
            });
        }
        if (translationLink) {
            translationLink.classList.remove(UI_CLASSES.HIDDEN);
            translationLink.classList.add("nav-placeholder");
            translationLink.setAttribute("aria-hidden", "true");
            translationLink.setAttribute("tabindex", "-1");
        }
        if (searchLink) {
            searchLink.classList.remove(UI_CLASSES.HIDDEN);
            searchLink.classList.add("nav-placeholder");
        }
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 검색";
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }
    },

    updateTranslationTypeLabel: () => {
        const {translationTypeLabel} = App.elements;
        if (translationTypeLabel) {
            translationTypeLabel.textContent = App.state.translationType ?? "";
        }
    },

    initFormControls: () => {
        const {clearBtn, keywordInput, searchForm} = App.elements;
        if (clearBtn && keywordInput) {
            clearBtn.addEventListener("click", () => {
                keywordInput.value = "";
                keywordInput.focus();
                App.clearResults();
                App.setEmptyState("검색어를 입력하고 검색을 시작하세요.");
                App.resetSearchState();
                App.updateUrl();
            });

            keywordInput.addEventListener("input", () => {
                clearBtn.disabled = keywordInput.value.trim().length === 0;
            });
            keywordInput.focus();
            clearBtn.disabled = keywordInput.value.trim().length === 0;
        }

        if (searchForm && keywordInput) {
            searchForm.addEventListener("submit", async event => {
                event.preventDefault();
                await App.startSearch(keywordInput.value);
            });
        }
    },

    initResultHandlers: () => {
        const {searchResultList} = App.elements;
        if (searchResultList) {
            searchResultList.addEventListener("click", App.handleResultClick);
        }
    },

    initScrollToTop: () => {
        const {scrollToTopBtn} = App.elements;
        if (!scrollToTopBtn) {
            return;
        }

        scrollToTopBtn.addEventListener("click", () => {
            window.scrollTo({top: 0, behavior: "smooth"});
        });

        window.addEventListener("scroll", App.updateScrollToTopVisibility, {passive: true});
        App.updateScrollToTopVisibility();
    },

    updateScrollToTopVisibility: () => {
        const {scrollToTopBtn} = App.elements;
        if (!scrollToTopBtn) {
            return;
        }
        const shouldShow = window.scrollY >= CONFIG.SCROLL_THRESHOLD;
        scrollToTopBtn.classList.toggle(UI_CLASSES.VISIBLE, shouldShow);
    },

    setEmptyState: message => {
        const {emptyState, searchResultList} = App.elements;
        if (emptyState) {
            emptyState.textContent = message;
            emptyState.classList.remove(UI_CLASSES.HIDDEN);
        }
        App.hideLoading();
        if (searchResultList) {
            searchResultList.classList.add(UI_CLASSES.HIDDEN);
        }
    },

    setResultsVisible: () => {
        const {emptyState, searchResultList} = App.elements;
        if (emptyState) {
            emptyState.classList.add(UI_CLASSES.HIDDEN);
        }
        App.hideLoading();
        if (searchResultList) {
            searchResultList.classList.remove(UI_CLASSES.HIDDEN);
        }
    },

    hideEmptyState: () => {
        const {emptyState} = App.elements;
        if (emptyState) {
            emptyState.classList.add(UI_CLASSES.HIDDEN);
        }
    },

    showLoading: message => {
        const {searchLoading, searchLoadingMessage} = App.elements;
        if (searchLoading) {
            searchLoading.classList.remove(UI_CLASSES.HIDDEN);
            searchLoading.setAttribute("aria-busy", "true");
        }
        if (searchLoadingMessage) {
            searchLoadingMessage.textContent = message;
        }
    },

    hideLoading: () => {
        const {searchLoading} = App.elements;
        if (searchLoading) {
            searchLoading.classList.add(UI_CLASSES.HIDDEN);
            searchLoading.removeAttribute("aria-busy");
        }
    },

    setLoading: loading => {
        const {searchBtn, keywordInput, clearBtn} = App.elements;
        if (searchBtn) {
            searchBtn.disabled = loading;
        }
        if (keywordInput) {
            keywordInput.disabled = loading;
        }
        if (clearBtn) {
            clearBtn.disabled = loading;
        }
    },

    setLoadingState: message => {
        const {resultCount} = App.elements;
        if (!resultCount) {
            return;
        }
        App.hideEmptyState();
        App.showLoading(message);
        resultCount.textContent = message;
    },

    resetSearchState: () => {
        App.state.currentPage = 0;
        App.state.hasNext = false;
        App.state.totalCount = null;
        App.state.activeKeyword = "";
        App.state.isLoading = false;
    },

    buildSearchUrl: () => {
        const params = new URLSearchParams();
        params.set("translationId", App.state.translationId);
        if (App.state.activeKeyword) {
            params.set("keyword", App.state.activeKeyword);
        }
        const queryString = params.toString();
        return `${window.location.pathname}?${queryString}`;
    },

    updateUrl: () => {
        history.replaceState(null, "", App.buildSearchUrl());
    },

    updateResultCount: () => {
        const {resultCount} = App.elements;
        if (resultCount && App.state.totalCount !== null && App.state.activeKeyword) {
            resultCount.textContent = `"${App.state.activeKeyword}"에 대한 결과 ${formatNumberWithComma(App.state.totalCount)}건`;
        }
    },

    clearResults: () => {
        const {searchResultList, resultCount} = App.elements;
        if (searchResultList) {
            searchResultList.innerHTML = "";
        }
        if (resultCount) {
            resultCount.textContent = "";
        }
        App.hideLoading();
    },

    appendResults: items => {
        const {searchResultList} = App.elements;
        if (!searchResultList) {
            return;
        }
        items.forEach(item => {
            const resultItem = document.createElement("div");
            resultItem.className = "search-result-item verse-link";

            // 데이터 속성 설정 (클릭 이벤트용)
            resultItem.dataset.bookId = item.bookId;
            resultItem.dataset.bookOrder = item.bookOrder;
            resultItem.dataset.bookName = item.bookName;
            resultItem.dataset.chapterId = item.chapterId;
            resultItem.dataset.chapterNumber = item.chapterNumber;
            resultItem.dataset.verseId = item.verseId;
            resultItem.dataset.verseNumber = item.verseNumber;

            const highlightedText = item.text.replace(
                new RegExp(`(${App.state.activeKeyword})`, "gi"),
                "<span class=\"highlight-keyword\">$1</span>"
            );

            resultItem.innerHTML = `
                <div class="search-result-meta">
                    <span class="search-result-badge">${item.bookName} ${item.chapterNumber}:${item.verseNumber}</span>
                </div>
                <div class="search-result-text">
                    ${highlightedText}
                </div>
            `;
            searchResultList.appendChild(resultItem);
        });
    },

    fetchSearchPage: async page => {
        if (!App.state.activeKeyword || App.state.isLoading || !App.state.hasNext) {
            return;
        }
        App.state.isLoading = true;
        if (page === 0) {
            App.setLoading(true);
            App.hideEmptyState();
            App.setLoadingState("검색 중...");
        }
        try {
            const response = await fetch(`${API_CONFIG.TRANSLATIONS}/${App.state.translationId}/search?keyword=${encodeURIComponent(App.state.activeKeyword)}&page=${page}&size=${CONFIG.PAGE_SIZE}`);
            if (!response.ok) {
                throw new Error("검색 실패");
            }
            const data = await response.json();
            if (page === 0) {
                App.state.totalCount = data.totalCount ?? null;
                if (App.state.totalCount === 0) {
                    App.setEmptyState("다른 검색어로 다시 시도해 보세요.");
                    App.state.hasNext = false;
                }
                App.updateResultCount();
            }

            if (!data.content || data.content.length === 0) {
                if (page === 0) {
                    App.setEmptyState("다른 검색어로 다시 시도해 보세요.");
                }
                App.state.hasNext = false;
                return;
            }

            App.setResultsVisible();
            App.appendResults(data.content);
            App.state.hasNext = data.hasNext === true;
        } catch (error) {
            if (page === 0) {
                const {resultCount} = App.elements;
                if (resultCount) {
                    resultCount.textContent = "검색 중 오류가 발생했습니다.";
                }
                App.setEmptyState("잠시 후 다시 시도해 주세요.");
            }
            console.error(error);
        } finally {
            App.state.isLoading = false;
            if (page === 0) {
                App.setLoading(false);
                App.hideLoading();
            }
        }
    },

    startSearch: async keyword => {
        const normalizedKeyword = keyword.trim();
        App.clearResults();
        App.resetSearchState();
        if (!normalizedKeyword) {
            App.setEmptyState("검색어를 입력하고 검색을 시작하세요.");
            App.updateUrl();
            return;
        }
        App.hideEmptyState();
        App.state.activeKeyword = normalizedKeyword;
        App.state.hasNext = true;
        App.state.currentPage = 0;
        App.updateUrl();
        await App.fetchSearchPage(0);
        App.maybeLoadNextPage();
    },

    maybeLoadNextPage: () => {
        if (!App.state.hasNext || App.state.isLoading) {
            return;
        }
        const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - CONFIG.SCROLL_LOAD_OFFSET;
        if (nearBottom) {
            App.state.currentPage += 1;
            App.fetchSearchPage(App.state.currentPage);
        }
    },

    handleResultClick: event => {
        const td = event.target.closest(".verse-link");
        if (!td) {
            return;
        }
        const selectedBookOrder = parseInt(td.dataset.bookOrder, 10);
        const selectedChapterNumber = parseInt(td.dataset.chapterNumber, 10);
        BookStore.saveCurrentBook({
            bookOrder: selectedBookOrder,
            bookName: td.dataset.bookName
        });
        ChapterStore.saveNumber(selectedChapterNumber);
        VerseStore.saveNumber(td.dataset.verseNumber);
        const targetUrl = new URL(ROUTES.VERSE_LIST, window.location.origin);
        targetUrl.searchParams.set("translationId", App.state.translationId);
        targetUrl.searchParams.set("bookOrder", selectedBookOrder);
        targetUrl.searchParams.set("chapterNumber", selectedChapterNumber);
        targetUrl.searchParams.set("verseNumber", td.dataset.verseNumber);
        targetUrl.searchParams.set("from", "search");
        window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
    },

    getStoredTranslation: () => ({
        id: TranslationStore.getCurrentTranslationId(),
        type: TranslationStore.getCurrentTranslationType(),
        name: TranslationStore.getCurrentTranslationName(),
        language: TranslationStore.getCurrentTranslationLanguage()
    }),

    hasCompleteTranslation: (stored, targetId) =>
        stored.id === targetId && stored.type && stored.name && stored.language,

    ensureTranslationInfo: async targetId => {
        const stored = App.getStoredTranslation();
        if (App.hasCompleteTranslation(stored, targetId)) {
            return stored;
        }
        try {
            const response = await fetch(API_CONFIG.TRANSLATIONS);
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
                    language: match.translationLanguage
                };
                TranslationStore.saveCurrentTranslation(translation);
                return translation;
            }
        } catch (error) {
            console.warn(error.message);
        }
        return stored;
    },

    redirectToTranslation: () => {
        window.location.href = ROUTES.TRANSLATION_LIST;
    }
};

document.addEventListener("DOMContentLoaded", App.init);
