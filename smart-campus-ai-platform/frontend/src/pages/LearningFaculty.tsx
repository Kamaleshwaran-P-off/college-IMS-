import { useEffect, useState, type FormEvent } from "react";
import { UploadCloud } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import LottieAnimation from "@/components/lottie/LottieAnimation";
import CardSkeleton from "@/components/skeletons/CardSkeleton";
import { getJson, postForm } from "@/lib/api";

interface CourseSummary {
  id: number;
  title: string;
  description?: string;
  createdAt: string;
  topicCount: number;
}

interface UploadResponse {
  courseId: number;
  topicCount: number;
}

export default function LearningFaculty() {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchCourses = async () => {
    setLoading(true);
    try {
      const response = await getJson<CourseSummary[]>("/api/faculty/learning/courses");
      setCourses(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load courses");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!file) {
      setError("Please upload a PDF file.");
      return;
    }

    setUploading(true);
    setMessage(null);
    setError(null);

    const formData = new FormData();
    formData.append("title", title);
    formData.append("description", description);
    formData.append("file", file);

    try {
      const response = await postForm<UploadResponse>("/api/faculty/learning/courses", formData);
      setMessage(`Course created. ${response.topicCount} topics generated.`);
      setTitle("");
      setDescription("");
      setFile(null);
      await fetchCourses();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="min-h-screen bg-campus px-6 py-10">
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">Faculty Studio</p>
          <h2 className="text-2xl font-semibold">AI Learning Flow</h2>
          <p className="mt-1 text-sm text-muted-foreground">Upload PDFs and let AI build a structured learning path.</p>
        </div>

        <Card className="space-y-4 p-6">
          <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-[1fr_1fr]">
            <div className="space-y-2">
              <label className="text-sm font-medium">Course title</label>
              <Input value={title} onChange={(event) => setTitle(event.target.value)} required />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Description</label>
              <Input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Optional" />
            </div>
            <div className="md:col-span-2 space-y-2">
              <label className="text-sm font-medium">PDF upload</label>
              <Input type="file" accept="application/pdf" onChange={(event) => setFile(event.target.files?.[0] || null)} />
            </div>
            <div className="md:col-span-2 flex flex-wrap items-center gap-3">
              <Button type="submit" disabled={uploading} className="gap-2">
                <UploadCloud size={16} /> {uploading ? "Analyzing..." : "Upload & Generate"}
              </Button>
              {message && <span className="text-sm text-emerald-600">{message}</span>}
              {error && <span className="text-sm text-red-600">{error}</span>}
            </div>
          </form>
        </Card>

        <div className="grid gap-4 md:grid-cols-2">
          {loading && Array.from({ length: 4 }).map((_, index) => <CardSkeleton key={`loading-${index}`} />)}
          {!loading && courses.length === 0 && (
            <Card className="flex flex-col items-center justify-center gap-3 p-8 text-center">
              <LottieAnimation src="/lottie/empty-state.json" className="h-40 w-40" />
              <p className="text-sm text-muted-foreground">No courses uploaded yet. Start with your first PDF.</p>
            </Card>
          )}
          {courses.map((course) => (
            <Card key={course.id} className="space-y-2 p-5">
              <p className="text-xs uppercase tracking-[0.3em] text-muted-foreground">{new Date(course.createdAt).toLocaleDateString()}</p>
              <h3 className="text-lg font-semibold">{course.title}</h3>
              <p className="text-sm text-muted-foreground">{course.description || "No description provided."}</p>
              <p className="text-xs text-muted-foreground">Topics generated: {course.topicCount}</p>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
