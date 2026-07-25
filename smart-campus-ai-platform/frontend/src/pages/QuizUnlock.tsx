import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getJson, postJson } from "@/lib/api";
import NotificationBell from "@/components/NotificationBell";

interface QuizQuestion {
  id: number;
  category: "APTITUDE" | "DSA";
  question: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
}

interface QuizQuestionsResponse {
  questions: QuizQuestion[];
  timeLimitSeconds: number;
}

interface QuizSubmissionResponse {
  passed: boolean;
  correctCount: number;
  total: number;
  message: string;
  bonusQueries: number;
}

type OptionKey = "A" | "B" | "C" | "D";

export default function QuizUnlock() {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState<QuizQuestion[]>([]);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);
  const [answers, setAnswers] = useState<Record<number, OptionKey>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<QuizSubmissionResponse | null>(null);
  const [timeExpired, setTimeExpired] = useState(false);

  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);

  useEffect(() => {
    let interval: number | undefined;

    const load = async () => {
      try {
        const response = await getJson<QuizQuestionsResponse>("/api/quiz/questions");
        setQuestions(response.questions);
        setTimeLeft(response.timeLimitSeconds);
        setLoading(false);
        interval = window.setInterval(() => {
          setTimeLeft((prev) => {
            if (prev === null) return prev;
            if (prev <= 1) {
              setTimeExpired(true);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load quiz");
        setLoading(false);
      }
    };

    load();

    return () => {
      if (interval) window.clearInterval(interval);
    };
  }, []);

  useEffect(() => {
    if (timeExpired && !result && !submitting) {
      handleSubmit(true);
    }
  }, [timeExpired, result, submitting]);

  const formattedTime = useMemo(() => {
    if (timeLeft === null) return "--:--";
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    return `${minutes}:${seconds.toString().padStart(2, "0")}`;
  }, [timeLeft]);

  const handleSubmit = async (auto = false) => {
    if (!userId) {
      setError("Please log in first to unlock the AI assistant.");
      return;
    }
    if (submitting || result) return;

    const missing = questions.some((q) => !answers[q.id]);
    if (missing && !auto) {
      setError("Please answer both questions before submitting.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const payload = {
        userId,
        answers: questions.map((q) => ({
          questionId: q.id,
          selectedOption: answers[q.id] ?? "A"
        }))
      };

      const response = await postJson<QuizSubmissionResponse>("/api/quiz/submit", payload);
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submission failed");
    } finally {
      setSubmitting(false);
    }
  };

  if (!userId) {
    return (
      <div className="min-h-screen bg-campus flex items-center justify-center px-6">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Login required</CardTitle>
            <CardDescription>Please sign in to access the unlock quiz.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/login")}>Go to Login</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-4xl space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Unlock Quiz</h2>
          <NotificationBell />
        </div>
        <Card>
          <CardHeader>
            <CardTitle>Unlock Quiz</CardTitle>
            <CardDescription>
              Answer 2 questions correctly to unlock extra AI queries. Timer: {formattedTime}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading && <p className="text-sm text-muted-foreground">Loading questions...</p>}
            {error && <p className="mb-4 text-sm text-red-600">{error}</p>}
            {!loading && !result && (
              <div className="space-y-6">
                {questions.map((q, idx) => (
                  <div key={q.id} className="rounded-lg border border-border p-4">
                    <p className="text-xs uppercase tracking-wide text-muted-foreground">
                      {q.category} question {idx + 1}
                    </p>
                    <p className="mt-2 font-medium">{q.question}</p>
                    <div className="mt-3 grid gap-2">
                      {["A", "B", "C", "D"].map((opt) => {
                        const optionText =
                          opt === "A"
                            ? q.optionA
                            : opt === "B"
                              ? q.optionB
                              : opt === "C"
                                ? q.optionC
                                : q.optionD;
                        return (
                          <label
                            key={opt}
                            className={`flex items-center gap-2 rounded-md border px-3 py-2 text-sm ${
                              answers[q.id] === opt ? "border-primary" : "border-border"
                            }`}
                          >
                            <input
                              type="radio"
                              name={`q-${q.id}`}
                              value={opt}
                              checked={answers[q.id] === opt}
                              onChange={() =>
                                setAnswers((prev) => ({
                                  ...prev,
                                  [q.id]: opt as OptionKey
                                }))
                              }
                            />
                            <span>
                              {opt}. {optionText}
                            </span>
                          </label>
                        );
                      })}
                    </div>
                  </div>
                ))}

                <div className="flex gap-3">
                  <Button onClick={() => handleSubmit(false)} disabled={submitting}>
                    {submitting ? "Submitting..." : "Submit Answers"}
                  </Button>
                  <Button variant="secondary" onClick={() => navigate("/chat")}>Return to Chat</Button>
                </div>
              </div>
            )}

            {result && (
              <div className="space-y-3">
                <p className="text-lg font-semibold">
                  {result.passed ? "Unlocked!" : "Not quite."}
                </p>
                <p className="text-sm text-muted-foreground">
                  You answered {result.correctCount} of {result.total} correctly.
                </p>
                <p className="text-sm">{result.message}</p>
                {timeExpired && (
                  <p className="text-xs text-muted-foreground">
                    Time expired. Unanswered questions were submitted as incorrect.
                  </p>
                )}
                <div className="flex gap-3">
                  <Button onClick={() => navigate("/chat")}>Go to Chat</Button>
                  <Button variant="secondary" onClick={() => window.location.reload()}>
                    Try Again
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
