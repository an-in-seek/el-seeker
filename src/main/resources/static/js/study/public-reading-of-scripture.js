/**
 * 공동체성경읽기(PRS) - 드라마바이블 영상 목록
 */

const PRS_VIDEOS = [
    // ── 구약 (39권) ──
    {bookOrder: 1,  bookName: "창세기",       youtubeUrl: "https://youtu.be/NbGHNcPhlUY?si=h9WLvhkuNUyrOYXz"},
    {bookOrder: 2,  bookName: "출애굽기",     youtubeUrl: "https://youtu.be/pPqhb_cVZT8?si=6OP8XpUnPxaPxguN"},
    {bookOrder: 3,  bookName: "레위기",       youtubeUrl: "https://youtu.be/w5_gU3NnsVw?si=hKV8aXPVdB4dixWI"},
    {bookOrder: 4,  bookName: "민수기",       youtubeUrl: "https://youtu.be/nyw5Qki5SIw?si=TbN0jHOJ7FveGDr7"},
    {bookOrder: 5,  bookName: "신명기",       youtubeUrl: "https://youtu.be/hRRWuQHVTe0?si=d8tnP2IsufUPllFf"},
    {bookOrder: 6,  bookName: "여호수아",     youtubeUrl: ""},
    {bookOrder: 7,  bookName: "사사기",       youtubeUrl: ""},
    {bookOrder: 8,  bookName: "룻기",         youtubeUrl: ""},
    {bookOrder: 9,  bookName: "사무엘상",     youtubeUrl: ""},
    {bookOrder: 10, bookName: "사무엘하",     youtubeUrl: ""},
    {bookOrder: 11, bookName: "열왕기상",     youtubeUrl: ""},
    {bookOrder: 12, bookName: "열왕기하",     youtubeUrl: ""},
    {bookOrder: 13, bookName: "역대상",       youtubeUrl: ""},
    {bookOrder: 14, bookName: "역대하",       youtubeUrl: ""},
    {bookOrder: 15, bookName: "에스라",       youtubeUrl: ""},
    {bookOrder: 16, bookName: "느헤미야",     youtubeUrl: ""},
    {bookOrder: 17, bookName: "에스더",       youtubeUrl: ""},
    {bookOrder: 18, bookName: "욥기",         youtubeUrl: ""},
    {bookOrder: 19, bookName: "시편",         youtubeUrl: ""},
    {bookOrder: 20, bookName: "잠언",         youtubeUrl: ""},
    {bookOrder: 21, bookName: "전도서",       youtubeUrl: ""},
    {bookOrder: 22, bookName: "아가",         youtubeUrl: ""},
    {bookOrder: 23, bookName: "이사야",       youtubeUrl: ""},
    {bookOrder: 24, bookName: "예레미야",     youtubeUrl: ""},
    {bookOrder: 25, bookName: "예레미야애가", youtubeUrl: ""},
    {bookOrder: 26, bookName: "에스겔",       youtubeUrl: ""},
    {bookOrder: 27, bookName: "다니엘",       youtubeUrl: ""},
    {bookOrder: 28, bookName: "호세아",       youtubeUrl: ""},
    {bookOrder: 29, bookName: "요엘",         youtubeUrl: ""},
    {bookOrder: 30, bookName: "아모스",       youtubeUrl: ""},
    {bookOrder: 31, bookName: "오바댜",       youtubeUrl: ""},
    {bookOrder: 32, bookName: "요나",         youtubeUrl: ""},
    {bookOrder: 33, bookName: "미가",         youtubeUrl: ""},
    {bookOrder: 34, bookName: "나훔",         youtubeUrl: ""},
    {bookOrder: 35, bookName: "하박국",       youtubeUrl: ""},
    {bookOrder: 36, bookName: "스바냐",       youtubeUrl: ""},
    {bookOrder: 37, bookName: "학개",         youtubeUrl: ""},
    {bookOrder: 38, bookName: "스가랴",       youtubeUrl: ""},
    {bookOrder: 39, bookName: "말라기",       youtubeUrl: ""},
    // ── 신약 (27권) ──
    {bookOrder: 40, bookName: "마태복음",       youtubeUrl: ""},
    {bookOrder: 41, bookName: "마가복음",       youtubeUrl: ""},
    {bookOrder: 42, bookName: "누가복음",       youtubeUrl: ""},
    {bookOrder: 43, bookName: "요한복음",       youtubeUrl: ""},
    {bookOrder: 44, bookName: "사도행전",       youtubeUrl: ""},
    {bookOrder: 45, bookName: "로마서",         youtubeUrl: ""},
    {bookOrder: 46, bookName: "고린도전서",     youtubeUrl: ""},
    {bookOrder: 47, bookName: "고린도후서",     youtubeUrl: ""},
    {bookOrder: 48, bookName: "갈라디아서",     youtubeUrl: ""},
    {bookOrder: 49, bookName: "에베소서",       youtubeUrl: ""},
    {bookOrder: 50, bookName: "빌립보서",       youtubeUrl: ""},
    {bookOrder: 51, bookName: "골로새서",       youtubeUrl: ""},
    {bookOrder: 52, bookName: "데살로니가전서", youtubeUrl: ""},
    {bookOrder: 53, bookName: "데살로니가후서", youtubeUrl: ""},
    {bookOrder: 54, bookName: "디모데전서",     youtubeUrl: ""},
    {bookOrder: 55, bookName: "디모데후서",     youtubeUrl: ""},
    {bookOrder: 56, bookName: "디도서",         youtubeUrl: ""},
    {bookOrder: 57, bookName: "빌레몬서",       youtubeUrl: ""},
    {bookOrder: 58, bookName: "히브리서",       youtubeUrl: ""},
    {bookOrder: 59, bookName: "야고보서",       youtubeUrl: ""},
    {bookOrder: 60, bookName: "베드로전서",     youtubeUrl: ""},
    {bookOrder: 61, bookName: "베드로후서",     youtubeUrl: ""},
    {bookOrder: 62, bookName: "요한1서",        youtubeUrl: ""},
    {bookOrder: 63, bookName: "요한2서",        youtubeUrl: ""},
    {bookOrder: 64, bookName: "요한3서",        youtubeUrl: ""},
    {bookOrder: 65, bookName: "유다서",         youtubeUrl: ""},
    {bookOrder: 66, bookName: "요한계시록",     youtubeUrl: ""},
];

