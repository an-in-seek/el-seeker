import {BookStore, ChapterStore, LastReadStore, TranslationStore, VerseStore} from "/js/storage-util.js?v=2.1";
import {buildLoginRedirectUrl, checkAuthStatus} from "/js/auth/auth-check.js";

const UI_CLASSES = {
    HIDDEN: "d-none"
};

const API_CONFIG = {
    TRANSLATIONS: "/api/v1/bibles/translations",
    MEMOS_BASE: "/api/v1/bibles/translations",
    HIGHLIGHTS_BASE: "/api/v1/bibles/translations"
};

const ROUTES = {
    TRANSLATION_LIST: "/web/bible/translation",
    BOOK_LIST: "/web/bible/book",
    CHAPTER_LIST: "/web/bible/chapter",
    VERSE_LIST: "/web/bible/verse"
};

const DomHelper = {
    getElements: () => {
        const get = id => document.getElementById(id);
        return {
            backButton: get("topNavBackButton"),
            translationLink: get("topNavTranslationLink"),
            searchLink: get("topNavSearchLink"),
            translationTypeLabel: get("translationTypeLabel"),
            pageTitleLabel: get("pageTitleLabel"),
            verseTable: get("verseTableBody"),
            prevBtn: get("prevChapterBtn"),
            chapterSelectLink: get("chapterSelectLink"),
            chapterSelectLinkLabel: get("chapterSelectLinkLabel"),
            nextBtn: get("nextChapterBtn"),
            fab: get("verseFab")
        };
    }
};

const HIGHLIGHT_COLORS = [
    {id: "yellow", label: "노랑", className: "verse-highlight-yellow"},
    {id: "green", label: "초록", className: "verse-highlight-green"},
    {id: "pink", label: "핑크", className: "verse-highlight-pink"}
];

