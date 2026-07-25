import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { postJson } from "@/lib/api";
import AuthSuccessToast from "@/components/AuthSuccessToast";
import { useAuth } from "@/context/AuthContext";

interface AuthResponse {
  token: string;
  role: string;
  userId: number;
  email: string;
  fullName?: string;
}

export default function Login() {
  const navigate = useNavigate();
  const { refreshProfile } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [redirectTo, setRedirectTo] = useState("/dashboard");

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => navigate(redirectTo), 1300);
    return () => window.clearTimeout(timer);
  }, [success, navigate, redirectTo]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setError(null);

    try {
      const response = await postJson<AuthResponse>("/api/auth/login", { email, password });
      localStorage.setItem("authToken", response.token);
      localStorage.setItem("userId", String(response.userId));
      localStorage.setItem("userRole", response.role);
      localStorage.setItem("token", response.token);
      localStorage.setItem("role", response.role);
      localStorage.setItem("email", response.email);
      localStorage.setItem("userEmail", response.email);

      const normalizedRole = response.role?.replace("ROLE_", "").toUpperCase();
      localStorage.setItem("userRoleNormalized", normalizedRole);
      localStorage.setItem("name", response.fullName || response.email);
      localStorage.setItem("userName", response.fullName || response.email);

      await refreshProfile();

      setMessage(`Welcome back, ${response.fullName || response.email}`);
      if (normalizedRole === "ADMIN") setRedirectTo("/admin-dashboard");
      else if (normalizedRole === "FACULTY" || normalizedRole === "STAFF") setRedirectTo("/faculty-dashboard");
      else setRedirectTo("/student-dashboard");
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        .lp-root {
          min-height: 100vh;
          display: flex;
          font-family: 'Sora', sans-serif;
          background: #060912;
          overflow: hidden;
          position: relative;
        }

        .lp-orb {
          position: absolute;
          border-radius: 50%;
          filter: blur(80px);
          pointer-events: none;
          animation: orbFloat 12s ease-in-out infinite;
        }
        .lp-orb-1 {
          width: 520px; height: 520px;
          background: radial-gradient(circle, rgba(56,115,245,0.22) 0%, transparent 70%);
          top: -120px; left: -80px;
          animation-delay: 0s;
        }
        .lp-orb-2 {
          width: 400px; height: 400px;
          background: radial-gradient(circle, rgba(99,212,178,0.14) 0%, transparent 70%);
          bottom: -60px; right: -60px;
          animation-delay: -4s;
        }
        .lp-orb-3 {
          width: 300px; height: 300px;
          background: radial-gradient(circle, rgba(246,173,85,0.10) 0%, transparent 70%);
          top: 40%; left: 60%;
          animation-delay: -8s;
        }

        @keyframes orbFloat {
          0%, 100% { transform: translate(0, 0) scale(1); }
          33% { transform: translate(30px, -40px) scale(1.05); }
          66% { transform: translate(-20px, 20px) scale(0.97); }
        }

        .lp-grid {
          position: absolute;
          inset: 0;
          background-image:
            linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
          background-size: 48px 48px;
          pointer-events: none;
        }

        .lp-left {
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: center;
          padding: 80px 72px;
          position: relative;
          z-index: 1;
        }

        .lp-badge {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          background: rgba(56,115,245,0.12);
          border: 1px solid rgba(56,115,245,0.3);
          border-radius: 100px;
          padding: 6px 16px;
          font-size: 11px;
          font-weight: 600;
          color: #6ea8ff;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          margin-bottom: 32px;
          width: fit-content;
          opacity: 0;
          animation: fadeUp 0.7s 0.1s ease both;
        }

        .lp-badge-dot {
          width: 6px; height: 6px;
          border-radius: 50%;
          background: #6ea8ff;
          animation: blink 2s ease-in-out infinite;
        }

        @keyframes blink {
          0%, 100% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.4; transform: scale(0.8); }
        }

        .lp-headline {
          font-size: clamp(2.4rem, 4vw, 3.6rem);
          font-weight: 800;
          line-height: 1.08;
          letter-spacing: -0.03em;
          color: #f0f4ff;
          margin-bottom: 20px;
          opacity: 0;
          animation: fadeUp 0.7s 0.2s ease both;
        }

        .lp-headline-accent {
          background: linear-gradient(135deg, #6ea8ff 0%, #63d4b2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }

        .lp-subtext {
          font-size: 1rem;
          color: rgba(200,215,255,0.5);
          line-height: 1.7;
          max-width: 380px;
          margin-bottom: 52px;
          opacity: 0;
          animation: fadeUp 0.7s 0.3s ease both;
        }

        .lp-features {
          display: flex;
          flex-direction: column;
          gap: 16px;
          opacity: 0;
          animation: fadeUp 0.7s 0.4s ease both;
        }

        .lp-feature {
          display: flex;
          align-items: center;
          gap: 14px;
        }

        .lp-feature-icon {
          width: 36px; height: 36px;
          border-radius: 10px;
          background: rgba(255,255,255,0.05);
          border: 1px solid rgba(255,255,255,0.08);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          flex-shrink: 0;
        }

        .lp-feature-text {
          font-size: 0.88rem;
          color: rgba(200,215,255,0.6);
        }

        .lp-right {
          width: 480px;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 48px;
          position: relative;
          z-index: 1;
        }

        .lp-card {
          width: 100%;
          background: rgba(255,255,255,0.04);
          backdrop-filter: blur(24px);
          -webkit-backdrop-filter: blur(24px);
          border: 1px solid rgba(255,255,255,0.09);
          border-radius: 24px;
          padding: 44px 40px;
          position: relative;
          opacity: 0;
          animation: fadeUp 0.8s 0.15s ease both;
          box-shadow:
            0 0 0 1px rgba(255,255,255,0.04) inset,
            0 40px 80px rgba(0,0,0,0.4),
            0 0 60px rgba(56,115,245,0.06);
        }

        .lp-card::before {
          content: '';
          position: absolute;
          top: 0; left: 10%; right: 10%;
          height: 1px;
          background: linear-gradient(90deg, transparent, rgba(110,168,255,0.6), transparent);
          border-radius: 1px;
        }

        .lp-card-label {
          font-family: 'JetBrains Mono', monospace;
          font-size: 10px;
          font-weight: 500;
          color: rgba(110,168,255,0.5);
          letter-spacing: 0.12em;
          text-transform: uppercase;
          margin-bottom: 10px;
        }

        .lp-card-title {
          font-size: 1.75rem;
          font-weight: 700;
          color: #eef2ff;
          letter-spacing: -0.025em;
          margin-bottom: 6px;
        }

        .lp-card-desc {
          font-size: 0.85rem;
          color: rgba(180,200,240,0.45);
          margin-bottom: 36px;
        }

        .lp-field { margin-bottom: 20px; }

        .lp-label {
          display: block;
          font-size: 0.75rem;
          font-weight: 600;
          color: rgba(180,210,255,0.7);
          letter-spacing: 0.06em;
          text-transform: uppercase;
          margin-bottom: 8px;
        }

        .lp-input-wrap { position: relative; }

        .lp-input-icon {
          position: absolute;
          left: 14px;
          top: 50%;
          transform: translateY(-50%);
          color: rgba(110,168,255,0.4);
          font-size: 15px;
          pointer-events: none;
          line-height: 1;
        }

        .lp-input {
          width: 100%;
          padding: 13px 14px 13px 42px;
          background: rgba(255,255,255,0.05);
          border: 1px solid rgba(255,255,255,0.09);
          border-radius: 12px;
          color: #e8f0ff;
          font-size: 0.92rem;
          font-family: 'Sora', sans-serif;
          outline: none;
          transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
        }

        .lp-input::placeholder {
          color: rgba(150,175,220,0.3);
          font-size: 0.88rem;
        }

        .lp-input:focus {
          border-color: rgba(110,168,255,0.5);
          background: rgba(110,168,255,0.06);
          box-shadow: 0 0 0 3px rgba(110,168,255,0.1);
        }

        .lp-btn {
          width: 100%;
          padding: 14px;
          border-radius: 12px;
          border: none;
          background: linear-gradient(135deg, #3873f5 0%, #2457d0 100%);
          color: #fff;
          font-size: 0.92rem;
          font-weight: 600;
          font-family: 'Sora', sans-serif;
          letter-spacing: 0.02em;
          cursor: pointer;
          position: relative;
          overflow: hidden;
          transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s;
          margin-top: 8px;
          box-shadow: 0 4px 20px rgba(56,115,245,0.35);
        }

        .lp-btn::before {
          content: '';
          position: absolute;
          inset: 0;
          background: linear-gradient(135deg, rgba(255,255,255,0.12) 0%, transparent 60%);
          pointer-events: none;
        }

        .lp-btn:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 8px 32px rgba(56,115,245,0.5);
        }

        .lp-btn:active:not(:disabled) { transform: translateY(0); }

        .lp-btn:disabled { opacity: 0.6; cursor: not-allowed; }

        .lp-btn-inner {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
        }

        .lp-spinner {
          width: 15px; height: 15px;
          border: 2px solid rgba(255,255,255,0.3);
          border-top-color: white;
          border-radius: 50%;
          animation: spin 0.7s linear infinite;
        }

        @keyframes spin { to { transform: rotate(360deg); } }

        .lp-divider {
          display: flex;
          align-items: center;
          gap: 12px;
          margin: 24px 0;
        }

        .lp-divider-line {
          flex: 1;
          height: 1px;
          background: rgba(255,255,255,0.07);
        }

        .lp-divider-text {
          font-size: 0.75rem;
          color: rgba(160,185,230,0.3);
          font-weight: 500;
          letter-spacing: 0.04em;
        }

        .lp-success-msg {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-top: 14px;
          padding: 11px 14px;
          border-radius: 10px;
          background: rgba(99,212,178,0.08);
          border: 1px solid rgba(99,212,178,0.2);
          color: #63d4b2;
          font-size: 0.83rem;
          font-weight: 500;
        }

        .lp-error-msg {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-top: 14px;
          padding: 11px 14px;
          border-radius: 10px;
          background: rgba(252,129,129,0.08);
          border: 1px solid rgba(252,129,129,0.2);
          color: #fc8181;
          font-size: 0.83rem;
          font-weight: 500;
        }

        .lp-footer {
          margin-top: 28px;
          text-align: center;
          font-size: 0.83rem;
          color: rgba(160,185,230,0.4);
        }

        .lp-footer a {
          color: #6ea8ff;
          font-weight: 600;
          text-decoration: none;
          transition: color 0.15s;
        }

        .lp-footer a:hover { color: #9fc3ff; }

        @keyframes fadeUp {
          from { opacity: 0; transform: translateY(22px); }
          to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 860px) {
          .lp-left { display: none; }
          .lp-right { width: 100%; padding: 24px 20px; }
        }
      `}</style>

      <div className="lp-root">
        <AuthSuccessToast open={success} title="Login successful" description="Redirecting to your dashboard..." />

        <div className="lp-orb lp-orb-1" />
        <div className="lp-orb lp-orb-2" />
        <div className="lp-orb lp-orb-3" />
        <div className="lp-grid" />

        {/* Left panel */}
        <div className="lp-left">
          <div className="lp-badge">
            <span className="lp-badge-dot" />
            Smart Campus Platform
          </div>
          <h1 className="lp-headline">
            Your campus,<br />
            <span className="lp-headline-accent">reimagined.</span>
          </h1>
          <p className="lp-subtext">
            One unified workspace for students, faculty, and staff — powered by intelligent tools built for modern academia.
          </p>
          <div className="lp-features">
            {[
              { icon: "🎓", text: "Personalized dashboards for every role" },
              { icon: "📡", text: "Real-time course & grade updates" },
              { icon: "🔒", text: "Secure, role-based access control" },
              { icon: "⚡", text: "Instant notifications & announcements" },
            ].map((f, i) => (
              <div className="lp-feature" key={i}>
                <div className="lp-feature-icon">{f.icon}</div>
                <span className="lp-feature-text">{f.text}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Right panel */}
        <div className="lp-right">
          <div className="lp-card">
            <div className="lp-card-label">// secure access</div>
            <div className="lp-card-title">Welcome back</div>
            <div className="lp-card-desc">Sign in to continue to your workspace</div>

            <form onSubmit={handleSubmit}>
              <div className="lp-field">
                <label className="lp-label">Email address</label>
                <div className="lp-input-wrap">
                  <span className="lp-input-icon">✉</span>
                  <input
                    className="lp-input"
                    type="email"
                    placeholder="you@campus.edu"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                  />
                </div>
              </div>

              <div className="lp-field">
                <label className="lp-label">Password</label>
                <div className="lp-input-wrap">
                  <span className="lp-input-icon">🔑</span>
                  <input
                    className="lp-input"
                    type="password"
                    placeholder="••••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                  />
                </div>
              </div>

              <button type="submit" className="lp-btn" disabled={loading}>
                <span className="lp-btn-inner">
                  {loading && <span className="lp-spinner" />}
                  {loading ? "Signing in…" : "Sign in →"}
                </span>
              </button>

              {message && (
                <div className="lp-success-msg"><span>✓</span> {message}</div>
              )}
              {error && (
                <div className="lp-error-msg"><span>✕</span> {error}</div>
              )}
            </form>

            <div className="lp-divider">
              <div className="lp-divider-line" />
              <span className="lp-divider-text">NEW HERE?</span>
              <div className="lp-divider-line" />
            </div>

            <div className="lp-footer">
              Don't have an account? <Link to="/signup">Create one</Link>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
