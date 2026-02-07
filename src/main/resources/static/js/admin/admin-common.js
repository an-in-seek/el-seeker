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
    const text = await response.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch (error) {
        return null;
    }
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

export const initAdminSidebarToggle = () => {
    const toggleButtons = document.querySelectorAll("[data-admin-sidebar-toggle]");
    if (!toggleButtons.length) return;
    const closeTargets = document.querySelectorAll("[data-admin-sidebar-close]");
    const closeSidebar = () => document.body.classList.remove("admin-sidebar-open");
    const toggleSidebar = () => document.body.classList.toggle("admin-sidebar-open");

    toggleButtons.forEach((btn) => btn.addEventListener("click", toggleSidebar));
    closeTargets.forEach((target) => target.addEventListener("click", closeSidebar));
    window.addEventListener("resize", () => {
        if (window.innerWidth > 992) closeSidebar();
    });
};
