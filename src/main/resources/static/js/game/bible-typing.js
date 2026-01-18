const apiBase = "/api/v1/bible";
const sessionApi = "/api/v1/game/bible-typing/sessions";
const resumeApi = "/api/v1/game/bible-typing/verse-results";
const FIXED_TRANSLATION_CODE = "KRV";

const elements = {
    translationSelect: document.getElementById("typingTranslationSelect"),
    bookSelect: document.getElementById("typingBookSelect"),
    chapterSelect: document.getElementById("typingChapterSelect"),
    ignorePunctuation: document.getElementById("typingIgnorePunctuation"),
    verseList: document.getElementById("typingVerseList"),
    message: document.getElementById("typingMessage"),
    startBtn: document.getElementById("typingStartBtn"),
    endBtn: document.getElementById("typingEndBtn"),
    resetBtn: document.getElementById("typingResetBtn"),
    verseHeader: document.getElementById("typingVerseHeader"),
    verseStatus: document.getElementById("typingVerseStatus"),
    progressText: document.getElementById("typingProgressText"),
    progressBar: document.getElementById("typingProgressBar"),
    cpm: document.getElementById("typingCpm"),
    accuracy: document.getElementById("typingAccuracy"),
    elapsedTime: document.getElementById("typingElapsedTime"),
    sessionSummary: document.getElementById("typingSessionSummary"),
    sessionSummaryText: document.getElementById("typingSessionSummaryText"),
    ariaStatus: document.getElementById("typingAriaStatus"),
    backButton: document.getElementById("topNavBackButton")
};

const state = {
    translations: [],
    books: [],
    chapters: [],
    verses: [],
    verseStates: [],
    tokenMap: new Map(),
    currentIndex: 0,
    sessionActive: false,
    practiceStarted: false,
    sessionKey: null,
    transitioning: false,
    composing: false,
    pendingCompleteIndex: null,
    startedAt: null,
    endedAt: null,
    totalTyped: 0,
    totalCorrect: 0,
    timerId: null
};

const punctuationRegex = /[.,!?;:"'“”‘’(){}\[\]—\-]/g;

const fetchJson = async (url) => {
    const response = await fetch(url, {credentials: "same-origin"});
    if (!response.ok) {
        throw new Error(`요청 실패: ${response.status}`);
    }
    return response.json();
};

const showMessage = (message) => {
    if (!elements.message) return;
    elements.message.textContent = message;
    elements.message.classList.remove("d-none");
};

const clearMessage = () => {
    if (!elements.message) return;
    elements.message.textContent = "";
    elements.message.classList.add("d-none");
};

const parseNumber = (value) => {
    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
};

const getQueryParams = () => {
    const params = new URLSearchParams(window.location.search);
    return {
        translationId: parseNumber(params.get("translationId")),
        bookOrder: parseNumber(params.get("bookOrder")),
        chapterNumber: parseNumber(params.get("chapterNumber"))
    };
};

const updateQueryParams = (params, replace = false) => {
    const url = new URL(window.location.href);
    Object.entries(params).forEach(([key, value]) => {
        if (value === null || value === undefined) {
            url.searchParams.delete(key);
        } else {
            url.searchParams.set(key, String(value));
        }
    });
    if (replace) {
        window.history.replaceState({}, "", url);
    } else {
        window.history.pushState({}, "", url);
    }
};

const normalizeText = (text, ignorePunctuation) => {
    let normalized = text.replace(/\r?\n/g, " ").replace(/\s+/g, " ").trim();
    if (ignorePunctuation) {
        normalized = normalized.replace(punctuationRegex, "");
    }
    return normalized;
};

const createSessionKey = () => {
    if (state.sessionKey) return state.sessionKey;
    if (window.crypto?.randomUUID) {
        state.sessionKey = window.crypto.randomUUID();
    } else {
        state.sessionKey = `session-${Date.now()}-${Math.random().toString(16).slice(2)}`;
    }
    return state.sessionKey;
};

const getResetStorageKey = ({translationId, bookOrder, chapterNumber}) =>
    `bible-typing-reset:${translationId}:${bookOrder}:${chapterNumber}`;

const getResetTimestamp = (selection) => {
    const value = window.localStorage.getItem(getResetStorageKey(selection));
    return value ? Number(value) : 0;
};

const markResetTimestamp = (selection) => {
    window.localStorage.setItem(getResetStorageKey(selection), String(Date.now()));
};

const createOption = (value, label) => {
    const option = document.createElement("option");
    option.value = String(value);
    option.textContent = label;
    return option;
};

const clearChildren = (element) => {
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
};

const formatDuration = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remaining = seconds % 60;
    return `${String(minutes).padStart(2, "0")}:${String(remaining).padStart(2, "0")}`;
};

