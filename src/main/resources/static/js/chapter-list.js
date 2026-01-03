document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const parsedTranslationId = parseInt(urlParams.get("translationId"), 10);
    const parsedBookOrder = parseInt(urlParams.get("bookOrder"), 10);
    const storedTranslationId = TranslationStore.getCurrentTranslationId();
    const storedBookOrder = BookStore.getCurrentBookOrder();

    const init = async () => {
        const navDOM = {
            translationLink: document.getElementById("topNavTranslationLink"),
            searchLink: document.getElementById("topNavSearchLink"),
            translationTypeLabel: document.getElementById("translationTypeLabel"),
            bookLabel: document.getElementById("bookLabel"),
        };
        const translationId = getTranslationId();
        const bookOrder = getBookOrder();

        initNav(navDOM, translationId, bookOrder);

        if (!translationId) {
            redirectToTranslation();
            return;
        }

        const translationInfo = await ensureTranslationInfo(translationId);
        updateTranslationLabel(navDOM, translationInfo);

        if (!bookOrder) {
            redirectToBookList(translationId);
            return;
        }

        const books = await ensureBookList(translationId);
        const bookName = resolveBookName(translationId, bookOrder, books);
        if (!bookName) {
            redirectToBookList(translationId);
            return;
        }

        const dom = {
            translationTypeLabel: navDOM.translationTypeLabel,
            bookLabel: navDOM.bookLabel,
            bookDescription: document.getElementById("bookDescription"),
            bookDescriptionSummary: document.getElementById("bookDescriptionSummary"),
            chapterList: document.getElementById("chapterList"),
            prevBtn: document.getElementById("prevBookBtn"),
            bookSelectLink: document.getElementById("bookSelectLink"),
            bookSelectLinkLabel: document.getElementById("bookSelectLinkLabel"),
            nextBtn: document.getElementById("nextBookBtn"),
        };

        updateHeader(dom, translationId, bookOrder, translationInfo.type, bookName);
        setupPrevNext(dom, books, translationId, bookOrder);

        if (!renderFromSessionStorage(translationId, bookOrder, dom)) {
            await fetchChaptersFromAPI(translationId, bookOrder, dom);
        }
    };

    const getTranslationId = () => {
        return Number.isNaN(parsedTranslationId)
            ? storedTranslationId
            : parsedTranslationId;
    };

    const getBookOrder = () => {
        const canUseStoredBookOrder = Number.isNaN(parsedTranslationId)
            || (storedTranslationId && parsedTranslationId === storedTranslationId);
        return Number.isNaN(parsedBookOrder)
            ? (canUseStoredBookOrder ? storedBookOrder : null)
            : parsedBookOrder;
    };

    const initNav = (navDOM, translationId, bookOrder) => {
        if (navDOM.translationLink) {
            navDOM.translationLink.classList.remove("d-none");
            navDOM.translationLink.addEventListener("click", () => {
                const returnUrl = new URL("/web/bible/chapter", window.location.origin);
                if (translationId) {
                    returnUrl.searchParams.set("translationId", translationId);
                }
                if (bookOrder) {
                    returnUrl.searchParams.set("bookOrder", bookOrder);
                }
                TranslationStore.saveTranslationReturnPath(`${returnUrl.pathname}${returnUrl.search}`);
            });
        }
        if (navDOM.searchLink) {
            navDOM.searchLink.classList.remove("d-none");
        }
        if (navDOM.bookLabel) {
            navDOM.bookLabel.classList.remove("d-none");
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

    const ensureTranslationInfo = async targetId => {
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
    };

    const ensureBookList = async targetTranslationId => {
        const cached = BookStore.getListForTranslation(targetTranslationId);
        if (cached && cached.length > 0) {
            return cached;
        }
        try {
            const response = await fetch(`/api/v1/bibles/translations/${targetTranslationId}/books`);
            if (!response.ok) {
                throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
            }
            const data = await response.json();
            BookStore.saveListForTranslation(targetTranslationId, data);
            return data;
        } catch (error) {
            console.warn(error.message);
        }
        return null;
    };

    const resolveBookName = (translationId, bookOrder, books) => {
        let bookName = BookStore.getBookName(translationId, bookOrder);
        if (!bookName && books) {
            const currentBook = books.find(book => book.bookOrder === bookOrder);
            if (currentBook) {
                BookStore.saveCurrentBook(currentBook);
                bookName = currentBook.bookName;
            }
        }
        return bookName;
    };

    const updateTranslationLabel = (navDOM, translationInfo) => {
        if (navDOM.translationTypeLabel && translationInfo.type) {
            navDOM.translationTypeLabel.textContent = translationInfo.type;
        }
    };

    const updateHeader = (dom, translationId, bookOrder, translationType, bookName) => {
        if (dom.translationTypeLabel && translationType) {
            dom.translationTypeLabel.textContent = translationType;
        }
        if (dom.bookLabel) {
            dom.bookLabel.textContent = bookName;
            dom.bookLabel.href = `/web/bible/book?translationId=${translationId}`;
        }
        if (dom.bookSelectLinkLabel) {
            dom.bookSelectLinkLabel.textContent = bookName;
        }
        if (dom.bookSelectLink) {
            dom.bookSelectLink.href = `/web/bible/book?translationId=${translationId}`;
        }
        if (dom.bookDescription) {
            dom.bookDescription.href = `/web/bible/book/description?translationId=${translationId}&bookOrder=${bookOrder}`;
        }
    };

    const setupPrevNext = (dom, books, translationId, bookOrder) => {
        if (!books || books.length === 0) {
            return;
        }
        const currentIndex = books.findIndex(book => book.bookOrder === bookOrder);
        if (dom.prevBtn) {
            dom.prevBtn.disabled = currentIndex <= 0;
            dom.prevBtn.addEventListener("click", () => {
                if (currentIndex > 0) {
                    const prevBook = books[currentIndex - 1];
                    BookStore.saveCurrentBook(prevBook);
                    navigateToBook(translationId, prevBook.bookOrder);
                }
            });
        }
        if (dom.nextBtn) {
            dom.nextBtn.disabled = currentIndex >= books.length - 1;
            dom.nextBtn.addEventListener("click", () => {
                if (currentIndex < books.length - 1) {
                    const nextBook = books[currentIndex + 1];
                    BookStore.saveCurrentBook(nextBook);
                    navigateToBook(translationId, nextBook.bookOrder);
                }
            });
        }
    };

    const renderFromSessionStorage = (translationId, bookOrder, dom) => {
        const bookData = BookStore.getDetail(translationId, bookOrder);
        if (bookData) {
            renderChapters(bookData, translationId, bookOrder, dom);
            return true;
        }
        return false;
    };

    const renderChapters = (data, translationId, bookOrder, dom) => {
        if (dom.bookSelectLinkLabel) {
            dom.bookSelectLinkLabel.textContent = data.book.bookName;
        }
        if (dom.bookLabel) {
            dom.bookLabel.textContent = data.book.bookName;
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
                    targetUrl.searchParams.set("translationId", translationId);
                    targetUrl.searchParams.set("bookOrder", bookOrder);
                    targetUrl.searchParams.set("chapterNumber", chapter.chapterNumber);
                    window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
                });
                dom.chapterList.appendChild(tile);
            });
        }
        BookStore.saveDetail(translationId, bookOrder, data);
        window.scrollTo({top: 0, behavior: "smooth"});
    };

    const fetchChaptersFromAPI = async (translationId, bookOrder, dom) => {
        try {
            const response = await fetch(`/api/v1/bibles/translations/${translationId}/books/${bookOrder}/chapters`);
            if (!response.ok) {
                throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
            }
            const data = await response.json();
            renderChapters(data, translationId, bookOrder, dom);
        } catch (error) {
            alert(error.message);
        }
    };

    const navigateToBook = (translationId, bookOrder) => {
        const targetUrl = new URL("/web/bible/chapter", window.location.origin);
        targetUrl.searchParams.set("translationId", translationId);
        targetUrl.searchParams.set("bookOrder", bookOrder);
        window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
    };

    const redirectToTranslation = () => {
        window.location.href = "/web/bible/translation";
    };

    const redirectToBookList = translationId => {
        const bookUrl = new URL("/web/bible/book", window.location.origin);
        bookUrl.searchParams.set("translationId", translationId);
        window.location.href = `${bookUrl.pathname}${bookUrl.search}`;
    };

    init();
});
