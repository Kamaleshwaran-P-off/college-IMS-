import React, { Suspense, lazy, useCallback, useMemo, useState } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import App from "./App";
import "./styles/globals.css";
import "highlight.js/styles/github-dark.css";
import PageLoader from "./components/feedback/PageLoader";
import ErrorBoundary from "./components/feedback/ErrorBoundary";
import IntroScreen from "./components/IntroScreen";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import ToastHost from "./components/feedback/ToastHost";
import { AuthProvider, useAuth } from "./context/AuthContext";

const Dashboard = lazy(() => import("./pages/Dashboard"));
const AdminDashboard = lazy(() => import("./pages/AdminDashboard"));
const AdminAssignClasses = lazy(() => import("./pages/AdminAssignClasses"));
const AdminCarousel = lazy(() => import("./pages/AdminCarousel"));
const Chatbot = lazy(() => import("./pages/Chatbot"));
const DoubtDetailPage = lazy(() => import("./pages/DoubtDetail"));
const Doubts = lazy(() => import("./pages/Doubts"));
const FacultyDashboard = lazy(() => import("./pages/FacultyDashboard"));
const MentorLeave = lazy(() => import("./pages/MentorLeave"));
const MentorMatching = lazy(() => import("./pages/MentorMatching"));
const LeaveRequests = lazy(() => import("./pages/LeaveRequests"));
const Inbox = lazy(() => import("./pages/Inbox"));
const EmailDashboard = lazy(() => import("./pages/EmailDashboard"));
const CareerFeed = lazy(() => import("./pages/CareerFeed"));
const MicroFeed = lazy(() => import("./pages/MicroFeed"));
const Planner = lazy(() => import("./pages/Planner"));
const Profile = lazy(() => import("./pages/Profile"));
const QuizUnlock = lazy(() => import("./pages/QuizUnlock"));
const StudentDashboard = lazy(() => import("./pages/StudentDashboard"));
const Assignments = lazy(() => import("./pages/Assignments"));
const StudyMaterials = lazy(() => import("./pages/StudyMaterials"));
const AssignmentPlannerPage = lazy(() => import("./pages/AssignmentPlannerPage"));
const Quiz = lazy(() => import("./pages/Quiz"));
const LearningStudent = lazy(() => import("./pages/LearningStudent"));
const LearningFaculty = lazy(() => import("./pages/LearningFaculty"));
const HackathonHub = lazy(() => import("./pages/HackathonHub"));
const HappenstanceEngine = lazy(() => import("./pages/HappenstanceEngine"));
const HabitTrackerPage = lazy(() => import("./pages/HabitTrackerPage"));
const AccessDenied = lazy(() => import("./pages/AccessDenied"));

// 🔥 NEW IMPORT
const Resources = lazy(() => import("./pages/resources"));

const withSuspense = (element: React.ReactNode) => (
  <ErrorBoundary>
    <Suspense fallback={<PageLoader />}>{element}</Suspense>
  </ErrorBoundary>
);

