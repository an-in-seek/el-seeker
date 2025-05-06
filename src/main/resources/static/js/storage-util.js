// /js/storage-util.js

// 스토리지 키 상수
const STORAGE_KEYS = Object.freeze({
    TRANSLATION_ID: "translationId",
    TRANSLATION_NAME: "translationName",
    TRANSLATION_TYPE: "translationType",
    BOOK_ID: "bookId",
    BOOK_NAME: "bookName",
    BOOK_DATA_PREFIX: "bible_book_",
    TRANSLATION_BOOKS_PREFIX: "bible_translation_",
    CHAPTER_ID: "chapterId",
    CHAPTER_NUMBER: "chapterNumber",
    VERSE_ID: "verseId",
    VERSE_NUMBER: "verseNumber",
});

// === Local Storage Utility ===
const LocalStore = {
    set(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    },
    get(key) {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    },
    remove(key) {
        localStorage.removeItem(key);
    },
    consume(key) {
        const item = this.get(key);
        this.remove(key);
        return item;
    }
};

// === Session Storage Utility ===
const SessionStore = {
    set(key, value) {
        sessionStorage.setItem(key, JSON.stringify(value));
    },
    get(key) {
        const item = sessionStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    },
    remove(key) {
        sessionStorage.removeItem(key);
    },
    consume(key) {
        const item = this.get(key);
        this.remove(key);
        return item;
    }
};

// 번역본 관련
const TranslationStore = {
    save({id, name, type}) {
        LocalStore.set(STORAGE_KEYS.TRANSLATION_ID, id);
        LocalStore.set(STORAGE_KEYS.TRANSLATION_NAME, name);
        LocalStore.set(STORAGE_KEYS.TRANSLATION_TYPE, type);
    },
    getId() {
        return parseInt(LocalStore.get(STORAGE_KEYS.TRANSLATION_ID));
    },
    getName() {
        return LocalStore.get(STORAGE_KEYS.TRANSLATION_NAME);
    },
    getType() {
        return LocalStore.get(STORAGE_KEYS.TRANSLATION_TYPE);
    },
}

// 책 관련
const BookStore = {
    save({id, name}) {
        LocalStore.set(STORAGE_KEYS.BOOK_ID, id);
        LocalStore.set(STORAGE_KEYS.BOOK_NAME, name);
    },
    getId() {
        return parseInt(LocalStore.get(STORAGE_KEYS.BOOK_ID));
    },
    getName() {
        return LocalStore.get(STORAGE_KEYS.BOOK_NAME);
    },
    saveListForTranslation(translationId, books) {
        const key = `${STORAGE_KEYS.TRANSLATION_BOOKS_PREFIX}${translationId}`;
        SessionStore.set(key, books);
    },
    getListForTranslation(translationId) {
        const key = `${STORAGE_KEYS.TRANSLATION_BOOKS_PREFIX}${translationId}`;
        return SessionStore.get(key);
    },
    saveDetail(bookId, bookDetail) {
        const key = `${STORAGE_KEYS.BOOK_DATA_PREFIX}${bookId}`;
        SessionStore.set(key, bookDetail);
    },
    getDetail(bookId) {
        const key = `${STORAGE_KEYS.BOOK_DATA_PREFIX}${bookId}`;
        return SessionStore.get(key);
    },
};

// 장 관련
const ChapterStore = {
    saveNumber(number) {
        const parsed = parseInt(number);
        if (!isNaN(parsed)) {
            LocalStore.set(STORAGE_KEYS.CHAPTER_NUMBER, parsed);
        } else {
            console.warn("ChapterStore.saveNumber: 유효하지 않은 chapterNumber:", number);
        }
    },
    getNumber() {
        return parseInt(LocalStore.get(STORAGE_KEYS.CHAPTER_NUMBER));
    }
};

// 절 관련
const VerseStore = {
    saveNumber(number) {
        const parsed = parseInt(number);
        if (!isNaN(parsed)) {
            SessionStore.set(STORAGE_KEYS.VERSE_NUMBER, parsed);
        } else {
            console.warn("VerseStore.saveNumber: 유효하지 않은 verseNumber:", number);
        }
    },
    consumeVerseNumber() {
        return SessionStore.consume(STORAGE_KEYS.VERSE_NUMBER);
    }
};