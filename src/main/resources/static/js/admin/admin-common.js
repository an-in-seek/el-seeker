import {fetchWithAuthRetry} from "/js/common-util.js";

export const fetchAdmin = async (url, options = {}) => {
    const defaults = {
        headers: {"Content-Type": "application/json"},
    };
    const merged = {...defaults, ...options, headers: {...defaults.headers, ...options.headers}};
    const response = await fetchWithAuthRetry(url, merged);
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.message || `요청 실패 (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
};

export const confirmDelete = (name) => confirm(`"${name}" 항목을 삭제하시겠습니까?`);

export const handleDelete = async (url, displayName, onSuccess) => {
    if (!confirmDelete(displayName)) return;
    try {
        await fetchAdmin(url, {method: "DELETE"});
        if (onSuccess) onSuccess();
        else location.reload();
    } catch (e) {
        alert(e.message);
    }
};
