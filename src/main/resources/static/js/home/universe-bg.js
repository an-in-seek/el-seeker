import * as THREE from 'three';

const STAR_COUNT = 1600;
const MOUSE_INFLUENCE = 0.015;

export function initUniverse(canvasId, sectionId) {
    const canvas = document.getElementById(canvasId);
    const section = document.getElementById(sectionId);
    if (!canvas || !section) return;

    // ── 씬 / 카메라 / 렌더러 ──
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(60, 1, 0.1, 1000);
    camera.position.z = 5;

    const renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setClearColor(0x000000, 0);

    // ── 별 파티클 ──
    const positions = new Float32Array(STAR_COUNT * 3);
    const sizes = new Float32Array(STAR_COUNT);
    const colors = new Float32Array(STAR_COUNT * 3);

    for (let i = 0; i < STAR_COUNT; i++) {
        const i3 = i * 3;
        positions[i3] = (Math.random() - 0.5) * 20;
        positions[i3 + 1] = (Math.random() - 0.5) * 12;
        positions[i3 + 2] = (Math.random() - 0.5) * 10 - 2;

        sizes[i] = Math.random() < 0.05
            ? Math.random() * 5 + 3        // 5% 밝은 별 (크게)
            : Math.random() * 3 + 0.5;

        // 별 색상: 흰색 ~ 은은한 파랑/보라
        const tone = Math.random();
        if (tone < 0.7) {
            colors[i3] = 0.9 + Math.random() * 0.1;
            colors[i3 + 1] = 0.9 + Math.random() * 0.1;
            colors[i3 + 2] = 1.0;
        } else if (tone < 0.85) {
            colors[i3] = 0.7 + Math.random() * 0.15;
            colors[i3 + 1] = 0.75 + Math.random() * 0.15;
            colors[i3 + 2] = 1.0;
        } else {
            colors[i3] = 0.8 + Math.random() * 0.1;
            colors[i3 + 1] = 0.7 + Math.random() * 0.1;
            colors[i3 + 2] = 0.95 + Math.random() * 0.05;
        }
    }

    const geometry = new THREE.BufferGeometry();
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
    geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

    const material = new THREE.ShaderMaterial({
        uniforms: {
            uTime: { value: 0 },
            uPixelRatio: { value: renderer.getPixelRatio() }
        },
        vertexShader: `
            attribute float size;
            attribute vec3 color;
            varying vec3 vColor;
            varying float vAlpha;
            uniform float uTime;
            uniform float uPixelRatio;

            void main() {
                vColor = color;
                vec4 mvPos = modelViewMatrix * vec4(position, 1.0);

                // 다층 twinkle: 느린 파동 + 빠른 깜빡 + 간헐적 플래시
                float slow = sin(uTime * 0.8 + position.x * 2.0 + position.y * 1.5) * 0.35;
                float fast = sin(uTime * 3.0 + position.y * 5.0 + position.z * 3.0) * 0.2;
                float flash = pow(max(sin(uTime * 2.5 + position.x * 7.0 + position.z * 4.0), 0.0), 8.0) * 0.6;
                vAlpha = clamp(0.3 + slow + fast + flash, 0.1, 1.0);

                gl_PointSize = size * uPixelRatio * (4.0 / -mvPos.z);
                gl_Position = projectionMatrix * mvPos;
            }
        `,
        fragmentShader: `
            varying vec3 vColor;
            varying float vAlpha;

            void main() {
                float dist = length(gl_PointCoord - vec2(0.5));
                if (dist > 0.5) discard;
                float glow = 1.0 - smoothstep(0.0, 0.5, dist);
                glow = pow(glow, 1.2);
                gl_FragColor = vec4(vColor, glow * vAlpha);
            }
        `,
        transparent: true,
        depthWrite: false,
        blending: THREE.AdditiveBlending
    });

    const stars = new THREE.Points(geometry, material);
    scene.add(stars);

    // ── 마우스 패럴랙스 ──
    let mouseX = 0, mouseY = 0;
    let targetMouseX = 0, targetMouseY = 0;

    section.addEventListener('mousemove', (e) => {
        const rect = section.getBoundingClientRect();
        targetMouseX = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
        targetMouseY = ((e.clientY - rect.top) / rect.height - 0.5) * 2;
    });

    section.addEventListener('mouseleave', () => {
        targetMouseX = 0;
        targetMouseY = 0;
    });

    // ── 리사이즈 ──
    const resize = () => {
        const w = section.clientWidth;
        const h = section.clientHeight;
        renderer.setSize(w, h);
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
        material.uniforms.uPixelRatio.value = renderer.getPixelRatio();
    };

    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(section);
    resize();

    // ── 가시성 감지 (성능 최적화) ──
    let isVisible = false;
    const visibilityObserver = new IntersectionObserver((entries) => {
        isVisible = entries[0].isIntersecting;
    }, { threshold: 0.05 });
    visibilityObserver.observe(section);

    // ── 섹션 전체 등장 (스크롤 시 우주 배경이 웅장하게 나타남) ──
    const sectionRevealObserver = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
            section.classList.add('revealed');
            sectionRevealObserver.disconnect();
        }
    }, { threshold: 0.15 });
    sectionRevealObserver.observe(section);

    // ── 텍스트 페이드인 (밤안개 속에서 서서히 드러남) ──
    const fadeElements = section.querySelectorAll('.universe-fade');
    const fadeObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, { threshold: 0.3 });
    fadeElements.forEach((el) => fadeObserver.observe(el));

    // ── 애니메이션 루프 ──
    const clock = new THREE.Clock();

    const animate = () => {
        requestAnimationFrame(animate);
        if (!isVisible) return;

        const elapsed = clock.getElapsedTime();
        material.uniforms.uTime.value = elapsed;

        // 부드러운 마우스 추적
        mouseX += (targetMouseX - mouseX) * 0.05;
        mouseY += (targetMouseY - mouseY) * 0.05;

        // 패럴랙스: 별 전체를 미세하게 이동
        stars.rotation.y = mouseX * MOUSE_INFLUENCE;
        stars.rotation.x = mouseY * MOUSE_INFLUENCE * 0.5;

        // 느린 자전
        stars.rotation.z += 0.0001;

        renderer.render(scene, camera);
    };

    animate();
}
