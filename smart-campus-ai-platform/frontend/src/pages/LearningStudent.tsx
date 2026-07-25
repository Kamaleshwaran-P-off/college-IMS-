import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Clock3, Lock, Sparkles } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import ProgressPanel from "@/components/learning/ProgressPanel";
import QuizSection from "@/components/learning/QuizSection";
import TopicCard from "@/components/learning/TopicCard";
import TopicViewer from "@/components/learning/TopicViewer";
import {
  buildInitialProgress,
  COURSE_CONTENT,
  FOCUS_DURATIONS,
  normaliseSubjectProgress,
  PROGRESS_STORAGE_KEY
} from "@/components/learning/learningFlowData";

interface QuizResult {
  score: number;
  passed: boolean;
  autoSubmitted: boolean;
}

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

const getBadgeLabel = (completedCount: number, totalTopics: number) => {
  if (completedCount === totalTopics && totalTopics > 0) return "Course cracked";
  if (completedCount >= Math.ceil(totalTopics / 2)) return "Momentum builder";
  if (completedCount > 0) return "Getting started";
  return "First sprint";
};

export default function LearningStudent() {
  const [selectedSubject, setSelectedSubject] = useState(COURSE_CONTENT[0].title);
  const [progressStore, setProgressStore] = useState(buildInitialProgress);
  const [currentTopicIndex, setCurrentTopicIndex] = useState(0);
  const [focusTopicIndex, setFocusTopicIndex] = useState<number | null>(null);
  const [focusDuration, setFocusDuration] = useState(FOCUS_DURATIONS[0]);
  const [timeRemaining, setTimeRemaining] = useState(FOCUS_DURATIONS[0] * 60);
  const [answers, setAnswers] = useState<string[]>([]);
  const [result, setResult] = useState<QuizResult | null>(null);
  const submittedRef = useRef(false);

  const selectedCourse = useMemo(
    () => COURSE_CONTENT.find((course) => course.title === selectedSubject) ?? COURSE_CONTENT[0],
    [selectedSubject]
  );

  const subjectProgress = useMemo(
    () => normaliseSubjectProgress(selectedCourse, progressStore[selectedSubject]),
    [progressStore, selectedCourse, selectedSubject]
  );

  const previewTopic = selectedCourse.topics[currentTopicIndex] ?? selectedCourse.topics[0];
  const focusTopic = focusTopicIndex != null ? selectedCourse.topics[focusTopicIndex] : null;

  useEffect(() => {
    setCurrentTopicIndex(subjectProgress.currentTopicIndex);
  }, [selectedSubject, subjectProgress.currentTopicIndex]);

  useEffect(() => {
    window.localStorage.setItem(PROGRESS_STORAGE_KEY, JSON.stringify(progressStore));
  }, [progressStore]);

  useEffect(() => {
    if (focusTopicIndex == null || result) return undefined;

    const timer = window.setInterval(() => {
      setTimeRemaining((previous) => Math.max(previous - 1, 0));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [focusTopicIndex, result]);

  const getTopicStatus = useCallback((topicIndex: number) => {
    if (subjectProgress.completedTopicIndexes.includes(topicIndex)) return "COMPLETED";
    if (topicIndex === subjectProgress.unlockedTopicIndex) return "UNLOCKED";
    return "LOCKED";
  }, [subjectProgress.completedTopicIndexes, subjectProgress.unlockedTopicIndex]);

  const handleSubmit = useCallback((autoSubmitted = false) => {
    if (!focusTopic || focusTopicIndex == null || submittedRef.current) return;

    submittedRef.current = true;
    const score = focusTopic.questions.reduce((total, question, index) => total + (answers[index] === question.answer ? 1 : 0), 0);
    const passed = score >= 2;
    const nextTopicIndex = Math.min(focusTopicIndex + 1, selectedCourse.topics.length - 1);

    setResult({ score, passed, autoSubmitted });
    setCurrentTopicIndex(passed ? nextTopicIndex : focusTopicIndex);

    // Persist unlock state so students can continue from the same point after refresh.
    setProgressStore((previous) => {
      const existing = normaliseSubjectProgress(selectedCourse, previous[selectedSubject]);
      const completedTopicIndexes = new Set(existing.completedTopicIndexes);
      const bestScores = { ...existing.bestScores, [focusTopicIndex]: Math.max(existing.bestScores[focusTopicIndex] ?? 0, score) };

      let unlockedTopicIndex = existing.unlockedTopicIndex;
      let currentTopic = focusTopicIndex;

      if (passed) {
        completedTopicIndexes.add(focusTopicIndex);
        unlockedTopicIndex = Math.max(existing.unlockedTopicIndex, nextTopicIndex);
        currentTopic = nextTopicIndex;
      }

      if (completedTopicIndexes.size === selectedCourse.topics.length) {
        unlockedTopicIndex = selectedCourse.topics.length - 1;
        currentTopic = selectedCourse.topics.length - 1;
      }

      return {
        ...previous,
        [selectedSubject]: {
          unlockedTopicIndex,
          currentTopicIndex: currentTopic,
          completedTopicIndexes: Array.from(completedTopicIndexes).sort((left, right) => left - right),
          bestScores
        }
      };
    });
  }, [answers, focusTopic, focusTopicIndex, selectedCourse, selectedSubject]);

  useEffect(() => {
    if (focusTopicIndex != null && timeRemaining === 0 && !result) {
      handleSubmit(true);
    }
  }, [focusTopicIndex, handleSubmit, result, timeRemaining]);

  const startFocusMode = (topicIndex: number) => {
    if (getTopicStatus(topicIndex) === "LOCKED") return;

    submittedRef.current = false;
    setCurrentTopicIndex(topicIndex);
    setFocusTopicIndex(topicIndex);
    setAnswers(new Array(selectedCourse.topics[topicIndex].questions.length).fill(""));
    setResult(null);
    setTimeRemaining(focusDuration * 60);

    setProgressStore((previous) => ({
      ...previous,
      [selectedSubject]: {
        ...normaliseSubjectProgress(selectedCourse, previous[selectedSubject]),
        currentTopicIndex: topicIndex
      }
    }));
  };

  const closeFocusMode = () => {
    if (!result && !window.confirm("Exit focus mode? This attempt will reset.")) return;
    submittedRef.current = false;
    setFocusTopicIndex(null);
    setAnswers([]);
    setResult(null);
    setTimeRemaining(focusDuration * 60);
  };

  const retryFocusMode = () => {
    if (focusTopicIndex == null) return;
    submittedRef.current = false;
    setAnswers(new Array(selectedCourse.topics[focusTopicIndex].questions.length).fill(""));
    setResult(null);
    setTimeRemaining(focusDuration * 60);
  };

  const continueAfterPass = () => {
    submittedRef.current = false;
    setFocusTopicIndex(null);
    setAnswers([]);
    setResult(null);
    setTimeRemaining(focusDuration * 60);
  };

  const subjectCompletedCount = subjectProgress.completedTopicIndexes.length;
  const subjectPercent = selectedCourse.topics.length === 0 ? 0 : (subjectCompletedCount / selectedCourse.topics.length) * 100;
  const subjectRecentScores = Object.entries(subjectProgress.bestScores).sort(([left], [right]) => Number(left) - Number(right)).map(([, score]) => score);
  const subjectStreak = selectedCourse.topics.reduce((count, _, index) => {
    if (subjectProgress.completedTopicIndexes.includes(index) && count === index) return count + 1;
    return count;
  }, 0);

  const overallProgress = useMemo(() => {
    const totals = COURSE_CONTENT.reduce(
      (accumulator, subject) => {
        const itemProgress = normaliseSubjectProgress(subject, progressStore[subject.title]);
        accumulator.totalTopics += subject.topics.length;
        accumulator.completedTopics += itemProgress.completedTopicIndexes.length;
        return accumulator;
      },
      { totalTopics: 0, completedTopics: 0 }
    );

    return {
      totalTopics: totals.totalTopics,
      completedTopics: totals.completedTopics,
      percent: totals.totalTopics === 0 ? 0 : (totals.completedTopics / totals.totalTopics) * 100,
      points: totals.completedTopics * 120
    };
  }, [progressStore]);

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Student Learning</p>
          <h2 className="text-2xl font-semibold">AI-Structured Study Flow</h2>
          <p className="mt-1 text-sm text-muted-foreground">Learn, answer 3 questions, unlock the next topic, and keep your streak moving.</p>
        </div>

        <Card className="flex flex-wrap items-center gap-3 p-4">
          {COURSE_CONTENT.map((course) => (
            <button
              key={course.title}
              type="button"
              onClick={() => setSelectedSubject(course.title)}
              className={`rounded-full px-4 py-2 text-sm transition ${course.title === selectedSubject ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:text-foreground"}`}
            >
              {course.title}
            </button>
          ))}
        </Card>

        <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
          <div className="space-y-4">
            <Card className="space-y-3 p-5">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Current Subject</p>
                  <h3 className="text-xl font-semibold">{selectedCourse.title}</h3>
                  <p className="mt-2 max-w-2xl text-sm text-muted-foreground">{selectedCourse.description}</p>
                </div>

                <div className="flex items-center gap-2 rounded-full border border-border/60 bg-white/70 px-3 py-1 text-xs text-muted-foreground dark:bg-white/10">
                  <Clock3 size={14} />
                  <span>Focus duration</span>
                  <select className="bg-transparent text-xs outline-none" value={focusDuration} onChange={(event) => setFocusDuration(Number(event.target.value))}>
                    {FOCUS_DURATIONS.map((duration) => (
                      <option key={duration} value={duration}>{duration} min</option>
                    ))}
                  </select>
                </div>
              </div>
            </Card>

            <Card className="border-primary/10 bg-primary/5 p-4">
              <div className="flex items-start gap-3">
                <Sparkles className="mt-0.5 text-primary" size={18} />
                <div>
                  <p className="text-sm font-semibold">Complete this topic to unlock next</p>
                  <p className="mt-1 text-sm text-muted-foreground">Only one new topic unlocks at a time. Score at least 2 out of 3 in the quiz to continue the path.</p>
                </div>
              </div>
            </Card>

            {selectedCourse.topics.map((topic, index) => (
              <TopicCard
                key={`${selectedCourse.title}-${topic.title}`}
                title={topic.title}
                description={topic.description}
                status={getTopicStatus(index)}
                order={index + 1}
                bestScore={subjectProgress.bestScores[index] ?? null}
                onStart={() => startFocusMode(index)}
              />
            ))}
          </div>

          <div className="space-y-6">
            <ProgressPanel
              completionPercent={subjectPercent}
              completedTopics={subjectCompletedCount}
              totalTopics={selectedCourse.topics.length}
              points={overallProgress.points}
              streak={subjectStreak}
              badges={getBadgeLabel(subjectCompletedCount, selectedCourse.topics.length)}
              recentScores={subjectRecentScores}
            />

            <Card className="space-y-4 p-5">
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Next Up</p>
                <h3 className="mt-2 text-lg font-semibold">{previewTopic.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground">{previewTopic.description}</p>
              </div>

              <div className="rounded-2xl border border-border/60 bg-muted/35 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">Current unlock</p>
                <p className="mt-2 text-sm font-medium">Topic {subjectProgress.unlockedTopicIndex + 1} of {selectedCourse.topics.length}</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  {getTopicStatus(currentTopicIndex) === "COMPLETED"
                    ? "Nice work - revisit any completed topic whenever you want."
                    : "Start Focus opens the lesson, starts the timer, and unlocks the next topic when you pass."}
                </p>
              </div>

              <div className="rounded-2xl border border-border/60 bg-muted/35 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">Overall academy progress</p>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-muted">
                  <div className="h-full bg-emerald-500 transition-all" style={{ width: `${overallProgress.percent}%` }} />
                </div>
                <p className="mt-3 text-sm text-muted-foreground">{overallProgress.completedTopics} of {overallProgress.totalTopics} topics completed across all subjects.</p>
              </div>
            </Card>
          </div>
        </div>
      </div>

      {focusTopic && (
        <div className="fixed inset-0 z-[60] overflow-y-auto bg-slate-950/90 px-4 py-6">
          <div className="mx-auto flex min-h-full max-w-6xl flex-col rounded-3xl border border-white/10 bg-gradient-to-br from-slate-900/95 via-slate-950/95 to-black text-white shadow-[0_30px_80px_-40px_rgba(15,23,42,0.85)]">
            <div className="border-b border-white/10 px-6 py-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <p className="text-xs uppercase tracking-[0.4em] text-white/50">Focus Mode</p>
                  <h2 className="mt-3 text-3xl font-semibold">{focusTopic.title}</h2>
                  <p className="mt-2 max-w-2xl text-sm text-white/60">{focusTopic.description}</p>
                </div>

                <div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-right">
                  <p className="text-xs uppercase tracking-[0.3em] text-white/45">Timer</p>
                  <div className="mt-2 text-3xl font-semibold tracking-[0.2em]">{formatTime(timeRemaining)}</div>
                  <div className="mt-3 h-2 w-48 overflow-hidden rounded-full bg-white/10">
                    <div className="h-full bg-emerald-400 transition-all" style={{ width: `${((focusDuration * 60 - timeRemaining) / (focusDuration * 60)) * 100}%` }} />
                  </div>
                </div>
              </div>
            </div>

            <div className="grid flex-1 gap-6 p-6 lg:grid-cols-[1.15fr_0.85fr]">
              <TopicViewer topic={focusTopic} />
              <QuizSection
                topic={focusTopic}
                answers={answers}
                disabled={Boolean(result)}
                result={result}
                onAnswerChange={(questionIndex, value) => {
                  if (result) return;
                  setAnswers((previous) => {
                    const next = [...previous];
                    next[questionIndex] = value;
                    return next;
                  });
                }}
                onSubmit={() => handleSubmit(false)}
                onRetry={retryFocusMode}
                onContinue={continueAfterPass}
              />
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4 border-t border-white/10 px-6 py-5">
              <div className="flex items-center gap-2 text-sm text-white/60">
                <Lock size={14} />
                <span>Locked topics stay unavailable until you clear the current quiz.</span>
              </div>
              <Button variant="secondary" onClick={closeFocusMode}>{result?.passed ? "Close" : "Exit Focus"}</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