const getElapsedSeconds = () => {
    if (!state.startedAt) return 0;
    const now = state.endedAt || new Date();
    return Math.max(0, Math.floor((now - state.startedAt) / 1000));
};

const updateMetrics = () => {
    const totalVerses = state.verses.length;
    const completedVerses = state.verseStates.filter((verse) => verse.completed).length;
    const progress = totalVerses === 0 ? 0 : Math.round((completedVerses / totalVerses) * 100);
    const accuracyValue = state.totalTyped === 0 ? 0 : Math.round((state.totalCorrect / state.totalTyped) * 100);
    const elapsedSeconds = getElapsedSeconds();
    const elapsedMinutes = elapsedSeconds / 60;
    const cpmValue = elapsedMinutes > 0 ? Math.round(state.totalTyped / elapsedMinutes) : 0;

    if (elements.progressText) elements.progressText.textContent = `${progress}%`;
    if (elements.progressBar) elements.progressBar.style.width = `${progress}%`;
    if (elements.cpm) elements.cpm.textContent = `${cpmValue}`;
    if (elements.accuracy) elements.accuracy.textContent = `${accuracyValue}`;
    if (elements.elapsedTime) elements.elapsedTime.textContent = formatDuration(elapsedSeconds);

    if (elements.ariaStatus) {
        elements.ariaStatus.textContent = `진행률 ${progress}%, 정확도 ${accuracyValue}%, 속도 ${cpmValue} CPM`;
    }
};

const resetSessionState = () => {
    state.sessionActive = false;
    state.practiceStarted = false;
    state.sessionKey = null;
    state.transitioning = false;
    state.composing = false;
    state.pendingCompleteIndex = null;
    state.startedAt = null;
    state.endedAt = null;
    state.currentIndex = 0;
    state.totalTyped = 0;
    state.totalCorrect = 0;
    state.verseStates = state.verses.map((verse) => ({
        verseNumber: verse.verseNumber,
        originalText: verse.text,
        normalizedText: normalizeText(verse.text, elements.ignorePunctuation?.checked),
        typedText: "",
        normalizedTyped: "",
        correctCount: 0,
        saved: false,
        completed: false
    }));
    state.tokenMap.clear();
    if (state.timerId) {
        clearInterval(state.timerId);
        state.timerId = null;
    }
    if (elements.verseStatus) elements.verseStatus.textContent = "대기";
    if (elements.sessionSummary) elements.sessionSummary.classList.add("d-none");
    updateMetrics();
};

const ensureTokenized = (row, normalizedText) => {
    const content = row.querySelector(".typing-verse-content");
    if (!content || row.dataset.tokenized === "true") return;
    clearChildren(content);
    const tokens = [];
    for (const char of normalizedText) {
        const span = document.createElement("span");
        span.className = "typing-token";
        span.textContent = char;
        content.appendChild(span);
        tokens.push(span);
    }
    if (tokens.length === 0) {
        const span = document.createElement("span");
        span.className = "typing-token";
        span.textContent = " ";
        content.appendChild(span);
        tokens.push(span);
    }
    state.tokenMap.set(row.dataset.index, tokens);
    row.dataset.tokenized = "true";
};

