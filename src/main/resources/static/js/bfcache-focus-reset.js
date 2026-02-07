// Clear focus on bfcache restore to avoid touch shadow on mobile.
window.addEventListener("pageshow", (event) => {
    if (!event.persisted) return;
    const active = document.activeElement;
    if (active && typeof active.blur === "function") {
        active.blur();
    }
});
