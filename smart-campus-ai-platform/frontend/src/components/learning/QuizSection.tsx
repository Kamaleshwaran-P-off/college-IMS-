import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import type { CourseTopic } from "@/components/learning/learningFlowData";

interface QuizResult {
  score: number;
  passed: boolean;
  autoSubmitted: boolean;
}

interface QuizSectionProps {
  topic: CourseTopic;
  answers: string[];
  disabled: boolean;
  result: QuizResult | null;
  onAnswerChange: (questionIndex: number, value: string) => void;
  onSubmit: () => void;
  onRetry: () => void;
  onContinue: () => void;
}

export default function QuizSection({
  topic,
  answers,
  disabled,
  result,
  onAnswerChange,
  onSubmit,
  onRetry,
  onContinue
}: QuizSectionProps) {
  const canSubmit = answers.every((answer) => answer.trim().length > 0);

  return (
    <Card className="flex h-full flex-col overflow-hidden border-border/60 bg-white/85 shadow-sm dark:bg-white/10">
      <div className="border-b border-border/60 px-5 py-4">
        <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Knowledge Check</p>
        <h3 className="mt-2 text-xl font-semibold">3 quick questions</h3>
        <p className="mt-2 text-sm text-muted-foreground">Score 2/3 or better to unlock the next topic.</p>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto px-5 py-5">
        {topic.questions.map((question, questionIndex) => (
          <div key={`${topic.title}-question-${questionIndex}`} className="rounded-2xl border border-border/60 bg-muted/35 p-4">
            <p className="text-sm font-semibold">
              {questionIndex + 1}. {question.question}
            </p>

            <div className="mt-3 grid gap-2">
              {question.options.map((option) => {
                const isSelected = answers[questionIndex] === option;
                const isCorrect = result && option === question.answer;
                const isIncorrectChoice = result && isSelected && option !== question.answer;

                return (
                  <label
                    key={option}
                    className={`flex cursor-pointer items-center gap-3 rounded-xl border px-3 py-2 text-sm transition ${
                      isCorrect
                        ? "border-emerald-400/50 bg-emerald-400/10 text-emerald-700 dark:text-emerald-200"
                        : isIncorrectChoice
                          ? "border-rose-400/50 bg-rose-400/10 text-rose-700 dark:text-rose-200"
                          : isSelected
                            ? "border-primary/50 bg-primary/10"
                            : "border-border/60 bg-white/80 dark:bg-white/5"
                    } ${disabled ? "cursor-default" : ""}`}
                  >
                    <input
                      type="radio"
                      name={`question-${questionIndex}`}
                      value={option}
                      checked={isSelected}
                      disabled={disabled}
                      onChange={() => onAnswerChange(questionIndex, option)}
                    />
                    <span>{option}</span>
                  </label>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="border-t border-border/60 px-5 py-4">
        {result && (
          <div className={`mb-4 rounded-2xl border px-4 py-3 text-sm ${result.passed ? "border-emerald-400/40 bg-emerald-400/10 text-emerald-700 dark:text-emerald-200" : "border-amber-400/40 bg-amber-400/10 text-amber-800 dark:text-amber-100"}`}>
            <p className="font-semibold">{result.passed ? "Topic cleared - next lesson unlocked." : "Almost there - retry this topic to keep the chain moving."}</p>
            <p className="mt-1">Score: {result.score} / {topic.questions.length}</p>
            {result.autoSubmitted && <p className="mt-1">Timer ended, so your answers were submitted automatically.</p>}
          </div>
        )}

        <div className="flex flex-wrap gap-3">
          {!result ? (
            <Button onClick={onSubmit} disabled={!canSubmit}>
              Submit Quiz
            </Button>
          ) : result.passed ? (
            <Button onClick={onContinue}>Continue</Button>
          ) : (
            <Button onClick={onRetry}>Retry Topic</Button>
          )}

          {result && (
            <Button variant="secondary" onClick={onRetry}>
              Try Again
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}
