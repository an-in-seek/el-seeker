import {buildLoginRedirectUrl, checkAuthStatus, showAuthError} from "/js/auth/auth-check.js";

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
    const providerDetail = document.getElementById("mypageProviderDetail");
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
    const resetButton = document.getElementById("mypageResetButton");

    let memberUid = null;
    let initialNickname = "";

    const redirectToLogin = () => {
        window.location.replace(buildLoginRedirectUrl());
    };

    const setFormEnabled = (enabled) => {
        if (!nicknameInput || !saveButton || !resetButton) {
            return;
        }
        nicknameInput.disabled = !enabled;
        saveButton.disabled = !enabled;
        resetButton.disabled = !enabled;
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

    checkAuthStatus({
        onAuthenticated: (data) => {
            if (!data) {
                showAuthError(errorMessage, "회원 정보를 불러오지 못했습니다. 다시 시도해 주세요.");
                return;
            }

            memberUid = data.memberUid || null;
            const emailValue = data.email || "";
            const nicknameValue = (data.nickname || "").trim();
            const displayName = nicknameValue || (emailValue ? emailValue.split("@")[0] : "회원");
            const roleLabel = roleLabels[data.role] || data.role || "회원";
            const providerKey = (data.provider || "").toLowerCase();
            const providerLabel = providerLabels[providerKey] || data.provider || "연동 없음";

            updateText(title, `${displayName} 님`);
            updateText(email, emailValue || "이메일 정보 없음");
            updateText(nicknameDetail, nicknameValue || "미등록");
            updateText(emailDetail, emailValue || "미등록");
            updateText(roleDetail, roleLabel);
            updateText(providerDetail, providerLabel);
            updateText(roleBadge, roleLabel);
            updateText(providerBadge, providerLabel);

            if (avatar && data.profileImageUrl) {
                avatar.src = data.profileImageUrl;
                avatar.alt = `${displayName} 프로필 이미지`;
            }

            initialNickname = nicknameValue;
            if (nicknameInput) {
                nicknameInput.value = initialNickname;
            }
            setFormEnabled(true);
        },
        onUnauthenticated: redirectToLogin,
        onError: () => showAuthError(errorMessage, "인증 정보를 확인할 수 없습니다. 다시 로그인해 주세요."),
    });

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
                        // profileImageUrl is omitted as requested
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
                const updatedProviderKey = (data?.provider || "").toLowerCase();
                const updatedProviderLabel = providerLabels[updatedProviderKey] || data?.provider || "연동 없음";
                const displayName = updatedNickname || (updatedEmail ? updatedEmail.split("@")[0] : "회원");

                updateText(title, `${displayName} 님`);
                updateText(email, updatedEmail || "이메일 정보 없음");
                updateText(nicknameDetail, updatedNickname || "미등록");
                updateText(emailDetail, updatedEmail || "미등록");
                updateText(roleDetail, updatedRole);
                updateText(providerDetail, updatedProviderLabel);
                updateText(roleBadge, updatedRole);
                updateText(providerBadge, updatedProviderLabel);

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

    if (resetButton) {
        resetButton.addEventListener("click", () => {
            resetMessages();
            if (nicknameInput) {
                nicknameInput.value = initialNickname;
            }
        });
    }

    loadDailyVerse();
});
