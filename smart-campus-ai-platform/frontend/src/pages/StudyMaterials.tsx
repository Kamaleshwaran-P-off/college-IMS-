import { useEffect, useState } from "react";
import { API_BASE_URL, getAuthHeaders, getAuthToken, getJson, readErrorMessage } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import TableSkeleton from "@/components/skeletons/TableSkeleton";
import EmptyState from "@/components/feedback/EmptyState";
import { useAuth } from "@/context/AuthContext";

type StudyMaterial = {
  id: number;
  title: string;
  description?: string | null;
  department?: string | null;
  className?: string | null;
  uploadedBy?: string | null;
  uploadedAt: string;
  updatedAt?: string | null;
  attachmentAvailable: boolean;
  visible?: boolean;
};

const formatDateTime = (value?: string | null) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
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

export default function StudyMaterials() {
  const { role } = useAuth();
  const normalizedRole = (role || "STUDENT").toUpperCase();
  const isFaculty = normalizedRole === "FACULTY" || normalizedRole === "STAFF";

  const [materials, setMaterials] = useState<StudyMaterial[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [assignedClasses, setAssignedClasses] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [department, setDepartment] = useState("");
  const [className, setClassName] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editDrafts, setEditDrafts] = useState<Record<number, {
    title: string;
    description: string;
    department: string;
    className: string;
    file: File | null;
  }>>({});
  const [savingEdit, setSavingEdit] = useState<Record<number, boolean>>({});

  const loadMaterials = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJson<StudyMaterial[]>("/api/coursework/materials");
      setMaterials(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load materials.");
    } finally {
      setLoading(false);
    }
  };

  const loadAssignedClasses = async () => {
    if (!isFaculty) return;
    try {
      const data = await getJson<string[]>("/api/faculty/classes");
      setAssignedClasses(data);
      if (data.length > 0) {
        setClassName(data[0]);
      }
    } catch {
      setAssignedClasses([]);
    }
  };

  useEffect(() => {
    loadMaterials();
    loadAssignedClasses();
  }, []);

  const handleUpload = async () => {
    if (!title.trim()) {
      setError("Title is required.");
      return;
    }
    if (!file) {
      setError("Please attach a file to upload.");
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append("title", title.trim());
      if (description.trim()) formData.append("description", description.trim());
      if (department.trim()) formData.append("department", department.trim());
      if (className.trim()) formData.append("className", className.trim());
      formData.append("file", file);

      const response = await fetch(`${API_BASE_URL}/api/coursework/materials`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: formData
      });

      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      setTitle("");
      setDescription("");
      setDepartment("");
      setFile(null);
      await loadMaterials();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed.");
    } finally {
      setUploading(false);
    }
  };

  const startEdit = (material: StudyMaterial) => {
    setEditingId(material.id);
    setEditDrafts((prev) => ({
      ...prev,
      [material.id]: {
        title: material.title || "",
        description: material.description || "",
        department: material.department || "",
        className: material.className || className || "",
        file: null
      }
    }));
  };

  const cancelEdit = () => {
    setEditingId(null);
  };

  const saveEdit = async (materialId: number) => {
    const draft = editDrafts[materialId];
    if (!draft?.title?.trim()) {
      setError("Title is required.");
      return;
    }

    setSavingEdit((prev) => ({ ...prev, [materialId]: true }));
    setError(null);
    try {
      const formData = new FormData();
      formData.append("title", draft.title.trim());
      if (draft.description.trim()) formData.append("description", draft.description.trim());
      if (draft.department.trim()) formData.append("department", draft.department.trim());
      if (draft.className.trim()) formData.append("className", draft.className.trim());
      if (draft.file) formData.append("file", draft.file);

      const response = await fetch(`${API_BASE_URL}/api/coursework/materials/${materialId}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: formData
      });
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      setEditingId(null);
      await loadMaterials();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update material.");
    } finally {
      setSavingEdit((prev) => ({ ...prev, [materialId]: false }));
    }
  };

  const toggleVisibility = async (materialId: number, current?: boolean) => {
    setError(null);
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/coursework/materials/${materialId}/hide?visible=${!current}`,
        {
          method: "PATCH",
          headers: getAuthHeaders()
        }
      );
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }
      await loadMaterials();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update visibility.");
    }
  };

  const deleteMaterial = async (materialId: number) => {
    if (!window.confirm("Delete this material permanently?")) return;
    setError(null);
    try {
      const response = await fetch(`${API_BASE_URL}/api/coursework/materials/${materialId}`, {
        method: "DELETE",
        headers: getAuthHeaders()
      });
      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }
      await loadMaterials();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete material.");
    }
  };

  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>Resource Hub</CardDescription>
          <CardTitle>Study Materials</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Access curated notes, PDFs, and lecture resources.
        </CardContent>
      </Card>

      {error && (
        <div className="rounded-2xl border border-rose-200/60 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/10 dark:text-rose-200">
          {error}
        </div>
      )}

      {isFaculty && (
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Upload Material</CardTitle>
            <CardDescription>Share PDFs and class notes.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-3 md:grid-cols-2">
              <div className="space-y-2">
                <Label>Title</Label>
                <Input value={title} onChange={(event) => setTitle(event.target.value)} />
              </div>
              <div className="space-y-2">
                <Label>Department (optional)</Label>
                <Input value={department} onChange={(event) => setDepartment(event.target.value)} />
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>Description</Label>
                <textarea
                  className="h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Class (optional)</Label>
                {assignedClasses.length > 0 ? (
                  <select
                    className="h-10 w-full rounded-md border border-input bg-transparent px-3 text-sm"
                    value={className}
                    onChange={(event) => setClassName(event.target.value)}
                  >
                    <option value="">All Classes</option>
                    {assignedClasses.map((cls) => (
                      <option key={cls} value={cls}>
                        {cls}
                      </option>
                    ))}
                  </select>
                ) : (
                  <Input value={className} onChange={(event) => setClassName(event.target.value)} />
                )}
              </div>
              <div className="space-y-2 md:col-span-2">
                <Label>File</Label>
                <Input type="file" onChange={(event) => setFile(event.target.files?.[0] || null)} />
              </div>
            </div>
            <Button disabled={uploading} onClick={handleUpload}>
              {uploading ? "Uploading..." : "Upload Material"}
            </Button>
          </CardContent>
        </Card>
      )}

      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardTitle>Available Materials</CardTitle>
          <CardDescription>{materials.length} resources available.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {loading && <TableSkeleton rows={4} columns={2} />}
          {!loading && materials.length === 0 && (
            <EmptyState
              title="No study materials yet"
              description="Faculty uploads will appear here once published."
            />
          )}
          {materials.map((material) => (
            (() => {
              const isSample = material.id < 0;
              const visible = material.visible ?? true;
              const isEditing = editingId === material.id;
              return (
            <div
              key={material.id}
              className="rounded-2xl border border-border/60 bg-white/70 p-4 dark:bg-white/5"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-base font-semibold text-foreground">{material.title}</p>
                  {!visible && (
                    <p className="mt-1 text-xs font-semibold text-rose-600">Hidden from students</p>
                  )}
                  {material.description && (
                    <p className="mt-2 text-sm text-muted-foreground">{material.description}</p>
                  )}
                  <p className="mt-2 text-xs text-muted-foreground">
                    Uploaded by {material.uploadedBy || "Faculty"} on {formatDateTime(material.uploadedAt)}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {material.className ? `Class: ${material.className}` : "Available to all classes"}
                  </p>
                </div>
                {material.attachmentAvailable && (
                  <Button
                    variant="secondary"
                    disabled={isSample}
                    onClick={() =>
                      downloadWithAuth(`/api/coursework/materials/${material.id}/file`, `material-${material.id}`).catch(
                        (err) => setError(err.message)
                      )
                    }
                  >
                    Download
                  </Button>
                )}
                {isFaculty && (
                  <div className="flex flex-wrap gap-2">
                    <Button variant="outline" onClick={() => startEdit(material)} disabled={isSample}>
                      Edit
                    </Button>
                    <Button
                      variant="secondary"
                      onClick={() => toggleVisibility(material.id, visible)}
                      disabled={isSample}
                    >
                      {visible ? "Hide" : "Unhide"}
                    </Button>
                    <Button variant="destructive" onClick={() => deleteMaterial(material.id)} disabled={isSample}>
                      Delete
                    </Button>
                  </div>
                )}
              </div>

              {isFaculty && isEditing && (
                <div className="mt-4 rounded-xl border border-border/60 bg-slate-50/60 p-4 dark:bg-white/10">
                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-1">
                      <Label>Title</Label>
                      <Input
                        value={editDrafts[material.id]?.title || ""}
                        onChange={(event) =>
                          setEditDrafts((prev) => ({
                            ...prev,
                            [material.id]: {
                              ...prev[material.id],
                              title: event.target.value
                            }
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-1">
                      <Label>Department (optional)</Label>
                      <Input
                        value={editDrafts[material.id]?.department || ""}
                        onChange={(event) =>
                          setEditDrafts((prev) => ({
                            ...prev,
                            [material.id]: {
                              ...prev[material.id],
                              department: event.target.value
                            }
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-1 md:col-span-2">
                      <Label>Description</Label>
                      <textarea
                        className="h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                        value={editDrafts[material.id]?.description || ""}
                        onChange={(event) =>
                          setEditDrafts((prev) => ({
                            ...prev,
                            [material.id]: {
                              ...prev[material.id],
                              description: event.target.value
                            }
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-1">
                      <Label>Class (optional)</Label>
                      <Input
                        value={editDrafts[material.id]?.className || ""}
                        onChange={(event) =>
                          setEditDrafts((prev) => ({
                            ...prev,
                            [material.id]: {
                              ...prev[material.id],
                              className: event.target.value
                            }
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-1 md:col-span-2">
                      <Label>Replace File (optional)</Label>
                      <Input
                        type="file"
                        onChange={(event) =>
                          setEditDrafts((prev) => ({
                            ...prev,
                            [material.id]: {
                              ...prev[material.id],
                              file: event.target.files?.[0] || null
                            }
                          }))
                        }
                      />
                    </div>
                  </div>
                  <div className="mt-3 flex gap-2">
                    <Button
                      onClick={() => saveEdit(material.id)}
                      disabled={savingEdit[material.id]}
                    >
                      {savingEdit[material.id] ? "Saving..." : "Save"}
                    </Button>
                    <Button variant="ghost" onClick={cancelEdit}>
                      Cancel
                    </Button>
                  </div>
                </div>
              )}
            </div>
              );
            })()
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
