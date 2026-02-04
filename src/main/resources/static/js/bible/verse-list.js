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

const HIGHLIGHT_COLORS = [
    {id: "yellow", label: "노랑", className: "verse-highlight-yellow"},
    {id: "green", label: "초록", className: "verse-highlight-green"},
    {id: "pink", label: "핑크", className: "verse-highlight-pink"}
];

const state = {
    translationId: null,
    translationType: null,
    translationName: null,
    bookOrder: null,
    bookName: null,
    chapterNumber: null,
    verseNumber: null,
    fromSearch: false,
    fromHome: false
};

const selection = {
    selected: new Set(),
    menuOpen: false,
    highlightMap: new Map()
};

const memoState = {
    auth: createAuthState("메모 기능은 로그인 후 사용할 수 있습니다."),
    cache: new Map()
};

const highlightState = {
    auth: createAuthState("형광펜 기능은 로그인 후 사용할 수 있습니다.")
};

let elements = null;
let isAuthenticated = false;

function createAuthState(message) {
    return {
        checked: false,
        allowed: false,
        checking: false,
        redirected: false,
        message
    };
}

function getElements() {
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

function parseIntParam(params, key) {
    const value = parseInt(params.get(key), 10);
    return Number.isNaN(value) ? null : value;
}

function resolveInitialState() {
    const params = new URLSearchParams(window.location.search);
    const parsedTranslationId = parseIntParam(params, "translationId");
    const parsedBookOrder = parseIntParam(params, "bookOrder");
    const parsedChapterNumber = parseIntParam(params, "chapterNumber");
    const parsedVerseNumber = parseIntParam(params, "verseNumber");

    const storedTranslationId = TranslationStore.getCurrentTranslationId();
    const storedBookOrder = BookStore.getCurrentBookOrder();
    const storedChapterNumber = ChapterStore.getNumber();

    const canUseStoredBookOrder = parsedTranslationId === null
        || (storedTranslationId && parsedTranslationId === storedTranslationId);

    state.translationId = parsedTranslationId ?? storedTranslationId ?? null;
    state.bookOrder = parsedBookOrder ?? (canUseStoredBookOrder ? storedBookOrder : null) ?? null;

    let chapterNumber = parsedChapterNumber ?? storedChapterNumber ?? null;
    if (parsedChapterNumber === null
        && parsedBookOrder !== null
        && storedBookOrder
        && parsedBookOrder !== storedBookOrder) {
        chapterNumber = null;
    }

    state.chapterNumber = chapterNumber;
    state.verseNumber = parsedVerseNumber;

    const fromValue = params.get("from");
    state.fromSearch = fromValue === "search";
    state.fromHome = fromValue === "home";
}

async function init() {
    elements = getElements();
    resolveInitialState();

    if (!state.translationId) {
        redirectToTranslation();
        return;
    }

    const translationInfo = await ensureTranslationInfo();
    state.translationType = translationInfo.type;
    state.translationName = translationInfo.name;

    if (!state.bookOrder) {
        redirectToBookList();
        return;
    }

    if (!state.chapterNumber) {
        redirectToChapterList();
        return;
    }

    const books = await ensureBookList();
    state.bookName = resolveBookName(books);
    if (!state.bookName) {
        redirectToBookList();
        return;
    }

    initNav();
    updateLabels();
    updateVerseUrl();
    saveLastRead();
    bindEvents();
    initFabMenu();

    await initAuthStatus();
    await loadChapter("CURRENT");
}

async function initAuthStatus() {
    return new Promise(resolve => {
        checkAuthStatus({
            onAuthenticated: () => {
                isAuthenticated = true;
                setAuthState(memoState.auth, true);
                setAuthState(highlightState.auth, true);
                resolve();
            },
            onUnauthenticated: () => {
                isAuthenticated = false;
                setAuthState(memoState.auth, false);
                setAuthState(highlightState.auth, false);
                resolve();
            },
            onError: () => {
                isAuthenticated = false;
                setAuthState(memoState.auth, false);
                setAuthState(highlightState.auth, false);
                resolve();
            }
        });
    });
}

function initNav() {
    const {backButton, translationLink, searchLink, pageTitleLabel} = elements;
    setupBackButton(backButton);
    if (translationLink) {
        translationLink.classList.remove(UI_CLASSES.HIDDEN);
        translationLink.addEventListener("click", () => {
            TranslationStore.saveTranslationReturnPath(buildVerseUrl());
        });
    }
    if (searchLink) {
        searchLink.classList.remove(UI_CLASSES.HIDDEN);
    }
    if (pageTitleLabel) {
        pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
    }
}

function setupBackButton(button) {
    if (!button) {
        return;
    }
    button.classList.remove(UI_CLASSES.HIDDEN);
    button.addEventListener("click", () => {
        if (state.fromSearch) {
            history.back();
            return;
        }
        if (state.fromHome) {
            window.location.href = "/";
            return;
        }
        window.location.href = state.translationId && state.bookOrder
            ? `${ROUTES.CHAPTER_LIST}?translationId=${state.translationId}&bookOrder=${state.bookOrder}`
            : ROUTES.TRANSLATION_LIST;
    });
}

function bindEvents() {
    const {prevBtn, nextBtn, verseTable} = elements;
    if (prevBtn) {
        prevBtn.addEventListener("click", () => loadChapter("PREV"));
    }
    if (nextBtn) {
        nextBtn.addEventListener("click", () => loadChapter("NEXT"));
    }
    if (verseTable) {
        verseTable.addEventListener("click", handleVerseClick);
        verseTable.addEventListener("keydown", handleMemoInputAttempt);
        verseTable.addEventListener("beforeinput", handleMemoInputAttempt);
    }
    document.addEventListener("click", handleOutsideFabClick);
    document.addEventListener("keydown", handleFabEscapeKey);
}

function updateLabels() {
    const {translationTypeLabel, pageTitleLabel, chapterSelectLinkLabel, chapterSelectLink} = elements;
    if (translationTypeLabel) {
        translationTypeLabel.textContent = state.translationType;
    }
    if (pageTitleLabel) {
        pageTitleLabel.textContent = `${state.bookName} ${state.chapterNumber}`;
    }
    if (chapterSelectLinkLabel) {
        chapterSelectLinkLabel.textContent = `${state.bookName} ${state.chapterNumber}`;
    }
    if (chapterSelectLink) {
        chapterSelectLink.href = `${ROUTES.CHAPTER_LIST}?translationId=${state.translationId}&bookOrder=${state.bookOrder}`;
    }
}

function getStoredTranslation() {
    return {
        id: TranslationStore.getCurrentTranslationId(),
        type: TranslationStore.getCurrentTranslationType(),
        name: TranslationStore.getCurrentTranslationName(),
        language: TranslationStore.getCurrentTranslationLanguage()
    };
}

function hasCompleteTranslation(stored, targetId) {
    return stored.id === targetId && stored.type && stored.name && stored.language;
}

async function ensureTranslationInfo() {
    const stored = getStoredTranslation();
    if (hasCompleteTranslation(stored, state.translationId)) {
        return stored;
    }
    try {
        const response = await fetch(API_CONFIG.TRANSLATIONS);
        if (!response.ok) {
            throw new Error("번역본 정보를 불러오는 중 오류가 발생했습니다.");
        }
        const translations = await response.json();
        const match = translations.find(item => item.translationId === state.translationId);
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
}

async function ensureBookList() {
    const cached = BookStore.getListForTranslation(state.translationId);
    if (cached && cached.length > 0) {
        return cached;
    }
    try {
        const response = await fetch(`${API_CONFIG.TRANSLATIONS}/${state.translationId}/books`);
        if (!response.ok) {
            throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
        }
        const data = await response.json();
        BookStore.saveListForTranslation(state.translationId, data);
        return data;
    } catch (error) {
        console.warn(error.message);
    }
    return null;
}

function resolveBookName(books) {
    let bookName = BookStore.getBookName(state.translationId, state.bookOrder);
    if (!bookName && books) {
        const currentBook = books.find(book => book.bookOrder === state.bookOrder);
        if (currentBook) {
            BookStore.saveCurrentBook(currentBook);
            bookName = currentBook.bookName;
        }
    }
    return bookName;
}

function buildVerseUrl() {
    const targetUrl = new URL(ROUTES.VERSE_LIST, window.location.origin);
    targetUrl.searchParams.set("translationId", state.translationId);
    targetUrl.searchParams.set("bookOrder", state.bookOrder);
    targetUrl.searchParams.set("chapterNumber", state.chapterNumber);
    if (state.verseNumber) {
        targetUrl.searchParams.set("verseNumber", state.verseNumber);
    }
    return `${targetUrl.pathname}${targetUrl.search}`;
}

function updateVerseUrl() {
    history.replaceState(null, "", buildVerseUrl());
}

function saveLastRead() {
    LastReadStore.save({
        translationId: state.translationId,
        bookOrder: state.bookOrder,
        chapterNumber: state.chapterNumber
    });
}

async function loadChapter(direction) {
    try {
        if (direction !== "CURRENT") {
            state.verseNumber = null;
        }
        const url = buildChapterUrl(direction);
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error("데이터 로딩 실패");
        }
        const data = await response.json();
        updateStateFromChapter(data);
        memoState.cache = await fetchMemosForChapter();
        const highlights = await fetchHighlightsForChapter();
        updateVerseUrl();
        renderChapter(data, highlights);
    } catch (error) {
        showAlert("장 정보를 불러오지 못했습니다.", "danger");
        console.error(error);
    }
}

function buildChapterUrl(direction) {
    const base = `${API_CONFIG.TRANSLATIONS}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}`;
    if (direction === "CURRENT") {
        return `${base}/verses`;
    }
    return `${base}/navigate?direction=${direction}`;
}

function updateStateFromChapter(data) {
    state.bookOrder = data.book.bookOrder;
    state.bookName = data.book.bookName;
    state.chapterNumber = data.book.chapter.chapterNumber;
    BookStore.saveCurrentBook({
        bookOrder: state.bookOrder,
        bookName: state.bookName
    });
    ChapterStore.saveNumber(state.chapterNumber);
    saveLastRead();
}

function renderChapter(data, highlights) {
    const chapter = data.book.chapter;
    updateLabels();
    if (elements.verseTable) {
        elements.verseTable.innerHTML = chapter.verses.map(renderVerseRow).join("");
    }
    if (elements.prevBtn) {
        elements.prevBtn.disabled = data.isFirst;
    }
    if (elements.nextBtn) {
        elements.nextBtn.disabled = data.isLast;
    }
    const verseNumber = state.verseNumber ?? VerseStore.consumeVerseNumber();
    if (verseNumber) {
        if (state.verseNumber) {
            state.verseNumber = null;
            VerseStore.consumeVerseNumber();
        }
        highlightVerse(verseNumber);
    } else {
        window.scrollTo(0, 0);
    }
    applyHighlights(highlights);
    resetSelectionState();
}

function highlightVerse(verseNumber) {
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
}

function renderVerseRow(verse) {
    const v = verse.verseNumber;
    const memo = memoState.cache.get(String(v));
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
}

async function handleVerseClick(event) {
    const actionTarget = event.target;
    if (actionTarget.classList.contains("memo-save-btn")) {
        await saveMemo(actionTarget.dataset.verse);
        return;
    }
    if (actionTarget.classList.contains("memo-delete-btn")) {
        await deleteMemo(actionTarget.dataset.verse);
        return;
    }
    if (actionTarget.closest(".memo-container")) {
        return;
    }
    const verseEl = actionTarget.closest(".verse-text[data-verse]");
    if (!verseEl) {
        return;
    }
    const verseNum = verseEl.getAttribute("data-verse");
    toggleVerseSelection(verseNum);
}

function handleMemoInputAttempt(event) {
    if (!isMemoInputTarget(event.target)) {
        return;
    }
    if (memoState.auth.allowed) {
        return;
    }
    if (memoState.auth.checked || memoState.auth.checking) {
        event.preventDefault();
        return;
    }
    event.preventDefault();
    requestAuth(memoState.auth);
}

function isMemoInputTarget(target) {
    return target
        && (target.matches("textarea[id^='memo-input-']") || target.matches("input[id^='memo-input-']"));
}

function requestAuth(authState) {
    if (authState.checking) {
        return;
    }
    authState.checking = true;
    checkAuthStatus({
        onAuthenticated: () => {
            setAuthState(authState, true);
        },
        onUnauthenticated: () => {
            setAuthState(authState, false);
            if (authState.redirected) {
                return;
            }
            authState.redirected = true;
            alert(authState.message);
            window.location.href = buildLoginRedirectUrl();
        },
        onError: () => {
            setAuthState(authState, false);
        }
    });
}

function setAuthState(authState, allowed) {
    authState.checked = true;
    authState.allowed = allowed;
    authState.checking = false;
}

function showMemo(verseNum) {
    const memoContainer = document.getElementById(`memo-${verseNum}`);
    if (!memoContainer) {
        return;
    }
    memoContainer.classList.remove("d-none");
    const textarea = document.getElementById(`memo-input-${verseNum}`);
    if (textarea) {
        const memo = memoState.cache.get(String(verseNum));
        textarea.value = memo ? memo.content : "";
    }
}

function hideMemo(verseNum) {
    const memoContainer = document.getElementById(`memo-${verseNum}`);
    if (memoContainer) {
        memoContainer.classList.add("d-none");
    }
}

async function saveMemo(verseNum) {
    if (!memoState.auth.allowed) {
        requestAuth(memoState.auth);
        return;
    }
    const textarea = document.getElementById(`memo-input-${verseNum}`);
    if (!textarea) {
        showAlert("메모 입력란을 찾을 수 없습니다", "danger");
        return;
    }
    const value = textarea.value.trim();
    if (!value) {
        return;
    }
    try {
        const response = await fetch(buildMemoUrl(verseNum), {
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
            requestAuth(memoState.auth);
            return;
        }
        if (!response.ok) {
            throw new Error("메모 저장 실패");
        }
        const memo = await response.json();
        memoState.cache.set(String(verseNum), memo);
        const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (verseTextEl) {
            verseTextEl.classList.add("verse-has-memo");
        }
        hideMemo(verseNum);
    } catch (error) {
        showAlert("메모 저장 중 오류가 발생했습니다.", "danger");
        console.error(error);
    }
}

async function deleteMemo(verseNum) {
    if (!memoState.auth.allowed) {
        requestAuth(memoState.auth);
        return;
    }
    try {
        const response = await fetch(buildMemoUrl(verseNum), {
            method: "DELETE",
            credentials: "include"
        });
        if (response.status === 401) {
            requestAuth(memoState.auth);
            return;
        }
        if (!response.ok) {
            throw new Error("메모 삭제 실패");
        }
        memoState.cache.delete(String(verseNum));
        const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (verseTextEl) {
            verseTextEl.classList.remove("verse-has-memo");
        }
        hideMemo(verseNum);
    } catch (error) {
        showAlert("메모 삭제 중 오류가 발생했습니다.", "danger");
        console.error(error);
    }
}

async function fetchMemosForChapter() {
    if (!isAuthenticated) {
        return new Map();
    }
    const url = new URL(buildChapterMemoUrl(), window.location.origin);
    try {
        const response = await fetch(url, {
            method: "GET",
            credentials: "include",
            headers: {
                Accept: "application/json"
            }
        });
        if (response.status === 401) {
            isAuthenticated = false;
            setAuthState(memoState.auth, false);
            setAuthState(highlightState.auth, false);
            return new Map();
        }
        if (!response.ok) {
            throw new Error("메모 조회 실패");
        }
        return new Map((await response.json()).map(item => [String(item.verseNumber), item]));
    } catch (error) {
        console.warn(error.message);
        return new Map();
    }
}

function buildChapterMemoUrl() {
    return `${API_CONFIG.MEMOS_BASE}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}/memos`;
}

function buildMemoUrl(verseNum) {
    return `${API_CONFIG.MEMOS_BASE}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}/verses/${parseInt(verseNum, 10)}/memo`;
}

function buildChapterHighlightUrl() {
    return `${API_CONFIG.HIGHLIGHTS_BASE}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}/highlights`;
}

function buildHighlightUrl(verseNum) {
    return `${API_CONFIG.HIGHLIGHTS_BASE}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}/verses/${parseInt(verseNum, 10)}/highlight`;
}

function showAlert(message, type = "success") {
    alert(`${type}: ` + message);
}

function redirectToTranslation() {
    window.location.href = ROUTES.TRANSLATION_LIST;
}

function redirectToBookList() {
    const bookUrl = new URL(ROUTES.BOOK_LIST, window.location.origin);
    bookUrl.searchParams.set("translationId", state.translationId);
    window.location.href = `${bookUrl.pathname}${bookUrl.search}`;
}

function redirectToChapterList() {
    const chapterUrl = new URL(ROUTES.CHAPTER_LIST, window.location.origin);
    chapterUrl.searchParams.set("translationId", state.translationId);
    chapterUrl.searchParams.set("bookOrder", state.bookOrder);
    window.location.href = `${chapterUrl.pathname}${chapterUrl.search}`;
}

function initFabMenu() {
    const fab = elements.fab;
    if (!fab) {
        return;
    }
    const toggle = fab.querySelector("[data-fab-toggle]");
    const menu = fab.querySelector("[data-fab-menu]");
    const highlightMenu = fab.querySelector("[data-fab-highlight]");
    if (toggle) {
        toggle.addEventListener("click", () => toggleFabMenu());
    }
    if (menu) {
        menu.addEventListener("click", handleFabMenuClick);
    }
    if (highlightMenu) {
        highlightMenu.addEventListener("click", handleHighlightPick);
    }
    fab.addEventListener("click", handleFabBackdropClick);
    syncFabStateFromDom();
    updateFabVisibility();
}

function handleFabBackdropClick(event) {
    if (event.target === elements.fab) {
        closeFabMenu();
    }
}

function handleOutsideFabClick(event) {
    const fab = elements?.fab;
    if (!fab || fab.classList.contains("d-none")) {
        return;
    }
    if (!event.target.closest("#verseFab")) {
        closeFabMenu();
    }
}

function handleFabEscapeKey(event) {
    if (event.key !== "Escape") {
        return;
    }
    const fab = elements?.fab;
    if (!fab || fab.classList.contains("d-none")) {
        return;
    }
    closeFabMenu();
}

function toggleVerseSelection(verseNum) {
    const number = String(verseNum);
    const verseEl = document.querySelector(`.verse-text[data-verse="${number}"]`);
    if (!verseEl) {
        return;
    }
    if (selection.selected.has(number)) {
        selection.selected.delete(number);
        verseEl.classList.remove("active");
    } else {
        selection.selected.add(number);
        verseEl.classList.add("active");
    }
    updateFabVisibility();
}

function resetSelectionState() {
    selection.selected.clear();
    document.querySelectorAll(".verse-text.active").forEach(el => el.classList.remove("active"));
    updateFabVisibility();
    closeFabMenu();
}

function updateFabVisibility() {
    const fab = elements?.fab;
    if (!fab) {
        return;
    }
    if (selection.selected.size > 0) {
        fab.classList.remove(UI_CLASSES.HIDDEN);
    } else {
        fab.classList.add(UI_CLASSES.HIDDEN);
    }
}

function syncFabStateFromDom() {
    const fab = elements?.fab;
    if (!fab) {
        return;
    }
    const menu = fab.querySelector("[data-fab-menu]");
    const menuOpen = fab.classList.contains("is-open")
        || (menu && menu.getAttribute("aria-hidden") === "false");
    selection.menuOpen = Boolean(menuOpen);
}

function toggleFabMenu() {
    syncFabStateFromDom();
    selection.menuOpen = !selection.menuOpen;
    const fab = elements?.fab;
    if (!fab) {
        return;
    }
    fab.classList.toggle("is-open", selection.menuOpen);
    const toggle = fab.querySelector("[data-fab-toggle]");
    const menu = fab.querySelector("[data-fab-menu]");
    if (toggle) {
        toggle.setAttribute("aria-expanded", String(selection.menuOpen));
    }
    if (menu) {
        menu.setAttribute("aria-hidden", String(!selection.menuOpen));
    }
    if (selection.menuOpen) {
        openHighlightMenu();
    } else {
        closeHighlightMenu();
    }
}

function closeFabMenu() {
    selection.menuOpen = false;
    const fab = elements?.fab;
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
    closeHighlightMenu();
}

function handleFabMenuClick(event) {
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton) {
        return;
    }
    const action = actionButton.dataset.action;
    switch (action) {
        case "copy":
            copySelectedVerses();
            closeFabMenu();
            break;
        case "memo":
            openMemoForSelected();
            closeFabMenu();
            break;
        case "share":
            shareSelectedVerses();
            closeFabMenu();
            break;
        default:
            break;
    }
}

function openHighlightMenu() {
    const menu = elements?.fab?.querySelector("[data-fab-highlight]");
    if (!menu) {
        return;
    }
    menu.setAttribute("aria-hidden", "false");
    menu.classList.add("is-open");
}

function closeHighlightMenu() {
    const menu = elements?.fab?.querySelector("[data-fab-highlight]");
    if (menu) {
        menu.setAttribute("aria-hidden", String(!selection.menuOpen));
        if (selection.menuOpen) {
            menu.classList.add("is-open");
        } else {
            menu.classList.remove("is-open");
        }
    }
}

async function handleHighlightPick(event) {
    const colorButton = event.target.closest("[data-highlight]");
    if (!colorButton) {
        return;
    }
    const colorId = colorButton.dataset.highlight;
    await applyHighlightToSelection(colorId);
    closeHighlightMenu();
    closeFabMenu();
}

async function applyHighlightToSelection(colorId) {
    if (!highlightState.auth.allowed) {
        requestAuth(highlightState.auth);
        return;
    }
    const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === colorId);
    if (!colorConfig) {
        return;
    }
    const verseNumbers = getSelectedVerseNumbers();
    if (verseNumbers.length === 0) {
        return;
    }
    await Promise.all(verseNumbers.map(async verseNum => {
        const current = selection.highlightMap.get(String(verseNum));
        if (current && current.id === colorConfig.id) {
            await deleteHighlight(verseNum);
        } else {
            await upsertHighlight(verseNum, colorConfig.id);
        }
    }));
    resetSelectionState();
}

