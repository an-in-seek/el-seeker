document.addEventListener("DOMContentLoaded", () => {

    const init = async () => {
        const navDOM = {
            translationLink: document.getElementById("topNavTranslationLink"),
            searchLink: document.getElementById("topNavSearchLink"),
            translationTypeLabel: document.getElementById("translationTypeLabel"),
            translationNameLabel: document.getElementById("translationNameLabel"),
        };

        initNav(navDOM);

        const translationId = getTranslationId();
        if (!translationId) {
            window.location.href = "/web/bible/translation";
            return;
        }

        const translationInfo = await ensureTranslationInfo(translationId);
        updateTranslationLabels(navDOM, translationInfo);

        if (!renderFromSessionStorage(translationId)) {
            await fetchBooksFromAPI(translationId);
        }
    };

    const initNav = navDOM => {
        if (navDOM.translationLink) {
            navDOM.translationLink.classList.remove("d-none");
            navDOM.translationLink.addEventListener("click", () => {
                TranslationStore.saveTranslationReturnPath("/web/bible/book");
            });
        }
        if (navDOM.searchLink) {
            navDOM.searchLink.classList.remove("d-none");
        }
        if (navDOM.translationNameLabel) {
            navDOM.translationNameLabel.classList.remove("d-none");
        }
    };

    const getTranslationId = () => {
        const urlParams = new URLSearchParams(window.location.search);
        const translationIdParam = parseInt(urlParams.get("translationId"), 10);
        return Number.isNaN(translationIdParam)
            ? TranslationStore.getCurrentTranslationId()
            : translationIdParam;
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

    const updateTranslationLabels = (navDOM, translationInfo) => {
        if (navDOM.translationTypeLabel && translationInfo.type) {
            navDOM.translationTypeLabel.textContent = translationInfo.type;
        }
        if (navDOM.translationNameLabel && translationInfo.name) {
            navDOM.translationNameLabel.textContent = translationInfo.name;
        }
    };

    const renderFromSessionStorage = translationId => {
        const books = BookStore.getListForTranslation(translationId);
        if (books) {
            renderBooks(books, translationId);
            return true;
        }
        return false;
    };

    const createBookButton = (book, translationId) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "book-tile";
        button.textContent = book.bookName;
        button.dataset.bookId = book.bookId;
        button.dataset.bookName = book.bookName;
        button.addEventListener("click", () => {
            BookStore.saveCurrentBook(book);
            const targetUrl = new URL("/web/bible/chapter", window.location.origin);
            targetUrl.searchParams.set("translationId", translationId);
            targetUrl.searchParams.set("bookOrder", book.bookOrder);
            window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
        });
        return button;
    };

    const renderGroup = (list, listId, countId, translationId) => {
        const container = document.getElementById(listId);
        const countLabel = document.getElementById(countId);
        if (!container) {
            return;
        }
        container.innerHTML = "";
        list.forEach(book => container.appendChild(createBookButton(book, translationId)));
        if (countLabel) {
            countLabel.textContent = `${list.length}권`;
        }
    };

    const renderBooks = (books, translationId) => {
        const oldTestament = [];
        const newTestament = [];
        books.forEach(book => {
            if (book.testamentType === "OLD") {
                oldTestament.push(book);
            } else if (book.testamentType === "NEW") {
                newTestament.push(book);
            }
        });
        renderGroup(oldTestament, "oldTestamentList", "oldTestamentCount", translationId);
        renderGroup(newTestament, "newTestamentList", "newTestamentCount", translationId);
        BookStore.saveListForTranslation(translationId, books);
    };

    const fetchBooksFromAPI = async translationId => {
        try {
            const response = await fetch(`/api/v1/bibles/translations/${translationId}/books`);
            if (!response.ok) {
                throw new Error("데이터를 불러오는 중 오류가 발생했습니다.");
            }
            const data = await response.json();
            renderBooks(data, translationId);
        } catch (error) {
            alert(error.message);
        }
    };

    init();
});
