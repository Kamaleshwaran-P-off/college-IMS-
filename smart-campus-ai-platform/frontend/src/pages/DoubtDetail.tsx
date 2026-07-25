import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getJson, postJson } from "@/lib/api";
import NotificationBell from "@/components/NotificationBell";

interface DoubtAnswer {
  id: number;
  authorId: number;
  authorName: string;
  authorRole: string;
  content: string;
  createdAt: string;
}

interface DoubtDetail {
  id: number;
  studentId: number;
  studentUserId: number;
  studentName: string;
  title: string;
  description: string;
  status: "OPEN" | "ANSWERED" | "CLOSED";
  createdAt: string;
  acceptedAnswerId: number | null;
  answers: DoubtAnswer[];
}

export default function DoubtDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<DoubtDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [answer, setAnswer] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);

  const loadDetail = async () => {
    if (!id) return;
    try {
      setLoading(true);
      const response = await getJson<DoubtDetail>(`/api/doubts/${id}`);
      setDetail(response);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load doubt");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetail();
  }, [id]);

  const handleAnswerSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!userId) {
      setError("Please log in to answer.");
      return;
    }
    if (!answer.trim()) {
      setError("Answer cannot be empty.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await postJson("/api/answers", {
        doubtId: Number(id),
        authorId: userId,
        content: answer.trim()
      });
      setAnswer("");
      await loadDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit answer");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAccept = async (answerId: number) => {
    try {
      await postJson(`/api/doubts/${id}/accept/${answerId}`, {});
      await loadDetail();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to accept answer");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-campus flex items-center justify-center px-6">
        <Card className="max-w-md">
          <CardContent className="py-10">Loading doubt...</CardContent>
        </Card>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className="min-h-screen bg-campus flex items-center justify-center px-6">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Not found</CardTitle>
            <CardDescription>Unable to load this doubt.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/doubts")}>Back to Doubts</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const acceptedAnswer = detail.acceptedAnswerId
    ? detail.answers.find((ans) => ans.id === detail.acceptedAnswerId)
    : null;
  const canAccept = userId && userId === detail.studentUserId && !detail.acceptedAnswerId;

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-4xl space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Doubt Detail</h2>
          <NotificationBell />
        </div>
        <Card>
          <CardHeader>
            <CardTitle>{detail.title}</CardTitle>
            <CardDescription>
              Posted by {detail.studentName} • {new Date(detail.createdAt).toLocaleString()}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">Status: {detail.status}</p>
            <p className="mt-3">{detail.description || "No description provided."}</p>
            {detail.acceptedAnswerId && (
              <p className="mt-4 text-sm text-green-600">
                Best answer accepted.{acceptedAnswer?.authorRole === "STUDENT" ? " +1 AI query unlocked." : ""}
              </p>
            )}
            <div className="mt-6 flex gap-3">
              <Link to="/doubts">
                <Button variant="secondary">Back to list</Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Answers</CardTitle>
            <CardDescription>{detail.answers.length} responses</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {detail.answers.map((ans) => (
                <div
                  key={ans.id}
                  className={`rounded-lg border p-4 ${
                    detail.acceptedAnswerId === ans.id ? "border-green-500 bg-green-50/50" : "border-border"
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-sm font-medium">
                        {ans.authorName} <span className="text-xs text-muted-foreground">({ans.authorRole})</span>
                      </p>
                      <p className="mt-2 text-sm">{ans.content}</p>
                      <p className="mt-2 text-xs text-muted-foreground">
                        {new Date(ans.createdAt).toLocaleString()}
                      </p>
                    </div>
                    {canAccept && (
                      <Button onClick={() => handleAccept(ans.id)} variant="secondary">
                        Accept
                      </Button>
                    )}
                  </div>
                  {detail.acceptedAnswerId === ans.id && (
                    <p className="mt-2 text-xs text-green-600">Accepted answer</p>
                  )}
                </div>
              ))}
              {detail.answers.length === 0 && (
                <p className="text-sm text-muted-foreground">No answers yet. Be the first to help.</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Post an Answer</CardTitle>
            <CardDescription>Contribute your solution or guidance.</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="grid gap-3" onSubmit={handleAnswerSubmit}>
              <Input
                placeholder="Type your answer here"
                value={answer}
                onChange={(event) => setAnswer(event.target.value)}
              />
              <div className="flex gap-3">
                <Button type="submit" disabled={submitting}>
                  {submitting ? "Submitting..." : "Submit Answer"}
                </Button>
                <Button type="button" variant="secondary" onClick={() => setAnswer("")}>
                  Clear
                </Button>
              </div>
              {error && <p className="text-sm text-red-600">{error}</p>}
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
