/**
 * 성경 족보 — 하나님 → 예수 그리스도
 */

const GENEALOGY_SECTIONS = [
    {
        id: "creation-to-flood",
        title: "창조에서 대홍수까지",
        subtitle: "창세기 1-9장",
        people: [
            {id: 1, name: "하나님", parentId: null, generation: 0, highlight: true, note: "창조주"},
            {id: 2, name: "아담", parentId: 1, generation: 1, highlight: true, note: "첫 번째 사람"},
            {id: 3, name: "셋", parentId: 2, generation: 2, highlight: false, note: "아담의 셋째 아들"},
            {id: 4, name: "에노스", parentId: 3, generation: 3, highlight: false, note: ""},
            {id: 5, name: "게난", parentId: 4, generation: 4, highlight: false, note: ""},
            {id: 6, name: "마할랄렐", parentId: 5, generation: 5, highlight: false, note: ""},
            {id: 7, name: "야렛", parentId: 6, generation: 6, highlight: false, note: ""},
            {id: 8, name: "에녹", parentId: 7, generation: 7, highlight: false, note: "하나님과 동행하다 옮겨짐"},
            {id: 9, name: "므두셀라", parentId: 8, generation: 8, highlight: false, note: "성경에서 가장 오래 산 사람 (969세)"},
            {id: 10, name: "라멕", parentId: 9, generation: 9, highlight: false, note: ""},
            {id: 11, name: "노아", parentId: 10, generation: 10, highlight: true, note: "방주를 지어 대홍수에서 살아남음"},
        ]
    },
    {
        id: "flood-to-abraham",
        title: "대홍수에서 아브라함까지",
        subtitle: "창세기 10-11장",
        people: [
            {id: 12, name: "셈", parentId: 11, generation: 11, highlight: false, note: "노아의 아들"},
            {id: 13, name: "아르박삿", parentId: 12, generation: 12, highlight: false, note: ""},
            {id: 14, name: "셀라", parentId: 13, generation: 13, highlight: false, note: ""},
            {id: 15, name: "에벨", parentId: 14, generation: 14, highlight: false, note: "'히브리'의 어원"},
            {id: 16, name: "벨렉", parentId: 15, generation: 15, highlight: false, note: ""},
            {id: 17, name: "르우", parentId: 16, generation: 16, highlight: false, note: ""},
            {id: 18, name: "스룩", parentId: 17, generation: 17, highlight: false, note: ""},
            {id: 19, name: "나홀", parentId: 18, generation: 18, highlight: false, note: ""},
            {id: 20, name: "데라", parentId: 19, generation: 19, highlight: false, note: ""},
            {id: 21, name: "아브라함", parentId: 20, generation: 20, highlight: true, note: "믿음의 조상, 하나님의 언약"},
        ]
    },
    {
        id: "patriarchs",
        title: "족장 시대",
        subtitle: "창세기 12-50장",
        people: [
            {id: 22, name: "이삭", parentId: 21, generation: 21, highlight: true, note: "약속의 아들"},
            {id: 23, name: "야곱(이스라엘)", parentId: 22, generation: 22, highlight: true, note: "이스라엘 12지파의 아버지"},
            {id: 24, name: "유다", parentId: 23, generation: 23, highlight: true, note: "왕의 지파"},
            {id: 25, name: "베레스", parentId: 24, generation: 24, highlight: false, note: "유다와 다말의 아들"},
            {id: 26, name: "헤스론", parentId: 25, generation: 25, highlight: false, note: ""},
            {id: 27, name: "람", parentId: 26, generation: 26, highlight: false, note: ""},
            {id: 28, name: "암미나답", parentId: 27, generation: 27, highlight: false, note: ""},
            {id: 29, name: "나손", parentId: 28, generation: 28, highlight: false, note: ""},
            {id: 30, name: "살몬", parentId: 29, generation: 29, highlight: false, note: ""},
            {id: 31, name: "보아스", parentId: 30, generation: 30, highlight: false, note: "룻의 남편"},
            {id: 32, name: "오벳", parentId: 31, generation: 31, highlight: false, note: ""},
            {id: 33, name: "이새", parentId: 32, generation: 32, highlight: false, note: ""},
        ]
    },
    {
        id: "kingdom",
        title: "왕국 시대",
        subtitle: "사무엘하 ~ 열왕기",
        people: [
            {id: 34, name: "다윗", parentId: 33, generation: 33, highlight: true, note: "이스라엘의 왕, 하나님의 마음에 합한 자"},
            {id: 35, name: "솔로몬", parentId: 34, generation: 34, highlight: true, note: "지혜의 왕, 성전 건축"},
            {id: 36, name: "르호보암", parentId: 35, generation: 35, highlight: false, note: "남유다 초대 왕"},
            {id: 37, name: "아비야", parentId: 36, generation: 36, highlight: false, note: ""},
            {id: 38, name: "아사", parentId: 37, generation: 37, highlight: false, note: ""},
            {id: 39, name: "여호사밧", parentId: 38, generation: 38, highlight: false, note: ""},
            {id: 40, name: "요람", parentId: 39, generation: 39, highlight: false, note: ""},
            {id: 41, name: "웃시야", parentId: 40, generation: 40, highlight: false, note: ""},
            {id: 42, name: "요담", parentId: 41, generation: 41, highlight: false, note: ""},
            {id: 43, name: "아하스", parentId: 42, generation: 42, highlight: false, note: ""},
            {id: 44, name: "히스기야", parentId: 43, generation: 43, highlight: false, note: "개혁 왕"},
            {id: 45, name: "므낫세", parentId: 44, generation: 44, highlight: false, note: ""},
            {id: 46, name: "아몬", parentId: 45, generation: 45, highlight: false, note: ""},
            {id: 47, name: "요시야", parentId: 46, generation: 46, highlight: false, note: "율법서 발견, 개혁"},
        ]
    },
    {
        id: "exile-to-christ",
        title: "포로기에서 예수 그리스도까지",
        subtitle: "마태복음 1장",
        people: [
            {id: 48, name: "여고냐", parentId: 47, generation: 47, highlight: false, note: "바벨론 포로기"},
            {id: 49, name: "스알디엘", parentId: 48, generation: 48, highlight: false, note: ""},
            {id: 50, name: "스룹바벨", parentId: 49, generation: 49, highlight: false, note: "성전 재건 지도자"},
            {id: 51, name: "아비훗", parentId: 50, generation: 50, highlight: false, note: ""},
            {id: 52, name: "엘리아김", parentId: 51, generation: 51, highlight: false, note: ""},
            {id: 53, name: "아소르", parentId: 52, generation: 52, highlight: false, note: ""},
            {id: 54, name: "사독", parentId: 53, generation: 53, highlight: false, note: ""},
            {id: 55, name: "아킴", parentId: 54, generation: 54, highlight: false, note: ""},
            {id: 56, name: "엘리웃", parentId: 55, generation: 55, highlight: false, note: ""},
            {id: 57, name: "엘르아살", parentId: 56, generation: 56, highlight: false, note: ""},
            {id: 58, name: "맛단", parentId: 57, generation: 57, highlight: false, note: ""},
            {id: 59, name: "야곱", parentId: 58, generation: 58, highlight: false, note: ""},
            {id: 60, name: "요셉", parentId: 59, generation: 59, highlight: false, note: "마리아의 남편"},
            {id: 61, name: "예수 그리스도", parentId: 60, generation: 60, highlight: true, note: "하나님의 아들, 구세주"},
        ]
    }
];