function openMemoForSelected() {
    if (!memoState.auth.allowed) {
        requestAuth(memoState.auth);
        return;
    }
    const verseNumbers = getSelectedVerseNumbers();
    verseNumbers.forEach(verseNum => showMemo(verseNum));
    if (verseNumbers.length > 0) {
        const firstTextarea = document.getElementById(`memo-input-${verseNumbers[0]}`);
        if (firstTextarea) {
            firstTextarea.focus();
        }
    }
}

async function fetchHighlightsForChapter() {
    if (!isAuthenticated) {
        return [];
    }
    const url = new URL(buildChapterHighlightUrl(), window.location.origin);
    try {
        const response = await fetch(url, {
            method: "GET",
            credentials: "include",
            headers: {
                Accept: "application/json"
            }
        });
        if (response.status === 401) {
            isAuthenticated = false;
            setAuthState(memoState.auth, false);
            setAuthState(highlightState.auth, false);
            return [];
        }
        if (!response.ok) {
            throw new Error("형광펜 조회 실패");
        }
        return await response.json();
    } catch (error) {
        console.warn(error.message);
        return [];
    }
}

function applyHighlights(highlights) {
    const colorClasses = HIGHLIGHT_COLORS.map(color => color.className);
    document.querySelectorAll(".verse-text").forEach(el => {
        colorClasses.forEach(className => el.classList.remove(className));
    });
    selection.highlightMap.clear();
    if (!highlights || highlights.length === 0) {
        return;
    }
    highlights.forEach(item => {
        const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === item.color);
        if (!colorConfig) {
            return;
        }
        setHighlightFromServer(item.verseNumber, colorConfig);
    });
}

