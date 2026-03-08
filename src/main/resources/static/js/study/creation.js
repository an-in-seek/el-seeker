const REDUCED_MOTION = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

document.addEventListener('DOMContentLoaded', () => {
    const main = document.querySelector('.creation-main');
    const sections = main.querySelectorAll('.cr-section');
    const progressFill = document.getElementById('crProgressFill');
    const progressLabel = document.getElementById('crProgressLabel');

    if (!main || sections.length === 0) return;

    // ── 텍스트 페이드인 ──
    const fadeElements = main.querySelectorAll('.cr-fade');

    const fadeObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                fadeObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.15 });

    fadeElements.forEach((el) => fadeObserver.observe(el));

    // ── 진행 바 업데이트 ──
    let currentDay = 0;
    const totalDays = 7;

    const sectionObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                const day = Number(entry.target.dataset.day);
                currentDay = day;
                const progress = Math.min(day / totalDays, 1) * 100;
                progressFill.style.width = progress + '%';

                if (day === 0) {
                    progressLabel.textContent = '';
                } else if (day <= 7) {
                    progressLabel.textContent = day + ' / ' + totalDays;
                } else {
                    progressLabel.textContent = '';
                    progressFill.style.width = '100%';
                }
            }
        });
    }, { threshold: 0.5 });

    sections.forEach((sec) => sectionObserver.observe(sec));

    // ── 네비게이션 숨김/노출 ──
    const page = document.querySelector('.creation-page');
    const epilogue = main.querySelector('.cr-epilogue');

    if (page && epilogue) {
        const navObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    page.classList.add('nav-visible');
                } else {
                    page.classList.remove('nav-visible');
                }
            });
        }, { threshold: 0.3 });

        navObserver.observe(epilogue);
    }

    // ── 키보드 네비게이션 ──
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowDown' && currentDay < sections.length - 1) {
            e.preventDefault();
            sections[currentDay + 1].scrollIntoView({ behavior: 'smooth' });
        } else if (e.key === 'ArrowUp' && currentDay > 0) {
            e.preventDefault();
            sections[currentDay - 1].scrollIntoView({ behavior: 'smooth' });
        }
    });
});
