const UI_CLASSES = {
    HIDDEN: "d-none"
};

const DomHelper = {
    getElements: () => {
        return {
            pageTitleLabel: document.getElementById("pageTitleLabel"),
            comingSoonCards: document.querySelectorAll(".coming-soon")
        };
    }
};

const App = {
    elements: null,
    init: () => {
        App.elements = DomHelper.getElements();
        App.initNav();
        App.bindEvents();
    },

    initNav: () => {
        const {pageTitleLabel} = App.elements;
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "학습";
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }
    },

    bindEvents: () => {
        const {comingSoonCards} = App.elements;
        if (!comingSoonCards || comingSoonCards.length === 0) {
            return;
        }
        comingSoonCards.forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();
            });
        });
    }
};

document.addEventListener("DOMContentLoaded", App.init);
