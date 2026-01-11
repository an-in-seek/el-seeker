import {TranslationStore} from "/js/storage-util.js?v=2.1";

document.addEventListener("DOMContentLoaded", () => {

    const DEV_CLICK_THRESHOLD = 12;
    const returnPath = TranslationStore.consumeTranslationReturnPath();
    const translationsContainer = document.getElementById("translationSections");
    const sourceList = document.getElementById("translationList");
    const translationButtons = sourceList ? Array.from(sourceList.querySelectorAll("button")) : [];
    const languageLabelMap = {
        ko: "한국어",
        en: "영어",
        zh: "중국어",
        ja: "일본어",
        es: "스페인어",
        de: "독일어",
        la: "라틴어"
    };

    const isDevParamPresent = () => {
        const params = new URLSearchParams(window.location.search);
        return params.get("dev") === "1" || params.get("dev") === "true";
    };

    const stripDevParam = () => {
        const url = new URL(window.location.href);
        if (!url.searchParams.has("dev")) {
            return;
        }
        url.searchParams.delete("dev");
        const nextUrl = `${url.pathname}${url.search}${url.hash}`;
        window.history.replaceState({}, "", nextUrl);
    };

    const redirectToDevMode = () => {
        const url = new URL(window.location.href);
        url.searchParams.set("dev", "1");
        window.location.replace(`${url.pathname}${url.search}${url.hash}`);
    };

    const attachDevClickCounter = () => {
        let clickCount = 0;
        document.addEventListener("click", () => {
            clickCount += 1;
            if (clickCount < DEV_CLICK_THRESHOLD) {
                return;
            }
            redirectToDevMode();
        });
    };

    const updatePageTitle = () => {
        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (!pageTitleLabel) {
            return;
        }
        pageTitleLabel.textContent = "성경 번역본";
        pageTitleLabel.classList.remove("d-none");
    };

    const groupButtonsByLanguage = buttons => {
        const orderedLanguages = [];
        const groupedByLanguage = {};
        buttons.forEach(btn => {
            const language = btn.dataset.translationLanguage || "unknown";
            if (!groupedByLanguage[language]) {
                groupedByLanguage[language] = [];
                orderedLanguages.push(language);
            }
            groupedByLanguage[language].push(btn);
        });
        return {orderedLanguages, groupedByLanguage};
    };

    const renderSections = (container, orderedLanguages, groupedByLanguage) => {
        if (!container) {
            return;
        }
        container.innerHTML = "";
        orderedLanguages.forEach(language => {
            const section = document.createElement("section");
            section.className = "translation-section";

            const header = document.createElement("div");
            header.className = "translation-section-header";

            const title = document.createElement("h2");
            const titleId = `translationLang-${language}`;
            title.id = titleId;
            title.className = "translation-section-title";
            title.textContent = languageLabelMap[language] || language.toUpperCase();

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
            container.appendChild(section);
        });
    };

    const hideSourceList = list => {
        if (!list) {
            return;
        }
        list.classList.add("d-none");
        list.setAttribute("aria-hidden", "true");
    };

    const attachSelectionHandlers = (buttons, fallbackPath) => {
        buttons.forEach(btn => {
            btn.addEventListener("click", () => {
                const {translationId: id, translationName: name, translationType: type, translationLanguage: language} = btn.dataset;
                const targetUrl = new URL(fallbackPath, window.location.origin);
                targetUrl.searchParams.set("translationId", id);
                TranslationStore.saveCurrentTranslation({id, name, type, language});
                window.location.href = `${targetUrl.pathname}${targetUrl.search}`;
            });
        });
    };

    const init = () => {
        const devEnabled = isDevParamPresent();
        if (devEnabled) {
            stripDevParam();
        } else {
            attachDevClickCounter();
        }
        updatePageTitle();
        const {orderedLanguages, groupedByLanguage} = groupButtonsByLanguage(translationButtons);
        renderSections(translationsContainer, orderedLanguages, groupedByLanguage);
        hideSourceList(sourceList);
        attachSelectionHandlers(translationButtons, returnPath);
    }

    init();
});
