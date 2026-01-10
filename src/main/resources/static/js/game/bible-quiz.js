const QUIZ_STAGE_COUNT = 10;
const QUESTIONS_PER_STAGE = 5;

const QUIZ_STORAGE_KEYS = Object.freeze({
    CURRENT_STAGE: "currentStage",
    LAST_COMPLETED_DATE: "lastCompletedDate",
    LAST_STAGE_SCORE: "lastStageScore",
});

const QUIZ_STAGES = [
    {
        stage: 1,
        questions: [
            {
                id: 1,
                question: "하나님께서 빛을 만드신 날은 창조 몇째 날일까요?",
                options: ["첫째 날", "둘째 날", "셋째 날", "넷째 날"],
                answerIndex: 0
            },
            {
                id: 2,
                question: "아담과 하와가 있었던 동산의 이름은 무엇인가요?",
                options: ["에덴 동산", "올리브 동산", "겟세마네", "갈멜 산"],
                answerIndex: 0
            },
            {
                id: 3,
                question: "노아가 방주를 지을 때 사용한 나무는 무엇일까요?",
                options: ["감람나무", "고페르나무", "백향목", "돌베개"],
                answerIndex: 1
            },
            {
                id: 4,
                question: "바벨탑 사건 이후에 생긴 일은 무엇인가요?",
                options: ["언어가 혼잡해졌다", "홍해가 갈라졌다", "모세가 태어났다", "약속의 땅에 들어갔다"],
                answerIndex: 0
            },
            {
                id: 5,
                question: "하나님께서 아브라함을 부르신 곳은 어디인가요?",
                options: ["갈대아 우르", "애굽", "가나안", "베들레헴"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 2,
        questions: [
            {
                id: 6,
                question: "하나님께서 아브라함에게 주신 약속의 표는 무엇인가요?",
                options: ["할례", "무지개", "십계명", "만나"],
                answerIndex: 0
            },
            {
                id: 7,
                question: "이삭의 아내가 된 사람은 누구인가요?",
                options: ["리브가", "라헬", "레아", "사라"],
                answerIndex: 0
            },
            {
                id: 8,
                question: "야곱의 이름이 바뀐 후의 새 이름은 무엇인가요?",
                options: ["이스라엘", "요셉", "여호수아", "요나"],
                answerIndex: 0
            },
            {
                id: 9,
                question: "요셉이 형들에게 미움을 받은 이유 중 하나는 무엇인가요?",
                options: ["채색 옷을 입어서", "왕이 되어서", "광야에 가서", "제사를 드려서"],
                answerIndex: 0
            },
            {
                id: 10,
                question: "요셉이 애굽에서 맡았던 높은 직책은 무엇인가요?",
                options: ["총리", "제사장", "장군", "판관"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 3,
        questions: [
            {
                id: 11,
                question: "모세가 하나님을 만난 곳에서 본 것은 무엇인가요?",
                options: ["떨기나무 불꽃", "무지개", "홍해", "불기둥"],
                answerIndex: 0
            },
            {
                id: 12,
                question: "애굽에 내린 열 가지 재앙 중 첫 번째는 무엇인가요?",
                options: ["물이 피로 변함", "우박", "메뚜기", "흑암"],
                answerIndex: 0
            },
            {
                id: 13,
                question: "이스라엘 백성이 홍해를 건널 때 어떻게 되었나요?",
                options: ["바다가 갈라졌다", "배를 타고 건넜다", "다리가 놓였다", "산길로 돌아갔다"],
                answerIndex: 0
            },
            {
                id: 14,
                question: "십계명이 주어진 산은 어디인가요?",
                options: ["시내 산", "갈멜 산", "올리브 산", "헤르몬 산"],
                answerIndex: 0
            },
            {
                id: 15,
                question: "광야에서 이스라엘 백성이 먹었던 양식은 무엇인가요?",
                options: ["만나", "포도", "빵", "생선"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 4,
        questions: [
            {
                id: 16,
                question: "여호수아가 이끈 가나안 정복의 첫 성은 어디인가요?",
                options: ["여리고", "아이", "기브온", "헤브론"],
                answerIndex: 0
            },
            {
                id: 17,
                question: "여리고 성이 무너진 방법은 무엇인가요?",
                options: ["나팔을 불고 함성을 질렀다", "성문을 불태웠다", "비가 내려 무너졌다", "바벨탑처럼 붕괴됐다"],
                answerIndex: 0
            },
            {
                id: 18,
                question: "사사 시대에 이스라엘을 이끈 여성 지도자는 누구인가요?",
                options: ["드보라", "에스더", "리브가", "라헬"],
                answerIndex: 0
            },
            {
                id: 19,
                question: "기드온이 전쟁에 함께한 병사의 수는 얼마로 줄었나요?",
                options: ["300명", "500명", "700명", "1000명"],
                answerIndex: 0
            },
            {
                id: 20,
                question: "삼손의 힘이 약해진 이유는 무엇인가요?",
                options: ["머리카락이 잘려서", "무기를 잃어서", "갑옷을 벗어서", "물을 마시지 못해서"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 5,
        questions: [
            {
                id: 21,
                question: "이스라엘의 첫 번째 왕은 누구인가요?",
                options: ["사울", "다윗", "솔로몬", "사무엘"],
                answerIndex: 0
            },
            {
                id: 22,
                question: "다윗이 골리앗을 쓰러뜨릴 때 사용한 무기는 무엇인가요?",
                options: ["창", "활", "물매", "돌칼"],
                answerIndex: 2
            },
            {
                id: 23,
                question: "다윗이 왕이 된 후에 세운 수도는 어디인가요?",
                options: ["예루살렘", "베들레헴", "헤브론", "사마리아"],
                answerIndex: 0
            },
            {
                id: 24,
                question: "솔로몬이 하나님께 구한 것은 무엇인가요?",
                options: ["지혜", "재물", "장수", "전쟁의 승리"],
                answerIndex: 0
            },
            {
                id: 25,
                question: "솔로몬이 건축한 대표적인 건물은 무엇인가요?",
                options: ["성전", "성벽", "궁전", "회당"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 6,
        questions: [
            {
                id: 26,
                question: "솔로몬 이후에 나라가 둘로 나뉘게 된 이유는 무엇인가요?",
                options: ["왕의 불순종과 우상숭배", "외적의 침입", "대기근", "전염병"],
                answerIndex: 0
            },
            {
                id: 27,
                question: "북이스라엘의 첫 왕은 누구인가요?",
                options: ["여로보암", "르호보암", "아합", "히스기야"],
                answerIndex: 0
            },
            {
                id: 28,
                question: "엘리야가 갈멜 산에서 맞섰던 선지자들은 누구인가요?",
                options: ["바알 선지자", "사독 선지자", "나단 선지자", "엘리사 선지자"],
                answerIndex: 0
            },
            {
                id: 29,
                question: "엘리사가 받았던 엘리야의 영감은 몇 배였나요?",
                options: ["두 배", "세 배", "한 배", "네 배"],
                answerIndex: 0
            },
            {
                id: 30,
                question: "요나가 전한 회개의 메시지를 들은 도시는 어디인가요?",
                options: ["니느웨", "베들레헴", "가버나움", "두로"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 7,
        questions: [
            {
                id: 31,
                question: "다니엘이 사자 굴에 던져진 이유는 무엇인가요?",
                options: ["하나님께 기도해서", "전쟁에서 패해서", "거짓말을 해서", "성전을 훼손해서"],
                answerIndex: 0
            },
            {
                id: 32,
                question: "에스더가 왕에게 나아갈 때 금식하자고 요청한 기간은 얼마인가요?",
                options: ["3일", "7일", "10일", "1일"],
                answerIndex: 0
            },
            {
                id: 33,
                question: "포로 귀환 후 성전을 재건하는 일을 이끈 인물은 누구인가요?",
                options: ["스룹바벨", "사무엘", "다윗", "요나단"],
                answerIndex: 0
            },
            {
                id: 34,
                question: "느헤미야가 재건한 것은 무엇인가요?",
                options: ["예루살렘 성벽", "성전", "궁전", "홍해의 길"],
                answerIndex: 0
            },
            {
                id: 35,
                question: "이스라엘 백성이 포로로 끌려갔던 제국은 어디인가요?",
                options: ["바벨론", "로마", "그리스", "애굽"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 8,
        questions: [
            {
                id: 36,
                question: "예수님이 태어나신 도시는 어디인가요?",
                options: ["베들레헴", "나사렛", "예루살렘", "가버나움"],
                answerIndex: 0
            },
            {
                id: 37,
                question: "예수님께 세례를 준 사람은 누구인가요?",
                options: ["세례 요한", "베드로", "바울", "야고보"],
                answerIndex: 0
            },
            {
                id: 38,
                question: "예수님이 가르치신 주기도문의 시작 구절은 무엇인가요?",
                options: ["하늘에 계신 우리 아버지여", "우리 아버지여", "주님, 들으소서", "하나님께 영광"],
                answerIndex: 0
            },
            {
                id: 39,
                question: "선한 사마리아인의 비유가 강조하는 것은 무엇인가요?",
                options: ["이웃 사랑", "제사 규례", "성전 건축", "금식"],
                answerIndex: 0
            },
            {
                id: 40,
                question: "예수님이 오병이어로 먹이신 사람 수는 약 몇 명인가요?",
                options: ["오천 명", "천 명", "삼천 명", "이천 명"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 9,
        questions: [
            {
                id: 41,
                question: "예수님이 최후의 만찬에서 나누신 것은 무엇인가요?",
                options: ["떡과 잔", "양고기", "물과 소금", "포도와 무화과"],
                answerIndex: 0
            },
            {
                id: 42,
                question: "예수님이 십자가에 못 박히신 곳은 어디인가요?",
                options: ["골고다", "감람산", "갈릴리", "여리고"],
                answerIndex: 0
            },
            {
                id: 43,
                question: "예수님의 부활은 며칠 만에 이루어졌나요?",
                options: ["셋째 날", "첫째 날", "일주일 후", "열흘 후"],
                answerIndex: 0
            },
            {
                id: 44,
                question: "오순절에 임한 사건은 무엇인가요?",
                options: ["성령 강림", "성전 건축", "애굽 탈출", "요단강 건넘"],
                answerIndex: 0
            },
            {
                id: 45,
                question: "베드로가 예수님을 부인한 횟수는 몇 번인가요?",
                options: ["세 번", "한 번", "두 번", "네 번"],
                answerIndex: 0
            }
        ]
    },
    {
        stage: 10,
        questions: [
            {
                id: 46,
                question: "사울이 바울로 변화되는 사건이 일어난 곳은 어디로 가는 길이었나요?",
                options: ["다메섹", "안디옥", "예루살렘", "로마"],
                answerIndex: 0
            },
            {
                id: 47,
                question: "사도 바울이 환상(마게도냐 사람의 요청)을 보고 처음으로 유럽에 건너가 전도한 지역은 어디인가요?",
                options: ["소아시아", "마게도냐", "로마", "예루살렘"],
                answerIndex: 1
            },
            {
                id: 48,
                question: "요한계시록을 기록한 곳으로 알려진 섬은 어디인가요?",
                options: ["밧모 섬", "몰타 섬", "구브로 섬", "크레타 섬"],
                answerIndex: 0
            },
            {
                id: 49,
                question: "사도 바울이 쓴 편지 중 하나가 아닌 것은 무엇인가요?",
                options: ["마태복음", "로마서", "에베소서", "빌립보서"],
                answerIndex: 0
            },
            {
                id: 50,
                question: "성령의 열매로 알려진 덕목은 무엇인가요?",
                options: ["사랑", "교만", "분노", "질투"],
                answerIndex: 0
            }
        ]
    }
];

const getLocalDateString = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

const clampNumber = (value, min, max) => Math.min(Math.max(value, min), max);

const normalizeStage = stageValue => {
    const parsed = parseInt(stageValue, 10);
    if (Number.isNaN(parsed)) {
        return 1;
    }
    return clampNumber(parsed, 1, QUIZ_STAGE_COUNT);
};

const getStageData = stageNumber => {
    const stageIndex = clampNumber(stageNumber, 1, QUIZ_STAGE_COUNT) - 1;
    return QUIZ_STAGES[stageIndex] || QUIZ_STAGES[0];
};

const getStoredDate = () => LocalStore.get(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE);

const getStoredScore = () => {
    const score = LocalStore.get(QUIZ_STORAGE_KEYS.LAST_STAGE_SCORE);
    const parsed = parseInt(score, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const showCompletion = (quizPanel, quizComplete, quizScore, score) => {
    quizPanel.setAttribute("aria-busy", "false");
    quizPanel.classList.add("d-none");
    if (score !== null && score !== undefined) {
        quizScore.textContent = `오늘 점수 ${score} / ${QUESTIONS_PER_STAGE}`;
    } else {
        quizScore.textContent = "";
    }
    quizComplete.classList.remove("d-none");
};

document.addEventListener("DOMContentLoaded", () => {
    const getElement = id => document.getElementById(id);
    const getQuizElements = () => {
        const elements = {
            quizPanel: getElement("quizPanel"),
            quizComplete: getElement("quizComplete"),
            quizStage: getElement("quizStage"),
            quizQuestionProgress: getElement("quizQuestionProgress"),
            quizStageProgress: getElement("quizStageProgress"),
            quizQuestionProgressBar: getElement("quizQuestionProgressBar"),
            quizQuestion: getElement("quizQuestion"),
            quizOptions: getElement("quizOptions"),
            quizFeedback: getElement("quizFeedback"),
            quizNext: getElement("quizNext"),
            quizScore: getElement("quizScore"),
            quizStartButton: getElement("quizStartButton")
        };
        const missingRequired = [
            "quizPanel",
            "quizComplete",
            "quizStage",
            "quizQuestionProgress",
            "quizStageProgress",
            "quizQuestionProgressBar",
            "quizQuestion",
            "quizOptions",
            "quizFeedback",
            "quizNext",
            "quizScore"
        ].some(key => !elements[key]);

        return missingRequired ? null : elements;
    };

    const elements = getQuizElements();
    if (!elements) {
        return;
    }

    const {
        quizPanel,
        quizComplete,
        quizStage,
        quizQuestionProgress,
        quizStageProgress,
        quizQuestionProgressBar,
        quizQuestion,
        quizOptions,
        quizFeedback,
        quizNext,
        quizScore,
        quizStartButton
    } = elements;

    const today = getLocalDateString();
    const storedDate = getStoredDate();
    const storedScore = getStoredScore();
    const queryParams = new URLSearchParams(window.location.search);
    const requestedStage = parseInt(queryParams.get("stage"), 10);
    const currentStage = normalizeStage(LocalStore.get(QUIZ_STORAGE_KEYS.CURRENT_STAGE));
    const isReviewMode = queryParams.get("mode") === "review"
        && !Number.isNaN(requestedStage)
        && requestedStage >= 1
        && requestedStage < currentStage;
    const activeStage = isReviewMode ? normalizeStage(requestedStage) : currentStage;
    const stageData = getStageData(activeStage);

    const setBusy = isBusy => {
        quizPanel.setAttribute("aria-busy", isBusy ? "true" : "false");
    };

    const resetFeedback = () => {
        quizFeedback.textContent = "";
        quizFeedback.classList.remove("is-correct", "is-wrong");
    };

    const updateProgressBar = (element, value, max, min) => {
        element.max = max;
        element.value = value;
        element.setAttribute("aria-valuemin", String(min));
        element.setAttribute("aria-valuemax", String(max));
        element.setAttribute("aria-valuenow", String(value));
    };

    const updateProgress = () => {
        const questionNumber = state.index + 1;
        quizStage.textContent = `${state.stage} / ${QUIZ_STAGE_COUNT}`;
        quizQuestionProgress.textContent = `${questionNumber} / ${state.questions.length}`;
        updateProgressBar(quizStageProgress, state.stage, QUIZ_STAGE_COUNT, 1);
        updateProgressBar(quizQuestionProgressBar, questionNumber, state.questions.length, 1);
    };

    const showLoadError = () => {
        setBusy(false);
        quizQuestion.textContent = "오늘의 퀴즈를 불러올 수 없습니다";
        quizOptions.textContent = "잠시 후 다시 시도해주세요.";
        quizNext.disabled = true;
        resetFeedback();
        quizStage.textContent = `0 / ${QUIZ_STAGE_COUNT}`;
        quizQuestionProgress.textContent = `0 / ${QUESTIONS_PER_STAGE}`;
        updateProgressBar(quizStageProgress, 0, QUIZ_STAGE_COUNT, 0);
        updateProgressBar(quizQuestionProgressBar, 0, QUESTIONS_PER_STAGE, 0);
    };

    if (quizStartButton) {
        if (isReviewMode) {
            quizStartButton.textContent = "오늘의 퀴즈 이어서 하기";
        }
        quizStartButton.addEventListener("click", event => {
            if (quizPanel.classList.contains("d-none")) {
                event.preventDefault();
            }
        });
    }

    if (!isReviewMode && storedDate === today) {
        showCompletion(quizPanel, quizComplete, quizScore, storedScore);
        return;
    }

    const renderQuestion = () => {
        setBusy(true);
        const current = state.questions[state.index];
        if (!current) {
            showLoadError();
            return;
        }
        quizQuestion.textContent = current.question;
        quizOptions.innerHTML = "";
        resetFeedback();
        quizNext.disabled = true;
        quizNext.textContent = "정답 확인";
        state.answered = false;
        state.selectedIndex = null;

        current.options.forEach((option, optionIndex) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "quiz-option";
            button.textContent = option;
            button.addEventListener("click", () => selectOption(button, optionIndex));
            quizOptions.appendChild(button);
        });
        updateProgress();
        setBusy(false);
    };

    const selectOption = (selectedButton, selectedIndex) => {
        if (state.answered) {
            return;
        }
        state.selectedIndex = selectedIndex;
        quizOptions.querySelectorAll(".quiz-option").forEach(button => {
            button.classList.remove("is-selected");
        });
        selectedButton.classList.add("is-selected");
        quizNext.disabled = false;
    };

    const gradeAnswer = () => {
        if (state.answered || state.selectedIndex === null) {
            return;
        }
        state.answered = true;
        const current = state.questions[state.index];
        const isCorrect = state.selectedIndex === current.answerIndex;

        quizOptions.querySelectorAll(".quiz-option")
            .forEach((button, index) => {
                button.disabled = true;
                if (index === current.answerIndex) {
                    button.classList.add("is-correct");
                    button.textContent = `${button.textContent} (정답)`;
                }
                if (index === state.selectedIndex && !isCorrect) {
                    button.classList.add("is-wrong");
                    button.textContent = `${button.textContent} (오답)`;
                }
            });

        if (isCorrect) {
            if (!state.isReview) {
                state.score += 1;
            }
            quizFeedback.textContent = "😊 잘하셨어요 정답입니다!";
            quizFeedback.classList.add("is-correct");
        } else {
            quizFeedback.textContent = "🥲 아쉽지만 오답입니다. 말씀을 다시 읽어보면 도움이 될 거예요!";
            quizFeedback.classList.add("is-wrong");
        }

        quizNext.disabled = false;
        quizNext.textContent = state.index === state.questions.length - 1 ? "완료" : "다음 문제";
    };

    quizNext.addEventListener("click", () => {
        if (!state.answered) {
            if (state.selectedIndex === null) {
                return;
            }
            gradeAnswer();
            return;
        }
        if (state.index === state.questions.length - 1) {
            if (!state.isReview) {
                const nextStage = Math.min(state.stage + 1, QUIZ_STAGE_COUNT);
                LocalStore.set(QUIZ_STORAGE_KEYS.CURRENT_STAGE, nextStage);
                LocalStore.set(QUIZ_STORAGE_KEYS.LAST_COMPLETED_DATE, today);
                LocalStore.set(QUIZ_STORAGE_KEYS.LAST_STAGE_SCORE, state.score);
            }
            showCompletion(quizPanel, quizComplete, quizScore, state.isReview ? null : state.score);
            return;
        }
        state.index += 1;
        renderQuestion();
    });

    if (!stageData || !Array.isArray(stageData.questions) || stageData.questions.length === 0) {
        showLoadError();
        return;
    }

    const state = {
        index: 0,
        answered: false,
        score: 0,
        stage: activeStage,
        questions: stageData.questions,
        isReview: isReviewMode,
        selectedIndex: null,
    };

    renderQuestion();
});
