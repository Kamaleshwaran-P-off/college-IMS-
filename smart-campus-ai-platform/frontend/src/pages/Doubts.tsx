import { useEffect, useMemo, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getJson, postJson } from "@/lib/api";
import NotificationBell from "@/components/NotificationBell";

interface DoubtItem {
  id: number;
  studentId: number;
  studentUserId: number;
  title: string;
  description: string;
  status: "OPEN" | "ANSWERED" | "CLOSED";
  createdAt: string;
  acceptedAnswerId: number | null;
}

interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface LeaderboardEntry {
  userId: number;
  name: string;
  acceptedCount: number;
}

export default function Doubts() {
  const [doubts, setDoubts] = useState<DoubtItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [assignmentId, setAssignmentId] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [statusFilter, setStatusFilter] = useState("ALL");
  const [acceptedFilter, setAcceptedFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [myOnly, setMyOnly] = useState(false);
  const [page, setPage] = useState(0);
  const [size] = useState(6);
  const [totalPages, setTotalPages] = useState(1);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);

  const userId = useMemo(() => {
    const raw = localStorage.getItem("userId");
    return raw ? Number(raw) : null;
  }, []);
  const role = useMemo(() => {
    const raw = localStorage.getItem("role") || localStorage.getItem("userRole") || "STUDENT";
    return raw.replace("ROLE_", "").toUpperCase();
  }, []);
  const isStudent = role === "STUDENT";

  const buildQuery = () => {
    const params = new URLSearchParams();
    if (statusFilter !== "ALL") params.set("status", statusFilter);
    if (acceptedFilter !== "ALL") params.set("accepted", acceptedFilter);
    if (search.trim()) params.set("search", search.trim());
    if (myOnly && userId) params.set("studentUserId", String(userId));
    params.set("page", String(page));
    params.set("size", String(size));
    return params.toString();
  };

  const loadDoubts = async () => {
    try {
      setLoading(true);
      const response = await getJson<PagedResponse<DoubtItem>>(`/api/doubts?${buildQuery()}`);
      setDoubts(response.content);
      setTotalPages(response.totalPages || 1);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load doubts");
    } finally {
      setLoading(false);
    }
  };

  const loadLeaderboard = async () => {
    try {
      const response = await getJson<LeaderboardEntry[]>("/api/doubts/leaderboard?limit=5");
      setLeaderboard(response);
    } catch (err) {
      // optional UI
    }
  };

  useEffect(() => {
    loadDoubts();
  }, [statusFilter, acceptedFilter, search, myOnly, page]);

  useEffect(() => {
    loadLeaderboard();
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!isStudent) {
      setError("Only students can post doubts.");
      return;
    }
    if (!userId) {
      setError("Please log in as a student to post a doubt.");
      return;
    }
    if (!title.trim()) {
      setError("Title is required.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await postJson<DoubtItem>("/api/doubts", {
        userId,
        assignmentId: assignmentId ? Number(assignmentId) : null,
        title: title.trim(),
        description: description.trim(),
        status: "OPEN"
      });
      setTitle("");
      setDescription("");
      setAssignmentId("");
      setPage(0);
      await loadDoubts();
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to post doubt";
      if (message.includes("Student access required")) {
        setError("Only students can post doubts.");
      } else if (message.includes("Student profile not found")) {
        setError("Your student profile is being set up. Please try again in a moment.");
      } else {
        setError(message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const resetFilters = () => {
    setStatusFilter("ALL");
    setAcceptedFilter("ALL");
    setSearch("");
    setMyOnly(false);
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-6xl space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Smart Doubt Forum</h2>
          <NotificationBell />
        </div>
        <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
          <Card>
            <CardHeader>
              <CardTitle>Smart Doubt Forum</CardTitle>
              <CardDescription>Post doubts, answer peers, and unlock more AI queries.</CardDescription>
            </CardHeader>
            <CardContent>
              {isStudent ? (
                <form className="grid gap-3" onSubmit={handleSubmit}>
                  <Input
                    placeholder="Doubt title"
                    value={title}
                    onChange={(event) => setTitle(event.target.value)}
                  />
                  <Input
                    placeholder="Describe your doubt"
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                  />
                  <Input
                    placeholder="Assignment ID (optional)"
                    value={assignmentId}
                    onChange={(event) => setAssignmentId(event.target.value)}
                  />
                  <div className="flex gap-3">
                    <Button type="submit" disabled={submitting}>
                      {submitting ? "Posting..." : "Post Doubt"}
                    </Button>
                    <Button variant="secondary" onClick={loadDoubts} type="button">
                      Refresh
                    </Button>
                  </div>
                  {error && <p className="text-sm text-red-600">{error}</p>}
                </form>
              ) : (
                <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                  Posting doubts is available only for students. You can still browse and answer.
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Top Contributors</CardTitle>
              <CardDescription>Accepted answers leaderboard</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {leaderboard.map((entry, index) => (
                  <div key={entry.userId} className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium">#{index + 1} {entry.name}</p>
                      <p className="text-xs text-muted-foreground">Accepted: {entry.acceptedCount}</p>
                    </div>
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-xs text-primary">
                      {entry.acceptedCount}
                    </span>
                  </div>
                ))}
                {leaderboard.length === 0 && (
                  <p className="text-sm text-muted-foreground">No accepted answers yet.</p>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Latest Doubts</CardTitle>
            <CardDescription>{loading ? "Loading..." : `${doubts.length} doubts`}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-3 md:grid-cols-[2fr_1fr_1fr_1fr_auto]">
              <Input
                placeholder="Search by title or description"
                value={search}
                onChange={(event) => {
                  setSearch(event.target.value);
                  setPage(0);
                }}
              />
              <select
                className="h-10 rounded-md border border-input bg-background px-3 text-sm"
                value={statusFilter}
                onChange={(event) => {
                  setStatusFilter(event.target.value);
                  setPage(0);
                }}
              >
                <option value="ALL">All status</option>
                <option value="OPEN">Open</option>
                <option value="ANSWERED">Answered</option>
                <option value="CLOSED">Closed</option>
              </select>
              <select
                className="h-10 rounded-md border border-input bg-background px-3 text-sm"
                value={acceptedFilter}
                onChange={(event) => {
                  setAcceptedFilter(event.target.value);
                  setPage(0);
                }}
              >
                <option value="ALL">All</option>
                <option value="true">Accepted</option>
                <option value="false">Unaccepted</option>
              </select>
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={myOnly}
                  onChange={() => {
                    setMyOnly((prev) => !prev);
                    setPage(0);
                  }}
                />
                My doubts
              </label>
              <Button variant="secondary" onClick={resetFilters} type="button">
                Reset
              </Button>
            </div>

            <div className="mt-4 space-y-3">
              {doubts.map((doubt) => (
                <div key={doubt.id} className="rounded-lg border border-border p-4">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">
                          {doubt.status}
                        </p>
                        {doubt.acceptedAnswerId && (
                          <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-700">
                            Accepted
                          </span>
                        )}
                      </div>
                      <h3 className="mt-2 text-lg font-semibold">{doubt.title}</h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {doubt.description || "No description provided."}
                      </p>
                      <p className="mt-2 text-xs text-muted-foreground">
                        {new Date(doubt.createdAt).toLocaleString()}
                      </p>
                    </div>
                    {doubt.id < 0 ? (
                      <Button variant="secondary" disabled>
                        View
                      </Button>
                    ) : (
                      <Link to={`/doubts/${doubt.id}`}>
                        <Button variant="secondary">View</Button>
                      </Link>
                    )}
                  </div>
                </div>
              ))}
              {!loading && doubts.length === 0 && (
                <p className="text-sm text-muted-foreground">No doubts match this filter.</p>
              )}
            </div>

            <div className="mt-6 flex items-center justify-between">
              <Button
                variant="secondary"
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <p className="text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </p>
              <Button
                variant="secondary"
                onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
