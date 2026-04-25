document.addEventListener("DOMContentLoaded", () => {
    const wrapper = document.querySelector(".book-search-wrapper");
    if (!wrapper || !("IntersectionObserver" in window)) {
        return;
    }

    const sentinel = document.createElement("div");
    sentinel.className = "book-search-sentinel";
    sentinel.setAttribute("aria-hidden", "true");
    wrapper.parentNode.insertBefore(sentinel, wrapper);

    const rootStyles = getComputedStyle(document.documentElement);
    const navHeight = parseInt(rootStyles.getPropertyValue("--top-nav-height"), 10) || 52;

    const observer = new IntersectionObserver(
        ([entry]) => wrapper.classList.toggle("is-stuck", !entry.isIntersecting),
        {threshold: 0, rootMargin: `-${navHeight + 1}px 0px 0px 0px`}
    );
    observer.observe(sentinel);
});
