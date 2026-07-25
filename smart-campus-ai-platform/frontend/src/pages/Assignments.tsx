import { useEffect, useState } from "react";
import { API_BASE_URL, getAuthHeaders, getAuthToken, getJson, patchJson, readErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import TableSkeleton from "@/components/skeletons/TableSkeleton";
import EmptyState from "@/components/feedback/EmptyState";
import SuccessState from "@/components/feedback/SuccessState";
import { useAuth } from "@/context/AuthContext";

type CourseAssignment = {
  id: number;
  title: string;
  description?: string | null;
  dueDate?: string | null;
  department?: string | null;
  className?: string | null;
  createdBy?: string | null;
  createdAt: string;
  updatedAt?: string | null;
  attachmentAvailable: boolean;
  visible?: boolean;
};

type AssignmentSubmission = {
  id: number;
  assignmentId: number;
  studentId: number;
  studentName: string;
  studentCode: string;
  answerText?: string | null;
  marks?: number | null;
  feedback?: string | null;
  submittedAt?: string | null;
  gradedAt?: string | null;
  attachmentAvailable: boolean;
};

type GradeDraft = {
  marks: string;
  feedback: string;
};

const formatDateTime = (value?: string | null) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
};

const formatDate = (value?: string | null) => {
  if (!value) return "No due date";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString();
};

