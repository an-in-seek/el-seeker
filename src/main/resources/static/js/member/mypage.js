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
    const oauthAccountsEmpty = document.getElementById("mypageOAuthAccountsEmpty");
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

    let memberUid = null;
    let initialNickname = "";

    const redirectToLogin = () => {
        window.location.replace(buildLoginRedirectUrl());
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
        if (!oauthAccountsList || !oauthAccountsEmpty) {
            return;
        }
        oauthAccountsList.innerHTML = "";
        if (!Array.isArray(accounts) || accounts.length === 0) {
            oauthAccountsList.classList.add("d-none");
            oauthAccountsEmpty.classList.remove("d-none");
            updateText(providerBadge, "연동 계정 0개");
            return;
        }
        accounts.forEach((account) => {
            const card = document.createElement("div");
            card.className = "mypage-oauth-card";
            const header = document.createElement("div");
            header.className = "mypage-oauth-header";
            const providerKey = (account.provider || "").toLowerCase();
            const icon = document.createElement("img");
            icon.className = "mypage-oauth-icon";
            const iconMap = {
                google: "/images/btn_google.svg",
                naver: "/images/btn_naver.svg",
                kakao: "/images/btn_kakao.svg",
            };
            icon.src = iconMap[providerKey] || "/images/user.png";
            icon.alt = `${providerLabels[providerKey] || account.provider || "연동 계정"} 로고`;
            icon.loading = "lazy";
            icon.onerror = () => {
                icon.src = "/images/user.png";
            };
            const label = document.createElement("span");
            label.textContent = providerLabels[providerKey] || account.provider || "연동 계정";
            header.appendChild(icon);
            header.appendChild(label);

            const meta = document.createElement("div");
            meta.className = "mypage-oauth-meta";
            const maskedEmail = maskEmail(account.email);
            const nickname = account.nickname || "닉네임 없음";
            meta.innerHTML = `
                <span>이메일</span>
                <strong>${maskedEmail}</strong>
                <span>닉네임</span>
                <strong>${nickname}</strong>
                <span>연동일</span>
                <strong>${formatConnectedAt(account.createdAt)}</strong>
            `;

            card.appendChild(header);
            card.appendChild(meta);
            oauthAccountsList.appendChild(card);
        });
        oauthAccountsEmpty.classList.add("d-none");
        oauthAccountsList.classList.remove("d-none");
        updateText(providerBadge, `연동 계정 ${accounts.length}개`);
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
            loadOAuthAccounts();
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