class PublicReadingOfScripture {
    constructor() {
        this.initElements();
        this.init();
    }

    initElements() {
        this.loadingEl = document.getElementById("videoLoading");
        this.contentEl = document.getElementById("videoContent");
        this.oldTestamentGrid = document.getElementById("oldTestamentGrid");
        this.newTestamentGrid = document.getElementById("newTestamentGrid");
        this.oldTestamentSection = document.getElementById("oldTestamentSection");
        this.newTestamentSection = document.getElementById("newTestamentSection");
        this.backButton = document.getElementById("topNavBackButton");
        this.bookSearchInput = document.getElementById("bookSearchInput");
        this.bookSearchClear = document.getElementById("bookSearchClear");
        this.bookSearchEmpty = document.getElementById("bookSearchEmpty");
    }

    init() {
        this.initNav();
        this.render();
        this.initBookSearch();
        this.scrollToTargetBook();
    }

    initNav() {
        if (!this.backButton) return;

        const pageTitleLabel = document.getElementById("pageTitleLabel");
        if (pageTitleLabel) {
            pageTitleLabel.textContent = "공동체성경읽기";
            pageTitleLabel.classList.remove("d-none");
        }
        this.backButton.classList.remove("d-none");

        const urlParams = new URLSearchParams(window.location.search);
        this.from = urlParams.get("from");

        this.backButton.addEventListener("click", () => {
            if (this.from === "chapter-list") {
                history.back();
                return;
            }
            window.location.href = "/web/study";
        });
    }

    render() {
        const oldTestament = PRS_VIDEOS.filter(b => b.bookOrder <= 39);
        const newTestament = PRS_VIDEOS.filter(b => b.bookOrder >= 40);

        oldTestament.forEach(book => this.oldTestamentGrid.appendChild(this.createCard(book)));
        newTestament.forEach(book => this.newTestamentGrid.appendChild(this.createCard(book)));

        this.loadingEl.classList.add("d-none");
        this.contentEl.classList.remove("d-none");
    }