function BootGate({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const [showIntro, setShowIntro] = useState(() => {
    if (typeof window === "undefined") return false;
    return localStorage.getItem("introSeen") !== "true";
  });
  const [bootReady, setBootReady] = useState(false);

  const handleIntroComplete = useCallback(() => {
    if (typeof window !== "undefined") {
      localStorage.setItem("introSeen", "true");
    }
    setShowIntro(false);
    setBootReady(true);
    navigate("/login", { replace: true });
  }, [navigate]);

  React.useEffect(() => {
    if (showIntro) return;
    if (!bootReady) {
      setBootReady(true);
      navigate("/login", { replace: true });
    }
  }, [bootReady, navigate, showIntro]);

  const content = useMemo(() => {
    if (showIntro) {
      return <IntroScreen onComplete={handleIntroComplete} />;
    }
    if (!bootReady) {
      return <PageLoader />;
    }
    return children;
  }, [bootReady, children, handleIntroComplete, showIntro]);

  return <>{content}</>;
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = typeof window !== "undefined" ? localStorage.getItem("authToken") : null;
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function StudentOnlyRoute({ children }: { children: React.ReactNode }) {
  const { role, loading } = useAuth();
  if (loading) return <PageLoader />;
  const normalized = (role || "STUDENT").toUpperCase();
  if (normalized !== "STUDENT") {
    return <AccessDenied />;
  }
  return <>{children}</>;
}

function RoleRoute({
  allow,
  children
}: {
  allow: string[];
  children: React.ReactNode;
}) {
  const { role, loading } = useAuth();
  if (loading) return <PageLoader />;
  const normalized = (role || "STUDENT").toUpperCase();
  if (!allow.includes(normalized)) {
    return <AccessDenied />;
  }
  return <>{children}</>;
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <BootGate>
          <ToastHost />
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            <Route
              element={
                <ProtectedRoute>
                  <App />
                </ProtectedRoute>
              }
            >
              <Route path="/dashboard" element={withSuspense(<Dashboard />)} />
              <Route path="/student-dashboard" element={withSuspense(<StudentDashboard />)} />

              {/* 🔥 NEW ROUTE ADDED */}
              <Route path="/resources" element={withSuspense(<Resources />)} />

              <Route path="/faculty-dashboard" element={withSuspense(<RoleRoute allow={["FACULTY", "STAFF"]}><FacultyDashboard /></RoleRoute>)} />
              <Route path="/admin-dashboard" element={withSuspense(<RoleRoute allow={["ADMIN"]}><AdminDashboard /></RoleRoute>)} />
              <Route path="/admin-assign-classes" element={withSuspense(<RoleRoute allow={["ADMIN"]}><AdminAssignClasses /></RoleRoute>)} />
              <Route path="/admin-carousel" element={withSuspense(<RoleRoute allow={["ADMIN"]}><AdminCarousel /></RoleRoute>)} />
              <Route path="/chat" element={withSuspense(<Chatbot />)} />
              <Route path="/assignments" element={withSuspense(<Assignments />)} />
              <Route path="/assignment-planner" element={withSuspense(<AssignmentPlannerPage />)} />
              <Route path="/study-materials" element={withSuspense(<StudyMaterials />)} />
              <Route path="/quiz" element={withSuspense(<Quiz />)} />
              <Route path="/doubts" element={withSuspense(<Doubts />)} />
              <Route path="/doubts/:id" element={withSuspense(<DoubtDetailPage />)} />
              <Route path="/mentor-leave" element={withSuspense(<MentorLeave />)} />
              <Route path="/mentor-matching" element={withSuspense(<MentorMatching />)} />
              <Route path="/leave-requests" element={withSuspense(<LeaveRequests />)} />
              <Route path="/inbox" element={withSuspense(<Inbox />)} />
              <Route path="/email-dashboard" element={withSuspense(<EmailDashboard />)} />
              <Route path="/career-feed" element={withSuspense(<CareerFeed />)} />
              <Route path="/micro-feed" element={withSuspense(<MicroFeed />)} />
              <Route path="/planner" element={withSuspense(<Planner />)} />
              <Route path="/profile" element={withSuspense(<Profile />)} />
              <Route path="/unlock-quiz" element={withSuspense(<QuizUnlock />)} />
              <Route path="/learning" element={withSuspense(<StudentOnlyRoute><LearningStudent /></StudentOnlyRoute>)} />
              <Route path="/faculty-learning" element={withSuspense(<RoleRoute allow={["FACULTY", "STAFF"]}><LearningFaculty /></RoleRoute>)} />
              <Route path="/hackathons" element={withSuspense(<StudentOnlyRoute><HackathonHub /></StudentOnlyRoute>)} />
              <Route path="/happenstance" element={withSuspense(<StudentOnlyRoute><HappenstanceEngine /></StudentOnlyRoute>)} />
              <Route path="/habit-tracker" element={withSuspense(<HabitTrackerPage />)} />
            </Route>
          </Routes>
        </BootGate>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);