import {LastReadStore, SessionStore, STORAGE_KEYS} from "/js/storage-util.js?v=2.2";
import {initUniverse} from "/js/home/universe-bg.js?v=1.4";

const HERO_INTERVAL_MS = 5000;
const HERO_SWIPE_THRESHOLD = 50;

const initHeroCarousel = () => {
    const track = document.getElementById("heroTrack");
    if (!track) {
        return;
    }
    const slides = track.querySelectorAll(".home-hero-slide");
    const dots = track.parentElement.querySelectorAll(".home-hero-dot");
    if (slides.length < 2) {
        return;
    }

    let current = 0;
    let timer = null;
    let touchStartX = 0;

    const goTo = (index) => {
        current = (index + slides.length) % slides.length;
        track.style.transform = `translateX(-${current * 100}%)`;
        dots.forEach((dot, i) => {
            const isActive = i === current;
            dot.classList.toggle("active", isActive);
            dot.setAttribute("aria-selected", String(isActive));
        });
    };

    const resetTimer = () => {
        clearInterval(timer);
        timer = setInterval(() => goTo(current + 1), HERO_INTERVAL_MS);
    };

    dots.forEach((dot) => {
        dot.addEventListener("click", () => {
            goTo(Number(dot.dataset.slide));
            resetTimer();
        });
    });

    track.parentElement.addEventListener("touchstart", (e) => {
        touchStartX = e.touches[0].clientX;
    }, {passive: true});

    track.parentElement.addEventListener("touchend", (e) => {
        const diff = touchStartX - e.changedTouches[0].clientX;
        if (Math.abs(diff) > HERO_SWIPE_THRESHOLD) {
            goTo(diff > 0 ? current + 1 : current - 1);
            resetTimer();
        }
    }, {passive: true});

    resetTimer();
};

document.addEventListener("DOMContentLoaded", () => {
    const homeVisited = SessionStore.get(STORAGE_KEYS.HOME_VISITED);
    const lastRead = LastReadStore.get();
    if (lastRead && !homeVisited) {
        SessionStore.set(STORAGE_KEYS.HOME_VISITED, true);
        const verseUrl = new URL("/web/bible/verse", window.location.origin);
        verseUrl.searchParams.set("translationId", lastRead.translationId);
        verseUrl.searchParams.set("bookOrder", lastRead.bookOrder);
        verseUrl.searchParams.set("chapterNumber", lastRead.chapterNumber);
        window.location.replace(`${verseUrl.pathname}${verseUrl.search}`);
        return;
    }
    SessionStore.set(STORAGE_KEYS.HOME_VISITED, true);

    initHeroCarousel();
    initUniverse("universeCanvas", "universeSection");

    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "ElSeeker";
        pageTitleLabel.classList.remove("d-none");
    }
});
