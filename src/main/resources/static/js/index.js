import {fetchWithAuthRetry} from "/js/common-util.js?v=2.1";
import {LastReadStore, SessionStore, STORAGE_KEYS, TranslationStore} from "/js/storage-util.js?v=2.1";

const updateText = (element, value) => {
    if (!element) {
        return;
    }
    element.textContent = value;
};

document.addEventListener("DOMContentLoaded", async () => {
    const skipAutoRedirect = SessionStore.consume(STORAGE_KEYS.SKIP_HOME_REDIRECT);
    const lastRead = LastReadStore.get();
    if (lastRead && !skipAutoRedirect) {
        const verseUrl = new URL("/web/bible/verse", window.location.origin);
        verseUrl.searchParams.set("translationId", lastRead.translationId);
        verseUrl.searchParams.set("bookOrder", lastRead.bookOrder);
        verseUrl.searchParams.set("chapterNumber", lastRead.chapterNumber);
        window.location.replace(`${verseUrl.pathname}${verseUrl.search}`);
        return;
    }

    const dailyVerseText = document.getElementById("dailyVerseText");
    const dailyVerseReference = document.getElementById("dailyVerseReference");
    const dailyVerseLink = document.getElementById("dailyVerseLink");

    const setDailyVerseFallback = (message) => {
        updateText(dailyVerseText, message);
        updateText(dailyVerseReference, "개역한글(KRV)");
    };

    const resolveTranslationId = async (translationType) => {
        const storedId = TranslationStore.getCurrentTranslationId();
        const storedType = TranslationStore.getCurrentTranslationType();
        if (storedId && storedType === translationType) {
            return storedId;
        }
        try {
            const response = await fetchWithAuthRetry("/api/v1/bibles/translations", {
                headers: {
                    Accept: "application/json",
                },
            });
            if (!response.ok) {
                throw new Error("translation fetch failed");
            }
            const translations = await response.json();
            const match = translations.find(item => item.translationType === translationType);
            if (match) {
                TranslationStore.saveCurrentTranslation({
                    id: match.translationId,
                    name: match.translationName,
                    type: match.translationType,
                    language: match.translationLanguage
                });
                return match.translationId;
            }
        } catch (error) {
            console.warn(error.message);
        }
        return null;
    };

    const setDailyVerseLink = (targetUrl) => {
        if (!dailyVerseLink) {
            return;
        }
        dailyVerseLink.dataset.verseUrl = targetUrl;
        dailyVerseLink.style.cursor = "pointer";
        dailyVerseLink.setAttribute("aria-disabled", "false");
    };

    const loadDailyVerse = async () => {
        if (!dailyVerseText || !dailyVerseReference) {
            return;
        }

        dailyVerseText.style.opacity = 0;
        dailyVerseReference.style.opacity = 0;

        try {
            const response = await fetchWithAuthRetry("/api/v1/bibles/daily?translationType=KRV", {
                headers: {
                    Accept: "application/json",
                },
            });

            if (!response.ok) {
                throw new Error("daily verse fetch failed");
            }

            const data = await response.json();
            const reference = `${data.translationName}(${data.translationType}) · ${data.bookName} ${data.chapterNumber}:${data.verseNumber}`;
            updateText(dailyVerseText, data.text);
            updateText(dailyVerseReference, reference);
            const translationId = await resolveTranslationId(data.translationType);
            if (translationId) {
                const verseUrl = new URL("/web/bible/verse", window.location.origin);
                verseUrl.searchParams.set("translationId", translationId);
                verseUrl.searchParams.set("bookOrder", data.bookOrder);
                verseUrl.searchParams.set("chapterNumber", data.chapterNumber);
                verseUrl.searchParams.set("verseNumber", data.verseNumber);
                verseUrl.searchParams.set("from", "home");
                setDailyVerseLink(`${verseUrl.pathname}${verseUrl.search}`);
            }
        } catch (error) {
            setDailyVerseFallback("오늘의 말씀을 불러오지 못했습니다.");
        } finally {
            dailyVerseText.style.opacity = 1;
            dailyVerseReference.style.opacity = 1;
        }
    };

    loadDailyVerse();

    if (dailyVerseLink) {
        dailyVerseLink.addEventListener("click", () => {
            const targetUrl = dailyVerseLink.dataset.verseUrl;
            if (targetUrl) {
                SessionStore.set(STORAGE_KEYS.SKIP_HOME_REDIRECT, true);
                window.location.href = targetUrl;
            }
        });
        dailyVerseLink.addEventListener("keydown", (event) => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                const targetUrl = dailyVerseLink.dataset.verseUrl;
                if (targetUrl) {
                    SessionStore.set(STORAGE_KEYS.SKIP_HOME_REDIRECT, true);
                    window.location.href = targetUrl;
                }
            }
        });
    }

    const comingSoonLinks = document.querySelectorAll(".coming-soon");
    comingSoonLinks.forEach((link) => {
        link.addEventListener("click", (event) => {
            event.preventDefault();
        });
    });
    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "ElSeeker";
        pageTitleLabel.classList.remove("d-none");
    }
});