const updateTokenClasses = (row, normalizedText, normalizedInput) => {
    const tokens = state.tokenMap.get(row.dataset.index);
    if (!tokens) return;
    const inputLength = normalizedInput.length;
    tokens.forEach((token, index) => {
        token.classList.remove("is-correct", "is-current", "is-incorrect");
        if (index < inputLength) {
            if (normalizedInput[index] === normalizedText[index]) {
                token.classList.add("is-correct");
            } else {
                token.classList.add("is-incorrect");
            }
        } else if (index === inputLength) {
            token.classList.add("is-current");
        }
    });
};

const countCorrectChars = (normalizedText, normalizedInput) => {
    const length = Math.min(normalizedText.length, normalizedInput.length);
    let count = 0;
    for (let i = 0; i < length; i += 1) {
        if (normalizedText[i] === normalizedInput[i]) {
            count += 1;
        }
    }
    return count;
};

const activateVerse = (index) => {
    const verseRows = elements.verseList.querySelectorAll(".typing-verse-row");
    verseRows.forEach((row, rowIndex) => {
        const input = row.querySelector(".typing-verse-input");
        if (!input) return;
        const isActive = rowIndex === index;
        row.classList.toggle("is-active", isActive);
        const verseState = state.verseStates[rowIndex];
        const isEnabled = isActive && state.practiceStarted && !verseState?.completed;
        input.disabled = !isEnabled;
        input.readOnly = !isEnabled;
        if (isEnabled) {
            ensureTokenized(row, verseState.normalizedText);
            updateTokenClasses(row, verseState.normalizedText, verseState.normalizedTyped);
        }
    });
};

const focusVerseInput = (index) => {
    const row = elements.verseList.querySelector(`.typing-verse-row[data-index="${index}"]`);
    if (!row) return;
    const input = row.querySelector(".typing-verse-input");
    if (!input) return;
    input.focus();
    input.scrollIntoView({block: "center", behavior: "smooth"});
};

const saveVerseProgress = async (verseState) => {
    if (verseState.saved) return;
    const params = getQueryParams();
    const payload = {
        sessionKey: createSessionKey(),
        translationId: params.translationId,
        bookOrder: params.bookOrder,
        chapterNumber: params.chapterNumber,
        verseNumber: verseState.verseNumber,
        originalText: verseState.originalText,
        typedText: verseState.typedText,
        accuracy: verseState.normalizedTyped.length === 0
            ? 0
            : Number(((verseState.correctCount / verseState.normalizedTyped.length) * 100).toFixed(2)),
        completed: verseState.completed
    };

    await fetch(resumeApi, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload),
        credentials: "same-origin"
    });
    verseState.saved = true;
};

