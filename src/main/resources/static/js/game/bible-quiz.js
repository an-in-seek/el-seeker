const QUIZ_STORAGE_KEY = "bibleQuiz.todayCompleted";

const QUIZ_QUESTIONS = [
    {
        id: 1,
        difficulty: "easy",
        question: "하나님께서 빛을 만드신 날은 창조 몇째 날일까요?",
        options: ["첫째 날", "둘째 날", "셋째 날", "넷째 날"],
        answerIndex: 0
    },
    {
        id: 2,
        difficulty: "easy",
        question: "노아가 방주를 지을 때 사용한 나무는 무엇일까요?",
        options: ["감람나무", "고페르나무", "백향목", "돌베개"],
        answerIndex: 1
    },
    {
        id: 3,
        difficulty: "easy",
        question: "예수님이 가르치신 주기도문의 시작 구절은 무엇인가요?",
        options: ["하늘에 계신 우리 아버지여", "우리 아버지여", "주님, 들으소서", "하나님께 영광"],
        answerIndex: 0
    },
    {
        id: 4,
        difficulty: "medium",
        question: "사도 바울이 환상(마게도냐 사람의 요청)을 보고 처음으로 유럽에 건너가 전도한 지역은 어디인가요?",
        options: ["소아시아", "마게도냐", "로마", "예루살렘"],
        answerIndex: 1
    },
    {
        id: 5,
        difficulty: "medium",
        question: "다윗이 골리앗을 쓰러뜨릴 때 사용한 무기는 무엇인가요?",
        options: ["창", "활", "물매", "돌칼"],
        answerIndex: 2
    }
];

const getLocalDateString = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const readCompletion = (today) => {
    const stored = LocalStore.get(QUIZ_STORAGE_KEY);
    if (!stored) {
        return false;
    }
    if (stored === true) {
        return true;
    }
    if (stored.completed && stored.date === today) {
        return true;
    }
    if (stored.completed && stored.date !== today) {
        LocalStore.remove(QUIZ_STORAGE_KEY);
    }
    return false;
};

const saveCompletion = (today) => {
    LocalStore.set(QUIZ_STORAGE_KEY, {date: today, completed: true});
};

document.addEventListener("DOMContentLoaded", () => {
    const quizPanel = document.getElementById("quizPanel");
    const quizComplete = document.getElementById("quizComplete");
    const quizProgress = document.getElementById("quizProgress");
    const quizQuestion = document.getElementById("quizQuestion");
    const quizOptions = document.getElementById("quizOptions");
    const quizFeedback = document.getElementById("quizFeedback");
    const quizNext = document.getElementById("quizNext");

    if (!quizPanel || !quizComplete || !quizProgress || !quizQuestion || !quizOptions || !quizFeedback || !quizNext) {
        return;
    }

    const today = getLocalDateString();
    if (readCompletion(today)) {
        quizPanel.classList.add("d-none");
        quizComplete.classList.remove("d-none");
        return;
    }

    const state = {
        index: 0,
        answered: false
    };

    const resetFeedback = () => {
        quizFeedback.textContent = "";
        quizFeedback.classList.remove("is-correct", "is-wrong");
    };

    const renderQuestion = () => {
        const current = QUIZ_QUESTIONS[state.index];
        quizProgress.textContent = `${state.index + 1} / ${QUIZ_QUESTIONS.length}`;
        quizQuestion.textContent = current.question;
        quizOptions.innerHTML = "";
        resetFeedback();
        quizNext.disabled = true;
        quizNext.textContent = state.index === QUIZ_QUESTIONS.length - 1 ? "완료" : "다음 문제";
        state.answered = false;

        current.options.forEach((option, optionIndex) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "quiz-option";
            button.textContent = option;
            button.addEventListener("click", () => handleAnswer(button, optionIndex));
            quizOptions.appendChild(button);
        });
    };

    const handleAnswer = (selectedButton, selectedIndex) => {
        if (state.answered) {
            return;
        }
        state.answered = true;
        const current = QUIZ_QUESTIONS[state.index];
        const isCorrect = selectedIndex === current.answerIndex;

        quizOptions.querySelectorAll(".quiz-option").forEach((button, index) => {
            button.disabled = true;
            if (index === current.answerIndex) {
                button.classList.add("is-correct");
            }
        });

        if (isCorrect) {
            quizFeedback.textContent = "맞았습니다";
            quizFeedback.classList.add("is-correct");
        } else {
            selectedButton.classList.add("is-wrong");
            quizFeedback.textContent = "다시 한번 읽어보세요";
            quizFeedback.classList.add("is-wrong");
        }

        quizNext.disabled = false;
    };

    quizNext.addEventListener("click", () => {
        if (!state.answered) {
            return;
        }
        if (state.index === QUIZ_QUESTIONS.length - 1) {
            saveCompletion(today);
            quizPanel.classList.add("d-none");
            quizComplete.classList.remove("d-none");
            return;
        }
        state.index += 1;
        renderQuestion();
    });

    renderQuestion();
});
