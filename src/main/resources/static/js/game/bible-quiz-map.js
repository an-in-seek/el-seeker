const QUIZ_STAGE_COUNT = 10;
const QUESTIONS_PER_STAGE = 5;

const QUIZ_STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "currentStage",
    LAST_COMPLETED_DATE: "lastCompletedDate",
});

const getLocalDateString = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const normalizeStage = stageValue => {
    const parsed = parseInt(stageValue, 10);
    if (Number.isNaN(parsed) || parsed < 1) {
        return 1;
    }
    if (parsed > QUIZ_STAGE_COUNT) {
        return QUIZ_STAGE_COUNT;
    }
    return parsed;
};

document.addEventListener("DOMContentLoaded", () => {
    const stageList = document.getElementById("stageList");
    const quizMapDate = document.getElementById("quizMapDate");
    const quizMapNote = document.getElementById("quizMapNote");

    if (!stageList || !quizMapDate || !quizMapNote) {
        return;
    }

    const today = getLocalDateString();
    const storedDate = LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE);
    const currentStage = normalizeStage(LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE));
    const canPlayToday = storedDate !== today;

    quizMapDate.textContent = `오늘 ${today}`;

    if (storedDate === today) {
        quizMapNote.textContent = "오늘의 퀴즈를 완료했습니다. 현재 스테이지는 내일부터 시작할 수 있습니다.";
    } else {
        quizMapNote.textContent = "현재 스테이지는 오늘의 퀴즈에서 진행됩니다.";
    }

    stageList.innerHTML = "";

    for (let stage = 1; stage <= QUIZ_STAGE_COUNT; stage += 1) {
        const status = stage < currentStage ? "complete" : stage === currentStage ? "current" : "locked";
        const card = document.createElement("button");
        card.type = "button";
        card.className = "stage-card";

        let statusLabel = "진행 전";
        let metaText = `${QUESTIONS_PER_STAGE}문제`;
        let route = null;
        let isClickable = false;

        if (status === "complete") {
            card.classList.add("is-complete", "is-clickable");
            statusLabel = "완료";
            metaText = "복습 가능";
            route = `/web/game/bible-quiz?stage=${stage}&mode=review`;
            isClickable = true;
        } else if (status === "current") {
            card.classList.add("is-current");
            statusLabel = "현재";
            if (canPlayToday) {
                card.classList.add("is-clickable");
                metaText = "오늘 진행";
                route = "/web/game/bible-quiz";
                isClickable = true;
            } else {
                metaText = "내일 시작 가능";
            }
        } else {
            card.classList.add("is-locked");
        }

        if (!isClickable) {
            card.disabled = true;
        }

        const header = document.createElement("div");
        header.className = "stage-card-header";

        const number = document.createElement("span");
        number.className = "stage-number";
        number.textContent = `${stage} 스테이지`;

        const statusBadge = document.createElement("span");
        statusBadge.className = "stage-status";
        if (status === "complete") {
            statusBadge.classList.add("is-complete");
        }
        if (status === "current") {
            statusBadge.classList.add("is-current");
        }
        statusBadge.textContent = statusLabel;

        const meta = document.createElement("div");
        meta.className = "stage-meta";
        meta.textContent = metaText;

        header.appendChild(number);
        header.appendChild(statusBadge);
        card.appendChild(header);
        card.appendChild(meta);

        if (route) {
            card.addEventListener("click", () => {
                window.location.href = route;
            });
        }

        stageList.appendChild(card);
    }
});
