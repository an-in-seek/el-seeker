const UI_CLASSES = {
    HIDDEN: "d-none"
};

const DomHelper = {
    getElements: () => {
        const get = id => document.getElementById(id);
        return {
            backButton: get("topNavBackButton"),
            pageTitleLabel: get("pageTitleLabel")
        };
    }
};

const App = {
    elements: null,
    init: () => {
        App.elements = DomHelper.getElements();
        App.initNav();
    },

    initNav: () => {
        const {backButton, pageTitleLabel} = App.elements;
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 사전";
            pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
        }
        if (backButton) {
            backButton.classList.remove(UI_CLASSES.HIDDEN);
            backButton.addEventListener("click", () => {
                const backLink = document.body.dataset.backLink || "/web/study";
                window.location.href = backLink;
            });
        }
    }
};

document.addEventListener("DOMContentLoaded", App.init);