    extractVideoId(url) {
        const match = url.match(/youtu\.be\/([^?]+)/);
        return match ? match[1] : null;
    }

    createCard(book) {
        const hasVideo = book.youtubeUrl !== "";

        if (hasVideo) {
            const videoId = this.extractVideoId(book.youtubeUrl);
            const thumbnailUrl = videoId ? `https://img.youtube.com/vi/${videoId}/mqdefault.jpg` : "";

            const link = document.createElement("a");
            link.className = "prs-card";
            link.dataset.bookOrder = book.bookOrder;
            link.href = book.youtubeUrl;
            link.target = "_blank";
            link.rel = "noopener noreferrer";
            link.setAttribute("aria-label", `${book.bookName} 영상 보기`);
            link.innerHTML = `
                <div class="prs-thumb">
                    <img src="${thumbnailUrl}" alt="" loading="lazy"
                         onerror="this.parentElement.classList.add('is-fallback');this.remove();">
                    <span class="prs-play" aria-hidden="true">▶</span>
                </div>
                <span class="prs-book-name">${book.bookName}</span>
            `;
            return link;
        }

        const div = document.createElement("div");
        div.className = "prs-card is-disabled";
        div.dataset.bookOrder = book.bookOrder;
        div.innerHTML = `
            <div class="prs-thumb is-fallback">
                <span class="prs-play" aria-hidden="true"></span>
            </div>
            <span class="prs-book-name">${book.bookName}</span>
            <span class="prs-badge">준비중</span>
        `;
        return div;
    }

    initBookSearch() {
        if (!this.bookSearchInput) return;

        this.bookSearchInput.addEventListener("input", () => {
            const keyword = this.bookSearchInput.value.trim();
            this.bookSearchClear.classList.toggle("d-none", keyword.length === 0);
            this.filterBooks(keyword);
        });

        this.bookSearchClear.addEventListener("click", () => {
            this.bookSearchInput.value = "";
            this.bookSearchClear.classList.add("d-none");
            this.filterBooks("");
            this.bookSearchInput.focus();
        });
    }

    filterBooks(keyword) {
        const filtered = keyword
            ? PRS_VIDEOS.filter(book => book.bookName.includes(keyword))
            : PRS_VIDEOS;

        const oldTestament = filtered.filter(b => b.bookOrder <= 39);
        const newTestament = filtered.filter(b => b.bookOrder >= 40);

        this.oldTestamentGrid.innerHTML = "";
        this.newTestamentGrid.innerHTML = "";
        oldTestament.forEach(book => this.oldTestamentGrid.appendChild(this.createCard(book)));
        newTestament.forEach(book => this.newTestamentGrid.appendChild(this.createCard(book)));

        this.oldTestamentSection.classList.toggle("d-none", oldTestament.length === 0);
        this.newTestamentSection.classList.toggle("d-none", newTestament.length === 0);

        if (!keyword) {
            this.oldTestamentSection.classList.remove("d-none");
            this.newTestamentSection.classList.remove("d-none");
        }

        if (this.bookSearchEmpty) {
            this.bookSearchEmpty.classList.toggle("d-none", filtered.length > 0 || !keyword);
        }
    }

    scrollToTargetBook() {
        const bookOrder = parseInt(new URLSearchParams(window.location.search).get("bookOrder"), 10);
        if (!bookOrder) return;

        const targetCard = document.querySelector(`.prs-card[data-book-order="${bookOrder}"]`);
        if (!targetCard) return;

        setTimeout(() => {
            const overlay = document.createElement("div");
            overlay.className = "prs-spotlight-overlay";
            document.body.appendChild(overlay);

            targetCard.classList.add("is-spotlight-target");

            requestAnimationFrame(() => {
                overlay.classList.add("is-active");
                targetCard.scrollIntoView({behavior: "smooth", block: "center"});
            });

            let dismissed = false;
            const dismiss = () => {
                if (dismissed) return;
                dismissed = true;
                overlay.classList.remove("is-active");
                targetCard.classList.remove("is-spotlight-target");
                overlay.addEventListener("transitionend", () => overlay.remove(), {once: true});
            };

            overlay.addEventListener("click", dismiss, {once: true});
            setTimeout(dismiss, 4000);
        }, 100);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    new PublicReadingOfScripture();
});