function setHighlightFromServer(verseNum, colorConfig) {
    const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
    if (!verseEl) {
        return;
    }
    verseEl.classList.add(colorConfig.className);
    selection.highlightMap.set(String(verseNum), colorConfig);
}

async function upsertHighlight(verseNum, colorId) {
    const response = await fetch(buildHighlightUrl(verseNum), {
        method: "PUT",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            Accept: "application/json"
        },
        body: JSON.stringify({color: colorId})
    });
    if (response.status === 401) {
        requestAuth(highlightState.auth);
        return;
    }
    if (!response.ok) {
        showAlert("형광펜 저장에 실패했습니다.", "danger");
        return;
    }
    const highlight = await response.json();
    const colorConfig = HIGHLIGHT_COLORS.find(color => color.id === highlight.color);
    if (!colorConfig) {
        return;
    }
    setHighlightFromServer(highlight.verseNumber, colorConfig);
}

async function deleteHighlight(verseNum) {
    const response = await fetch(buildHighlightUrl(verseNum), {
        method: "DELETE",
        credentials: "include"
    });
    if (response.status === 401) {
        requestAuth(highlightState.auth);
        return;
    }
    if (!response.ok) {
        showAlert("형광펜 삭제에 실패했습니다.", "danger");
        return;
    }
    const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
    if (verseEl) {
        const current = selection.highlightMap.get(String(verseNum));
        if (current) {
            verseEl.classList.remove(current.className);
        }
    }
    selection.highlightMap.delete(String(verseNum));
}

function getSelectedVerseNumbers() {
    return Array.from(selection.selected)
        .map(Number)
        .sort((a, b) => a - b)
        .map(String);
}

function buildSelectedText() {
    const verseNumbers = getSelectedVerseNumbers();
    const translationLabel = state.translationType || state.translationName || "";
    const header = `${translationLabel} ${state.bookName} ${state.chapterNumber}장`.trim();
    const lines = verseNumbers.map(verseNum => {
        const verseEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        const text = verseEl ? verseEl.textContent.trim() : "";
        return `${verseNum} ${text}`.trim();
    });
    return [header, ...lines].filter(Boolean).join("\n");
}

async function copySelectedVerses() {
    const text = buildSelectedText();
    if (!text) {
        return;
    }
    try {
        if (navigator.clipboard?.writeText) {
            await navigator.clipboard.writeText(text);
        } else {
            fallbackCopy(text);
        }
    } catch (error) {
        fallbackCopy(text);
    }
}

async function shareSelectedVerses() {
    const text = buildSelectedText();
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
    copySelectedVerses();
}

function fallbackCopy(text) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
}

document.addEventListener("DOMContentLoaded", init);
