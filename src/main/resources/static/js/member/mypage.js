import {buildLoginRedirectUrl, checkAuthStatus, showAuthError} from "/js/auth/auth-check.js";
import {LastReadStore} from "/js/storage-util.js?v=2.1";

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

    const title = document.getElementById("mypageTitle");
    const email = document.getElementById("mypageEmail");
    const nicknameDetail = document.getElementById("mypageNicknameDetail");
    const emailDetail = document.getElementById("mypageEmailDetail");
    const roleDetail = document.getElementById("mypageRoleDetail");
    const oauthAccountsList = document.getElementById("mypageOAuthAccountsList");
    const roleBadge = document.getElementById("mypageRole");
    const providerBadge = document.getElementById("mypageProvider");
    const avatar = document.getElementById("mypageAvatar");
    const dailyVerseText = document.getElementById("dailyVerseText");
    const dailyVerseRef = document.getElementById("dailyVerseRef");
    const errorMessage = document.getElementById("mypageErrorMessage");
    const successMessage = document.getElementById("mypageSuccessMessage");
    const editForm = document.getElementById("mypageEditForm");
    const nicknameInput = document.getElementById("mypageNicknameInput");
    const saveButton = document.getElementById("mypageSaveButton");
    const homeButton = document.querySelector(".top-nav-home-button");
    const oauthActionButtons = document.querySelectorAll(".mypage-oauth-action");
    const confirmModal = document.getElementById("mypageOAuthConfirmModal");
    const confirmCancel = document.getElementById("mypageOAuthConfirmCancel");
    const confirmSubmit = document.getElementById("mypageOAuthConfirmSubmit");
    const confirmMessage = document.getElementById("mypageOAuthConfirmMessage");

    let memberUid = null;
    let memberEmail = "";
    let initialNickname = "";
    let pendingOAuthUnlink = null;

    const redirectToLogin = () => {
        window.location.replace(buildLoginRedirectUrl());
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
        showAuthError(errorMessage, messages[errorCode] || messages.UNKNOWN);
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

    const resetMessages = () => {
        if (successMessage) {
            successMessage.classList.add("d-none");
            successMessage.textContent = "";
        }
        if (errorMessage) {
            errorMessage.classList.add("d-none");
            errorMessage.textContent = "";
        }
    };

    const setDailyVerseFallback = (message) => {
        updateText(dailyVerseText, message);
        updateText(dailyVerseRef, "개역한글(KRV)");
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

    const openConfirmModal = (providerLabel) => {
        if (!confirmModal) {
            return;
        }
        if (confirmMessage) {
            confirmMessage.textContent = `${providerLabel} 계정을 연동 해제하시겠습니까?`;
        }
        confirmModal.classList.remove("d-none");
        if (confirmSubmit) {
            confirmSubmit.focus();
        }
    };

    const closeConfirmModal = () => {
        if (!confirmModal) {
            return;
        }
        confirmModal.classList.add("d-none");
        pendingOAuthUnlink = null;
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
            const status = card.querySelector(".mypage-oauth-status");
            const emailField = card.querySelector(".mypage-oauth-email");
            const nicknameField = card.querySelector(".mypage-oauth-nickname");
            const connectedField = card.querySelector(".mypage-oauth-connected");
            const actionButton = card.querySelector(".mypage-oauth-action");
            const notice = card.querySelector(".mypage-oauth-notice");

            const linkedAccount = providerMap.get(provider);
            if (linkedAccount) {
                card.classList.remove("is-empty");
                updateText(status, "연동됨");
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
                        actionButton.setAttribute("aria-disabled", "true");
                        actionButton.dataset.action = "disabled";
                    }
                } else if (notice) {
                    notice.textContent = "";
                    notice.classList.add("d-none");
                }
            } else {
                card.classList.add("is-empty");
                updateText(status, "미연동");
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
        resetMessages();
        if (!memberUid) {
            showAuthError(errorMessage, "회원 정보를 확인할 수 없습니다. 다시 로그인해 주세요.");
            return;
        }
        const provider = target.dataset.provider;
        const providerUserId = target.dataset.providerUserId;
        if (!provider || !providerUserId) {
            showAuthError(errorMessage, "연동 정보를 확인할 수 없습니다.");
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
            const response = await fetch(`/api/v1/members/${memberUid}/oauth-accounts?provider=${provider}&providerUserId=${providerUserId}`, {
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
                showAuthError(errorMessage, "연동 해제에 실패했습니다. 다시 시도해 주세요.");
                return;
            }
            if (successMessage) {
                successMessage.textContent = "연동 계정이 해제되었습니다.";
                successMessage.classList.remove("d-none");
            }
            loadOAuthAccounts();
        } catch (error) {
            showAuthError(errorMessage, "연동 해제 중 오류가 발생했습니다. 다시 시도해 주세요.");
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
            const response = await fetch(`/api/v1/members/${memberUid}/oauth-accounts`, {
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
                showAuthError(errorMessage, "연동 계정 정보를 불러올 수 없습니다.");
                return;
            }
            if (!response.ok) {
                showAuthError(errorMessage, "연동 계정 정보를 불러오지 못했습니다. 다시 시도해 주세요.");
                return;
            }
            const data = await response.json().catch(() => []);
            renderOAuthAccounts(Array.isArray(data) ? data : []);
        } catch (error) {
            showAuthError(errorMessage, "연동 계정 정보를 불러오지 못했습니다. 다시 시도해 주세요.");
        }
    };

    const loadDailyVerse = async () => {
        if (!dailyVerseText || !dailyVerseRef) {
            return;
        }

        try {
            const response = await fetch("/api/v1/bibles/daily?translationType=KRV", {
                headers: {
                    Accept: "application/json",
                },
            });

            if (!response.ok) {
                throw new Error("daily verse fetch failed");
            }

            const data = await response.json();
            const reference = `${data.translationName}(${data.translationType}) · ${data.bookName} ${data.chapterNumber}:${data.verseNumber}`;
            updateText(dailyVerseText, data.text);
            updateText(dailyVerseRef, reference);
        } catch (error) {
            setDailyVerseFallback("오늘의 묵상을 불러오지 못했습니다.");
        }
    };

    setFormEnabled(false);
    if (homeButton) {
        homeButton.addEventListener("click", () => {
            LastReadStore.clear();
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
                showAuthError(errorMessage, "회원 정보를 불러오지 못했습니다. 다시 시도해 주세요.");
                return;
            }

            memberUid = data.memberUid || null;
            memberEmail = data.email || "";
            const emailValue = data.email || "";
            const nicknameValue = (data.nickname || "").trim();
            const displayName = nicknameValue || (emailValue ? emailValue.split("@")[0] : "회원");
            const roleLabel = roleLabels[data.role] || data.role || "회원";

            updateText(title, `${displayName}`);
            updateText(email, emailValue || "이메일 정보 없음");
            updateText(nicknameDetail, nicknameValue || "미등록");
            updateText(emailDetail, emailValue || "미등록");
            updateText(roleDetail, roleLabel);
            updateText(roleBadge, roleLabel);
            updateText(providerBadge, "연동 계정 확인 중");

            if (avatar && data.profileImageUrl) {
                avatar.src = data.profileImageUrl;
                avatar.alt = `${displayName} 프로필 이미지`;
            }

            initialNickname = nicknameValue;
            if (nicknameInput) {
                nicknameInput.value = initialNickname;
            }
            setFormEnabled(true);
            updateOAuthCards(new Map());
            loadOAuthAccounts();
        },
        onUnauthenticated: redirectToLogin,
        onError: () => showAuthError(errorMessage, "인증 정보를 확인할 수 없습니다. 다시 로그인해 주세요."),
    });

    showOAuthErrorFromUrl();

    if (editForm) {
        editForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            resetMessages();

            if (!memberUid) {
                showAuthError(errorMessage, "회원 정보를 확인할 수 없습니다. 다시 로그인해 주세요.");
                return;
            }

            const nicknameValue = nicknameInput ? nicknameInput.value.trim() : "";

            if (!nicknameValue) {
                showAuthError(errorMessage, "닉네임을 입력해 주세요.");
                return;
            }

            setFormEnabled(false);

            try {
                const response = await fetch(`/api/v1/members/${memberUid}`, {
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
                    showAuthError(errorMessage, "회원 정보에 접근할 수 없습니다.");
                    return;
                }

                if (!response.ok) {
                    showAuthError(errorMessage, "회원 정보 수정에 실패했습니다. 다시 시도해 주세요.");
                    return;
                }

                const data = await response.json().catch(() => null);
                const updatedNickname = (data?.nickname || nicknameValue).trim();
                const updatedEmail = data?.email || "";
                const updatedRole = roleLabels[data?.role] || data?.role || "회원";
                const displayName = updatedNickname || (updatedEmail ? updatedEmail.split("@")[0] : "회원");

                updateText(title, `${displayName} 님`);
                updateText(email, updatedEmail || "이메일 정보 없음");
                updateText(nicknameDetail, updatedNickname || "미등록");
                updateText(emailDetail, updatedEmail || "미등록");
                updateText(roleDetail, updatedRole);
                updateText(roleBadge, updatedRole);
                loadOAuthAccounts();

                // Profile image update logic removed

                initialNickname = updatedNickname;

                if (successMessage) {
                    successMessage.textContent = "회원 정보가 저장되었습니다.";
                    successMessage.classList.remove("d-none");
                }
            } catch (error) {
                showAuthError(errorMessage, "네트워크 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            } finally {
                setFormEnabled(true);
            }
        });
    }

    loadDailyVerse();
});
