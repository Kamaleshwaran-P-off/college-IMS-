"use client";

import { useEffect, useState, useId, type FormEvent } from "react";
import { postJson } from "@/lib/api";

/* ─────────────────────────────────────────────
   TYPEWRITER (from auth-fuse)
───────────────────────────────────────────── */
function Typewriter({
  text,
  speed = 80,
  cursor = "|",
}: {
  text: string;
  speed?: number;
  cursor?: string;
}) {
  const [display, setDisplay] = useState("");
  const [idx, setIdx] = useState(0);

  useEffect(() => {
    if (idx >= text.length) return;
    const t = setTimeout(() => {
      setDisplay((p) => p + text[idx]);
      setIdx((p) => p + 1);
    }, speed);
    return () => clearTimeout(t);
  }, [idx, text, speed]);

  return (
    <span>
      {display}
      <span style={{ animation: "blink 1s step-end infinite" }}>{cursor}</span>
    </span>
  );
}

/* ─────────────────────────────────────────────
   SUCCESS ANIMATION  (SVG recreation of JSON)
───────────────────────────────────────────── */
function SuccessAnimation() {
  return (
    <svg
      viewBox="0 0 200 200"
      width="120"
      height="120"
      xmlns="http://www.w3.org/2000/svg"
      style={{ overflow: "visible" }}
    >
      <style>{`
        @keyframes ripple1 {
          0%   { transform: scale(0.3); opacity: 0; }
          40%  { opacity: 0.35; }
          100% { transform: scale(1.01); opacity: 0; }
        }
        @keyframes ripple2 {
          0%   { transform: scale(0.3); opacity: 0; }
          40%  { opacity: 0.25; }
          100% { transform: scale(0.915); opacity: 0; }
        }
        @keyframes checkDraw {
          0%   { stroke-dashoffset: 200; }
          100% { stroke-dashoffset: 0; }
        }
        @keyframes orbitBall {
          from { transform: rotate(0deg) translateX(62px) rotate(0deg); }
          to   { transform: rotate(360deg) translateX(62px) rotate(-360deg); }
        }
        @keyframes orbitBall2 {
          from { transform: rotate(180deg) translateX(52px) rotate(-180deg); }
          to   { transform: rotate(540deg) translateX(52px) rotate(-540deg); }
        }
        @keyframes floatCross {
          0%   { opacity: 0; transform: translate(0,0) rotate(-28deg); }
          20%  { opacity: 1; }
          100% { opacity: 0; transform: translate(60px,-80px) rotate(18deg); }
        }
        @keyframes floatCross2 {
          0%   { opacity: 0; transform: translate(0,0) rotate(-30deg); }
          20%  { opacity: 1; }
          100% { opacity: 0; transform: translate(-70px,-60px) rotate(16deg); }
        }
        @keyframes floatCross3 {
          0%   { opacity: 0; transform: translate(0,0) rotate(-33deg); }
          20%  { opacity: 1; }
          100% { opacity: 0; transform: translate(50px,70px) rotate(13deg); }
        }
      `}</style>

      {/* ripple rings */}
      <circle
        cx="100" cy="100" r="88"
        fill="none" stroke="rgba(99,212,178,0.35)" strokeWidth="8"
        style={{ animation: "ripple1 0.8s 0.13s cubic-bezier(.22,1,.36,1) both", transformOrigin: "100px 100px" }}
      />
      <circle
        cx="100" cy="100" r="78"
        fill="none" stroke="rgba(99,212,178,0.25)" strokeWidth="8"
        style={{ animation: "ripple2 0.8s 0.21s cubic-bezier(.22,1,.36,1) both", transformOrigin: "100px 100px" }}
      />

      {/* filled circle */}
      <circle
        cx="100" cy="100" r="58"
        fill="#8B5CF6"
        style={{ animation: "ripple1 0.6s 0.13s cubic-bezier(.22,1,.36,1) both", transformOrigin: "100px 100px" }}
      />

      {/* checkmark */}
      <path
        d="M72 100 L90 118 L130 80"
        fill="none"
        stroke="white"
        strokeWidth="10"
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeDasharray="200"
        strokeDashoffset="200"
        style={{ animation: "checkDraw 0.5s 0.55s cubic-bezier(.22,1,.36,1) forwards" }}
      />

      {/* orbiting balls */}
      <g style={{ transformOrigin: "100px 100px", animation: "orbitBall 1.8s 0.11s linear both" }}>
        <circle cx="100" cy="100" r="5" fill="#8B5CF6" stroke="#8B5CF6" strokeWidth="4" />
      </g>
      <g style={{ transformOrigin: "100px 100px", animation: "orbitBall2 2.2s 0.13s linear both" }}>
        <circle cx="100" cy="100" r="4" fill="#8B5CF6" stroke="#8B5CF6" strokeWidth="3" />
      </g>

      {/* floating cross sparks */}
      <g style={{ transformOrigin: "140px 80px", animation: "floatCross 0.7s 0.55s ease-out both" }}>
        <line x1="140" y1="77" x2="140" y2="83" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
        <line x1="137" y1="80" x2="143" y2="80" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
      </g>
      <g style={{ transformOrigin: "62px 82px", animation: "floatCross2 0.8s 0.6s ease-out both" }}>
        <line x1="62" y1="79" x2="62" y2="85" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
        <line x1="59" y1="82" x2="65" y2="82" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
      </g>
      <g style={{ transformOrigin: "130px 130px", animation: "floatCross3 0.9s 0.65s ease-out both" }}>
        <line x1="130" y1="127" x2="130" y2="133" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
        <line x1="127" y1="130" x2="133" y2="130" stroke="#8B5CF6" strokeWidth="2.5" strokeLinecap="round" />
      </g>
    </svg>
  );
}

