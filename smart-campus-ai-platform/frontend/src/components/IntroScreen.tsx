import { motion } from "framer-motion";
import { useEffect, useMemo, useRef, useState } from "react";

type IntroScreenProps = { onComplete: () => void };

function measureStr(s: string, fontSize = 64): number {
  if (typeof document === "undefined") return fontSize * s.length * 0.55;
  const c = document.createElement("canvas");
  const ctx = c.getContext("2d")!;
  ctx.font = `600 ${fontSize}px "Sora", sans-serif`;
  return ctx.measureText(s).width;
}

/* ── Draw the smiley O onto a canvas ── */
function drawO(canvas: HTMLCanvasElement, glowFrac = 0) {
  const W = canvas.width, H = canvas.height;
  const ctx = canvas.getContext("2d")!;
  ctx.clearRect(0, 0, W, H);

  const cx = W / 2;
  const bodyH = H * 0.62;
  const cy = bodyH * 0.48;
  const r = Math.min(W * 0.4, bodyH * 0.44);

  /* glow aura */
  if (glowFrac > 0) {
    const g = ctx.createRadialGradient(cx, cy, r * 0.3, cx, cy, r * 2.2);
    g.addColorStop(0, `rgba(167,139,250,${0.65 * glowFrac})`);
    g.addColorStop(1, "rgba(56,189,248,0)");
    ctx.beginPath(); ctx.arc(cx, cy, r * 2.2, 0, Math.PI * 2);
    ctx.fillStyle = g; ctx.fill();
  }

  /* body */
  ctx.beginPath(); ctx.arc(cx, cy, r, 0, Math.PI * 2);
  ctx.fillStyle = glowFrac > 0
    ? `hsl(${260 + glowFrac * 40},${65 + glowFrac * 25}%,${58 + glowFrac * 12}%)`
    : "#a78bfa";
  ctx.fill();
  ctx.strokeStyle = "rgba(255,255,255,0.9)"; ctx.lineWidth = 1.8; ctx.stroke();

  /* eyes */
  const eyeY = cy - r * 0.22;
  const eox = r * 0.3;
  [cx - eox, cx + eox].forEach((ex) => {
    ctx.beginPath(); ctx.arc(ex, eyeY, r * 0.16, 0, Math.PI * 2);
    ctx.fillStyle = "#fff"; ctx.fill();
    ctx.beginPath(); ctx.arc(ex + r * 0.04, eyeY + r * 0.04, r * 0.08, 0, Math.PI * 2);
    ctx.fillStyle = "#1a0a2e"; ctx.fill();
    ctx.beginPath(); ctx.arc(ex - r * 0.02, eyeY - r * 0.04, r * 0.04, 0, Math.PI * 2);
    ctx.fillStyle = "#fff"; ctx.fill();
  });

  /* smile */
  ctx.beginPath();
  ctx.arc(cx, cy + r * 0.08, r * 0.35, 0.2 * Math.PI, 0.8 * Math.PI);
  ctx.strokeStyle = "#fff"; ctx.lineWidth = 1.5; ctx.lineCap = "round"; ctx.stroke();

  /* cheeks */
  [[cx - r * 0.5, cy + r * 0.22], [cx + r * 0.5, cy + r * 0.22]].forEach(([bx, by]) => {
    ctx.beginPath(); ctx.ellipse(bx, by, r * 0.14, r * 0.08, 0, 0, Math.PI * 2);
    ctx.fillStyle = "rgba(255,160,190,0.5)"; ctx.fill();
  });

  const armY = cy + r * 0.15;

  /* left arm → book */
  const lax = cx - r * 1.55, lay = cy - r * 0.3;
  ctx.beginPath();
  ctx.moveTo(cx - r * 0.85, armY);
  ctx.quadraticCurveTo(cx - r * 1.1, cy, lax, lay);
  ctx.strokeStyle = "#e9d5ff"; ctx.lineWidth = 2; ctx.lineCap = "round"; ctx.stroke();
  ctx.beginPath(); ctx.arc(lax, lay, r * 0.1, 0, Math.PI * 2);
  ctx.fillStyle = "#e9d5ff"; ctx.fill();

  /* book */
  const bW = r * 0.7, bH = r * 0.55;
  const bx = lax - bW - r * 0.05, by = lay - bH * 0.5;
  ctx.beginPath();
  (ctx as any).roundRect(bx, by, bW, bH, 2);
  ctx.fillStyle = "#38bdf8"; ctx.fill();
  ctx.strokeStyle = "rgba(255,255,255,0.8)"; ctx.lineWidth = 1; ctx.stroke();
  ctx.beginPath(); ctx.moveTo(bx + bW / 2, by); ctx.lineTo(bx + bW / 2, by + bH);
  ctx.strokeStyle = "rgba(255,255,255,0.4)"; ctx.lineWidth = 0.8; ctx.stroke();
  [0.25, 0.5, 0.75].forEach((f) => {
    ctx.beginPath();
    ctx.moveTo(bx + bW / 2 + 2, by + bH * f); ctx.lineTo(bx + bW - 3, by + bH * f);
    ctx.strokeStyle = "rgba(255,255,255,0.3)"; ctx.lineWidth = 0.7; ctx.stroke();
  });

  /* right arm */
  const rax = cx + r * 1.55, ray = cy - r * 0.22;
  ctx.beginPath();
  ctx.moveTo(cx + r * 0.85, armY);
  ctx.quadraticCurveTo(cx + r * 1.1, cy, rax, ray);
  ctx.strokeStyle = "#e9d5ff"; ctx.lineWidth = 2; ctx.lineCap = "round"; ctx.stroke();
  ctx.beginPath(); ctx.arc(rax, ray, r * 0.1, 0, Math.PI * 2);
  ctx.fillStyle = "#e9d5ff"; ctx.fill();

  /* legs */
  const legTopY = cy + r * 0.88;
  const llx = cx - r * 0.6, lly = H - r * 0.18;
  ctx.beginPath();
  ctx.moveTo(cx - r * 0.28, legTopY);
  ctx.quadraticCurveTo(cx - r * 0.5, cy + r * 1.1, llx, lly);
  ctx.strokeStyle = "#e9d5ff"; ctx.lineWidth = 2; ctx.lineCap = "round"; ctx.stroke();
  ctx.beginPath(); ctx.ellipse(llx - r * 0.08, lly + r * 0.12, r * 0.2, r * 0.1, 0.3, 0, Math.PI * 2);
  ctx.fillStyle = "#c4b5fd"; ctx.fill();

  const lrx = cx + r * 0.6, lry = H - r * 0.18;
  ctx.beginPath();
  ctx.moveTo(cx + r * 0.28, legTopY);
  ctx.quadraticCurveTo(cx + r * 0.5, cy + r * 1.1, lrx, lry);
  ctx.strokeStyle = "#e9d5ff"; ctx.lineWidth = 2; ctx.lineCap = "round"; ctx.stroke();
  ctx.beginPath(); ctx.ellipse(lrx + r * 0.08, lry + r * 0.12, r * 0.2, r * 0.1, -0.3, 0, Math.PI * 2);
  ctx.fillStyle = "#c4b5fd"; ctx.fill();
}

