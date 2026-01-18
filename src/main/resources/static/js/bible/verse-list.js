import {BookStore, ChapterStore, LastReadStore, TranslationStore, VerseStore} from "/js/storage-util.js?v=2.1";
import {buildLoginRedirectUrl, checkAuthStatus} from "/js/auth/auth-check.js";

const UI_CLASSES = {
    HIDDEN: "d-none"
};

const API_CONFIG = {
    TRANSLATIONS: "/api/v1/bibles/translations",
    MEMOS_BASE: "/api/v1/bibles/translations"
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
            nextBtn: get("nextChapterBtn")
        };
    }
};

const App = {
    elements: null,
    state: {
        translationId: null,
        translationType: null,
        bookOrder: null,
        bookName: null,
        chapterNumber: null,
        verseNumber: null,
        fromSearch: false
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
        App.state.fromSearch = urlParams.get("from") === "search";
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
            App.updateVerseUrl();
            App.renderChapter(data);
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

    renderChapter: data => {
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
        const verseEl = event.target.closest("[data-verse]");
        if (!verseEl) {
            return;
        }
        const verseNum = verseEl.getAttribute("data-verse");
        if (event.target.classList.contains("memo-save-btn")) {
            await App.saveMemo(verseNum);
            return;
        }
        if (event.target.classList.contains("memo-delete-btn")) {
            await App.deleteMemo(verseNum);
            return;
        }
        App.toggleMemo(verseNum);
        App.applyVerseHighlight(verseNum);
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

    toggleMemo: verseNum => {
        document.querySelectorAll(".memo-container").forEach(el => {
            const isTarget = el.id === `memo-${verseNum}`;
            if (isTarget) {
                el.classList.toggle("d-none");
            } else {
                el.classList.add("d-none");
            }
        });

        const textarea = document.getElementById(`memo-input-${verseNum}`);
        if (textarea) {
            const memo = App.memoCache.get(String(verseNum));
            textarea.value = memo ? memo.content : "";
        }
    },

    applyVerseHighlight: verseNum => {
        document.querySelectorAll(".verse-text").forEach(el => {
            const isTarget = el.id === `verse-text-${verseNum}`;
            if (isTarget) {
                el.classList.toggle("active");
            } else {
                el.classList.remove("active");
            }
        });
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
            App.toggleMemo(verseNum);
            App.applyVerseHighlight(verseNum);
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
            App.toggleMemo(verseNum);
            App.applyVerseHighlight(verseNum);
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
    }
};

document.addEventListener("DOMContentLoaded", App.init);
