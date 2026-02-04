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

        const comingSoonCards = document.querySelectorAll(".coming-soon");
        if (comingSoonCards.length === 0) {
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
