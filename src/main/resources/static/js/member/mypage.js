import {buildLoginRedirectUrl, checkAuthStatus} from "/js/auth/auth-check.js";
import {fetchWithAuthRetry} from "/js/common-util.js?v=2.2";

const roleLabels = {
    ADMIN: "관리자",
    USER: "회원",
};

const providerLabels = {
    google: "Google",
    naver: "Naver",
    kakao: "Kakao",
};

const updateText = (element, value) => {
    if (!element) {
        return;
    }
    element.textContent = value;
};

document.addEventListener("DOMContentLoaded", () => {
    const pageTitleLabel = document.getElementById("pageTitleLabel");
    if (pageTitleLabel) {
        pageTitleLabel.textContent = "마이페이지";
        pageTitleLabel.classList.remove("d-none");
    }

    const backButton = document.getElementById("topNavBackButton");
    if (backButton) {
        backButton.classList.remove("d-none");
        backButton.addEventListener("click", () => history.back());
    }

    // Top nav auto-hide on scroll
    let lastScrollY = window.scrollY;
    const SCROLL_THRESHOLD = 10;
    window.addEventListener("scroll", () => {
        const delta = window.scrollY - lastScrollY;
        if (Math.abs(delta) < SCROLL_THRESHOLD) return;
        if (delta > 0 && window.scrollY > 0) {
            document.body.classList.add("top-nav-hidden");
        } else {
            document.body.classList.remove("top-nav-hidden");
        }
        lastScrollY = window.scrollY;
    }, {passive: true});

    const title = document.getElementById("mypageTitle");
    const email = document.getElementById("mypageEmail");
    const oauthAccountsList = document.getElementById("mypageOAuthAccountsList");
    const roleBadge = document.getElementById("mypageRole");
    const providerBadge = document.getElementById("mypageProvider");
    const joinDateBadge = document.getElementById("mypageJoinDate");
    const avatar = document.getElementById("mypageAvatar");
    const editForm = document.getElementById("mypageEditForm");
    const nicknameInput = document.getElementById("mypageNicknameInput");
    const nicknameCount = document.getElementById("mypageNicknameCount");
    const saveButton = document.getElementById("mypageSaveButton");
    const saveToast = document.getElementById("mypageToast");
    const saveToastIcon = document.getElementById("mypageToastIcon");
    const saveToastBody = document.getElementById("mypageToastMessage");
    const saveToastClose = document.getElementById("mypageToastClose");
    const oauthActionButtons = document.querySelectorAll(".mypage-oauth-action");
    const confirmModal = document.getElementById("mypageOAuthConfirmModal");
    const confirmCancel = document.getElementById("mypageOAuthConfirmCancel");
    const confirmSubmit = document.getElementById("mypageOAuthConfirmSubmit");
    const confirmMessage = document.getElementById("mypageOAuthConfirmMessage");
    const mypageSkeleton = document.getElementById("mypageSkeleton");
    const mypageProfile = document.getElementById("mypageProfile");
    const memoSkeleton = document.getElementById("mypageMemoSkeleton");
    const urlParams = new URLSearchParams(window.location.search);
    const focusNickname = urlParams.get("focus") === "nickname";
    const returnUrl = urlParams.get("returnUrl");
    const safeReturnUrl =
        returnUrl && returnUrl.startsWith("/") && !returnUrl.startsWith("//") ? returnUrl : null;

    let memberUid = null;
    let memberEmail = "";
    let initialNickname = "";
    let pendingOAuthUnlink = null;
    let saveToastTimer = null;
    let memoPage = 0;
    let memoHasNext = false;
    let memoLoading = false;
    let memoTranslationFilter = null;
    let memoBookFilter = null;

    const memoList = document.getElementById("mypageMemoList");
    const memoEmpty = document.getElementById("mypageMemoEmpty");
    const memoMoreBtn = document.getElementById("mypageMemoMore");
    const memoFilterContainer = document.getElementById("mypageMemoFilter");
    const memoTranslationSelect = document.getElementById("mypageMemoTranslationFilter");
    const memoBookSelect = document.getElementById("mypageMemoBookFilter");

    const tabButtons = document.querySelectorAll(".mypage-tab");
    const tabPanels = {
        settings: document.getElementById("mypageTabSettings"),
        memo: document.getElementById("mypageTabMemo"),
    };
    const tabScrollPositions = {};

    const switchTab = (tabName) => {
        const currentActive = document.querySelector(".mypage-tab.active");
        if (currentActive) {
            const currentTab = currentActive.dataset.tab;
            tabScrollPositions[currentTab] = window.scrollY;
        }
        tabButtons.forEach((btn) => {
            const isTarget = btn.dataset.tab === tabName;
            btn.classList.toggle("active", isTarget);
            btn.setAttribute("aria-selected", isTarget ? "true" : "false");
        });
        Object.entries(tabPanels).forEach(([key, panel]) => {
            if (!panel) {
                return;
            }
            if (key === tabName) {
                panel.classList.remove("d-none");
            } else {
                panel.classList.add("d-none");
            }
        });
        const params = new URLSearchParams(window.location.search);
        params.set("tab", tabName);
        const newUrl = `${window.location.pathname}?${params.toString()}${window.location.hash}`;
        window.history.replaceState({}, "", newUrl);
        const savedScroll = tabScrollPositions[tabName];
        if (savedScroll != null) {
            window.scrollTo(0, savedScroll);
        } else {
            const tabsWrapper = document.querySelector(".mypage-tabs-wrapper");
            if (tabsWrapper) {
                const top = tabsWrapper.getBoundingClientRect().top + window.scrollY - 50;
                if (window.scrollY > top) {
                    window.scrollTo(0, top);
                }
            }
        }
    };

    const getInitialTab = () => {
        const params = new URLSearchParams(window.location.search);
        const tabParam = params.get("tab");
        if (tabParam === "memo" || tabParam === "settings") {
            return tabParam;
        }
        return "settings";
    };

    tabButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            switchTab(btn.dataset.tab);
        });
    });

    const redirectToLogin = () => {
        window.location.replace(buildLoginRedirectUrl());
    };

    const showProfile = () => {
        mypageSkeleton?.classList.add("d-none");
        mypageProfile?.classList.remove("d-none");
    };

    const showOAuthErrorFromUrl = () => {
        const params = new URLSearchParams(window.location.search);
        const errorCode = params.get("oauthError");
        if (!errorCode) {
            return;
        }
        const messages = {
            OAUTH_ACCOUNT_ALREADY_LINKED: "이미 다른 계정에 연결된 소셜 계정입니다. 다른 계정으로 시도해 주세요.",
            OAUTH_EMAIL_MISSING: "소셜 계정 이메일 정보를 가져오지 못했습니다.",
            OAUTH_PROVIDER_USER_ID_MISSING: "소셜 계정 식별 정보를 가져오지 못했습니다.",
            OAUTH_LINK_REQUIRED: "연동 전용 요청입니다. 마이페이지의 연동하기 버튼으로 다시 시도해 주세요.",
            UNKNOWN: "소셜 계정 연동에 실패했습니다. 다시 시도해 주세요.",
        };
        showSaveToast(messages[errorCode] || messages.UNKNOWN, "error");
        params.delete("oauthError");
        const newQuery = params.toString();
        const newUrl = `${window.location.pathname}${newQuery ? `?${newQuery}` : ""}${window.location.hash}`;
        window.history.replaceState({}, "", newUrl);
    };

    const setFormEnabled = (enabled) => {
        if (!nicknameInput || !saveButton) {
            return;
        }
        nicknameInput.disabled = !enabled;
        saveButton.disabled = !enabled;
    };

    const hideSaveToast = () => {
        if (!saveToast) {
            return;
        }
        saveToast.classList.remove("show");
        saveToast.setAttribute("aria-hidden", "true");
        if (saveToastTimer) {
            clearTimeout(saveToastTimer);
            saveToastTimer = null;
        }
    };

    const setToastVariant = (variant) => {
        if (!saveToast || !saveToastIcon) {
            return;
        }
        saveToast.classList.remove("is-error", "is-info", "is-success");
        saveToastIcon.classList.remove("success", "error", "info");
        switch (variant) {
            case "error":
                saveToast.classList.add("is-error");
                saveToastIcon.classList.add("error");
                break;
            case "info":
                saveToast.classList.add("is-info");
                saveToastIcon.classList.add("info");
                break;
            default:
                saveToast.classList.add("is-success");
                saveToastIcon.classList.add("success");
                break;
        }
    };

    const showSaveToast = (message, variant = "success") => {
        if (!saveToast) {
            return;
        }
        setToastVariant(variant);
        if (saveToastBody) {
            saveToastBody.textContent = message;
        }
        saveToast.classList.add("show");
        saveToast.removeAttribute("aria-hidden");
        if (saveToastTimer) {
            clearTimeout(saveToastTimer);
        }
        saveToastTimer = setTimeout(() => {
            hideSaveToast();
        }, 4000);
    };

    const formatConnectedAt = (value) => {
        if (!value) {
            return "연동됨";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return "연동됨";
        }
        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const maskEmail = (value) => {
        if (!value) {
            return "이메일 없음";
        }
        const [local, domain] = value.split("@");
        if (!domain) {
            return "이메일 없음";
        }
        if (local.length <= 2) {
            return `${local.charAt(0)}***@${domain}`;
        }
        return `${local.slice(0, 2)}***@${domain}`;
    };

    const renderOAuthAccounts = (accounts) => {
        if (!oauthAccountsList) {
            return;
        }
        const providerMap = new Map();
        if (Array.isArray(accounts)) {
            accounts.forEach((account) => {
                const providerKey = (account.provider || "").toLowerCase();
                if (providerKey) {
                    providerMap.set(providerKey, account);
                }
            });
        }
        updateOAuthCards(providerMap);
        updateText(providerBadge, `연동 계정 ${providerMap.size}개`);
    };

    let modalTriggerElement = null;

    const trapFocusInModal = (event) => {
        if (!confirmModal || confirmModal.classList.contains("d-none")) {
            return;
        }
        if (event.key !== "Tab") {
            return;
        }
        const focusableElements = confirmModal.querySelectorAll(
            "button:not([disabled]), [href]:not([disabled]), [tabindex]:not([tabindex='-1'])"
        );
        if (focusableElements.length === 0) {
            return;
        }
        const first = focusableElements[0];
        const last = focusableElements[focusableElements.length - 1];
        if (event.shiftKey) {
            if (document.activeElement === first) {
                event.preventDefault();
                last.focus();
            }
        } else {
            if (document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        }
    };

    const openConfirmModal = (providerLabel) => {
        if (!confirmModal) {
            return;
        }
        modalTriggerElement = document.activeElement;
        if (confirmMessage) {
            confirmMessage.textContent = `${providerLabel} 계정을 연동 해제하시겠습니까?`;
        }
        confirmModal.classList.remove("d-none");
        document.body.style.overflow = "hidden";
        document.addEventListener("keydown", trapFocusInModal);
        if (confirmSubmit) {
            confirmSubmit.focus();
        }
    };

    const closeConfirmModal = () => {
        if (!confirmModal) {
            return;
        }
        confirmModal.classList.add("d-none");
        document.body.style.overflow = "";
        document.removeEventListener("keydown", trapFocusInModal);
        const triggerToRestore = modalTriggerElement;
        pendingOAuthUnlink = null;
        modalTriggerElement = null;
        if (triggerToRestore && typeof triggerToRestore.focus === "function") {
            triggerToRestore.focus();
        }
    };

    const updateOAuthCards = (providerMap) => {
        if (!oauthAccountsList) {
            return;
        }
        const returnUrl = `${window.location.pathname}${window.location.search}`;
        const cards = oauthAccountsList.querySelectorAll(".mypage-oauth-card");
        cards.forEach((card) => {
            const provider = card.dataset.provider;
            const providerLabel = providerLabels[provider] || provider;
            const status = card.querySelector(".mypage-oauth-status, .mypage-oauth-status-badge");
            const emailField = card.querySelector(".mypage-oauth-email");
            const nicknameField = card.querySelector(".mypage-oauth-nickname");
            const connectedField = card.querySelector(".mypage-oauth-connected");
            const actionButton = card.querySelector(".mypage-oauth-action");
            const notice = card.querySelector(".mypage-oauth-notice");

            const linkedAccount = providerMap.get(provider);

            card.setAttribute("aria-label",
                linkedAccount
                    ? `${providerLabel} 계정 연동됨`
                    : `${providerLabel} 계정 미연동`
            );

            if (linkedAccount) {
                card.classList.remove("is-empty");
                if (status) {
                    status.className = "mypage-oauth-status-badge is-linked";
                    status.textContent = "연동됨";
                }
                updateText(emailField, maskEmail(linkedAccount.email));
                updateText(nicknameField, linkedAccount.nickname || "닉네임 없음");
                updateText(connectedField, formatConnectedAt(linkedAccount.createdAt));
                if (actionButton) {
                    actionButton.textContent = `${providerLabel} 연동 해제`;
                    actionButton.classList.remove("btn-outline-secondary");
                    actionButton.classList.add("btn-outline-danger");
                    actionButton.dataset.action = "unlink";
                    actionButton.dataset.providerUserId = linkedAccount.providerUserId;
                    actionButton.setAttribute("href", "#");
                    actionButton.removeAttribute("aria-disabled");
                    actionButton.classList.remove("disabled");
                }
                const isPrimary = memberEmail
                    && linkedAccount.email
                    && memberEmail.toLowerCase() === linkedAccount.email.toLowerCase();
                if (isPrimary) {
                    if (notice) {
                        notice.textContent = "최초 가입 계정은 해제할 수 없습니다. 해제하려면 회원 탈퇴를 진행해 주세요.";
                        notice.classList.remove("d-none");
                    }
                    if (actionButton) {
                        actionButton.classList.add("disabled");
                        actionButton.classList.add("mypage-oauth-action-disabled");
                        actionButton.setAttribute("aria-disabled", "true");
                        actionButton.dataset.action = "disabled";
                    }
                } else if (notice) {
                    notice.textContent = "";
                    notice.classList.add("d-none");
                    if (actionButton) {
                        actionButton.classList.remove("mypage-oauth-action-disabled");
                    }
                }
            } else {
                card.classList.add("is-empty");
                if (status) {
                    status.className = "mypage-oauth-status-badge is-unlinked";
                    status.textContent = "미연동";
                }
                updateText(emailField, "-");
                updateText(nicknameField, "-");
                updateText(connectedField, "-");
                if (notice) {
                    notice.textContent = "";
                    notice.classList.add("d-none");
                }
                if (actionButton) {
                    actionButton.textContent = `${providerLabel} 연동하기`;
                    actionButton.classList.add("btn-outline-secondary");
                    actionButton.classList.remove("btn-outline-danger");
                    actionButton.classList.remove("mypage-oauth-action-disabled");
                    actionButton.dataset.action = "link";
                    actionButton.dataset.providerUserId = "";
                    actionButton.setAttribute(
                        "href",
                        `/oauth2/authorization/${provider}?returnUrl=${encodeURIComponent(returnUrl)}&link=true`
                    );
                    actionButton.removeAttribute("aria-disabled");
                    actionButton.classList.remove("disabled");
                }
            }
        });
    };

    const handleOAuthAction = async (event) => {
        const target = event.currentTarget;
        if (!target || target.dataset.action !== "unlink") {
            return;
        }
        event.preventDefault();
        hideSaveToast();
        if (!memberUid) {
            showSaveToast("회원 정보를 확인할 수 없습니다. 다시 로그인해 주세요.", "error");
            return;
        }
        const provider = target.dataset.provider;
        const providerUserId = target.dataset.providerUserId;
        if (!provider || !providerUserId) {
            showSaveToast("연동 정보를 확인할 수 없습니다.", "error");
            return;
        }
        const providerLabel = providerLabels[provider] || provider;
        pendingOAuthUnlink = {provider, providerUserId, button: target, providerLabel};
        openConfirmModal(providerLabel);
    };

    const confirmOAuthUnlink = async () => {
        if (!pendingOAuthUnlink) {
            closeConfirmModal();
            return;
        }
        const {provider, providerUserId, button} = pendingOAuthUnlink;
        button.setAttribute("aria-disabled", "true");
        button.classList.add("disabled");
        try {
            const response = await fetchWithAuthRetry(`/api/v1/members/${memberUid}/oauth-accounts?provider=${provider}&providerUserId=${providerUserId}`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    Accept: "application/json",
                },
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (!response.ok) {
                showSaveToast("연동 해제에 실패했습니다. 다시 시도해 주세요.", "error");
                return;
            }
            showSaveToast("연동 계정이 해제되었습니다.");
            loadOAuthAccounts();
        } catch (error) {
            showSaveToast("연동 해제 중 오류가 발생했습니다. 다시 시도해 주세요.", "error");
        } finally {
            button.classList.remove("disabled");
            button.removeAttribute("aria-disabled");
            closeConfirmModal();
        }
    };
    const loadOAuthAccounts = async () => {
        if (!memberUid) {
            return;
        }
        try {
            const response = await fetchWithAuthRetry(`/api/v1/members/${memberUid}/oauth-accounts`, {
                credentials: "include",
                headers: {
                    Accept: "application/json",
                },
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (response.status === 403) {
                showSaveToast("연동 계정 정보를 불러올 수 없습니다.", "error");
                return;
            }
            if (!response.ok) {
                showSaveToast("연동 계정 정보를 불러오지 못했습니다. 다시 시도해 주세요.", "error");
                return;
            }
            const data = await response.json().catch(() => []);
            renderOAuthAccounts(Array.isArray(data) ? data : []);
        } catch (error) {
            showSaveToast("연동 계정 정보를 불러오지 못했습니다. 다시 시도해 주세요.", "error");
        }
    };

    const formatMemoDate = (value) => {
        if (!value) {
            return "";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return "";
        }
        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const createMemoCard = (memo) => {
        const card = document.createElement("a");
        card.className = "mypage-memo-card";
        card.href = `/web/bible/verse?translationId=${encodeURIComponent(memo.translationId)}&bookOrder=${encodeURIComponent(memo.bookOrder)}&chapterNumber=${encodeURIComponent(memo.chapterNumber)}&verseNumber=${encodeURIComponent(memo.verseNumber)}&from=mypage`;

        const header = document.createElement("div");
        header.className = "mypage-memo-card-header";

        const refSpan = document.createElement("span");
        refSpan.className = "mypage-memo-card-ref";
        refSpan.textContent = `${memo.bookName} ${memo.chapterNumber}:${memo.verseNumber}`;

        const dateSpan = document.createElement("span");
        dateSpan.className = "mypage-memo-card-date";
        dateSpan.textContent = formatMemoDate(memo.updatedAt);

        header.appendChild(refSpan);
        header.appendChild(dateSpan);

        const content = document.createElement("div");
        content.className = "mypage-memo-card-content";
        content.textContent = memo.content;

        card.appendChild(header);
        card.appendChild(content);
        return card;
    };

    const loadMyMemos = async (append = false) => {
        if (memoLoading) {
            return;
        }
        memoLoading = true;
        if (!append) {
            memoSkeleton?.classList.remove("d-none");
        }
        try {
            let memoApiUrl = `/api/v1/bibles/my-memos?page=${memoPage}&size=20`;
            if (memoTranslationFilter != null) {
                memoApiUrl += `&translationId=${memoTranslationFilter}`;
            }
            if (memoBookFilter != null) {
                memoApiUrl += `&bookOrder=${memoBookFilter}`;
            }
            const response = await fetchWithAuthRetry(memoApiUrl, {
                credentials: "include",
                headers: {Accept: "application/json"},
            });
            if (response.status === 401) {
                redirectToLogin();
                return;
            }
            if (!response.ok) {
                return;
            }
            const data = await response.json().catch(() => null);
            if (!data) {
                return;
            }

            if (!append && memoList) {
                memoList.innerHTML = "";
            }

            if (data.content && data.content.length > 0) {
                if (memoList) {
                    const fragment = document.createDocumentFragment();
                    data.content.forEach((memo) => {
                        fragment.appendChild(createMemoCard(memo));
                    });
                    memoList.appendChild(fragment);
                }
                if (memoEmpty) {
                    memoEmpty.classList.add("d-none");
                }
            } else if (!append) {
                if (memoList) {
                    memoList.innerHTML = "";
                }
                if (memoEmpty) {
                    memoEmpty.classList.remove("d-none");
                }
            }

            if (!append && data.totalCount != null) {
                const countBadge = document.getElementById("mypageMemoCountBadge");
                if (countBadge) {
                    countBadge.textContent = data.totalCount;
                    countBadge.classList.remove("d-none");
                }
                const tabBadge = document.getElementById("mypageTabMemoBadge");
                if (tabBadge) {
                    if (data.totalCount > 0) {
                        tabBadge.textContent = data.totalCount;
                        tabBadge.classList.remove("d-none");
                    } else {
                        tabBadge.classList.add("d-none");
                    }
                }
            }

            memoHasNext = data.hasNext === true;
            if (memoMoreBtn) {
                if (memoHasNext) {
                    memoMoreBtn.classList.remove("d-none");
                } else {
                    memoMoreBtn.classList.add("d-none");
                }
            }
        } catch (error) {
            if (!append && memoList) {
                memoList.innerHTML = "";
                const errorDiv = document.createElement("div");
                errorDiv.className = "mypage-memo-empty";
                const errorMsg = document.createElement("p");
                errorMsg.className = "mb-2";
                errorMsg.textContent = "메모를 불러오지 못했습니다.";
                const retryBtn = document.createElement("button");
                retryBtn.type = "button";
                retryBtn.className = "btn btn-outline-primary btn-sm";
                retryBtn.textContent = "다시 시도";
                retryBtn.addEventListener("click", () => {
                    memoPage = 0;
                    loadMyMemos(false);
                });
                errorDiv.appendChild(errorMsg);
                errorDiv.appendChild(retryBtn);
                memoList.appendChild(errorDiv);
            }
        } finally {
            memoSkeleton?.classList.add("d-none");
            memoLoading = false;
        }
    };

    const loadMemoTranslationFilter = async () => {
        if (!memoTranslationSelect || !memoFilterContainer) {
            return;
        }
        try {
            const response = await fetchWithAuthRetry("/api/v1/bibles/my-memos/translations", {
                credentials: "include",
                headers: {Accept: "application/json"},
            });
            if (!response.ok) {
                return;
            }
            const translations = await response.json().catch(() => []);
            if (!Array.isArray(translations) || translations.length === 0) {
                return;
            }
            memoFilterContainer.classList.remove("d-none");
            memoTranslationSelect.innerHTML = '<option value="">전체 번역본</option>';
            translations.forEach((t) => {
                const option = document.createElement("option");
                option.value = t.translationId;
                option.textContent = t.translationName;
                memoTranslationSelect.appendChild(option);
            });
            if (translations.length === 1) {
                memoTranslationSelect.value = String(translations[0].translationId);
                memoTranslationFilter = translations[0].translationId;
                await loadMemoBookFilter(memoTranslationFilter);
            }
        } catch {
            // 필터 로드 실패 시 무시
        }
    };

    const loadMemoBookFilter = async (translationId) => {
        if (!memoBookSelect) {
            return;
        }
        memoBookSelect.innerHTML = '<option value="">전체 성경</option>';
        memoBookFilter = null;
        if (!translationId) {
            memoBookSelect.disabled = true;
            return;
        }
        try {
            const response = await fetchWithAuthRetry(`/api/v1/bibles/my-memos/books?translationId=${translationId}`, {
                credentials: "include",
                headers: {Accept: "application/json"},
            });
            if (!response.ok) {
                return;
            }
            const books = await response.json().catch(() => []);
            if (!Array.isArray(books) || books.length === 0) {
                memoBookSelect.disabled = true;
                return;
            }
            books.forEach((book) => {
                const option = document.createElement("option");
                option.value = book.bookOrder;
                option.textContent = book.bookName;
                memoBookSelect.appendChild(option);
            });
            memoBookSelect.disabled = false;
        } catch {
            memoBookSelect.disabled = true;
        }
    };

    if (memoTranslationSelect) {
        memoTranslationSelect.addEventListener("change", async () => {
            const value = memoTranslationSelect.value;
            memoTranslationFilter = value ? parseInt(value, 10) : null;
            memoBookFilter = null;
            memoPage = 0;
            await loadMemoBookFilter(memoTranslationFilter);
            loadMyMemos(false);
        });
    }

    if (memoBookSelect) {
        memoBookSelect.addEventListener("change", () => {
            const value = memoBookSelect.value;
            memoBookFilter = value ? parseInt(value, 10) : null;
            memoPage = 0;
            loadMyMemos(false);
        });
    }

    if (memoMoreBtn) {
        memoMoreBtn.addEventListener("click", () => {
            memoPage++;
            loadMyMemos(true);
        });
    }

    setFormEnabled(false);

    if (nicknameInput) {
        nicknameInput.addEventListener("input", () => {
            const len = nicknameInput.value.trim().length;
            if (nicknameCount) {
                nicknameCount.textContent = len;
            }
            saveButton.disabled = (nicknameInput.value.trim() === initialNickname);
        });
    }

    if (oauthActionButtons && oauthActionButtons.length > 0) {
        oauthActionButtons.forEach((button) => {
            button.addEventListener("click", handleOAuthAction);
        });
    }
    if (confirmCancel) {
        confirmCancel.addEventListener("click", closeConfirmModal);
    }
    if (confirmSubmit) {
        confirmSubmit.addEventListener("click", confirmOAuthUnlink);
    }
    if (saveToastClose) {
        saveToastClose.addEventListener("click", hideSaveToast);
    }
    if (confirmModal) {
        confirmModal.addEventListener("click", (event) => {
            const target = event.target;
            if (target && target.dataset && target.dataset.modalClose) {
                closeConfirmModal();
            }
        });
        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape" && !confirmModal.classList.contains("d-none")) {
                closeConfirmModal();
            }
        });
    }

    checkAuthStatus({
        onAuthenticated: (data) => {
            if (!data) {
                showSaveToast("회원 정보를 불러오지 못했습니다. 다시 시도해 주세요.", "error");
                return;
            }

            memberUid = data.memberUid || null;
            memberEmail = data.email || "";
            const nicknameValue = (data.nickname || "").trim();
            const displayName = nicknameValue || (memberEmail ? memberEmail.split("@")[0] : "회원");
            const roleLabel = roleLabels[data.role] || data.role || "회원";

            updateText(title, displayName);
            updateText(email, memberEmail || "이메일 정보 없음");
            updateText(roleBadge, roleLabel);
            updateText(providerBadge, "연동 계정 확인 중");

            if (data.createdAt) {
                const joinDate = new Date(data.createdAt);
                if (!Number.isNaN(joinDate.getTime())) {
                    const formatted = joinDate.toLocaleDateString("ko-KR", {
                        year: "numeric", month: "long"
                    });
                    updateText(joinDateBadge, `가입 ${formatted}`);
                }
            }

            if (avatar && data.profileImageUrl) {
                avatar.src = data.profileImageUrl;
                avatar.alt = `${displayName} 프로필 이미지`;
            }

            initialNickname = nicknameValue;
            if (nicknameInput) {
                nicknameInput.value = initialNickname;
            }
            if (nicknameCount) {
                nicknameCount.textContent = initialNickname.length;
            }
            setFormEnabled(true);
            saveButton.disabled = true;

            showProfile();

            const initialTab = getInitialTab();
            switchTab(initialTab);

            if (focusNickname && nicknameInput) {
                nicknameInput.focus();
                nicknameInput.select();
                nicknameInput.scrollIntoView({behavior: "smooth", block: "center"});
            }
            loadOAuthAccounts();
            loadMyMemos();
            loadMemoTranslationFilter();
        },
        onUnauthenticated: redirectToLogin,
        onError: () => showSaveToast("인증 정보를 확인할 수 없습니다. 다시 로그인해 주세요.", "error"),
    });

    showOAuthErrorFromUrl();

    if (editForm) {
        editForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            hideSaveToast();

            if (!memberUid) {
                showSaveToast("회원 정보를 확인할 수 없습니다. 다시 로그인해 주세요.", "error");
                return;
            }

            const nicknameValue = nicknameInput ? nicknameInput.value.trim() : "";

            if (!nicknameValue) {
                showSaveToast("닉네임을 입력해 주세요.", "error");
                return;
            }

            setFormEnabled(false);

            try {
                const response = await fetchWithAuthRetry(`/api/v1/members/${memberUid}`, {
                    method: "PUT",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                    },
                    body: JSON.stringify({
                        nickname: nicknameValue,
                    }),
                });

                if (response.status === 401) {
                    redirectToLogin();
                    return;
                }

                if (response.status === 403) {
                    showSaveToast("회원 정보에 접근할 수 없습니다.", "error");
                    return;
                }

                if (!response.ok) {
                    const error = await response.json().catch(() => null);
                    showSaveToast(error?.message || "회원 정보 수정에 실패했습니다. 다시 시도해 주세요.", "error");
                    return;
                }

                const data = await response.json().catch(() => null);
                const updatedNickname = (data?.nickname || nicknameValue).trim();
                const updatedEmail = data?.email || "";
                const updatedRole = roleLabels[data?.role] || data?.role || "회원";
                const displayName = updatedNickname || (updatedEmail ? updatedEmail.split("@")[0] : "회원");

                updateText(title, displayName);
                updateText(email, updatedEmail || "이메일 정보 없음");
                updateText(roleBadge, updatedRole);

                initialNickname = updatedNickname;
                if (nicknameCount) {
                    nicknameCount.textContent = updatedNickname.length;
                }

                showSaveToast("회원 정보가 저장되었습니다.");
                if (focusNickname && safeReturnUrl) {
                    setTimeout(() => {
                        window.location.href = safeReturnUrl;
                    }, 300);
                }
            } catch (error) {
                showSaveToast("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", "error");
            } finally {
                setFormEnabled(true);
                saveButton.disabled = true;
            }
        });
    }

});