export default function IntroScreen({ onComplete }: IntroScreenProps) {
  const [phase, setPhase] = useState(0);
  const [dims, setDims] = useState<{
    oW: number; oH: number; suffixW: number; canvasLeft: number;
  } | null>(null);

  const canvasRef = useRef<HTMLCanvasElement>(null);
  const wordRef   = useRef<HTMLDivElement>(null);
  const glowRaf   = useRef<number | null>(null);

  const O_START_X = -640;

  /* refs for the text spans so we can toggle -webkit-text-fill-color */
  const txtRefs = useRef<(HTMLSpanElement | null)[]>([]);

  const particles = useMemo(() => [
    { dx:-145,dy:-55,size:7,delay:0.00,dur:1.5,color:"#c4b5fd" },
    { dx: 160,dy:-38,size:6,delay:0.07,dur:1.4,color:"#7dd3fc" },
    { dx: -95,dy: 62,size:5,delay:0.14,dur:1.6,color:"#f9a8d4" },
    { dx: 105,dy: 72,size:6,delay:0.21,dur:1.7,color:"#86efac" },
    { dx:   0,dy:-95,size:8,delay:0.28,dur:1.8,color:"#fde68a" },
    { dx:-180,dy: 20,size:5,delay:0.35,dur:1.3,color:"#c4b5fd" },
    { dx: 185,dy: 10,size:5,delay:0.42,dur:1.5,color:"#7dd3fc" },
    { dx:  60,dy:-70,size:4,delay:0.49,dur:1.4,color:"#f9a8d4" },
    { dx: -65,dy: 78,size:4,delay:0.56,dur:1.6,color:"#86efac" },
    { dx: 125,dy:-72,size:5,delay:0.63,dur:1.5,color:"#fde68a" },
  ], []);

  /* ── Size canvas & slots once fonts are ready ── */
  useEffect(() => {
    const run = () => {
      /* probe actual "o" glyph bounding box */
      const probe = document.createElement("span");
      probe.style.cssText =
        'font-family:"Sora",sans-serif;font-size:64px;font-weight:600;' +
        'visibility:hidden;position:absolute;top:-9999px;white-space:nowrap;';
      probe.textContent = "o";
      document.body.appendChild(probe);
      const r = probe.getBoundingClientRect();
      document.body.removeChild(probe);

      const gW = Math.ceil(r.width)  || 40;
      const gH = Math.ceil(r.height * 0.82) || 44;
      const CW = Math.round(gW * 1.0);
      const CH = Math.round(gH * 1.55);

      const oW     = measureStr("o");
      const suffixW = Math.max(measureStr("5"), measureStr("IQ"));
      const canvasLeft = (oW - CW) / 2;

      const cv = canvasRef.current!;
      cv.width  = CW; cv.height = CH;
      drawO(cv, 0);

      setDims({ oW, oH: gH, suffixW, canvasLeft });
    };
    if (document.fonts?.ready) document.fonts.ready.then(run);
    else run();
  }, []);

  /* ── Timeline ── */
  useEffect(() => {
    if (typeof window === "undefined") return;
    const seen = localStorage.getItem("introSeen") === "true";
    if (seen) { onComplete(); return; }

    const timers = [
      window.setTimeout(() => setPhase(1), 400),
      window.setTimeout(() => setPhase(2), 1600),
      window.setTimeout(() => setPhase(3), 2420),
      window.setTimeout(() => setPhase(4), 3600),
      window.setTimeout(() => {
        localStorage.setItem("introSeen", "true");
        onComplete();
      }, 4200),
    ];
    return () => timers.forEach(window.clearTimeout);
  }, [onComplete]);

  /* ── Glow O canvas when phase = 3 ── */
  useEffect(() => {
    if (phase !== 3) return;
    const cv = canvasRef.current;
    if (!cv) return;
    const start = performance.now();
    const dur = 650;
    const frame = (now: number) => {
      const t = Math.min((now - start) / dur, 1);
      drawO(cv, t);
      if (t < 1) glowRaf.current = requestAnimationFrame(frame);
    };
    glowRaf.current = requestAnimationFrame(frame);
    return () => { if (glowRaf.current) cancelAnimationFrame(glowRaf.current); };
  }, [phase]);

  /* ── Bounce keyframes for "o" jump ── */
  const STEPS = 32;
  const oKf = useMemo(() => {
    const x: number[] = [], y: number[] = [], op: number[] = [];
    const arcs = [{ p: 0.26, h: -80, w: 0.22 }, { p: 0.63, h: -42, w: 0.22 }];
    for (let i = 0; i <= STEPS; i++) {
      const t = i / STEPS;
      x.push(O_START_X * (1 - t));
      y.push(arcs.reduce((acc, { p, h, w }) => {
        const dt = (t - p) / w; return Math.abs(dt) < 1 ? acc + h * (1 - dt * dt) : acc;
      }, 0));
      op.push(Math.min(t * 6, 1));
    }
    return { x, y, opacity: op, times: x.map((_, i) => i / STEPS) };
  }, []);

  /* ── Glow styles ──
     CRITICAL: apply backgroundImage+clip to the OUTER word div,
     and set webkitTextFillColor:transparent on every text span.
     This ensures ALL letters (Fusi + n + IQ) show the gradient. */
  const wordGlow: React.CSSProperties = phase >= 3 ? {
    backgroundImage: "linear-gradient(90deg,#38bdf8 0%,#a78bfa 45%,#f472b6 100%)",
    WebkitBackgroundClip: "text",
    backgroundClip: "text",
  } : {};

  /* span text-fill: transparent when glowing, white otherwise */
  const spanFill: React.CSSProperties = {
    WebkitTextFillColor: phase >= 3 ? "transparent" : "#fff",
  };

  return (
    <motion.div
      className="fixed inset-0 z-[9999] flex items-center justify-center overflow-hidden"
      style={{ background: "radial-gradient(ellipse at 50% 55%,#0b0e1c 0%,#000 100%)" }}
      animate={{ opacity: phase >= 4 ? 0 : 1 }}
      transition={{ duration: 0.6, ease: "easeInOut" }}
    >
      {/* Sparkles */}
      {phase >= 3 && particles.map((p, i) => (
        <motion.span key={i} className="pointer-events-none absolute rounded-full"
          style={{
            width: p.size, height: p.size,
            background: p.color,
            boxShadow: `0 0 8px 2px ${p.color}99`,
            left: `calc(50% + ${p.dx}px)`, top: `calc(50% + ${p.dy}px)`,
            translateX: "-50%", translateY: "-50%",
          }}
          initial={{ opacity: 0, scale: 0.3, y: 0 }}
          animate={{ opacity: [0, 1, 0], scale: [0.3, 1.1, 0.4], y: [-6, -30] }}
          transition={{ duration: p.dur, delay: p.delay, repeat: Infinity, repeatDelay: 0.25 }}
        />
      ))}

      {/* Word — gradient applied here so ALL children inherit it */}
      <motion.div
        ref={wordRef}
        className="inline-flex select-none"
        style={{
          fontFamily: '"Sora", sans-serif',
          fontSize: 64, fontWeight: 600, letterSpacing: "0.06em",
          alignItems: "baseline",
          ...wordGlow,
          filter: phase >= 3
            ? "drop-shadow(0 0 34px rgba(139,92,246,0.82)) drop-shadow(0 0 18px rgba(56,189,248,0.52))"
            : "none",
        }}
        animate={{ scale: phase >= 3 ? 1.05 : 1 }}
        transition={{ duration: 0.45, ease: [0.4, 0, 0.2, 1] }}
      >
        {/* "Fusi" */}
        <span ref={el => txtRefs.current[0] = el} style={spanFill}>Fusi</span>

        {/* "o" slot */}
        <span style={{
          display: "inline-block", position: "relative",
          width: dims ? `${dims.oW}px` : "0.57em",
          height: dims ? `${dims.oH}px` : "1em",
          verticalAlign: "baseline", overflow: "visible",
          marginLeft: "0.04em", marginRight: "0.08em",
        }}>
          <motion.canvas
            ref={canvasRef}
            style={{
              position: "absolute",
              left: dims ? `${dims.canvasLeft}px` : "0",
              bottom: 0,
            }}
            initial={{ x: O_START_X, opacity: 0 }}
            animate={phase >= 1 ? {
              x: oKf.x, y: oKf.y, opacity: oKf.opacity,
              scaleX: [1,0.9,1.06,0.94,1.08,0.97,1,...Array(STEPS-6).fill(1)],
              scaleY: [1,1.1,0.92,1.06,0.9,1.03,1,...Array(STEPS-6).fill(1)],
            } : { x: O_START_X, opacity: 0 }}
            transition={phase >= 1
              ? { duration: 1.15, ease: "linear", times: oKf.times }
              : {}}
          />
        </span>

        {/* "n" */}
        <span ref={el => txtRefs.current[1] = el} style={spanFill}>n</span>

        {/* suffix slot */}
        <span style={{
          display: "inline-block", position: "relative",
          width: dims ? `${dims.suffixW}px` : "1.3em",
          height: "1em", verticalAlign: "baseline", overflow: "visible",
          marginLeft: "0.02em",
        }}>
          <motion.span
            ref={el => txtRefs.current[2] = el}
            style={{ position: "absolute", left: 0, top: 0, ...spanFill }}
            initial={{ opacity: 1, y: 0 }}
            animate={{ opacity: phase >= 2 ? 0 : 1, y: phase >= 2 ? 28 : 0 }}
            transition={{ duration: 0.75, ease: "easeInOut" }}
          >5</motion.span>
          <motion.span
            ref={el => txtRefs.current[3] = el}
            style={{ position: "absolute", left: 0, top: 0, ...spanFill }}
            initial={{ opacity: 0, y: -28 }}
            animate={{ opacity: phase >= 2 ? 1 : 0, y: phase >= 2 ? 0 : -28 }}
            transition={{ duration: 0.75, ease: "easeInOut" }}
          >IQ</motion.span>
        </span>
      </motion.div>
    </motion.div>
  );
}