/* ─────────────────────────────────────────────
   PASSWORD INPUT  (from auth-fuse)
───────────────────────────────────────────── */
function EyeIcon({ off }: { off?: boolean }) {
  return off ? (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  ) : (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

/* ─────────────────────────────────────────────
   MAIN SIGNUP COMPONENT
───────────────────────────────────────────── */

interface SignupResponse {
  message: string;
}

export default function Signup() {
  const pwdId = useId();
  const [fullName, setFullName] = useState("");
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole]         = useState("STUDENT");
  const [showPwd, setShowPwd]   = useState(false);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState<string | null>(null);
  const [success, setSuccess]   = useState(false);

  // Auto-navigate after success (replace with useNavigate if using react-router)
  useEffect(() => {
    if (!success) return;
    const t = window.setTimeout(() => {
      // navigate("/login");  // ← uncomment if using react-router
      console.log("Redirecting to /login...");
    }, 2800);
    return () => window.clearTimeout(t);
  }, [success]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await postJson<SignupResponse>("/api/auth/signup", {
        fullName,
        email,
        password,
        role,
      });
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Signup failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        body { background: #060912; }

        @keyframes blink { 50% { opacity: 0; } }

        /* ── Root layout ── */
        .sp-root {
          min-height: 100vh;
          display: flex;
          font-family: 'Sora', sans-serif;
          background: #060912;
          overflow: hidden;
          position: relative;
        }

        /* ── Orbs ── */
        .sp-orb {
          position: absolute;
          border-radius: 50%;
          filter: blur(80px);
          pointer-events: none;
          animation: spOrbFloat 14s ease-in-out infinite;
        }
        .sp-orb-1 { width:500px;height:500px;background:radial-gradient(circle,rgba(99,212,178,.18)0%,transparent 70%);top:-100px;left:-60px;animation-delay:0s; }
        .sp-orb-2 { width:420px;height:420px;background:radial-gradient(circle,rgba(56,115,245,.18)0%,transparent 70%);bottom:-80px;right:-80px;animation-delay:-5s; }
        .sp-orb-3 { width:280px;height:280px;background:radial-gradient(circle,rgba(139,92,246,.14)0%,transparent 70%);top:55%;left:55%;animation-delay:-9s; }

        @keyframes spOrbFloat {
          0%,100% { transform:translate(0,0) scale(1); }
          33%      { transform:translate(-28px,36px) scale(1.04); }
          66%      { transform:translate(22px,-18px) scale(.96); }
        }

        .sp-grid {
          position:absolute;inset:0;
          background-image:
            linear-gradient(rgba(255,255,255,.025) 1px,transparent 1px),
            linear-gradient(90deg,rgba(255,255,255,.025) 1px,transparent 1px);
          background-size:48px 48px;
          pointer-events:none;
        }

        /* ── Left panel ── */
        .sp-left {
          flex:1;display:flex;flex-direction:column;justify-content:center;
          padding:80px 72px;position:relative;z-index:1;
        }

        .sp-logo { display:flex;align-items:center;gap:10px;margin-bottom:48px;opacity:0;animation:spFadeUp .7s .05s ease both; }
        .sp-logo-mark {
          width:40px;height:40px;border-radius:12px;
          background:linear-gradient(135deg,#3873f5 0%,#63d4b2 100%);
          display:flex;align-items:center;justify-content:center;
          font-size:18px;font-weight:800;color:white;letter-spacing:-.05em;
          box-shadow:0 4px 16px rgba(56,115,245,.4);
        }
        .sp-logo-name { font-size:1.1rem;font-weight:700;color:#eef2ff;letter-spacing:-.02em; }
        .sp-logo-name span { background:linear-gradient(135deg,#6ea8ff 0%,#63d4b2 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text; }

        .sp-welcome-label {
          font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:500;
          color:rgba(99,212,178,.6);letter-spacing:.14em;text-transform:uppercase;
          margin-bottom:18px;opacity:0;animation:spFadeUp .7s .15s ease both;
        }

        .sp-headline {
          font-size:clamp(2.2rem,3.6vw,3.4rem);font-weight:800;line-height:1.08;
          letter-spacing:-.03em;color:#f0f4ff;margin-bottom:36px;
          opacity:0;animation:spFadeUp .7s .22s ease both;
        }
        .sp-headline-accent { background:linear-gradient(135deg,#63d4b2 0%,#6ea8ff 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text; }

        .sp-quote-wrap {
          position:relative;padding:28px 28px 28px 36px;border-radius:16px;
          background:rgba(99,212,178,.05);border:1px solid rgba(99,212,178,.15);
          max-width:420px;opacity:0;animation:spFadeUp .7s .32s ease both;
        }
        .sp-quote-bar { position:absolute;left:0;top:20px;bottom:20px;width:3px;border-radius:2px;background:linear-gradient(180deg,#63d4b2,#6ea8ff); }
        .sp-quote-mark { font-size:3rem;line-height:1;color:rgba(99,212,178,.25);font-family:Georgia,serif;display:block;margin-bottom:6px;margin-top:-8px; }
        .sp-quote-text { font-size:.95rem;color:rgba(200,220,255,.65);line-height:1.75;font-style:italic;font-weight:300; }
        .sp-quote-text strong { font-style:normal;font-weight:600;color:rgba(99,212,178,.85);-webkit-text-fill-color:initial; }

        /* ── Right panel ── */
        .sp-right {
          width:500px;display:flex;align-items:center;justify-content:center;
          padding:48px;position:relative;z-index:1;
        }

        /* ── Card ── */
        .sp-card {
          width:100%;background:rgba(255,255,255,.04);backdrop-filter:blur(24px);
          -webkit-backdrop-filter:blur(24px);border:1px solid rgba(255,255,255,.09);
          border-radius:24px;padding:44px 40px;position:relative;
          opacity:0;animation:spFadeUp .8s .15s ease both;
          box-shadow:0 0 0 1px rgba(255,255,255,.04) inset,0 40px 80px rgba(0,0,0,.4),0 0 60px rgba(99,212,178,.05);
        }
        .sp-card::before {
          content:'';position:absolute;top:0;left:10%;right:10%;height:1px;
          background:linear-gradient(90deg,transparent,rgba(99,212,178,.55),transparent);border-radius:1px;
        }

        .sp-card-label { font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:500;color:rgba(99,212,178,.5);letter-spacing:.12em;text-transform:uppercase;margin-bottom:10px; }
        .sp-card-title { font-size:1.7rem;font-weight:700;color:#eef2ff;letter-spacing:-.025em;margin-bottom:6px; }
        .sp-card-desc  { font-size:.85rem;color:rgba(180,200,240,.45);margin-bottom:30px; }

        /* ── Fields ── */
        .sp-row { display:grid;grid-template-columns:1fr 1fr;gap:14px; }
        .sp-field { margin-bottom:16px; }

        .sp-label { display:block;font-size:.72rem;font-weight:600;color:rgba(180,210,255,.7);letter-spacing:.06em;text-transform:uppercase;margin-bottom:7px; }

        .sp-input-wrap { position:relative; }
        .sp-input-icon { position:absolute;left:13px;top:50%;transform:translateY(-50%);color:rgba(110,168,255,.4);font-size:14px;pointer-events:none;line-height:1; }

        .sp-input {
          width:100%;padding:12px 12px 12px 40px;
          background:rgba(255,255,255,.05);border:1px solid rgba(255,255,255,.09);
          border-radius:11px;color:#e8f0ff;font-size:.9rem;font-family:'Sora',sans-serif;
          outline:none;transition:border-color .2s,box-shadow .2s,background .2s;
        }
        .sp-input.sp-input-pwd { padding-right: 42px; }
        .sp-input::placeholder { color:rgba(150,175,220,.3);font-size:.86rem; }
        .sp-input:focus { border-color:rgba(99,212,178,.45);background:rgba(99,212,178,.05);box-shadow:0 0 0 3px rgba(99,212,178,.09); }

        /* password toggle btn */
        .sp-pwd-toggle {
          position:absolute;right:0;top:0;bottom:0;width:40px;
          display:flex;align-items:center;justify-content:center;
          color:rgba(110,168,255,.4);background:none;border:none;cursor:pointer;
          transition:color .15s;
        }
        .sp-pwd-toggle:hover { color:rgba(200,220,255,.8); }

        /* ── Role pills ── */
        .sp-role-group { display:flex;gap:8px; }
        .sp-role-pill {
          flex:1;padding:10px 4px;border-radius:10px;
          border:1px solid rgba(255,255,255,.09);background:rgba(255,255,255,.04);
          color:rgba(180,200,240,.5);font-size:.8rem;font-weight:600;
          font-family:'Sora',sans-serif;cursor:pointer;text-align:center;
          transition:all .18s ease;
        }
        .sp-role-pill:hover { border-color:rgba(99,212,178,.3);color:rgba(200,230,255,.75); }
        .sp-role-pill.active { background:rgba(99,212,178,.12);border-color:rgba(99,212,178,.45);color:#63d4b2;box-shadow:0 0 0 3px rgba(99,212,178,.08); }

        /* ── Submit button ── */
        .sp-btn {
          width:100%;padding:13px;border-radius:12px;border:none;
          background:linear-gradient(135deg,#1da87a 0%,#15896a 100%);
          color:#fff;font-size:.92rem;font-weight:600;font-family:'Sora',sans-serif;
          letter-spacing:.02em;cursor:pointer;position:relative;overflow:hidden;
          transition:transform .18s ease,box-shadow .18s ease,opacity .18s;
          margin-top:10px;box-shadow:0 4px 20px rgba(29,168,122,.35);
        }
        .sp-btn::before { content:'';position:absolute;inset:0;background:linear-gradient(135deg,rgba(255,255,255,.12)0%,transparent 60%);pointer-events:none; }
        .sp-btn:hover:not(:disabled) { transform:translateY(-2px);box-shadow:0 8px 32px rgba(29,168,122,.48); }
        .sp-btn:active:not(:disabled) { transform:translateY(0); }
        .sp-btn:disabled { opacity:.6;cursor:not-allowed; }
        .sp-btn-inner { display:flex;align-items:center;justify-content:center;gap:8px; }

        .sp-spinner { width:15px;height:15px;border:2px solid rgba(255,255,255,.3);border-top-color:white;border-radius:50%;animation:spSpin .7s linear infinite; }
        @keyframes spSpin { to { transform:rotate(360deg); } }

        /* ── Error ── */
        .sp-error-msg {
          display:flex;align-items:center;gap:8px;margin-top:14px;padding:11px 14px;
          border-radius:10px;background:rgba(252,129,129,.08);border:1px solid rgba(252,129,129,.2);
          color:#fc8181;font-size:.83rem;font-weight:500;
        }

        /* ── Divider + footer ── */
        .sp-divider { display:flex;align-items:center;gap:12px;margin:22px 0; }
        .sp-divider-line { flex:1;height:1px;background:rgba(255,255,255,.07); }
        .sp-divider-text { font-size:.72rem;color:rgba(160,185,230,.3);font-weight:500;letter-spacing:.05em; }

        .sp-footer { text-align:center;font-size:.83rem;color:rgba(160,185,230,.4); }
        .sp-footer a { color:#6ea8ff;font-weight:600;text-decoration:none;transition:color .15s; }
        .sp-footer a:hover { color:#9fc3ff; }

        @keyframes spFadeUp { from{opacity:0;transform:translateY(22px)}to{opacity:1;transform:translateY(0)} }

        /* ── Success overlay ── */
        .sp-success-overlay {
          position:fixed;inset:0;z-index:100;
          display:flex;flex-direction:column;align-items:center;justify-content:center;
          background:rgba(6,9,18,.92);backdrop-filter:blur(12px);
          animation:spFadeUp .35s ease both;
          gap:24px;
        }
        .sp-success-title { font-size:1.6rem;font-weight:700;color:#eef2ff;letter-spacing:-.02em; }
        .sp-success-sub { font-size:.9rem;color:rgba(160,185,230,.5);font-family:'JetBrains Mono',monospace; }
        .sp-redirect-bar {
          width:200px;height:3px;border-radius:2px;
          background:rgba(255,255,255,.08);overflow:hidden;margin-top:8px;
        }
        .sp-redirect-fill {
          height:100%;
          background:linear-gradient(90deg,#63d4b2,#6ea8ff);
          animation:fillBar 2.8s linear forwards;
        }
        @keyframes fillBar { from{width:0%}to{width:100%} }

        /* ── Responsive ── */
        @media (max-width: 900px) {
          .sp-left { display:none; }
          .sp-right { width:100%;padding:24px 20px; }
        }
      `}</style>

      <div className="sp-root">
        {/* ── Success overlay ── */}
        {success && (
          <div className="sp-success-overlay">
            <SuccessAnimation />
            <div className="sp-success-title">Account created! 🎉</div>
            <div className="sp-success-sub">// taking you to login…</div>
            <div className="sp-redirect-bar">
              <div className="sp-redirect-fill" />
            </div>
          </div>
        )}

        {/* Orbs */}
        <div className="sp-orb sp-orb-1" />
        <div className="sp-orb sp-orb-2" />
        <div className="sp-orb sp-orb-3" />
        <div className="sp-grid" />

        {/* ── Left panel ── */}
        <div className="sp-left">
          <div className="sp-logo">
            <div className="sp-logo-mark">F</div>
            <div className="sp-logo-name">Fusion<span>IQ</span></div>
          </div>

          <div className="sp-welcome-label">// welcome aboard</div>

          <h1 className="sp-headline">
            Begin your<br />
            <span className="sp-headline-accent">
              <Typewriter text="learning journey." speed={70} />
            </span>
          </h1>

          <div className="sp-quote-wrap">
            <div className="sp-quote-bar" />
            <span className="sp-quote-mark">"</span>
            <p className="sp-quote-text">
              Focus on sparking <strong>curiosity</strong>, fostering{" "}
              <strong>resilience</strong>, and promoting{" "}
              <strong>lifelong learning</strong> — moving beyond mere academic achievement.
            </p>
          </div>
        </div>

        {/* ── Right panel ── */}
        <div className="sp-right">
          <div className="sp-card">
            <div className="sp-card-label">// create account</div>
            <div className="sp-card-title">Join FusionIQ</div>
            <div className="sp-card-desc">Set up your campus workspace in seconds</div>

            <form onSubmit={handleSubmit}>
              {/* Full name + Email */}
              <div className="sp-row">
                <div className="sp-field">
                  <label className="sp-label">Full name</label>
                  <div className="sp-input-wrap">
                    <span className="sp-input-icon">👤</span>
                    <input
                      className="sp-input"
                      type="text"
                      placeholder="Ava Patel"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      required
                      autoComplete="name"
                    />
                  </div>
                </div>

                <div className="sp-field">
                  <label className="sp-label">Email</label>
                  <div className="sp-input-wrap">
                    <span className="sp-input-icon">✉</span>
                    <input
                      className="sp-input"
                      type="email"
                      placeholder="you@campus.edu"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      autoComplete="email"
                    />
                  </div>
                </div>
              </div>

              {/* Password (auth-fuse PasswordInput style) */}
              <div className="sp-field">
                <label htmlFor={pwdId} className="sp-label">Password</label>
                <div className="sp-input-wrap">
                  <span className="sp-input-icon">🔑</span>
                  <input
                    id={pwdId}
                    className="sp-input sp-input-pwd"
                    type={showPwd ? "text" : "password"}
                    placeholder="••••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                    style={{ WebkitTextSecurity: showPwd ? "none" : undefined } as React.CSSProperties}
                  />
                  <button
                    type="button"
                    className="sp-pwd-toggle"
                    onClick={() => setShowPwd((p) => !p)}
                    aria-label={showPwd ? "Hide password" : "Show password"}
                  >
                    <EyeIcon off={showPwd} />
                  </button>
                </div>
              </div>

              {/* Role pills */}
              <div className="sp-field">
                <label className="sp-label">I am a…</label>
                <div className="sp-role-group">
                  {[
                    { value: "STUDENT", label: "🎓 Student" },
                    { value: "STAFF",   label: "🏫 Staff"   },
                    { value: "ADMIN",   label: "⚙️ Admin"   },
                  ].map((r) => (
                    <button
                      key={r.value}
                      type="button"
                      className={`sp-role-pill${role === r.value ? " active" : ""}`}
                      onClick={() => setRole(r.value)}
                    >
                      {r.label}
                    </button>
                  ))}
                </div>
              </div>

              <button type="submit" className="sp-btn" disabled={loading}>
                <span className="sp-btn-inner">
                  {loading && <span className="sp-spinner" />}
                  {loading ? "Creating account…" : "Create account →"}
                </span>
              </button>

              {error && (
                <div className="sp-error-msg"><span>✕</span> {error}</div>
              )}
            </form>

            <div className="sp-divider">
              <div className="sp-divider-line" />
              <span className="sp-divider-text">HAVE AN ACCOUNT?</span>
              <div className="sp-divider-line" />
            </div>

            <div className="sp-footer">
              Already registered? <a href="/login">Sign in</a>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
