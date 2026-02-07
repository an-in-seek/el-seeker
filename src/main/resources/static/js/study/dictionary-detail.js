const UI_CLASSES = {
    HIDDEN: "d-none"
};

const DomHelper = {
    getElements: () => {
        const get = id => document.getElementById(id);
        return {
            backButton: get("topNavBackButton"),
            pageTitleLabel: get("pageTitleLabel"),
            relatedVerses: document.querySelector(".related-verses-content")
        };
    }
};

const App = {
    elements: null,
    init: () => {
        App.elements = DomHelper.getElements();
        App.initNav();
        App.renderRelatedVersesTags();
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
    },

    renderRelatedVersesTags: () => {
        const {relatedVerses} = App.elements;
        if (!relatedVerses || relatedVerses.classList.contains("empty-text")) {
            return;
        }
        const rawText = relatedVerses.textContent?.trim();
        if (!rawText) {
            return;
        }
        const parts = rawText
            .split(/\s*,\s*|\n+/)
            .map(part => part.trim())
            .filter(Boolean);
        if (parts.length === 0) {
            return;
        }
        const container = document.createElement("div");
        container.className = "related-verses-tags";
        parts.forEach(part => {
            const tag = document.createElement("span");
            tag.className = "related-verse-tag";
            tag.textContent = part;
            tag.title = part;
            container.appendChild(tag);
        });
        relatedVerses.replaceChildren(container);
    }
};

document.addEventListener("DOMContentLoaded", App.init);
