import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { postJson } from "@/lib/api";

interface QuizQuestion {
  id: number;
  type: string;
  question: string;
  options?: string[];
  answer?: string;
  explanation?: string;
}

interface QuizResponse {
  topicId: number;
  questions: QuizQuestion[];
}

interface QuizSubmitResponse {
  score: number;
  passed: boolean;
  nextTopicId?: number | null;
  explanations?: string[];
}

interface QuizPanelProps {
  topicId: number;
  topicTitle: string;
  onPassed: () => void;
}

export default function QuizPanel({ topicId, topicTitle, onPassed }: QuizPanelProps) {
  const [loading, setLoading] = useState(true);
  const [questions, setQuestions] = useState<QuizQuestion[]>([]);
  const [answers, setAnswers] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<QuizSubmitResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadQuiz = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const response = await postJson<QuizResponse>(`/api/student/learning/topics/${topicId}/quiz`, {});
      setQuestions(response.questions);
      setAnswers(new Array(response.questions.length).fill(""));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load quiz");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQuiz();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [topicId]);

  const canSubmit = useMemo(() => answers.every((answer) => answer.trim().length > 0), [answers]);

  const handleSubmit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      const response = await postJson<QuizSubmitResponse>(`/api/student/learning/topics/${topicId}/quiz/submit`, {
        answers
      });
      setResult(response);
      if (response.passed) {
        onPassed();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Quiz submission failed");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Card className="p-6">
        <p className="text-sm text-muted-foreground">Generating your quiz...</p>
      </Card>
    );
  }

  return (
    <Card className="space-y-4 p-6">
      <div>
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Quiz</p>
        <h3 className="text-xl font-semibold">{topicTitle}</h3>
      </div>

      {questions.map((question, index) => (
        <div key={question.id} className="rounded-2xl border border-border/60 bg-muted/40 p-4">
          <p className="text-sm font-semibold">
            {index + 1}. {question.question}
          </p>

          {question.type === "FILL_BLANK" ? (
            <input
              className="mt-3 w-full rounded-xl border border-border bg-white/70 px-3 py-2 text-sm dark:bg-white/10"
              placeholder="Type your answer"
              value={answers[index]}
              onChange={(event) => {
                const next = [...answers];
                next[index] = event.target.value;
                setAnswers(next);
              }}
            />
          ) : (
            <div className="mt-3 grid gap-2">
              {((question.options && question.options.length > 0) ? question.options : ["True", "False"]).map((option) => (
                <label
                  key={option}
                  className="flex cursor-pointer items-center gap-2 rounded-xl border border-border/60 bg-white/70 px-3 py-2 text-sm dark:bg-white/10"
                >
                  <input
                    type="radio"
                    name={`question-${index}`}
                    checked={answers[index] === option}
                    onChange={() => {
                      const next = [...answers];
                      next[index] = option;
                      setAnswers(next);
                    }}
                  />
                  <span>{option}</span>
                </label>
              ))}
            </div>
          )}
        </div>
      ))}

      {result && (
        <div className={`rounded-2xl border px-4 py-3 text-sm ${result.passed ? "border-emerald-400/40 bg-emerald-400/10 text-emerald-700 dark:text-emerald-200" : "border-rose-400/40 bg-rose-400/10 text-rose-700 dark:text-rose-200"}`}>
          <p className="font-semibold">{result.passed ? "Great job!" : "Keep trying"}</p>
          <p className="mt-1">Score: {result.score} / {questions.length}</p>
          {result.explanations && result.explanations.length > 0 && (
            <ul className="mt-2 list-disc space-y-1 pl-5">
              {result.explanations.map((item, idx) => (
                <li key={`${idx}-${item.slice(0, 12)}`}>{item || "Review this concept again."}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="flex flex-wrap gap-3">
        <Button onClick={handleSubmit} disabled={submitting || !canSubmit}>
          {submitting ? "Submitting..." : "Submit Quiz"}
        </Button>
        <Button variant="secondary" onClick={loadQuiz}>
          Retry Quiz
        </Button>
      </div>
    </Card>
  );
}
