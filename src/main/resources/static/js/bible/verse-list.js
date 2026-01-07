document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const parsedTranslationId = parseInt(urlParams.get("translationId"), 10);
    const parsedBookOrder = parseInt(urlParams.get("bookOrder"), 10);
    const parsedChapterNumber = parseInt(urlParams.get("chapterNumber"), 10);
    const parsedVerseNumber = parseInt(urlParams.get("verseNumber"), 10);
    const storedTranslationId = TranslationStore.getCurrentTranslationId();
    const storedBookOrder = BookStore.getCurrentBookOrder();
    const storedChapterNumber = ChapterStore.getNumber();
    const translationId = Number.isNaN(parsedTranslationId)
        ? storedTranslationId
        : parsedTranslationId;
    const canUseStoredBookOrder = Number.isNaN(parsedTranslationId)
        || (storedTranslationId && parsedTranslationId === storedTranslationId);
    const bookOrder = Number.isNaN(parsedBookOrder)
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
    const verseNumber = Number.isNaN(parsedVerseNumber) ? null : parsedVerseNumber;
    const dom = {
        backButton: document.getElementById("topNavBackButton"),
        translationLink: document.getElementById("topNavTranslationLink"),
        searchLink: document.getElementById("topNavSearchLink"),
        translationTypeLabel: document.getElementById("translationTypeLabel"),
        pageTitleLabel: document.getElementById("pageTitleLabel"),
        verseTable: document.getElementById("verseTableBody"),
        prevBtn: document.getElementById("prevChapterBtn"),
        chapterSelectLink: document.getElementById("chapterSelectLink"),
        chapterSelectLinkLabel: document.getElementById("chapterSelectLinkLabel"),
        nextBtn: document.getElementById("nextChapterBtn"),
    };

    const state = {
        translationId,
        translationType: null,
        bookOrder,
        bookName: null,
        chapterNumber,
        verseNumber,
        fromSearch: urlParams.get("from") === "search",
    };

    const init = async () => {
        if (!state.translationId) {
            redirectToTranslation();
            return;
        }

        const translationInfo = await ensureTranslationInfo();
        state.translationType = translationInfo.type;

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

        if (dom.prevBtn) {
            dom.prevBtn.addEventListener("click", () => loadChapter("PREV"));
        }
        if (dom.nextBtn) {
            dom.nextBtn.addEventListener("click", () => loadChapter("NEXT"));
        }
        if (dom.verseTable) {
            dom.verseTable.addEventListener("click", handleVerseClick);
        }

        await loadChapter("CURRENT");
    };

    const initNav = () => {
        const handleBack = () => {
            if (state.fromSearch) {
                history.back();
                return;
            }
            window.location.href = state.translationId && state.bookOrder
                ? `/web/bible/chapter?translationId=${state.translationId}&bookOrder=${state.bookOrder}`
                : "/web/bible/translation";
        };
        setupBackButton(dom.backButton, handleBack);
        if (dom.translationLink) {
            dom.translationLink.classList.remove("d-none");
            dom.translationLink.addEventListener("click", () => {
                TranslationStore.saveTranslationReturnPath(buildVerseUrl());
            });
        }
        if (dom.searchLink) {
            dom.searchLink.classList.remove("d-none");
        }
        if (dom.pageTitleLabel) {
            dom.pageTitleLabel.classList.remove("d-none");
        }
    };

    const setupBackButton = (button, onClick) => {
        if (!button) {
            return;
        }
        button.classList.remove("d-none");
        button.addEventListener("click", onClick);
    };

    const updateLabels = () => {
        if (dom.translationTypeLabel) {
            dom.translationTypeLabel.textContent = state.translationType;
        }
        if (dom.pageTitleLabel) {
            dom.pageTitleLabel.textContent = `${state.bookName} ${state.chapterNumber}`;
        }
        if (dom.chapterSelectLinkLabel) {
            dom.chapterSelectLinkLabel.textContent = `${state.bookName} ${state.chapterNumber}`;
        }
        if (dom.chapterSelectLink) {
            dom.chapterSelectLink.href = `/web/bible/chapter?translationId=${state.translationId}&bookOrder=${state.bookOrder}`;
        }
    };

    const getStoredTranslation = () => ({
        id: TranslationStore.getCurrentTranslationId(),
        type: TranslationStore.getCurrentTranslationType(),
        name: TranslationStore.getCurrentTranslationName(),
        language: TranslationStore.getCurrentTranslationLanguage(),
    });

    const hasCompleteTranslation = (stored, targetId) =>
        stored.id === targetId && stored.type && stored.name && stored.language;

    const ensureTranslationInfo = async () => {
        const stored = getStoredTranslation();
        if (hasCompleteTranslation(stored, state.translationId)) {
            return stored;
        }
        try {
            const response = await fetch("/api/v1/bibles/translations");
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
                    language: match.translationLanguage,
                };
                TranslationStore.saveCurrentTranslation(translation);
                return translation;
            }
        } catch (error) {
            console.warn(error.message);
        }
        return stored;
    };

    const ensureBookList = async () => {
        const cached = BookStore.getListForTranslation(state.translationId);
        if (cached && cached.length > 0) {
            return cached;
        }
        try {
            const response = await fetch(`/api/v1/bibles/translations/${state.translationId}/books`);
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
    };

    const resolveBookName = books => {
        let bookName = BookStore.getBookName(state.translationId, state.bookOrder);
        if (!bookName && books) {
            const currentBook = books.find(book => book.bookOrder === state.bookOrder);
            if (currentBook) {
                BookStore.saveCurrentBook(currentBook);
                bookName = currentBook.bookName;
            }
        }
        return bookName;
    };

    const buildVerseUrl = () => {
        const targetUrl = new URL("/web/bible/verse", window.location.origin);
        targetUrl.searchParams.set("translationId", state.translationId);
        targetUrl.searchParams.set("bookOrder", state.bookOrder);
        targetUrl.searchParams.set("chapterNumber", state.chapterNumber);
        if (state.verseNumber) {
            targetUrl.searchParams.set("verseNumber", state.verseNumber);
        }
        return `${targetUrl.pathname}${targetUrl.search}`;
    };

    const updateVerseUrl = () => {
        history.replaceState(null, "", buildVerseUrl());
    };

    const loadChapter = async direction => {
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
            updateVerseUrl();
            renderChapter(data);
        } catch (error) {
            showAlert("장 정보를 불러오지 못했습니다.", "danger");
            console.error(error);
        }
    };

    const buildChapterUrl = direction => {
        const base = `/api/v1/bibles/translations/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}`;
        if (direction === "CURRENT") {
            return `${base}/verses`;
        }
        return `${base}/navigate?direction=${direction}`;
    };

    const updateStateFromChapter = data => {
        state.bookOrder = data.book.bookOrder;
        state.bookName = data.book.bookName;
        state.chapterNumber = data.book.chapter.chapterNumber;
        BookStore.saveCurrentBook({
            bookOrder: state.bookOrder,
            bookName: state.bookName
        });
        ChapterStore.saveNumber(state.chapterNumber);
    };

    const renderChapter = data => {
        const chapter = data.book.chapter;
        updateLabels();
        if (dom.verseTable) {
            dom.verseTable.innerHTML = chapter.verses.map(renderVerseRow).join("");
        }
        if (dom.prevBtn) {
            dom.prevBtn.disabled = data.isFirst;
        }
        if (dom.nextBtn) {
            dom.nextBtn.disabled = data.isLast;
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
    };

    const highlightVerse = verseNumber => {
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
    };

    const renderVerseRow = verse => {
        const v = verse.verseNumber;
        const memoKey = buildMemoKey(v);
        const hasMemo = !!localStorage.getItem(memoKey);
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
    };

    const handleVerseClick = event => {
        const verseEl = event.target.closest("[data-verse]");
        if (!verseEl) {
            return;
        }
        const verseNum = verseEl.getAttribute("data-verse");
        if (event.target.classList.contains("memo-save-btn")) {
            saveMemo(verseNum);
            return;
        }
        if (event.target.classList.contains("memo-delete-btn")) {
            deleteMemo(verseNum);
            return;
        }
        toggleMemo(verseNum);
        applyVerseHighlight(verseNum);
    };

    const toggleMemo = verseNum => {
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
            const key = buildMemoKey(verseNum);
            textarea.value = localStorage.getItem(key) || "";
        }
    };

    const applyVerseHighlight = verseNum => {
        document.querySelectorAll(".verse-text").forEach(el => {
            const isTarget = el.id === `verse-text-${verseNum}`;
            if (isTarget) {
                el.classList.toggle("active");
            } else {
                el.classList.remove("active");
            }
        });
    };

    const saveMemo = verseNum => {
        const textarea = document.getElementById(`memo-input-${verseNum}`);
        if (!textarea) {
            showAlert("메모 입력란을 찾을 수 없습니다", "danger");
            return;
        }
        const value = textarea.value.trim();
        if (!value) {
            return;
        }
        const key = buildMemoKey(verseNum);
        localStorage.setItem(key, value);
        const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (verseTextEl) {
            verseTextEl.classList.add("verse-has-memo");
        }
        toggleMemo(verseNum);
        applyVerseHighlight(verseNum);
    };

    const deleteMemo = verseNum => {
        const key = buildMemoKey(verseNum);
        localStorage.removeItem(key);
        const verseTextEl = document.querySelector(`.verse-text[data-verse="${verseNum}"]`);
        if (verseTextEl) {
            verseTextEl.classList.remove("verse-has-memo");
        }
        toggleMemo(verseNum);
        applyVerseHighlight(verseNum);
    };

    const buildMemoKey = verseNum =>
        `memo_${state.translationId}_${state.bookOrder}_${state.chapterNumber}_${verseNum}`;

    const showAlert = (message, type = "success") => {
        alert(`${type}: ` + message);
    };

    const redirectToTranslation = () => {
        window.location.href = "/web/bible/translation";
    };

    const redirectToBookList = () => {
        const bookUrl = new URL("/web/bible/book", window.location.origin);
        bookUrl.searchParams.set("translationId", state.translationId);
        window.location.href = `${bookUrl.pathname}${bookUrl.search}`;
    };

    const redirectToChapterList = () => {
        const chapterUrl = new URL("/web/bible/chapter", window.location.origin);
        chapterUrl.searchParams.set("translationId", state.translationId);
        chapterUrl.searchParams.set("bookOrder", state.bookOrder);
        window.location.href = `${chapterUrl.pathname}${chapterUrl.search}`;
    };

    init();
});