class BibleGenealogy {
    constructor() {
        this.initElements();
        this.init();
    }

    initElements() {
        this.loadingEl = document.getElementById("genealogyLoading");
        this.contentEl = document.getElementById("genealogyContent");
        this.sectionsEl = document.getElementById("genealogySections");
        this.backButton = document.getElementById("topNavBackButton");
    }

    init() {
        this.initNav();
        this.render();
    }

    initNav() {
        if (!this.backButton) return;

        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "성경 족보";
            pageTitleLabel.classList.remove("d-none");
        }
        this.backButton.classList.remove("d-none");
        this.backButton.addEventListener("click", () => {
            window.location.href = "/web/study";
        });
    }

    render() {
        GENEALOGY_SECTIONS.forEach((section, sectionIdx) => {
            const sectionEl = document.createElement("section");
            sectionEl.className = "genealogy-section";
            sectionEl.setAttribute("aria-label", section.title);

            const isCollapsible = section.people.length > 5;
            const collapsedCount = 3;

            sectionEl.innerHTML = `
                <div class="genealogy-section-header">
                    <h2 class="genealogy-section-title">${section.title}</h2>
                    <span class="genealogy-section-subtitle">${section.subtitle}</span>
                </div>
                <div class="genealogy-timeline" id="timeline-${section.id}"></div>
            `;

            this.sectionsEl.appendChild(sectionEl);

            const timeline = sectionEl.querySelector(`#timeline-${section.id}`);
            this.renderTimeline(timeline, section, sectionIdx);
        });

        this.loadingEl.classList.add("d-none");
        this.contentEl.classList.remove("d-none");
    }

    renderTimeline(container, section, sectionIdx) {
        const people = section.people;
        const threshold = 6;
        const showCollapse = people.length > threshold;
        const visibleInitial = 3;

        people.forEach((person, idx) => {
            const isFirst = idx === 0 && sectionIdx === 0;
            const isLast = idx === people.length - 1 && sectionIdx === GENEALOGY_SECTIONS.length - 1;
            const isHidden = showCollapse && idx >= visibleInitial && idx < people.length - 2;

            if (idx > 0) {
                const connector = document.createElement("div");
                connector.className = "genealogy-connector";
                connector.setAttribute("aria-hidden", "true");
                if (isHidden) connector.classList.add("genealogy-collapsed-item", "d-none");
                container.appendChild(connector);
            }

            const node = this.createNode(person, isFirst, isLast);
            if (isHidden) node.classList.add("genealogy-collapsed-item", "d-none");
            container.appendChild(node);

            if (showCollapse && idx === visibleInitial - 1) {
                const hiddenCount = people.length - visibleInitial - 2;
                const toggle = document.createElement("button");
                toggle.className = "genealogy-toggle";
                toggle.setAttribute("aria-expanded", "false");
                toggle.innerHTML = `<span class="genealogy-toggle-icon">+</span> ${hiddenCount}명 더 보기`;
                toggle.addEventListener("click", () => {
                    const expanded = toggle.getAttribute("aria-expanded") === "true";
                    const items = container.querySelectorAll(".genealogy-collapsed-item");
                    items.forEach(item => item.classList.toggle("d-none", expanded));
                    toggle.setAttribute("aria-expanded", String(!expanded));
                    toggle.innerHTML = expanded
                        ? `<span class="genealogy-toggle-icon">+</span> ${hiddenCount}명 더 보기`
                        : `<span class="genealogy-toggle-icon">−</span> 접기`;
                });
                container.appendChild(toggle);

                const connectorAfterToggle = document.createElement("div");
                connectorAfterToggle.className = "genealogy-connector genealogy-collapsed-item d-none";
                connectorAfterToggle.setAttribute("aria-hidden", "true");
                container.appendChild(connectorAfterToggle);
            }
        });
    }

    createNode(person, isFirst, isLast) {
        const node = document.createElement("div");
        node.className = "genealogy-node";
        if (person.highlight) node.classList.add("is-highlight");
        if (isFirst) node.classList.add("is-origin");
        if (isLast) node.classList.add("is-final");

        const noteHtml = person.note
            ? `<p class="genealogy-node-note">${person.note}</p>`
            : "";

        node.setAttribute("role", "listitem");
        node.setAttribute("aria-label", `${person.name}${person.note ? ', ' + person.note : ''}`);

        node.innerHTML = `
            <div class="genealogy-node-content">
                <span class="genealogy-node-name">${person.name}</span>
                ${noteHtml}
            </div>
        `;

        return node;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    new BibleGenealogy();
});
