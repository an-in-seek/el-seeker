const UI_CLASSES = {
    HIDDEN: "d-none"
};

const App = {
    init: () => {
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "커뮤니티";
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }

        App.initComingSoon();
        App.initCategoryTabs();
        App.initSortToggle();
    },

    initComingSoon: () => {
        const comingSoonCards = document.querySelectorAll(".coming-soon");
        comingSoonCards.forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();
            });
        });
    },

    initCategoryTabs: () => {
        const tabs = document.querySelectorAll(".community-tab");
        const feedCards = document.querySelectorAll(".feed-card[data-category]");
        const emptyState = document.querySelector(".feed-empty");

        if (tabs.length === 0) return;

        tabs.forEach(tab => {
            tab.addEventListener("click", () => {
                tabs.forEach(t => {
                    t.classList.remove("active");
                    t.setAttribute("aria-selected", "false");
                });
                tab.classList.add("active");
                tab.setAttribute("aria-selected", "true");

                const category = tab.dataset.category;
                let visibleCount = 0;

                feedCards.forEach(card => {
                    if (category === "all" || card.dataset.category === category) {
                        card.classList.remove("hidden");
                        visibleCount++;
                    } else {
                        card.classList.add("hidden");
                    }
                });

                if (emptyState) {
                    emptyState.style.display = visibleCount === 0 ? "block" : "none";
                }
            });
        });
    },

    initSortToggle: () => {
        const sortBtns = document.querySelectorAll(".feed-sort-btn");
        const feedList = document.querySelector(".feed-list");

        if (sortBtns.length === 0 || !feedList) return;

        sortBtns.forEach(btn => {
            btn.addEventListener("click", () => {
                sortBtns.forEach(b => b.classList.remove("active"));
                btn.classList.add("active");

                const sortType = btn.dataset.sort;
                App.sortFeed(feedList, sortType);
            });
        });
    },

    sortFeed: (feedList, sortType) => {
        const feedCards = Array.from(feedList.querySelectorAll(".feed-card[data-category]"));
        const emptyState = feedList.querySelector(".feed-empty");

        feedCards.sort((a, b) => {
            if (sortType === "popular") {
                const aLikes = parseInt(a.dataset.likes) || 0;
                const bLikes = parseInt(b.dataset.likes) || 0;
                return bLikes - aLikes;
            } else {
                // latest - based on DOM order (assumes initial order is latest)
                // For demo, we'll reverse the popular sort
                const aLikes = parseInt(a.dataset.likes) || 0;
                const bLikes = parseInt(b.dataset.likes) || 0;
                return aLikes - bLikes;
            }
        });

        // Re-append in sorted order
        feedCards.forEach(card => feedList.appendChild(card));

        // Keep empty state at the end
        if (emptyState) {
            feedList.appendChild(emptyState);
        }
    }
};

document.addEventListener("DOMContentLoaded", App.init);
