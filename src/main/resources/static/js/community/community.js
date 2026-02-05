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
    }
};

document.addEventListener("DOMContentLoaded", App.init);
