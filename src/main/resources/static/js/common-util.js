import {refreshAccessToken} from "/js/auth/auth-check.js";

/**
 * 숫자에 천 단위 콤마를 적용하여 반환합니다.
 * @param {number|string} number - 포맷팅할 숫자
 * @returns {string} 콤마가 적용된 문자열 (입력이 유효하지 않으면 '0' 반환)
 */
export const formatNumberWithComma = (number) => {
    if (number === null || number === undefined || isNaN(number)) {
        return "0";
    }
    return Number(number).toLocaleString();
};

export const fetchWithAuthRetry = async (url, options = {}) => {
    const response = await fetch(url, options);
    if (response.status !== 401) {
        return response;
    }
    const refreshed = await refreshAccessToken();
    if (!refreshed) {
        return response;
    }
    return fetch(url, options);
};
