import {TranslationStore} from "/js/storage-util.js?v=2.2";

const UI_CLASSES = {
    HIDDEN: "d-none"
};

const CONFIG = {
    DEV_CLICK_THRESHOLD: 12
};

const LABELS = {
    LANGUAGE: {
        ko: "한국어",
        en: "영어",
        zh: "중국어",
        ja: "일본어",
        es: "스페인어",
        de: "독일어",
        la: "라틴어"
    },
    TITLE: "성경 번역본"
};

const DomHelper = {
    getElements: () => {
        const get = id => document.getElementById(id);
        return {
            translationsContainer: get("translationSections"),
            sourceList: get("translationList"),
            pageTitleLabel: get("pageTitleLabel")
        };
    }
};

const App = {
    elements: null,
    state: {
        returnPath: "/web/bible/book",
        translationButtons: []
    },

    init: () => {
        App.elements = DomHelper.getElements();
        if (!App.elements.sourceList) {
            return;
        }
        App.state.returnPath = TranslationStore.consumeTranslationReturnPath();
        App.state.translationButtons = Array.from(App.elements.sourceList.querySelectorAll("button"));
        App.initDevToggle();
        App.updatePageTitle();
        App.renderSections();
        App.hideSourceList();
        App.bindSelectionHandlers();
    },

    initDevToggle: () => {
        const params = new URLSearchParams(window.location.search);
        const devParam = params.get("dev");
        const devEnabled = devParam === "1" || devParam === "true";
        if (devEnabled) {
            params.delete("dev");
            const nextUrl = `${window.location.pathname}?${params.toString()}`.replace(/\?$/, "");
            window.history.replaceState({}, "", nextUrl);
        } else {
            App.attachDevClickCounter();
        }
    },

    attachDevClickCounter: () => {
        let clickCount = 0;
        document.addEventListener("click", () => {
            clickCount += 1;
            if (clickCount < CONFIG.DEV_CLICK_THRESHOLD) {
                return;
            }
            const url = new URL(window.location.href);
            url.searchParams.set("dev", "1");
            window.location.replace(`${url.pathname}${url.search}${url.hash}`);
        });
    },

    updatePageTitle: () => {
        const {pageTitleLabel} = App.elements;
        if (!pageTitleLabel) {
            return;
        }
        pageTitleLabel.textContent = LABELS.TITLE;
        pageTitleLabel.classList.remove(UI_CLASSES.HIDDEN);
    },

    groupButtonsByLanguage: () => {
        const orderedLanguages = [];
        const groupedByLanguage = {};
        App.state.translationButtons.forEach(btn => {
            const language = btn.dataset.translationLanguage || "unknown";
            if (!groupedByLanguage[language]) {
                groupedByLanguage[language] = [];
                orderedLanguages.push(language);
            }
            groupedByLanguage[language].push(btn);
        });
        return {orderedLanguages, groupedByLanguage};
    },

    renderSections: () => {
        const {translationsContainer} = App.elements;
        if (!translationsContainer) {
            return;
        }
        const {orderedLanguages, groupedByLanguage} = App.groupButtonsByLanguage();
        translationsContainer.innerHTML = "";
        orderedLanguages.forEach(language => {
            const section = document.createElement("section");
            section.className = "translation-section";

            const header = document.createElement("div");
            header.className = "translation-section-header";

            const title = document.createElement("h2");
            const titleId = `translationLang-${language}`;
            title.id = titleId;
            title.className = "translation-section-title";
            title.textContent = LABELS.LANGUAGE[language] || language.toUpperCase();

            const count = document.createElement("span");
            count.className = "translation-section-count";
            count.textContent = `${groupedByLanguage[language].length}개`;

            header.appendChild(title);
            header.appendChild(count);

            const listGroup = document.createElement("div");
            listGroup.className = "list-group";
            listGroup.setAttribute("aria-labelledby", titleId);
            groupedByLanguage[language].forEach(btn => listGroup.appendChild(btn));

            section.appendChild(header);
            section.appendChild(listGroup);
            translationsContainer.appendChild(section);
        });
    },

    hideSourceList: () => {
        const {sourceList} = App.elements;
        if (!sourceList) {
            return;
        }
        sourceList.classList.add(UI_CLASSES.HIDDEN);
        sourceList.setAttribute("aria-hidden", "true");
    },

    bindSelectionHandlers: () => {
        App.state.translationButtons.forEach(btn => {
            btn.addEventListener("click", () => {
                const {translationId: id, translationName: name, translationType: type, translationLanguage: language} = btn.dataset;
                const targetUrl = new URL(App.state.returnPath, window.location.origin);
                targetUrl.searchParams.set("translationId", id);
                TranslationStore.saveCurrentTranslation({id, name, type, language});
                window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
            });
        });
    }
};

document.addEventListener("DOMContentLoaded", App.init);
