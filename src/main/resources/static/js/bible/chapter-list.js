document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const parsedTranslationId = parseInt(urlParams.get("translationId"), 10);
    const parsedBookOrder = parseInt(urlParams.get("bookOrder"), 10);
    const storedTranslationId = TranslationStore.getCurrentTranslationId();
    const storedBookOrder = BookStore.getCurrentBookOrder();
    const canUseStoredBookOrder = Number.isNaN(parsedTranslationId)
        || (storedTranslationId && parsedTranslationId === storedTranslationId);
    const state = {
        translationId: Number.isNaN(parsedTranslationId)
            ? storedTranslationId
            : parsedTranslationId,
        bookOrder: Number.isNaN(parsedBookOrder)
            ? (canUseStoredBookOrder ? storedBookOrder : null)
            : parsedBookOrder,
    };

    const navDOM = {
        backButton: document.getElementById("topNavBackButton"),
        translationLink: document.getElementById("topNavTranslationLink"),
        searchLink: document.getElementById("topNavSearchLink"),
        translationTypeLabel: document.getElementById("translationTypeLabel"),
        pageTitleLabel: document.getElementById("pageTitleLabel"),
    };

    const dom = {
        translationTypeLabel: navDOM.translationTypeLabel,
        pageTitleLabel: navDOM.pageTitleLabel,
        bookDescription: document.getElementById("bookDescription"),
        bookDescriptionSummary: document.getElementById("bookDescriptionSummary"),
        chapterList: document.getElementById("chapterList"),
        prevBtn: document.getElementById("prevBookBtn"),
        bookSelectLink: document.getElementById("bookSelectLink"),
        bookSelectLinkLabel: document.getElementById("bookSelectLinkLabel"),
        nextBtn: document.getElementById("nextBookBtn"),
    };

    const init = async () => {
        const translationId = state.translationId;
        const bookOrder = state.bookOrder;

        initNav();

        if (!translationId) {
            redirectToTranslation();
            return;
        }

        const translationInfo = await ensureTranslationInfo();
        updateTranslationLabel(translationInfo);

        if (!bookOrder) {
            redirectToBookList();
            return;
        }

        const books = await ensureBookList();
        const bookName = resolveBookName(books);
        if (!bookName) {
            redirectToBookList();
            return;
        }

        updateHeader(translationInfo.type, bookName);
        setupPrevNext(books);

        if (!renderFromSessionStorage()) {
            await fetchChaptersFromAPI();
        }
    };

    const initNav = () => {
        const buildBackUrl = () => state.translationId
            ? `/web/bible/book?translationId=${state.translationId}`
            : "/web/bible/translation";
        setupBackButton(navDOM.backButton, buildBackUrl);
        if (navDOM.translationLink) {
            navDOM.translationLink.classList.remove("d-none");
            navDOM.translationLink.addEventListener("click", () => {
                const returnUrl = new URL("/web/bible/chapter", window.location.origin);
                if (state.translationId) {
                    returnUrl.searchParams.set("translationId", state.translationId);
                }
                if (state.bookOrder) {
                    returnUrl.searchParams.set("bookOrder", state.bookOrder);
                }
                TranslationStore.saveTranslationReturnPath(`${returnUrl.pathname}${returnUrl.search}`);
            });
        }
        if (navDOM.searchLink) {
            navDOM.searchLink.classList.remove("d-none");
        }
        if (navDOM.pageTitleLabel) {
            navDOM.pageTitleLabel.classList.remove("d-none");
        }
    };

    const setupBackButton = (button, getTargetUrl) => {
        if (!button) {
            return;
        }
        button.classList.remove("d-none");
        button.addEventListener("click", () => {
            window.location.href = getTargetUrl();
        });
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

    const updateTranslationLabel = translationInfo => {
        if (navDOM.translationTypeLabel && translationInfo.type) {
            navDOM.translationTypeLabel.textContent = translationInfo.type;
        }
    };

    const updateHeader = (translationType, bookName) => {
        if (dom.translationTypeLabel && translationType) {
            dom.translationTypeLabel.textContent = translationType;
        }
        if (dom.pageTitleLabel) {
            dom.pageTitleLabel.textContent = bookName;
        }
        if (dom.bookSelectLinkLabel) {
            dom.bookSelectLinkLabel.textContent = bookName;
        }
        if (dom.bookSelectLink) {
            dom.bookSelectLink.href = `/web/bible/book?translationId=${state.translationId}`;
        }
        if (dom.bookDescription) {
            dom.bookDescription.href = `/web/bible/book/description?translationId=${state.translationId}&bookOrder=${state.bookOrder}`;
        }
    };

    const setupPrevNext = books => {
        if (!books || books.length === 0) {
            return;
        }
        const currentIndex = books.findIndex(book => book.bookOrder === state.bookOrder);
        if (dom.prevBtn) {
            dom.prevBtn.disabled = currentIndex <= 0;
            dom.prevBtn.addEventListener("click", () => {
                if (currentIndex > 0) {
                    const prevBook = books[currentIndex - 1];
                    BookStore.saveCurrentBook(prevBook);
                    navigateToBook(prevBook.bookOrder);
                }
            });
        }
        if (dom.nextBtn) {
            dom.nextBtn.disabled = currentIndex >= books.length - 1;
            dom.nextBtn.addEventListener("click", () => {
                if (currentIndex < books.length - 1) {
                    const nextBook = books[currentIndex + 1];
                    BookStore.saveCurrentBook(nextBook);
                    navigateToBook(nextBook.bookOrder);
                }
            });
        }
    };

    const renderFromSessionStorage = () => {
        const bookData = BookStore.getDetail(state.translationId, state.bookOrder);
        if (bookData) {
            renderChapters(bookData);
            return true;
        }
        return false;
    };

    const renderChapters = data => {
        if (dom.bookSelectLinkLabel) {
            dom.bookSelectLinkLabel.textContent = data.book.bookName;
        }
        if (dom.pageTitleLabel) {
            dom.pageTitleLabel.textContent = data.book.bookName;
        }
        if (dom.bookDescriptionSummary) {
            dom.bookDescriptionSummary.textContent = data.book.descriptionSummary;
        }
        if (dom.chapterList) {
            dom.chapterList.innerHTML = "";
            data.book.chapters.forEach(chapter => {
                const tile = document.createElement("button");
                tile.type = "button";
                tile.className = "chapter-tile";
                tile.textContent = chapter.chapterNumber;
                tile.addEventListener("click", () => {
                    ChapterStore.saveNumber(chapter.chapterNumber);
                    const targetUrl = new URL("/web/bible/verse", window.location.origin);
                    targetUrl.searchParams.set("translationId", state.translationId);
                    targetUrl.searchParams.set("bookOrder", state.bookOrder);
                    targetUrl.searchParams.set("chapterNumber", chapter.chapterNumber);
                    window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
                });
                dom.chapterList.appendChild(tile);
            });
        }
        BookStore.saveDetail(state.translationId, state.bookOrder, data);
        window.scrollTo({top: 0, behavior: "smooth"});
    };

    const fetchChaptersFromAPI = async () => {
        try {
            const response = await fetch(`/api/v1/bibles/translations/${state.translationId}/books/${state.bookOrder}/chapters`);
            if (!response.ok) {
                throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
            }
            const data = await response.json();
            renderChapters(data);
        } catch (error) {
            alert(error.message);
        }
    };

    const navigateToBook = bookOrder => {
        state.bookOrder = bookOrder;
        const targetUrl = new URL("/web/bible/chapter", window.location.origin);
        targetUrl.searchParams.set("translationId", state.translationId);
        targetUrl.searchParams.set("bookOrder", state.bookOrder);
        window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
    };

    const redirectToTranslation = () => {
        window.location.href = "/web/bible/translation";
    };

    const redirectToBookList = () => {
        const bookUrl = new URL("/web/bible/book", window.location.origin);
        bookUrl.searchParams.set("translationId", state.translationId);
        window.location.href = `${bookUrl.pathname}${bookUrl.search}`;
    };

    init();
});