async function downloadWithAuth(path: string, fallbackName: string) {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  });

  if (!response.ok) {
    throw new Error("Failed to download file");
  }

  const blob = await response.blob();
  const contentDisposition = response.headers.get("content-disposition") || "";
  const match = contentDisposition.match(/filename=\"?([^\";]+)\"?/i);
  const filename = match?.[1] || fallbackName;

  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export default function Assignments() {
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();
  const isFaculty = normalizedRole === "FACULTY" || normalizedRole === "STAFF";
  const isStudent = normalizedRole === "STUDENT";

  const [assignments, setAssignments] = useState<CourseAssignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [assignedClasses, setAssignedClasses] = useState<string[]>([]);
  const [createTitle, setCreateTitle] = useState("");
  const [createDescription, setCreateDescription] = useState("");
  const [createDepartment, setCreateDepartment] = useState("");
  const [createClassName, setCreateClassName] = useState("");
  const [createDueDate, setCreateDueDate] = useState("");
  const [createFile, setCreateFile] = useState<File | null>(null);
  const [creating, setCreating] = useState(false);

  const [studentSubmissions, setStudentSubmissions] = useState<Record<number, AssignmentSubmission>>({});
  const [submissionDrafts, setSubmissionDrafts] = useState<Record<number, { text: string; file?: File | null }>>({});

  const [assignmentSubmissions, setAssignmentSubmissions] = useState<Record<number, AssignmentSubmission[]>>({});
  const [expandedAssignmentId, setExpandedAssignmentId] = useState<number | null>(null);
  const [gradeDrafts, setGradeDrafts] = useState<Record<number, GradeDraft>>({});
  const [grading, setGrading] = useState<Record<number, boolean>>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [editingAssignmentId, setEditingAssignmentId] = useState<number | null>(null);
  const [editDrafts, setEditDrafts] = useState<Record<number, {
    title: string;
    description: string;
    dueDate: string;
    department: string;
    className: string;
    file: File | null;
  }>>({});
  const [savingEdit, setSavingEdit] = useState<Record<number, boolean>>({});

  const loadAssignments = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJson<CourseAssignment[]>("/api/coursework/assignments");
      setAssignments(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load assignments.");
    } finally {
      setLoading(false);
    }
  };

  const loadStudentSubmissions = async () => {
    if (!isStudent) return;
    try {
      const data = await getJson<AssignmentSubmission[]>("/api/coursework/assignments/submissions/mine");
      const mapped: Record<number, AssignmentSubmission> = {};
      data.forEach((submission) => {
        mapped[submission.assignmentId] = submission;
      });
      setStudentSubmissions(mapped);
    } catch {
      // ignore
    }
  };

  const loadAssignedClasses = async () => {
    if (!isFaculty) return;
    try {
      const data = await getJson<string[]>("/api/faculty/classes");
      setAssignedClasses(data);
      if (data.length > 0) {
        setCreateClassName(data[0]);
      }
    } catch {
      setAssignedClasses([]);
    }
  };

  useEffect(() => {
    loadAssignments();
    loadStudentSubmissions();
    loadAssignedClasses();
  }, []);

  const handleCreateAssignment = async () => {
    if (!createTitle.trim()) {
      setError("Title is required.");
      return;
    }
    if (!createClassName.trim()) {
      setError("Class name is required.");
      return;
    }

    setCreating(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append("title", createTitle.trim());
      if (createDescription.trim()) formData.append("description", createDescription.trim());
      if (createDueDate) formData.append("dueDate", createDueDate);
      if (createDepartment.trim()) formData.append("department", createDepartment.trim());
      formData.append("className", createClassName.trim());
      if (createFile) formData.append("file", createFile);

      const response = await fetch(`${API_BASE_URL}/api/coursework/assignments`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: formData
      });

      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      setCreateTitle("");
      setCreateDescription("");
      setCreateDepartment("");
      setCreateDueDate("");
      setCreateFile(null);
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create assignment.");
    } finally {
      setCreating(false);
    }
  };

  const handleSubmissionChange = (assignmentId: number, key: "text" | "file", value: string | File | null) => {
    setSubmissionDrafts((prev) => ({
      ...prev,
      [assignmentId]: {
        text: key === "text" ? String(value ?? "") : prev[assignmentId]?.text || "",
        file: key === "file" ? (value as File | null) : prev[assignmentId]?.file || null
      }
    }));
  };

  const handleSubmitAssignment = async (assignmentId: number) => {
    const draft = submissionDrafts[assignmentId];
    if (!draft?.text && !draft?.file) {
      setError("Please add an answer text or attach a file.");
      return;
    }

    setError(null);

    try {
      const formData = new FormData();
      if (draft?.text) formData.append("answerText", draft.text);
      if (draft?.file) formData.append("file", draft.file);

      const response = await fetch(`${API_BASE_URL}/api/coursework/assignments/${assignmentId}/submissions`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: formData
      });

      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      const data: AssignmentSubmission = await response.json();
      setStudentSubmissions((prev) => ({ ...prev, [assignmentId]: data }));
      setSubmissionDrafts((prev) => ({ ...prev, [assignmentId]: { text: "", file: null } }));
      setSuccessMessage("Assignment submitted successfully.");
      window.setTimeout(() => setSuccessMessage(null), 2500);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submission failed.");
    }
  };

  const toggleSubmissions = async (assignmentId: number) => {
    if (expandedAssignmentId === assignmentId) {
      setExpandedAssignmentId(null);
      return;
    }
    setExpandedAssignmentId(assignmentId);

    if (assignmentSubmissions[assignmentId]) {
      return;
    }

    try {
      const data = await getJson<AssignmentSubmission[]>(
        `/api/coursework/assignments/${assignmentId}/submissions`
      );
      setAssignmentSubmissions((prev) => ({ ...prev, [assignmentId]: data }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load submissions.");
    }
  };

  const startEdit = (assignment: CourseAssignment) => {
    setEditingAssignmentId(assignment.id);
    setEditDrafts((prev) => ({
      ...prev,
      [assignment.id]: {
        title: assignment.title || "",
        description: assignment.description || "",
        dueDate: assignment.dueDate || "",
        department: assignment.department || "",
        className: assignment.className || createClassName || "",
        file: null
      }
    }));
  };

  const cancelEdit = () => {
    setEditingAssignmentId(null);
  };

  const saveEdit = async (assignmentId: number) => {
    const draft = editDrafts[assignmentId];
    if (!draft?.title?.trim()) {
      setError("Title is required.");
      return;
    }

    setSavingEdit((prev) => ({ ...prev, [assignmentId]: true }));
    setError(null);
    try {
      const formData = new FormData();
      formData.append("title", draft.title.trim());
      if (draft.description.trim()) formData.append("description", draft.description.trim());
      if (draft.dueDate) formData.append("dueDate", draft.dueDate);
      if (draft.department.trim()) formData.append("department", draft.department.trim());
      if (draft.className.trim()) formData.append("className", draft.className.trim());
      if (draft.file) formData.append("file", draft.file);

      const response = await fetch(`${API_BASE_URL}/api/coursework/assignments/${assignmentId}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: formData
      });
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      setEditingAssignmentId(null);
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update assignment.");
    } finally {
      setSavingEdit((prev) => ({ ...prev, [assignmentId]: false }));
    }
  };

  const toggleVisibility = async (assignmentId: number, current?: boolean) => {
    setError(null);
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/coursework/assignments/${assignmentId}/hide?visible=${!current}`,
        {
          method: "PATCH",
          headers: getAuthHeaders()
        }
      );
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update visibility.");
    }
  };

  const deleteAssignment = async (assignmentId: number) => {
    if (!window.confirm("Delete this assignment permanently?")) return;
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/coursework/assignments/${assignmentId}`, {
        method: "DELETE",
        headers: getAuthHeaders()
      });
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }
      await loadAssignments();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete assignment.");
    }
  };

  const handleGrade = async (submissionId: number) => {
    const draft = gradeDrafts[submissionId];
    if (!draft?.marks) {
      setError("Marks are required.");
      return;
    }
    const numericMarks = Number(draft.marks);
    if (!Number.isFinite(numericMarks)) {
      setError("Marks must be a valid number.");
      return;
    }

    setGrading((prev) => ({ ...prev, [submissionId]: true }));
    setError(null);
    try {
      const updated = await patchJson<AssignmentSubmission>(
        `/api/coursework/assignments/submissions/${submissionId}`,
        {
          marks: numericMarks,
          feedback: draft.feedback || ""
        }
      );

      setAssignmentSubmissions((prev) => {
        const updatedMap = { ...prev };
        for (const key of Object.keys(updatedMap)) {
          updatedMap[Number(key)] = updatedMap[Number(key)].map((item) =>
            item.id === updated.id ? updated : item
          );
        }
        return updatedMap;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save grade.");
    } finally {
      setGrading((prev) => ({ ...prev, [submissionId]: false }));
    }
  };

  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>Coursework Hub</CardDescription>
          <CardTitle>Assignments</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          {isFaculty
            ? "Create assignments, review submissions, and publish feedback."
            : "View assignments, submit responses, and track your grades."}
        </CardContent>
      </Card>

      {error && (
        <div className="rounded-2xl border border-rose-200/60 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/10 dark:text-rose-200">
          {error}
        </div>
      )}

      {successMessage && (
        <SuccessState title={successMessage} description="Your response has been recorded." />
      )}

      {isFaculty && (
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Create Assignment</CardTitle>
            <CardDescription>Share questions and due dates with your class.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-3 md:grid-cols-2">
              <div className="space-y-2">
                <Label>Title</Label>
                <Input value={createTitle} onChange={(event) => setCreateTitle(event.target.value)} />
              </div>
              <div className="space-y-2">
                <Label>Due Date</Label>
                <Input
                  type="date"
                  value={createDueDate}
                  onChange={(event) => setCreateDueDate(event.target.value)}
                />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>Description</Label>
                <textarea
                  className="h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                  value={createDescription}
                  onChange={(event) => setCreateDescription(event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Department (optional)</Label>
                <Input
                  value={createDepartment}
                  onChange={(event) => setCreateDepartment(event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Class</Label>
                {assignedClasses.length > 0 ? (
                  <select
                    className="h-10 w-full rounded-md border border-input bg-transparent px-3 text-sm"
                    value={createClassName}
                    onChange={(event) => setCreateClassName(event.target.value)}
                  >
                    {assignedClasses.map((cls) => (
                      <option key={cls} value={cls}>
                        {cls}
                      </option>
                    ))}
                  </select>
                ) : (
                  <Input
                    value={createClassName}
                    onChange={(event) => setCreateClassName(event.target.value)}
                    placeholder="CSE-A"
                  />
                )}
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>Attachment (optional)</Label>
                <Input
                  type="file"
                  onChange={(event) => setCreateFile(event.target.files?.[0] || null)}
                />
              </div>
            </div>
            <Button disabled={creating} onClick={handleCreateAssignment}>
              {creating ? "Uploading..." : "Create Assignment"}
            </Button>
          </CardContent>
        </Card>
      )}

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Assignments List</CardTitle>
          <CardDescription>{assignments.length} assignments available.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {loading && <TableSkeleton rows={4} columns={3} />}
          {!loading && assignments.length === 0 && (
            <EmptyState
              title="No assignments yet"
              description="Faculty uploads will appear here once published."
            />
          )}
          {assignments.map((assignment) => {
            const isSample = assignment.id < 0;
            const submission = studentSubmissions[assignment.id];
            const visible = assignment.visible ?? true;
            const isEditing = editingAssignmentId === assignment.id;
            return (
              <div
                key={assignment.id}
                className="rounded-2xl border border-border/60 bg-white/70 p-4 dark:bg-white/5"
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="text-base font-semibold text-foreground">{assignment.title}</p>
                    <p className="text-xs text-muted-foreground">
                      Due: {formatDate(assignment.dueDate)} - {assignment.className || "General"}
                    </p>
                    {!visible && (
                      <p className="mt-1 text-xs font-semibold text-rose-600">Hidden from students</p>
                    )}
                    {assignment.description && (
                      <p className="mt-2 text-sm text-muted-foreground">{assignment.description}</p>
                    )}
                    <p className="mt-2 text-xs text-muted-foreground">
                      Posted by {assignment.createdBy || "Faculty"} on {formatDateTime(assignment.createdAt)}
                    </p>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    {assignment.attachmentAvailable && (
                      <Button
                        variant="secondary"
                        disabled={isSample}
                        onClick={() =>
                          downloadWithAuth(
                            `/api/coursework/assignments/${assignment.id}/file`,
                            `assignment-${assignment.id}`
                          ).catch((err) => setError(err.message))
                        }
                      >
                        Download File
                      </Button>
                    )}

                    {isFaculty && (
                      <>
                        <Button
                          variant="ghost"
                          onClick={() => toggleSubmissions(assignment.id)}
                          disabled={isSample}
                        >
                          {expandedAssignmentId === assignment.id ? "Hide Submissions" : "View Submissions"}
                        </Button>
                        <Button variant="outline" onClick={() => startEdit(assignment)} disabled={isSample}>
                          Edit
                        </Button>
                        <Button
                          variant="secondary"
                          onClick={() => toggleVisibility(assignment.id, visible)}
                          disabled={isSample}
                        >
                          {visible ? "Hide" : "Unhide"}
                        </Button>
                        <Button variant="destructive" onClick={() => deleteAssignment(assignment.id)} disabled={isSample}>
                          Delete
                        </Button>
                      </>
                    )}
                  </div>
                </div>

                {isFaculty && isEditing && (
                  <div className="mt-4 rounded-xl border border-border/60 bg-slate-50/60 p-4 dark:bg-white/10">
                    <div className="grid gap-3 md:grid-cols-2">
                      <div className="space-y-1">
                        <Label>Title</Label>
                        <Input
                          value={editDrafts[assignment.id]?.title || ""}
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                title: event.target.value
                              }
                            }))
                          }
                        />
                      </div>
                      <div className="space-y-1">
                        <Label>Due Date</Label>
                        <Input
                          type="date"
                          value={editDrafts[assignment.id]?.dueDate || ""}
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                dueDate: event.target.value
                              }
                            }))
                          }
                        />
                      </div>
                      <div className="space-y-1 md:col-span-2">
                        <Label>Description</Label>
                        <textarea
                          className="h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                          value={editDrafts[assignment.id]?.description || ""}
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                description: event.target.value
                              }
                            }))
                          }
                        />
                      </div>
                      <div className="space-y-1">
                        <Label>Department (optional)</Label>
                        <Input
                          value={editDrafts[assignment.id]?.department || ""}
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                department: event.target.value
                              }
                            }))
                          }
                        />
                      </div>
                      <div className="space-y-1">
                        <Label>Class</Label>
                        <Input
                          value={editDrafts[assignment.id]?.className || ""}
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                className: event.target.value
                              }
                            }))
                          }
                        />
                      </div>
                      <div className="space-y-1 md:col-span-2">
                        <Label>Replace Attachment (optional)</Label>
                        <Input
                          type="file"
                          onChange={(event) =>
                            setEditDrafts((prev) => ({
                              ...prev,
                              [assignment.id]: {
                                ...prev[assignment.id],
                                file: event.target.files?.[0] || null
                              }
                            }))
                          }
                        />
                      </div>
                    </div>
                    <div className="mt-3 flex gap-2">
                      <Button
                        onClick={() => saveEdit(assignment.id)}
                        disabled={savingEdit[assignment.id]}
                      >
                        {savingEdit[assignment.id] ? "Saving..." : "Save"}
                      </Button>
                      <Button variant="ghost" onClick={cancelEdit}>
                        Cancel
                      </Button>
                    </div>
                  </div>
                )}

                {isStudent && (
                  <div className="mt-4 rounded-xl border border-border/60 bg-slate-50/60 p-4 dark:bg-white/5">
                    <p className="text-sm font-semibold text-foreground">Submit Your Answer</p>
                    <textarea
                      className="mt-2 h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                      placeholder="Type your answer here..."
                      value={submissionDrafts[assignment.id]?.text || ""}
                      onChange={(event) =>
                        handleSubmissionChange(assignment.id, "text", event.target.value)
                      }
                    />
                  <div className="mt-3 flex flex-wrap items-center gap-3">
                    <Input
                      type="file"
                      onChange={(event) =>
                        handleSubmissionChange(assignment.id, "file", event.target.files?.[0] || null)
                      }
                    />
                    <Button onClick={() => handleSubmitAssignment(assignment.id)} disabled={isSample}>
                      {submission ? "Update Submission" : "Submit Assignment"}
                    </Button>
                  </div>

                    {submission && (
                      <div className="mt-4 rounded-xl border border-border/60 bg-white/80 p-3 text-xs text-muted-foreground dark:bg-white/10">
                        <p>
                          Submitted: {formatDateTime(submission.submittedAt)} -{" "}
                          {submission.gradedAt ? "Graded" : "Awaiting review"}
                        </p>
                        {submission.marks != null && (
                          <p className="mt-2 text-sm font-semibold text-foreground">
                            Marks: {submission.marks}
                          </p>
                        )}
                        {submission.feedback && (
                          <p className="mt-1 text-sm text-muted-foreground">Feedback: {submission.feedback}</p>
                        )}
                        {submission.attachmentAvailable && (
                          <Button
                            variant="ghost"
                            className="mt-3"
                            onClick={() =>
                              downloadWithAuth(
                                `/api/coursework/assignments/submissions/${submission.id}/file`,
                                `submission-${submission.id}`
                              ).catch((err) => setError(err.message))
                            }
                          >
                            Download Your File
                          </Button>
                        )}
                      </div>
                    )}
                  </div>
                )}

                {isFaculty && expandedAssignmentId === assignment.id && (
                  <div className="mt-4 space-y-3">
                    {(assignmentSubmissions[assignment.id] || []).length === 0 && (
                      <p className="text-sm text-muted-foreground">No submissions yet.</p>
                    )}
                    {(assignmentSubmissions[assignment.id] || []).map((submission) => (
                      <div
                        key={submission.id}
                        className="rounded-xl border border-border/60 bg-white/80 p-4 text-sm dark:bg-white/10"
                      >
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          <div>
                            <p className="font-semibold text-foreground">{submission.studentName}</p>
                            <p className="text-xs text-muted-foreground">{submission.studentCode}</p>
                          </div>
                          <div className="text-xs text-muted-foreground">
                            Submitted: {formatDateTime(submission.submittedAt)}
                          </div>
                        </div>
                        {submission.answerText && (
                          <p className="mt-2 text-sm text-muted-foreground">{submission.answerText}</p>
                        )}
                        {submission.attachmentAvailable && (
                          <Button
                            variant="ghost"
                            className="mt-2"
                            onClick={() =>
                              downloadWithAuth(
                                `/api/coursework/assignments/submissions/${submission.id}/file`,
                                `submission-${submission.id}`
                              ).catch((err) => setError(err.message))
                            }
                          >
                            Download Attachment
                          </Button>
                        )}

                        <div className="mt-3 grid gap-2 md:grid-cols-[120px_1fr_auto]">
                          <Input
                            placeholder="Marks"
                            value={gradeDrafts[submission.id]?.marks ?? submission.marks ?? ""}
                            onChange={(event) =>
                              setGradeDrafts((prev) => ({
                                ...prev,
                                [submission.id]: {
                                  marks: event.target.value,
                                  feedback: prev[submission.id]?.feedback ?? submission.feedback ?? ""
                                }
                              }))
                            }
                          />
                          <Input
                            placeholder="Feedback"
                            value={gradeDrafts[submission.id]?.feedback ?? submission.feedback ?? ""}
                            onChange={(event) =>
                              setGradeDrafts((prev) => ({
                                ...prev,
                                [submission.id]: {
                                  marks: prev[submission.id]?.marks ?? String(submission.marks ?? ""),
                                  feedback: event.target.value
                                }
                              }))
                            }
                          />
                          <Button
                            variant="secondary"
                            disabled={grading[submission.id]}
                            onClick={() => handleGrade(submission.id)}
                          >
                            {grading[submission.id] ? "Saving..." : "Save"}
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </CardContent>
      </Card>
    </div>
  );
}
