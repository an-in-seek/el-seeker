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
        App.initTop3MoreLink();
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
        const feedTop3 = document.getElementById("feedTop3");
        const feedList = document.querySelector(".feed-list");
        const emptyState = document.querySelector(".feed-empty");

        if (tabs.length === 0) return;

        tabs.forEach(tab => {
            tab.addEventListener("click", () => {
                // Update tab states
                tabs.forEach(t => {
                    t.classList.remove("active");
                    t.setAttribute("aria-selected", "false");
                });
                tab.classList.add("active");
                tab.setAttribute("aria-selected", "true");

                const category = tab.dataset.category;

                // Show/hide TOP3 section (only in "전체" tab)
                const mobileTop3 = document.getElementById("mobileTop3");
                
                if (feedTop3) {
                    feedTop3.classList.toggle("hidden", category !== "all");
                }
                if (mobileTop3) {
                    mobileTop3.classList.toggle("hidden", category !== "all");
                }

                // Handle feed filtering and sorting
                App.filterAndSortFeed(category, feedCards, feedList, emptyState);
            });
        });
    },

    filterAndSortFeed: (category, feedCards, feedList, emptyState) => {
        const cardsArray = Array.from(feedCards);
        let visibleCount = 0;

        // For "인기" tab, sort by likes descending
        if (category === "인기") {
            cardsArray.sort((a, b) => {
                const aLikes = parseInt(a.dataset.likes) || 0;
                const bLikes = parseInt(b.dataset.likes) || 0;
                return bLikes - aLikes;
            });

            // Re-append in sorted order
            cardsArray.forEach(card => {
                feedList.appendChild(card);
                card.classList.remove("hidden");
                visibleCount++;
            });
        } else {
            // For other tabs, filter by category
            cardsArray.forEach(card => {
                const cardCategory = card.dataset.category;
                const isVisible = category === "all" || cardCategory === category;

                card.classList.toggle("hidden", !isVisible);
                if (isVisible) visibleCount++;
            });
        }

        // Keep empty state at the end and update visibility
        if (emptyState) {
            feedList.appendChild(emptyState);
            emptyState.style.display = visibleCount === 0 ? "block" : "none";
        }
    },

    initTop3MoreLink: () => {
        const moreLink = document.querySelector(".top3-more");
        if (!moreLink) return;

        moreLink.addEventListener("click", (e) => {
            e.preventDefault();

            // Find and click the "인기" tab
            const popularTab = document.querySelector('.community-tab[data-category="인기"]');
            if (popularTab) {
                popularTab.click();
            }
        });
    }
};

document.addEventListener("DOMContentLoaded", App.init);