const App = {
    elements: null,
    state: {
        translationId: null,
        translationType: null,
        translationName: null,
        bookOrder: null,
        bookName: null,
        chapterNumber: null,
        verseNumber: null,
        fromSearch: false,
        fromHome: false
    },
    selection: {
        selected: new Set(),
        menuOpen: false,
        highlightOpen: false,
        highlightMap: new Map()
    },
    highlightAuth: {
        checked: false,
        allowed: false,
        checking: false,
        redirected: false
    },
    memoAuth: {
        checked: false,
        allowed: false,
        checking: false,
        redirected: false
    },
    memoCache: new Map(),

    init: async () => {
        App.elements = DomHelper.getElements();
        App.initStateFromUrl();

        if (!App.state.translationId) {
            App.redirectToTranslation();
            return;
        }

        const translationInfo = await App.ensureTranslationInfo();
        App.state.translationType = translationInfo.type;
        App.state.translationName = translationInfo.name;

        if (!App.state.bookOrder) {
            App.redirectToBookList();
            return;
        }

        if (!App.state.chapterNumber) {
            App.redirectToChapterList();
            return;
        }

        const books = await App.ensureBookList();
        App.state.bookName = App.resolveBookName(books);
        if (!App.state.bookName) {
            App.redirectToBookList();
            return;
        }

        App.initNav();
        App.updateLabels();
        App.updateVerseUrl();
        App.saveLastRead();
        App.bindEvents();
        App.initFabMenu();

        await App.loadChapter("CURRENT");
    },

    initStateFromUrl: () => {
        const urlParams = new URLSearchParams(window.location.search);
        const parsedTranslationId = parseInt(urlParams.get("translationId"), 10);
        const parsedBookOrder = parseInt(urlParams.get("bookOrder"), 10);
        const parsedChapterNumber = parseInt(urlParams.get("chapterNumber"), 10);
        const parsedVerseNumber = parseInt(urlParams.get("verseNumber"), 10);
        const storedTranslationId = TranslationStore.getCurrentTranslationId();
        const storedBookOrder = BookStore.getCurrentBookOrder();
        const storedChapterNumber = ChapterStore.getNumber();
        const canUseStoredBookOrder = Number.isNaN(parsedTranslationId)
            || (storedTranslationId && parsedTranslationId === storedTranslationId);
        App.state.translationId = Number.isNaN(parsedTranslationId)
            ? storedTranslationId
            : parsedTranslationId;
        App.state.bookOrder = Number.isNaN(parsedBookOrder)
            ? (canUseStoredBookOrder ? storedBookOrder : null)
            : parsedBookOrder;
        let chapterNumber = Number.isNaN(parsedChapterNumber)
            ? storedChapterNumber
            : parsedChapterNumber;
        if (Number.isNaN(chapterNumber)) {
            chapterNumber = null;
        }
        if (Number.isNaN(parsedChapterNumber)
            && !Number.isNaN(parsedBookOrder)
            && storedBookOrder
            && parsedBookOrder !== storedBookOrder) {
            chapterNumber = null;
        }
        App.state.chapterNumber = chapterNumber;
        App.state.verseNumber = Number.isNaN(parsedVerseNumber) ? null : parsedVerseNumber;
        const fromValue = urlParams.get("from");
        App.state.fromSearch = fromValue === "search";
        App.state.fromHome = fromValue === "home";
    },

    initNav: () => {
        const {backButton, translationLink, searchLink, pageTitleLabel} = App.elements;
        App.setupBackButton(backButton);
        if (translationLink) {
            translationLink.classList.remove(UI_CLASSES.HIDDEN);
            translationLink.addEventListener("click", () => {
                TranslationStore.saveTranslationReturnPath(App.buildVerseUrl());
            });
        }
        if (searchLink) {
            searchLink.classList.remove(UI_CLASSES.HIDDEN);
        }
        if (pageTitleLabel) {
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }
    },

    setupBackButton: (button) => {
        if (!button) {
            return;
        }
        button.classList.remove(UI_CLASSES.HIDDEN);
        button.addEventListener("click", () => {
            if (App.state.fromSearch) {
                history.back();
                return;
            }
            if (App.state.fromHome) {
                window.location.href = "/";
                return;
            }
            window.location.href = App.state.translationId && App.state.bookOrder
                ? `${ROUTES.CHAPTER_LIST}?translationId=${App.state.translationId}&bookOrder=${App.state.bookOrder}`
                : ROUTES.TRANSLATION_LIST;
        });
    },

    bindEvents: () => {
        const {prevBtn, nextBtn, verseTable} = App.elements;
        if (prevBtn) {
            prevBtn.addEventListener("click", () => App.loadChapter("PREV"));
        }
        if (nextBtn) {
            nextBtn.addEventListener("click", () => App.loadChapter("NEXT"));
        }
        if (verseTable) {
            verseTable.addEventListener("click", App.handleVerseClick);
            verseTable.addEventListener("keydown", App.handleMemoInputAttempt);
            verseTable.addEventListener("beforeinput", App.handleMemoInputAttempt);
        }
        document.addEventListener("click", App.handleOutsideFabClick);
    },

    updateLabels: () => {
        const {translationTypeLabel, pageTitleLabel, chapterSelectLinkLabel, chapterSelectLink} = App.elements;
        if (translationTypeLabel) {
            translationTypeLabel.textContent = App.state.translationType;
        }
        if (pageTitleLabel) {
            pageTitleLabel.textContent = `${App.state.bookName} ${App.state.chapterNumber}`;
        }
        if (chapterSelectLinkLabel) {
            chapterSelectLinkLabel.textContent = `${App.state.bookName} ${App.state.chapterNumber}`;
        }
        if (chapterSelectLink) {
            chapterSelectLink.href = `${ROUTES.CHAPTER_LIST}?translationId=${App.state.translationId}&bookOrder=${App.state.bookOrder}`;
        }
    },

    getStoredTranslation: () => ({
        id: TranslationStore.getCurrentTranslationId(),
        type: TranslationStore.getCurrentTranslationType(),
        name: TranslationStore.getCurrentTranslationName(),
        language: TranslationStore.getCurrentTranslationLanguage()
    }),

    hasCompleteTranslation: (stored, targetId) =>
        stored.id === targetId && stored.type && stored.name && stored.language,

    ensureTranslationInfo: async () => {
        const stored = App.getStoredTranslation();
        if (App.hasCompleteTranslation(stored, App.state.translationId)) {
            return stored;
        }
        try {
            const response = await fetch(API_CONFIG.TRANSLATIONS);
            if (!response.ok) {
                throw new Error("번역본 정보를 불러오는 중 오류가 발생했습니다.");
            }
            const translations = await response.json();
            const match = translations.find(item => item.translationId === App.state.translationId);
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

    ensureBookList: async () => {
        const cached = BookStore.getListForTranslation(App.state.translationId);
        if (cached && cached.length > 0) {
            return cached;
        }
        try {
            const response = await fetch(`${API_CONFIG.TRANSLATIONS}/${App.state.translationId}/books`);
            if (!response.ok) {
                throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
            }
            const data = await response.json();
            BookStore.saveListForTranslation(App.state.translationId, data);
            return data;
        } catch (error) {
            console.warn(error.message);
        }
        return null;
    },

    resolveBookName: books => {
        let bookName = BookStore.getBookName(App.state.translationId, App.state.bookOrder);
        if (!bookName && books) {
            const currentBook = books.find(book => book.bookOrder === App.state.bookOrder);
            if (currentBook) {
                BookStore.saveCurrentBook(currentBook);
                bookName = currentBook.bookName;
            }
        }
        return bookName;
    },

    buildVerseUrl: () => {
        const targetUrl = new URL(ROUTES.VERSE_LIST, window.location.origin);
        targetUrl.searchParams.set("translationId", App.state.translationId);
        targetUrl.searchParams.set("bookOrder", App.state.bookOrder);
        targetUrl.searchParams.set("chapterNumber", App.state.chapterNumber);
        if (App.state.verseNumber) {
            targetUrl.searchParams.set("verseNumber", App.state.verseNumber);
        }
        return `${targetUrl.pathname}${targetUrl.search}`;
    },

    updateVerseUrl: () => {
        history.replaceState(null, "", App.buildVerseUrl());
    },

    saveLastRead: () => {
        LastReadStore.save({
            translationId: App.state.translationId,
            bookOrder: App.state.bookOrder,
            chapterNumber: App.state.chapterNumber
        });
    },

    loadChapter: async direction => {
        try {
            if (direction !== "CURRENT") {
                App.state.verseNumber = null;
            }
            const url = App.buildChapterUrl(direction);
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error("데이터 로딩 실패");
            }
            const data = await response.json();
            App.updateStateFromChapter(data);
            App.memoCache = await App.fetchMemosForChapter();
            const highlights = await App.fetchHighlightsForChapter();
            App.updateVerseUrl();
            App.renderChapter(data, highlights);
        } catch (error) {
            App.showAlert("장 정보를 불러오지 못했습니다.", "danger");
            console.error(error);
        }
    },

    buildChapterUrl: direction => {
        const base = `${API_CONFIG.TRANSLATIONS}/${App.state.translationId}/books/${App.state.bookOrder}/chapters/${App.state.chapterNumber}`;
        if (direction === "CURRENT") {
            return `${base}/verses`;
        }
        return `${base}/navigate?direction=${direction}`;
    },

    updateStateFromChapter: data => {
        App.state.bookOrder = data.book.bookOrder;
        App.state.bookName = data.book.bookName;
        App.state.chapterNumber = data.book.chapter.chapterNumber;
        BookStore.saveCurrentBook({
            bookOrder: App.state.bookOrder,
            bookName: App.state.bookName
        });
        ChapterStore.saveNumber(App.state.chapterNumber);
        App.saveLastRead();
    },

    renderChapter: (data, highlights) => {
        const chapter = data.book.chapter;
        App.updateLabels();
        if (App.elements.verseTable) {
            App.elements.verseTable.innerHTML = chapter.verses.map(App.renderVerseRow).join("");
        }
        if (App.elements.prevBtn) {
            App.elements.prevBtn.disabled = data.isFirst;
        }
        if (App.elements.nextBtn) {
            App.elements.nextBtn.disabled = data.isLast;
        }
        const verseNumber = App.state.verseNumber ?? VerseStore.consumeVerseNumber();
        if (verseNumber) {
            if (App.state.verseNumber) {
                App.state.verseNumber = null;
                VerseStore.consumeVerseNumber();
            }
            App.highlightVerse(verseNumber);
        } else {
            window.scrollTo(0, 0);
        }
        App.applyHighlights(highlights);
        App.resetSelectionState();
    },

    highlightVerse: verseNumber => {
        setTimeout(() => {
            const targetVerse = document.querySelector(`.verse-text[data-verse="${verseNumber}"]`);
            if (!targetVerse) {
                return;
            }
            targetVerse.classList.add("highlighted-verse");
            targetVerse.scrollIntoView({behavior: "smooth", block: "center"});
            setTimeout(() => {
                targetVerse.classList.remove("highlighted-verse");
            }, 5000);
        }, 100);
    },

    renderVerseRow: verse => {
        const v = verse.verseNumber;
        const memo = App.memoCache.get(String(v));
        const hasMemo = memo && memo.content;
        const verseClass = hasMemo ? "verse-text text-body verse-has-memo" : "verse-text text-body";
        return `
            <tr>
              <td>${v}</td>
              <td>
                <div class="${verseClass}" id="verse-text-${v}" data-verse="${v}">${verse.text}</div>
                <div class="memo-container d-none mt-3" id="memo-${v}">
                  <div class="form-group">
                    <textarea class="form-control mb-2" rows="3" placeholder="메모를 입력하세요..." id="memo-input-${v}"></textarea>
                    <div class="text-end">
                      <button class="btn btn-sm btn-primary memo-save-btn" data-verse="${v}">💾 저장</button>
                      <button class="btn btn-sm btn-danger memo-delete-btn" data-verse="${v}">🗑️ 삭제</button>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          `;
    },

    handleVerseClick: async event => {
        if (event.target.classList.contains("memo-save-btn")) {
            const verseNum = event.target.dataset.verse;
            await App.saveMemo(verseNum);
            return;
        }
        if (event.target.classList.contains("memo-delete-btn")) {
            const verseNum = event.target.dataset.verse;
            await App.deleteMemo(verseNum);
            return;
        }
        if (event.target.closest(".memo-container")) {
            return;
        }
        const verseEl = event.target.closest(".verse-text[data-verse]");
        if (!verseEl) {
            return;
        }
        const verseNum = verseEl.getAttribute("data-verse");
        App.toggleVerseSelection(verseNum);
    },

    handleMemoInputAttempt: event => {
        if (!App.isMemoInputTarget(event.target)) {
            return;
        }
        if (App.memoAuth.allowed) {
            return;
        }
        if (App.memoAuth.checked) {
            event.preventDefault();
            return;
        }
        if (App.memoAuth.checking) {
            event.preventDefault();
            return;
        }
        event.preventDefault();
        App.checkMemoAuth();
    },

    isMemoInputTarget: target => target
        && (target.matches("textarea[id^='memo-input-']") || target.matches("input[id^='memo-input-']")),

    checkMemoAuth: () => {
        App.memoAuth.checking = true;
        checkAuthStatus({
            onAuthenticated: () => {
                App.memoAuth.checked = true;
                App.memoAuth.allowed = true;
                App.memoAuth.checking = false;
            },
            onUnauthenticated: () => {
                App.memoAuth.checked = true;
                App.memoAuth.allowed = false;
                App.memoAuth.checking = false;
                if (App.memoAuth.redirected) {
                    return;
                }
                App.memoAuth.redirected = true;
                alert("메모 기능은 로그인 후 사용할 수 있습니다.");
                window.location.href = buildLoginRedirectUrl();
            },
            onError: () => {
                App.memoAuth.checked = true;
                App.memoAuth.allowed = false;
                App.memoAuth.checking = false;
            }
        });
    },

    checkHighlightAuth: () => {
        App.highlightAuth.checking = true;
        checkAuthStatus({
            onAuthenticated: () => {
                App.highlightAuth.checked = true;
                App.highlightAuth.allowed = true;
                App.highlightAuth.checking = false;
            },
            onUnauthenticated: () => {
                App.highlightAuth.checked = true;
                App.highlightAuth.allowed = false;
                App.highlightAuth.checking = false;
                if (App.highlightAuth.redirected) {
                    return;
                }
                App.highlightAuth.redirected = true;
                alert("형광펜 기능은 로그인 후 사용할 수 있습니다.");
                window.location.href = buildLoginRedirectUrl();
            },
            onError: () => {
                App.highlightAuth.checked = true;
                App.highlightAuth.allowed = false;
                App.highlightAuth.checking = false;
            }
        });
    },

    showMemo: verseNum => {
        const memoContainer = document.getElementById(`memo-${verseNum}`);
        if (!memoContainer) {
            return;
        }
        memoContainer.classList.remove("d-none");
        const textarea = document.getElementById(`memo-input-${verseNum}`);
        if (textarea) {
            const memo = App.memoCache.get(String(verseNum));
            textarea.value = memo ? memo.content : "";
        }
    },

    hideMemo: verseNum => {
        const memoContainer = document.getElementById(`memo-${verseNum}`);
        if (memoContainer) {
            memoContainer.classList.add("d-none");
        }
    },

    saveMemo: async verseNum => {
        if (!App.memoAuth.allowed) {
            App.checkMemoAuth();
            return;
        }
        const textarea = document.getElementById(`memo-input-${verseNum}`);
        if (!textarea) {
            App.showAlert("메모 입력란을 찾을 수 없습니다", "danger");
            return;
        }
        const value = textarea.value.trim();
        if (!value) {
            return;
        }
        try {
            const response = await fetch(App.buildMemoUrl(verseNum), {
                method: "PUT",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json"
                },
                body: JSON.stringify({
                    content: value
                })
            });
            if (response.status === 401) {
                App.checkMemoAuth();
                return;
            }
            if (!response.ok) {
                throw new Error("메모 저장 실패");
            }
            const memo = await response.json();
            App.memoCache.set(String(verseNum), memo);
            const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
            if (verseTextEl) {
                verseTextEl.classList.add("verse-has-memo");
            }
            App.hideMemo(verseNum);
        } catch (error) {
            App.showAlert("메모 저장 중 오류가 발생했습니다.", "danger");
            console.error(error);
        }
    },

    deleteMemo: async verseNum => {
        if (!App.memoAuth.allowed) {
            App.checkMemoAuth();
            return;
        }
        try {
            const response = await fetch(App.buildMemoUrl(verseNum), {
                method: "DELETE",
                credentials: "include"
            });
            if (response.status === 401) {
                App.checkMemoAuth();
                return;
            }
            if (!response.ok) {
                throw new Error("메모 삭제 실패");
            }
            App.memoCache.delete(String(verseNum));
            const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
            if (verseTextEl) {
                verseTextEl.classList.remove("verse-has-memo");
            }
            App.hideMemo(verseNum);
        } catch (error) {
            App.showAlert("메모 삭제 중 오류가 발생했습니다.", "danger");
            console.error(error);
        }
    },

    fetchMemosForChapter: async () => {
        const url = new URL(App.buildChapterMemoUrl(), window.location.origin);
        try {
            const response = await fetch(url, {
                method: "GET",
                credentials: "include",
                headers: {
                    Accept: "application/json"
                }
            });
            if (response.status === 401) {
                App.memoAuth.checked = false;
                App.memoAuth.allowed = false;
                return new Map();
            }
            if (!response.ok) {
                throw new Error("메모 조회 실패");
            }
            const data = await response.json();
            App.memoAuth.checked = true;
            App.memoAuth.allowed = true;
            return new Map(data.map(item => [String(item.verseNumber), item]));
        } catch (error) {
            console.warn(error.message);
            return new Map();
        }
    },

    buildChapterMemoUrl: () =>
        `${API_CONFIG.MEMOS_BASE}/${App.state.translationId}/books/${App.state.bookOrder}/chapters/${App.state.chapterNumber}/memos`,

    buildMemoUrl: verseNum =>
        `${API_CONFIG.MEMOS_BASE}/${App.state.translationId}/books/${App.state.bookOrder}/chapters/${App.state.chapterNumber}/verses/${parseInt(verseNum, 10)}/memo`,

    buildChapterHighlightUrl: () =>
        `${API_CONFIG.HIGHLIGHTS_BASE}/${App.state.translationId}/books/${App.state.bookOrder}/chapters/${App.state.chapterNumber}/highlights`,

    buildHighlightUrl: verseNum =>
        `${API_CONFIG.HIGHLIGHTS_BASE}/${App.state.translationId}/books/${App.state.bookOrder}/chapters/${App.state.chapterNumber}/verses/${parseInt(verseNum, 10)}/highlight`,

    showAlert: (message, type = "success") => {
        alert(`${type}: ` + message);
    },

    redirectToTranslation: () => {
        window.location.href = ROUTES.TRANSLATION_LIST;
    },

    redirectToBookList: () => {
        const bookUrl = new URL(ROUTES.BOOK_LIST, window.location.origin);
        bookUrl.searchParams.set("translationId", App.state.translationId);
        window.location.href = `${bookUrl.pathname}${bookUrl.search}`;
    },

    redirectToChapterList: () => {
        const chapterUrl = new URL(ROUTES.CHAPTER_LIST, window.location.origin);
        chapterUrl.searchParams.set("translationId", App.state.translationId);
        chapterUrl.searchParams.set("bookOrder", App.state.bookOrder);
        window.location.href = `${chapterUrl.pathname}${chapterUrl.search}`;
    },

    initFabMenu: () => {
        const fab = App.elements.fab;
        if (!fab) {
            return;
        }
        const toggle = fab.querySelector("[data-fab-toggle]");
        const menu = fab.querySelector("[data-fab-menu]");
        const highlightMenu = fab.querySelector("[data-fab-highlight]");
        if (toggle) {
            toggle.addEventListener("click", () => App.toggleFabMenu());
        }
        if (menu) {
            menu.addEventListener("click", App.handleFabMenuClick);
        }
        if (highlightMenu) {
            highlightMenu.addEventListener("click", App.handleHighlightPick);
        }
        fab.addEventListener("click", App.handleFabBackdropClick);
        App.updateFabVisibility();
    },

    handleFabBackdropClick: event => {
        if (event.target === App.elements.fab) {
            App.closeFabMenu();
        }
    },

    handleOutsideFabClick: event => {
        const fab = App.elements?.fab;
        if (!fab || fab.classList.contains("d-none")) {
            return;
        }
        if (!event.target.closest("#verseFab")) {
            App.closeFabMenu();
        }
    },

    toggleVerseSelection: verseNum => {
        const number = String(verseNum);
        const verseEl = document.querySelector(`.verse-text[data-verse="${number}"]`);
        if (!verseEl) {
            return;
        }
        if (App.selection.selected.has(number)) {
            App.selection.selected.delete(number);
            verseEl.classList.remove("active");
        } else {
            App.selection.selected.add(number);
            verseEl.classList.add("active");
        }
        App.updateFabVisibility();
    },

    resetSelectionState: () => {
        App.selection.selected.clear();
        App.selection.menuOpen = false;
        App.selection.highlightOpen = false;
        document.querySelectorAll(".verse-text.active").forEach(el => el.classList.remove("active"));
        App.updateFabVisibility();
        App.closeFabMenu();
    },

    updateFabVisibility: () => {
        const fab = App.elements?.fab;
        if (!fab) {
            return;
        }
        if (App.selection.selected.size > 0) {
            fab.classList.remove(UI_CLASSES.HIDDEN);
        } else {
            fab.classList.add(UI_CLASSES.HIDDEN);
        }
    },

    toggleFabMenu: () => {
        App.selection.menuOpen = !App.selection.menuOpen;
        const fab = App.elements?.fab;
        if (!fab) {
            return;
        }
        fab.classList.toggle("is-open", App.selection.menuOpen);
        const toggle = fab.querySelector("[data-fab-toggle]");
        const menu = fab.querySelector("[data-fab-menu]");
        if (toggle) {
            toggle.setAttribute("aria-expanded", String(App.selection.menuOpen));
        }
        if (menu) {
            menu.setAttribute("aria-hidden", String(!App.selection.menuOpen));
        }
        if (!App.selection.menuOpen) {
            App.closeHighlightMenu();
        }
    },

    closeFabMenu: () => {
        if (!App.selection.menuOpen) {
            return;
        }
        App.selection.menuOpen = false;
        const fab = App.elements?.fab;
        if (fab) {
            fab.classList.remove("is-open");
            const toggle = fab.querySelector("[data-fab-toggle]");
            const menu = fab.querySelector("[data-fab-menu]");
            if (toggle) {
                toggle.setAttribute("aria-expanded", "false");
            }
            if (menu) {
                menu.setAttribute("aria-hidden", "true");
            }
        }
        App.closeHighlightMenu();
    },

    handleFabMenuClick: event => {
        const actionButton = event.target.closest("[data-action]");
        if (!actionButton) {
            return;
        }
        const action = actionButton.dataset.action;
        switch (action) {
            case "copy":
                App.copySelectedVerses();
                App.closeFabMenu();
                break;
            case "highlight":
                App.toggleHighlightMenu();
                break;
            case "memo":
                App.openMemoForSelected();
                App.closeFabMenu();
                break;
            case "share":
                App.shareSelectedVerses();
                App.closeFabMenu();
                break;
            default:
                break;
        }
    },

    toggleHighlightMenu: () => {
        App.selection.highlightOpen = !App.selection.highlightOpen;
        const menu = App.elements?.fab?.querySelector("[data-fab-highlight]");
        if (menu) {
            menu.setAttribute("aria-hidden", String(!App.selection.highlightOpen));
            menu.classList.toggle("is-open", App.selection.highlightOpen);
        }
    },

    closeHighlightMenu: () => {
        App.selection.highlightOpen = false;
        const menu = App.elements?.fab?.querySelector("[data-fab-highlight]");
        if (menu) {
            menu.setAttribute("aria-hidden", "true");
            menu.classList.remove("is-open");
        }
    },

    handleHighlightPick: async event => {
        const colorButton = event.target.closest("[data-highlight]");
        if (!colorButton) {
            return;
        }
        const colorId = colorButton.dataset.highlight;
        await App.applyHighlightToSelection(colorId);
        App.closeHighlightMenu();
        App.closeFabMenu();
    },

    applyHighlightToSelection: async colorId => {
        if (!App.highlightAuth.allowed) {
            App.checkHighlightAuth();
            return;
        }
        const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === colorId);
        if (!colorConfig) {
            return;
        }
        const verseNumbers = App.getSelectedVerseNumbers();
        if (verseNumbers.length === 0) {
            return;
        }
        await Promise.all(verseNumbers.map(async verseNum => {
            const current = App.selection.highlightMap.get(String(verseNum));
            if (current && current.id === colorConfig.id) {
                await App.deleteHighlight(verseNum);
            } else {
                await App.upsertHighlight(verseNum, colorConfig.id);
            }
        }));
    },

    openMemoForSelected: () => {
        if (!App.memoAuth.allowed) {
            App.checkMemoAuth();
            return;
        }
        const verseNumbers = App.getSelectedVerseNumbers();
        verseNumbers.forEach(verseNum => App.showMemo(verseNum));
        if (verseNumbers.length > 0) {
            const firstTextarea = document.getElementById(`memo-input-${verseNumbers[0]}`);
            if (firstTextarea) {
                firstTextarea.focus();
            }
        }
    },

    fetchHighlightsForChapter: async () => {
        const url = new URL(App.buildChapterHighlightUrl(), window.location.origin);
        try {
            const response = await fetch(url, {
                method: "GET",
                credentials: "include",
                headers: {
                    Accept: "application/json"
                }
            });
            if (response.status === 401) {
                App.highlightAuth.checked = false;
                App.highlightAuth.allowed = false;
                return [];
            }
            if (!response.ok) {
                throw new Error("형광펜 조회 실패");
            }
            const data = await response.json();
            App.highlightAuth.checked = true;
            App.highlightAuth.allowed = true;
            return data;
        } catch (error) {
            console.warn(error.message);
            return [];
        }
    },

    applyHighlights: highlights => {
        const colorClasses = HIGHLIGHT_COLORS.map(color => color.className);
        document.querySelectorAll(".verse-text").forEach(el => {
            colorClasses.forEach(className => el.classList.remove(className));
        });
        App.selection.highlightMap.clear();
        if (!highlights || highlights.length === 0) {
            return;
        }
        highlights.forEach(item => {
            const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === item.color);
            if (!colorConfig) {
                return;
            }
            App.setHighlightFromServer(item.verseNumber, colorConfig);
        });
    },

    setHighlightFromServer: (verseNum, colorConfig) => {
        const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (!verseEl) {
            return;
        }
        verseEl.classList.add(colorConfig.className);
        App.selection.highlightMap.set(String(verseNum), colorConfig);
    },

    upsertHighlight: async (verseNum, colorId) => {
        const response = await fetch(App.buildHighlightUrl(verseNum), {
            method: "PUT",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json"
            },
            body: JSON.stringify({color: colorId})
        });
        if (response.status === 401) {
            App.checkHighlightAuth();
            return;
        }
        if (!response.ok) {
            App.showAlert("형광펜 저장에 실패했습니다.", "danger");
            return;
        }
        const highlight = await response.json();
        const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === highlight.color);
        if (!colorConfig) {
            return;
        }
        App.setHighlightFromServer(highlight.verseNumber, colorConfig);
    },

    deleteHighlight: async verseNum => {
        const response = await fetch(App.buildHighlightUrl(verseNum), {
            method: "DELETE",
            credentials: "include"
        });
        if (response.status === 401) {
            App.checkHighlightAuth();
            return;
        }
        if (!response.ok) {
            App.showAlert("형광펜 삭제에 실패했습니다.", "danger");
            return;
        }
        const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (verseEl) {
            const current = App.selection.highlightMap.get(String(verseNum));
            if (current) {
                verseEl.classList.remove(current.className);
            }
        }
        App.selection.highlightMap.delete(String(verseNum));
    },

    getSelectedVerseNumbers: () => {
        return Array.from(App.selection.selected)
            .map(Number)
            .sort((a, b) => a - b)
            .map(String);
    },

    buildSelectedText: () => {
        const verseNumbers = App.getSelectedVerseNumbers();
        const translationLabel = App.state.translationType || App.state.translationName || "";
        const header = `${translationLabel} ${App.state.bookName} ${App.state.chapterNumber}장`.trim();
        const lines = verseNumbers.map(verseNum => {
            const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
            const text = verseEl ? verseEl.textContent.trim() : "";
            return `${verseNum} ${text}`.trim();
        });
        return [header, ...lines].filter(Boolean).join("\n");
    },

    copySelectedVerses: async () => {
        const text = App.buildSelectedText();
        if (!text) {
            return;
        }
        try {
            if (navigator.clipboard?.writeText) {
                await navigator.clipboard.writeText(text);
            } else {
                App.fallbackCopy(text);
            }
        } catch (error) {
            App.fallbackCopy(text);
            App.showAlert("복사에 실패했습니다.", "danger");
        }
    },

    shareSelectedVerses: async () => {
        const text = App.buildSelectedText();
        if (!text) {
            return;
        }
        if (navigator.share) {
            try {
                await navigator.share({
                    title: "성경 구절 공유",
                    text
                });
                return;
            } catch (error) {
                // ignore and fallback
            }
        }
        App.copySelectedVerses();
    },

    fallbackCopy: text => {
        const textarea = document.createElement("textarea");
        textarea.value = text;
        textarea.style.position = "fixed";
        textarea.style.left = "-9999px";
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
    }
};

document.addEventListener("DOMContentLoaded", App.init);
