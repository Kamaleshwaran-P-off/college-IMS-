import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { API_BASE_URL, getJson, postForm, postJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

type StudentProfile = {
  registerNumber?: string | null;
  phone?: string | null;
  department?: string | null;
  section?: string | null;
  interestedSkills?: string[];
};

type FacultyProfile = {
  staffId?: string | null;
  department?: string | null;
  skills?: string[];
  experienceYears?: number | null;
  bio?: string | null;
};

type UserProfile = {
  id: number;
  name: string;
  email: string;
  role: string;
  profileImageUrl?: string | null;
  student?: StudentProfile | null;
  faculty?: FacultyProfile | null;
};

const toFriendlyRole = (raw: string | null) => {
  const normalized = (raw || "").replace("ROLE_", "").toUpperCase();
  if (normalized === "ADMIN") return "Admin";
  if (normalized === "FACULTY" || normalized === "STAFF") return "Faculty";
  if (normalized === "STUDENT") return "Student";
  return "Guest";
};

export default function Profile() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  const [form, setForm] = useState({
    name: "",
    registerNumber: "",
    phone: "",
    department: "",
    section: "",
    interestedSkills: "",
    staffId: "",
    skills: "",
    experienceYears: "",
    bio: ""
  });

  const { role: authRole, refreshProfile } = useAuth();
  const role = useMemo(() => profile?.role || authRole, [profile, authRole]);
  const normalizedRole = (role || "").replace("ROLE_", "").toUpperCase();
  const isStudent = normalizedRole === "STUDENT";
  const isFaculty = ["FACULTY", "STAFF"].includes(normalizedRole);
  const incompleteProfile = useMemo(() => {
    if (isStudent) {
      return !form.registerNumber || !form.department || !form.section;
    }
    if (isFaculty) {
      return !form.staffId || !form.department || !form.skills;
    }
    return false;
  }, [form, isFaculty, isStudent]);

  const loadProfile = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJson<UserProfile>("/api/user/profile");
      setProfile(data);
      if (data.profileImageUrl) {
        const token = localStorage.getItem("authToken") || localStorage.getItem("token");
        fetch(`${API_BASE_URL}${data.profileImageUrl}`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        })
          .then((response) => (response.ok ? response.blob() : null))
          .then((blob) => {
            if (!blob) return;
            const url = URL.createObjectURL(blob);
            setImagePreview(url);
          })
          .catch(() => null);
      } else {
        setImagePreview(null);
      }
      setForm({
        name: data.name || "",
        registerNumber: data.student?.registerNumber || "",
        phone: data.student?.phone || "",
        department: data.student?.department || data.faculty?.department || "",
        section: data.student?.section || "",
        interestedSkills: data.student?.interestedSkills?.join(", ") || "",
        staffId: data.faculty?.staffId || "",
        skills: data.faculty?.skills?.join(", ") || "",
        experienceYears: data.faculty?.experienceYears?.toString() || "",
        bio: data.faculty?.bio || ""
      });
      localStorage.setItem("name", data.name);
      localStorage.setItem("userName", data.name);
      localStorage.setItem("email", data.email);
      localStorage.setItem("userEmail", data.email);
      localStorage.setItem("role", data.role);
      refreshProfile();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load profile.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview);
      }
    };
  }, [imagePreview]);

  const updateProfile = async () => {
    setSaving(true);
    setError(null);
    setMessage(null);
    try {
      const payload = {
        name: form.name,
        registerNumber: isStudent ? form.registerNumber : null,
        phone: form.phone,
        department: form.department,
        section: isStudent ? form.section : null,
        interestedSkills: isStudent
          ? form.interestedSkills.split(",").map((s) => s.trim()).filter(Boolean)
          : null,
        staffId: isFaculty ? form.staffId : null,
        skills: isFaculty
          ? form.skills.split(",").map((s) => s.trim()).filter(Boolean)
          : null,
        experienceYears: isFaculty && form.experienceYears ? Number(form.experienceYears) : null,
        bio: isFaculty ? form.bio : null
      };
      const data = await postJson<UserProfile>("/api/user/profile", payload);
      setProfile(data);
      setMessage("Profile updated successfully.");
      localStorage.setItem("name", data.name);
      localStorage.setItem("userName", data.name);
      localStorage.setItem("email", data.email);
      localStorage.setItem("userEmail", data.email);
      localStorage.setItem("role", data.role);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update profile.");
    } finally {
      setSaving(false);
    }
  };

  const uploadImage = async () => {
    if (!imageFile) return;
    setError(null);
    setMessage(null);
    try {
      const formData = new FormData();
      formData.append("file", imageFile);
      const response = await postForm<{ url: string }>("/api/user/profile/image", formData);
      setProfile((prev) =>
        prev
          ? {
              ...prev,
              profileImageUrl: response.url
            }
          : prev
      );
      if (response.url) {
        const token = localStorage.getItem("authToken") || localStorage.getItem("token");
        const blobResponse = await fetch(`${API_BASE_URL}${response.url}`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        if (blobResponse.ok) {
          const blob = await blobResponse.blob();
          const url = URL.createObjectURL(blob);
          setImagePreview(url);
        }
      }
      setImageFile(null);
      setMessage("Profile image updated.");
      refreshProfile();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to upload image.");
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Loading profile...</CardTitle>
          </CardHeader>
        </Card>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="space-y-6">
        <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
          <CardHeader>
            <CardTitle>Profile unavailable</CardTitle>
            <CardDescription>{error || "Unable to load profile data."}</CardDescription>
          </CardHeader>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Card className="bg-white/80 backdrop-blur dark:bg-white/10">
        <CardHeader>
          <CardDescription>Account</CardDescription>
          <CardTitle>Profile</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm text-muted-foreground">
          {error && <p className="text-sm text-rose-600 dark:text-rose-200">{error}</p>}
          {message && <p className="text-sm text-emerald-600 dark:text-emerald-200">{message}</p>}
          {incompleteProfile && (
            <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-2 text-xs text-amber-700 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200">
              Complete your profile to unlock better mentor matching and recommendations.
            </div>
          )}
          <div className="flex flex-wrap items-center gap-4">
            {imagePreview ? (
              <img
                src={imagePreview}
                alt="Profile"
                className="h-16 w-16 rounded-full border border-border object-cover"
              />
            ) : (
              <div className="flex h-16 w-16 items-center justify-center rounded-full border border-border bg-slate-900/10 text-sm font-semibold text-slate-900 dark:bg-white/10 dark:text-white">
                {profile.name
                  .split(" ")
                  .map((part) => part[0])
                  .join("")
                  .slice(0, 2)
                  .toUpperCase()}
              </div>
            )}
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-500">Signed in as</p>
              <p className="text-base font-semibold text-slate-900 dark:text-white">
                {profile.name} ({toFriendlyRole(profile.role)})
              </p>
              <p className="text-sm text-muted-foreground">{profile.email}</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Input
              type="file"
              accept="image/*"
              onChange={(event) => setImageFile(event.target.files?.[0] || null)}
              className="max-w-sm"
            />
            <Button type="button" onClick={uploadImage} disabled={!imageFile}>
              Upload Image
            </Button>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <p className="text-xs uppercase tracking-wide text-slate-500">Full Name</p>
              <Input value={form.name} onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <p className="text-xs uppercase tracking-wide text-slate-500">Email</p>
              <Input value={profile.email} disabled />
            </div>

            {isStudent && (
              <>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Register Number</p>
                  <Input
                    value={form.registerNumber}
                    onChange={(e) => setForm((prev) => ({ ...prev, registerNumber: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Phone</p>
                  <Input value={form.phone} onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))} />
                </div>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Department</p>
                  <Input
                    value={form.department}
                    onChange={(e) => setForm((prev) => ({ ...prev, department: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Section</p>
                  <Input
                    value={form.section}
                    onChange={(e) => setForm((prev) => ({ ...prev, section: e.target.value }))}
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Interested Skills</p>
                  <Input
                    placeholder="AI, Web Dev, Design"
                    value={form.interestedSkills}
                    onChange={(e) => setForm((prev) => ({ ...prev, interestedSkills: e.target.value }))}
                  />
                </div>
              </>
            )}

            {isFaculty && (
              <>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Staff ID</p>
                  <Input
                    value={form.staffId}
                    onChange={(e) => setForm((prev) => ({ ...prev, staffId: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Department</p>
                  <Input
                    value={form.department}
                    onChange={(e) => setForm((prev) => ({ ...prev, department: e.target.value }))}
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Skills</p>
                  <Input
                    placeholder="Mentoring, AI, Data Science"
                    value={form.skills}
                    onChange={(e) => setForm((prev) => ({ ...prev, skills: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Experience (years)</p>
                  <Input
                    type="number"
                    value={form.experienceYears}
                    onChange={(e) => setForm((prev) => ({ ...prev, experienceYears: e.target.value }))}
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <p className="text-xs uppercase tracking-wide text-slate-500">Bio</p>
                  <textarea
                    className="h-24 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={form.bio}
                    onChange={(e) => setForm((prev) => ({ ...prev, bio: e.target.value }))}
                  />
                </div>
              </>
            )}
          </div>

          <div className="flex items-center gap-3">
            <Button type="button" onClick={updateProfile} disabled={saving}>
              {saving ? "Saving..." : "Save Profile"}
            </Button>
            <span className="text-xs text-muted-foreground">
              Complete your profile to improve mentor matching results.
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
