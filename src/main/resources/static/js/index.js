import {fetchWithAuthRetry} from "/js/common-util.js?v=2.1";
import {LastReadStore} from "/js/storage-util.js?v=2.1";

const updateText = (element, value) => {
    if (!element) {
        return;
    }
    element.textContent = value;
};

document.addEventListener("DOMContentLoaded", async () => {
    const lastRead = LastReadStore.get();
    if (lastRead) {
        const verseUrl = new URL("/web/bible/verse", window.location.origin);
        verseUrl.searchParams.set("translationId", lastRead.translationId);
        verseUrl.searchParams.set("bookOrder", lastRead.bookOrder);
        verseUrl.searchParams.set("chapterNumber", lastRead.chapterNumber);
        window.location.replace(`${verseUrl.pathname}${verseUrl.search}`);
        return;
    }

    const dailyVerseText = document.getElementById("dailyVerseText");
    const dailyVerseReference = document.getElementById("dailyVerseReference");

    const setDailyVerseFallback = (message) => {
        updateText(dailyVerseText, message);
        updateText(dailyVerseReference, "개역한글(KRV)");
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
        } catch (error) {
            setDailyVerseFallback("오늘의 말씀을 불러오지 못했습니다.");
        } finally {
            dailyVerseText.style.opacity = 1;
            dailyVerseReference.style.opacity = 1;
        }
    };

    loadDailyVerse();

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