const handleVerseComplete = (index) => {
    const verseState = state.verseStates[index];
    verseState.completed = true;
    const row = elements.verseList.querySelector(`.typing-verse-row[data-index="${index}"]`);
    if (row) {
        row.classList.remove("is-active");
        row.classList.add("is-complete");
        const input = row.querySelector(".typing-verse-input");
        if (input) {
            input.disabled = true;
            input.blur();
        }
        updateTokenClasses(row, verseState.normalizedText, verseState.normalizedText);
    }
    if (index + 1 < state.verseStates.length) {
        state.transitioning = true;
        state.currentIndex = index + 1;
        activateVerse(state.currentIndex);
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                focusVerseInput(state.currentIndex);
                state.transitioning = false;
            });
        });
    } else {
        endSession(true);
    }
    saveVerseProgress(verseState).catch(() => {
        showMessage("구절 기록 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    });
};

const handleInput = (event) => {
    if (state.transitioning) return;
    const target = event.target;
    if (!(target instanceof HTMLTextAreaElement)) return;
    const index = Number.parseInt(target.dataset.index, 10);
    if (Number.isNaN(index) || index !== state.currentIndex) return;

    const verseState = state.verseStates[index];
    const ignorePunctuation = elements.ignorePunctuation?.checked;
    const normalizedInput = normalizeText(target.value, ignorePunctuation);
    const normalizedText = verseState.normalizedText;
    const correctCount = countCorrectChars(normalizedText, normalizedInput);

    state.totalTyped += normalizedInput.length - verseState.normalizedTyped.length;
    state.totalCorrect += correctCount - verseState.correctCount;

    verseState.typedText = target.value;
    verseState.normalizedTyped = normalizedInput;
    verseState.correctCount = correctCount;

    const row = elements.verseList.querySelector(`.typing-verse-row[data-index="${index}"]`);
    if (row) {
        updateTokenClasses(row, normalizedText, normalizedInput);
    }

    updateMetrics();

    if (normalizedInput.length > 0 && !state.sessionActive) {
        state.sessionActive = true;
        state.startedAt = new Date();
        createSessionKey();
        if (elements.verseStatus) elements.verseStatus.textContent = "진행 중";
        state.timerId = setInterval(updateMetrics, 1000);
    }

    if (normalizedInput === normalizedText && !verseState.completed) {
        if (state.composing) {
            state.pendingCompleteIndex = index;
            return;
        }
        handleVerseComplete(index);
    }
};

const renderVerses = () => {
    clearChildren(elements.verseList);
    state.verses.forEach((verse, index) => {
        const row = document.createElement("div");
        row.className = "typing-verse-row";
        row.dataset.index = String(index);
        row.dataset.verseNumber = String(verse.verseNumber);

        const textLine = document.createElement("div");
        textLine.className = "typing-verse-text";

        const number = document.createElement("span");
        number.className = "typing-verse-number";
        number.textContent = String(verse.verseNumber);

        const content = document.createElement("span");
        content.className = "typing-verse-content";
        content.textContent = verse.text;

        textLine.appendChild(number);
        textLine.appendChild(content);

        const input = document.createElement("textarea");
        input.className = "form-control typing-verse-input";
        input.rows = 2;
        input.placeholder = "여기에 구절을 입력해 주세요.";
        input.disabled = true;
        input.readOnly = true;
        input.dataset.index = String(index);
        input.setAttribute("aria-label", `${verse.verseNumber}절 입력`);
        input.addEventListener("beforeinput", (event) => {
            if (state.transitioning) {
                event.preventDefault();
            }
        });
        input.addEventListener("input", handleInput);
        input.addEventListener("keydown", (event) => {
            if (state.transitioning) {
                event.preventDefault();
            }
        });
        input.addEventListener("compositionstart", () => {
            state.composing = true;
        });
        input.addEventListener("compositionend", () => {
            state.composing = false;
            if (state.pendingCompleteIndex !== null) {
                const pendingIndex = state.pendingCompleteIndex;
                state.pendingCompleteIndex = null;
                if (pendingIndex === state.currentIndex) {
                    handleVerseComplete(pendingIndex);
                }
            }
        });

        row.appendChild(textLine);
        row.appendChild(input);
        elements.verseList.appendChild(row);
    });
    activateVerse(state.currentIndex);
};

const updateHeader = () => {
    const params = getQueryParams();
    const translation = state.translations.find((item) => item.id === params.translationId);
    const book = state.books.find((item) => item.bookOrder === params.bookOrder);
    const chapterNumber = params.chapterNumber;
    const title = book && chapterNumber ? `${book.name} ${chapterNumber}장` : "선택한 구절";
    const subTitle = translation ? `${translation.name} (${translation.code})` : "번역본 선택";
    if (elements.verseHeader) elements.verseHeader.textContent = `${title} · ${subTitle}`;
};

const loadSelections = async () => {
    clearMessage();
    state.translations = await fetchJson(`${apiBase}/translations`);
    if (state.translations.length === 0) {
        showMessage("사용 가능한 번역본이 없습니다.");
        return null;
    }
    const params = getQueryParams();
    const fixedTranslation = state.translations.find((item) => item.code === FIXED_TRANSLATION_CODE);
    let translationId = fixedTranslation?.id ?? params.translationId ?? state.translations[0].id;
    if (!state.translations.some((item) => item.id === translationId)) {
        translationId = state.translations[0].id;
    }

    clearChildren(elements.translationSelect);
    if (fixedTranslation) {
        elements.translationSelect.appendChild(
            createOption(fixedTranslation.id, `${fixedTranslation.name} (${fixedTranslation.code})`)
        );
        elements.translationSelect.value = String(fixedTranslation.id);
        elements.translationSelect.disabled = true;
    } else {
        state.translations.forEach((item) => {
            elements.translationSelect.appendChild(createOption(item.id, `${item.name} (${item.code})`));
        });
        elements.translationSelect.value = String(translationId);
        elements.translationSelect.disabled = false;
    }

    state.books = await fetchJson(`${apiBase}/books?translationId=${translationId}`);
    if (state.books.length === 0) {
        showMessage("선택한 번역본에 책 정보가 없습니다.");
        return null;
    }
    let bookOrder = params.bookOrder ?? state.books[0].bookOrder;
    if (!state.books.some((item) => item.bookOrder === bookOrder)) {
        bookOrder = state.books[0].bookOrder;
    }

    clearChildren(elements.bookSelect);
    state.books.forEach((item) => {
        elements.bookSelect.appendChild(createOption(item.bookOrder, item.name));
    });
    elements.bookSelect.value = String(bookOrder);

    state.chapters = await fetchJson(`${apiBase}/chapters?translationId=${translationId}&bookOrder=${bookOrder}`);
    if (state.chapters.length === 0) {
        showMessage("선택한 책에 장 정보가 없습니다.");
        return null;
    }
    let chapterNumber = params.chapterNumber ?? state.chapters[0].chapterNumber;
    if (!state.chapters.some((item) => item.chapterNumber === chapterNumber)) {
        chapterNumber = state.chapters[0].chapterNumber;
    }

    clearChildren(elements.chapterSelect);
    state.chapters.forEach((item) => {
        elements.chapterSelect.appendChild(createOption(item.chapterNumber, `${item.chapterNumber}장`));
    });
    elements.chapterSelect.value = String(chapterNumber);

    if (
        params.translationId !== translationId
        || params.bookOrder !== bookOrder
        || params.chapterNumber !== chapterNumber
    ) {
        updateQueryParams({translationId, bookOrder, chapterNumber}, true);
    }
    return {translationId, bookOrder, chapterNumber};
};

const loadVerses = async (selection) => {
    const {translationId, bookOrder, chapterNumber} = selection;
    const verses = await fetchJson(
        `${apiBase}/verses?translationId=${translationId}&bookOrder=${bookOrder}&chapterNumber=${chapterNumber}`
    );
    if (!Array.isArray(verses) || verses.length === 0) {
        showMessage("선택한 장에 구절 데이터가 없습니다.");
        state.verses = [];
        resetSessionState();
        renderVerses();
        updateHeader();
        updateMetrics();
        return;
    }
    state.verses = verses;
    resetSessionState();
    renderVerses();
    updateHeader();
    updateMetrics();
    try {
        const progress = await fetchLatestProgress(selection);
        if (progress) {
            applyResumeProgress(progress, selection);
        }
    } catch (error) {
        showMessage("이어하기 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }
};

const startSession = () => {
    if (state.verses.length === 0) return;
    state.practiceStarted = true;
    createSessionKey();
    if (elements.verseStatus) elements.verseStatus.textContent = "입력 준비";
    activateVerse(state.currentIndex);
    focusVerseInput(state.currentIndex);
};

const formatLocalDateTime = (date) => {
    return date.toISOString().split(".")[0];
};

const saveSession = async () => {
    if (!state.startedAt) return;
    const params = getQueryParams();
    const totalVerses = state.verseStates.length;
    const completedVerses = state.verseStates.filter((verse) => verse.completed).length;
    const accuracyValue = state.totalTyped === 0 ? 0 : (state.totalCorrect / state.totalTyped) * 100;
    const elapsedSeconds = getElapsedSeconds();
    const elapsedMinutes = elapsedSeconds / 60;
    const cpmValue = elapsedMinutes > 0 ? state.totalTyped / elapsedMinutes : 0;

    const payload = {
        sessionKey: createSessionKey(),
        translationId: params.translationId,
        bookOrder: params.bookOrder,
        chapterNumber: params.chapterNumber,
        startedAt: formatLocalDateTime(state.startedAt),
        endedAt: formatLocalDateTime(state.endedAt || new Date()),
        totalVerses,
        completedVerses,
        totalTypedChars: state.totalTyped,
        accuracy: Number(accuracyValue.toFixed(2)),
        cpm: Number(cpmValue.toFixed(2)),
        verses: state.verseStates.map((verse) => ({
            verseNumber: verse.verseNumber,
            originalText: verse.originalText,
            typedText: verse.typedText,
            accuracy: verse.normalizedTyped.length === 0
                ? 0
                : Number(((verse.correctCount / verse.normalizedTyped.length) * 100).toFixed(2)),
            completed: verse.completed
        }))
    };

    await fetch(sessionApi, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload),
        credentials: "same-origin"
    });
};

const fetchLatestProgress = async (selection) => {
    const {translationId, bookOrder, chapterNumber} = selection;
    const response = await fetch(
        `${resumeApi}?translationId=${translationId}&bookOrder=${bookOrder}&chapterNumber=${chapterNumber}`,
        {credentials: "same-origin"}
    );
    if (response.status === 204) return null;
    if (!response.ok) {
        throw new Error(`요청 실패: ${response.status}`);
    }
    return response.json();
};

const applyResumeProgress = (progress, selection) => {
    if (!progress || !Array.isArray(progress.verses) || progress.verses.length === 0) return false;
    const resetAt = getResetTimestamp(selection);
    const progressAt = Number(new Date(progress.createdAt));
    if (resetAt && progressAt <= resetAt) return false;

    const ignorePunctuation = elements.ignorePunctuation?.checked;
    const verseMap = new Map(progress.verses.map((verse) => [verse.verseNumber, verse]));
    state.sessionKey = progress.sessionKey;
    state.totalTyped = 0;
    state.totalCorrect = 0;

    state.verseStates = state.verseStates.map((verse) => {
        const saved = verseMap.get(verse.verseNumber);
        if (!saved) return verse;
        let normalizedTyped = normalizeText(saved.typedText || "", ignorePunctuation);
        if (saved.completed && normalizedTyped.length === 0) {
            normalizedTyped = verse.normalizedText;
        }
        const correctCount = countCorrectChars(verse.normalizedText, normalizedTyped);
        state.totalTyped += normalizedTyped.length;
        state.totalCorrect += correctCount;
        return {
            ...verse,
            typedText: saved.typedText,
            normalizedTyped,
            correctCount,
            completed: saved.completed,
            saved: true
        };
    });

    const firstIncomplete = state.verseStates.findIndex((verse) => !verse.completed);
    const allCompleted = firstIncomplete === -1 && state.verseStates.length > 0;
    state.currentIndex = allCompleted ? state.verseStates.length - 1 : firstIncomplete;
    state.practiceStarted = !allCompleted;
    if (elements.verseStatus) {
        elements.verseStatus.textContent = allCompleted ? "완료" : "이어하기";
    }

    const rows = elements.verseList.querySelectorAll(".typing-verse-row");
    rows.forEach((row, index) => {
        const verseState = state.verseStates[index];
        if (!verseState) return;
        const saved = verseMap.get(verseState.verseNumber);
        const input = row.querySelector(".typing-verse-input");
        if (input) {
            if (saved) {
                input.value = saved.typedText || "";
            }
            input.disabled = verseState.completed;
            input.readOnly = verseState.completed;
        }
        if (!state.tokenMap.get(row.dataset.index)) {
            ensureTokenized(row, verseState.normalizedText);
        }
        if (verseState.completed) {
            row.classList.add("is-complete");
            updateTokenClasses(row, verseState.normalizedText, verseState.normalizedText);
        }
    });

    if (!allCompleted) {
        activateVerse(state.currentIndex);
        requestAnimationFrame(() => {
            focusVerseInput(state.currentIndex);
        });
    }
    updateMetrics();
    return true;
};

const resetProgress = () => {
    const selection = getQueryParams();
    if (!selection.translationId || !selection.bookOrder || !selection.chapterNumber) return;
    markResetTimestamp(selection);
    resetSessionState();
    renderVerses();
    updateHeader();
    updateMetrics();
};

const endSession = async (completed) => {
    if (!state.sessionActive && !state.startedAt) return;
    state.sessionActive = false;
    state.practiceStarted = false;
    const inputs = elements.verseList.querySelectorAll(".typing-verse-input");
    inputs.forEach((input) => {
        input.disabled = true;
        input.readOnly = true;
    });
    state.endedAt = new Date();
    if (state.timerId) {
        clearInterval(state.timerId);
        state.timerId = null;
    }
    if (elements.verseStatus) elements.verseStatus.textContent = completed ? "완료" : "종료";
    updateMetrics();

    if (elements.sessionSummary) {
        elements.sessionSummary.classList.remove("d-none");
        elements.sessionSummaryText.textContent = completed
            ? "선택한 장의 모든 구절을 마쳤습니다. 기록을 저장했습니다."
            : "세션을 종료했습니다. 입력한 기록을 저장했습니다.";
    }

    try {
        await saveSession();
    } catch (error) {
        showMessage("세션 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }
};

const bindEvents = () => {
    elements.translationSelect?.addEventListener("change", async (event) => {
        const translationId = parseNumber(event.target.value);
        updateQueryParams({translationId, bookOrder: null, chapterNumber: null});
        const selection = await loadSelections();
        if (selection) await loadVerses(selection);
    });

    elements.bookSelect?.addEventListener("change", async (event) => {
        const params = getQueryParams();
        const bookOrder = parseNumber(event.target.value);
        updateQueryParams({translationId: params.translationId, bookOrder, chapterNumber: null});
        const selection = await loadSelections();
        if (selection) await loadVerses(selection);
    });

    elements.chapterSelect?.addEventListener("change", async (event) => {
        const params = getQueryParams();
        const chapterNumber = parseNumber(event.target.value);
        updateQueryParams({
            translationId: params.translationId,
            bookOrder: params.bookOrder,
            chapterNumber
        });
        const selection = await loadSelections();
        if (selection) await loadVerses(selection);
    });

    elements.ignorePunctuation?.addEventListener("change", () => {
        resetSessionState();
        renderVerses();
    });

    elements.startBtn?.addEventListener("click", () => {
        startSession();
    });

    elements.endBtn?.addEventListener("click", () => {
        endSession(false);
    });

    elements.resetBtn?.addEventListener("click", () => {
        if (confirm("현재 진행 상황을 초기화하고 처음부터 시작할까요?")) {
            resetProgress();
        }
    });
};

const initialize = async () => {
    try {
        if (elements.backButton) {
            elements.backButton.classList.remove("d-none");
            elements.backButton.addEventListener("click", () => {
                window.location.href = "/web/game";
            });
        }
        const selection = await loadSelections();
        if (!selection) return;
        await loadVerses(selection);
        bindEvents();
        window.addEventListener("popstate", async () => {
            const updatedSelection = await loadSelections();
            if (updatedSelection) await loadVerses(updatedSelection);
        });
    } catch (error) {
        showMessage("데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }
};

document.addEventListener("DOMContentLoaded", initialize);
