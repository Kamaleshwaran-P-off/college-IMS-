import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Upload, Trash2, ImagePlus, CheckCircle2, AlertCircle,
  Loader2, Images, Megaphone, Sparkles, Eye, X
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { API_BASE_URL, getAuthHeaders, getJson, readErrorMessage } from "@/lib/api";
import CarouselCropModal from "@/components/CarouselCropModal";

/* ─── Types ───────────────────────────────────────────────────────────── */
type CarouselItem = { id: number; url: string; createdAt: string };
type UploadState = "idle" | "uploading" | "success" | "error";

/* ─── Variants ────────────────────────────────────────────────────────── */
const heroVariants = {
  hidden: { opacity: 0, y: -20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5 } },
};

const cardVariants = {
  hidden: { opacity: 0, y: 24, scale: 0.97 },
  visible: (i: number) => ({
    opacity: 1, y: 0, scale: 1,
    transition: { delay: i * 0.1, duration: 0.45, type: "spring", stiffness: 110 },
  }),
};

const itemVariants = {
  hidden: { opacity: 0, scale: 0.92, y: 16 },
  visible: (i: number) => ({
    opacity: 1, scale: 1, y: 0,
    transition: { delay: i * 0.07, duration: 0.4, type: "spring", stiffness: 120 },
  }),
  exit: {
    opacity: 0, scale: 0.88, y: -10,
    transition: { duration: 0.28, ease: "easeIn" },
  },
};

const messageBanner = {
  hidden: { opacity: 0, y: -8, height: 0 },
  visible: { opacity: 1, y: 0, height: "auto", transition: { duration: 0.3 } },
  exit: { opacity: 0, y: -8, height: 0, transition: { duration: 0.22 } },
};

/* ─── Main Component ──────────────────────────────────────────────────── */
export default function AdminCarousel() {
  const [items, setItems] = useState<CarouselItem[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [cropSource, setCropSource] = useState<string | null>(null);
  const [status, setStatus] = useState<UploadState>("idle");
  const [message, setMessage] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [lightboxUrl, setLightboxUrl] = useState<string | null>(null);

  const authHeaders = getAuthHeaders();



  /* ── Load items ── */
  const loadItems = () => {
    getJson<CarouselItem[]>("/api/carousel")
      .then((data) => setItems(data))
      .catch(() => setItems([]));
  };

  useEffect(() => { loadItems(); }, []);

  /* ── Cleanup blob URLs ── */
  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      if (cropSource) URL.revokeObjectURL(cropSource);
    };
  }, [previewUrl, cropSource]);

  /* ── Upload ── */
  const uploadImage = async () => {
    if (!file) return;
    setStatus("uploading");
    setMessage(null);
    const formData = new FormData();
    formData.append("file", file);
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/carousel`, {
        method: "POST",
        headers: authHeaders,
        body: formData,
      });
      if (!response.ok) throw new Error(await readErrorMessage(response));
      setStatus("success");
      setMessage("Carousel image uploaded successfully.");
      setFile(null);
      setPreviewUrl(null);
      loadItems();
    } catch (error) {
      setStatus("error");
      setMessage(error instanceof Error ? error.message : "Upload failed");
    }
  };

  /* ── Delete ── */
  const deleteImage = async (id: number) => {
    setDeletingId(id);
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/carousel/${id}`, {
        method: "DELETE",
        headers: authHeaders,
      });
      if (!response.ok) throw new Error(await readErrorMessage(response));
      setItems((prev) => prev.filter((item) => item.id !== id));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Delete failed");
      setStatus("error");
    } finally {
      setDeletingId(null);
    }
  };

  /* ── Fallback SVG for broken images ── */
  const fallbackSvg =
    "data:image/svg+xml;utf8," +
    "<svg xmlns='http://www.w3.org/2000/svg' width='800' height='400'>" +
    "<rect width='100%' height='100%' fill='%23f1f5f9'/>" +
    "<text x='50%' y='50%' font-size='20' fill='%2394a3b8' text-anchor='middle' dominant-baseline='middle'>Image unavailable</text>" +
    "</svg>";

  return (
    <>
      {/* ── Crop Modal ── */}
      <CarouselCropModal
        open={Boolean(cropSource)}
        imageSrc={cropSource}
        onCancel={() => {
          if (cropSource) URL.revokeObjectURL(cropSource);
          setCropSource(null);
        }}
        onComplete={(croppedFile, croppedPreview) => {
          if (previewUrl) URL.revokeObjectURL(previewUrl);
          if (cropSource) URL.revokeObjectURL(cropSource);
          setFile(croppedFile);
          setPreviewUrl(croppedPreview);
          setCropSource(null);
        }}
      />

      {/* ── Lightbox ── */}
      <AnimatePresence>
        {lightboxUrl && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onClick={() => setLightboxUrl(null)}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 cursor-zoom-out"
          >
            <motion.div
              initial={{ scale: 0.85, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.85, opacity: 0 }}
              transition={{ type: "spring", stiffness: 200, damping: 22 }}
              onClick={(e) => e.stopPropagation()}
              className="relative max-w-4xl w-full"
            >
              <button
                onClick={() => setLightboxUrl(null)}
                className="absolute -top-3 -right-3 z-10 w-8 h-8 rounded-full bg-white dark:bg-slate-800 border border-slate-200 dark:border-white/15 flex items-center justify-center shadow-lg hover:scale-110 transition-transform"
              >
                <X className="w-4 h-4 text-slate-600 dark:text-slate-300" />
              </button>
              <img
                src={lightboxUrl}
                alt="Preview"
                className="w-full rounded-2xl border border-white/20 shadow-2xl object-cover"
              />
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="space-y-6 pb-10">

        {/* ── Hero header ── */}
        <motion.div variants={heroVariants} initial="hidden" animate="visible"
          className="relative overflow-hidden rounded-2xl
            bg-gradient-to-br from-violet-600 via-purple-600 to-fuchsia-600
            dark:from-violet-700 dark:via-purple-700 dark:to-fuchsia-700
            p-6 shadow-lg shadow-violet-500/20 dark:shadow-violet-900/30"
        >
          <div className="absolute -top-10 -right-10 w-48 h-48 rounded-full bg-white/10 blur-3xl pointer-events-none" />
          <div className="absolute -bottom-8 -left-8 w-36 h-36 rounded-full bg-fuchsia-300/20 blur-2xl pointer-events-none" />
          <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Megaphone className="w-4 h-4 text-violet-200" />
                <p className="text-violet-200 text-sm font-medium">Carousel Management</p>
              </div>
              <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
                Global Announcement Banner
              </h1>
              <p className="text-violet-200/80 text-sm mt-1">
                Upload images shown on the dashboard carousel for all users.
              </p>
            </div>
            <div className="flex items-center gap-3 bg-white/15 backdrop-blur-sm border border-white/20 rounded-xl px-4 py-2.5 shrink-0">
              <Images className="w-4 h-4 text-fuchsia-200" />
              <span className="text-white text-sm font-semibold">{items.length} image{items.length !== 1 ? "s" : ""}</span>
            </div>
          </div>
        </motion.div>

        {/* ── Upload card ── */}
        <motion.div custom={0} variants={cardVariants} initial="hidden" animate="visible">
          <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm hover:shadow-md dark:shadow-none transition-shadow duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-600 flex items-center justify-center shadow-sm">
                  <ImagePlus className="w-4 h-4 text-white" />
                </div>
                <div>
                  <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Upload Carousel Image</CardTitle>
                  <CardDescription className="text-xs mt-0.5">Recommended size: 1200 × 400px</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">

              {/* File drop zone */}
              <label className="
                relative flex flex-col items-center justify-center gap-2
                rounded-xl border-2 border-dashed cursor-pointer
                border-slate-200 dark:border-white/15
                hover:border-violet-400 dark:hover:border-violet-400/50
                bg-slate-50/50 dark:bg-white/[0.03]
                hover:bg-violet-50/50 dark:hover:bg-violet-500/5
                p-6 transition-all duration-200 group
              ">
                <input
                  type="file"
                  accept="image/*"
                  className="sr-only"
                  onChange={(e) => {
                    const selected = e.target.files?.[0] || null;
                    if (!selected) return;
                    const src = URL.createObjectURL(selected);
                    setCropSource(src);
                  }}
                />
                <motion.div
                  animate={{ y: [0, -4, 0] }}
                  transition={{ repeat: Infinity, duration: 2.5, ease: "easeInOut" }}
                  className="w-12 h-12 rounded-2xl bg-violet-100 dark:bg-violet-500/15 flex items-center justify-center group-hover:bg-violet-200 dark:group-hover:bg-violet-500/25 transition-colors"
                >
                  <Upload className="w-5 h-5 text-violet-500 dark:text-violet-400" />
                </motion.div>
                <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">Click to choose an image</p>
                <p className="text-xs text-slate-400 dark:text-slate-500">PNG, JPG, WEBP supported</p>
              </label>

              {/* Image preview */}
              <AnimatePresence>
                {previewUrl && (
                  <motion.div
                    initial={{ opacity: 0, scale: 0.95, height: 0 }}
                    animate={{ opacity: 1, scale: 1, height: "auto" }}
                    exit={{ opacity: 0, scale: 0.95, height: 0 }}
                    transition={{ duration: 0.35, type: "spring", stiffness: 140 }}
                    className="relative overflow-hidden rounded-xl"
                  >
                    <img
                      src={previewUrl}
                      alt="Preview"
                      className="h-44 w-full rounded-xl border border-slate-200 dark:border-white/10 object-cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent rounded-xl flex items-end p-3">
                      <span className="text-white text-xs font-medium bg-black/40 backdrop-blur-sm px-2 py-1 rounded-lg flex items-center gap-1">
                        <CheckCircle2 className="w-3 h-3 text-emerald-400" /> Ready to upload
                      </span>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>

              {/* Upload button */}
              <motion.button
                type="button"
                disabled={!file || status === "uploading"}
                onClick={uploadImage}
                whileHover={{ scale: file && status !== "uploading" ? 1.02 : 1 }}
                whileTap={{ scale: file && status !== "uploading" ? 0.97 : 1 }}
                className="
                  w-full flex items-center justify-center gap-2
                  rounded-xl px-4 py-2.5 text-sm font-semibold text-white
                  bg-gradient-to-r from-violet-500 to-fuchsia-600
                  hover:opacity-90 hover:shadow-lg hover:shadow-violet-500/25
                  disabled:opacity-40 disabled:cursor-not-allowed disabled:shadow-none
                  transition-all duration-200
                "
              >
                {status === "uploading"
                  ? <><Loader2 className="w-4 h-4 animate-spin" /> Uploading...</>
                  : <><Upload className="w-4 h-4" /> Upload Image</>
                }
              </motion.button>

              {/* Status message */}
              <AnimatePresence>
                {message && (
                  <motion.div
                    variants={messageBanner}
                    initial="hidden"
                    animate="visible"
                    exit="exit"
                    className={`flex items-center gap-2 rounded-xl px-3 py-2.5 text-xs font-medium overflow-hidden ${
                      status === "error"
                        ? "bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 border border-red-200/60 dark:border-red-500/20"
                        : "bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-200/60 dark:border-emerald-500/20"
                    }`}
                  >
                    {status === "error"
                      ? <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                      : <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                    }
                    {message}
                  </motion.div>
                )}
              </AnimatePresence>
            </CardContent>
          </Card>
        </motion.div>

        {/* ── Gallery card ── */}
        <motion.div custom={1} variants={cardVariants} initial="hidden" animate="visible">
          <Card className="bg-white dark:bg-white/[0.06] border border-slate-200/80 dark:border-white/10 shadow-sm hover:shadow-md dark:shadow-none transition-shadow duration-300">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-fuchsia-500 to-pink-600 flex items-center justify-center shadow-sm">
                    <Images className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-sm text-slate-800 dark:text-slate-100">Uploaded Images</CardTitle>
                    <CardDescription className="text-xs mt-0.5">Click preview to expand • Delete to remove</CardDescription>
                  </div>
                </div>
                {items.length > 0 && (
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-violet-50 dark:bg-violet-500/15 text-violet-700 dark:text-violet-300 border border-violet-200/60 dark:border-violet-400/20">
                    {items.length} total
                  </span>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {items.length === 0 ? (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="flex flex-col items-center justify-center gap-3 py-12 rounded-xl border-2 border-dashed border-slate-200 dark:border-white/10"
                >
                  <div className="w-14 h-14 rounded-2xl bg-slate-100 dark:bg-white/[0.06] flex items-center justify-center">
                    <Images className="w-6 h-6 text-slate-400 dark:text-slate-500" />
                  </div>
                  <p className="text-sm font-medium text-slate-500 dark:text-slate-400">No images uploaded yet</p>
                  <p className="text-xs text-slate-400 dark:text-slate-500">Upload your first carousel image above</p>
                </motion.div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <AnimatePresence mode="popLayout">
                    {items.map((item, i) => (
                      <motion.div
                        key={item.id}
                        layout
                        custom={i}
                        variants={itemVariants}
                        initial="hidden"
                        animate="visible"
                        exit="exit"
                        className="group relative rounded-2xl overflow-hidden
                          border border-slate-100 dark:border-white/8
                          hover:border-violet-200 dark:hover:border-violet-400/25
                          bg-slate-50 dark:bg-white/[0.04]
                          shadow-sm hover:shadow-md dark:shadow-none
                          transition-all duration-300"
                      >
                        {/* Image */}
                        <div className="relative overflow-hidden">
                          <img
                            src={`${API_BASE_URL}${item.url}`}
                            alt={`Carousel ${item.id}`}
                            className="h-40 w-full object-cover transition-transform duration-500 group-hover:scale-105"
                            loading="lazy"
                            onError={(e) => { e.currentTarget.src = fallbackSvg; }}
                          />

                          {/* Hover overlay */}
                          <motion.div
                            initial={{ opacity: 0 }}
                            whileHover={{ opacity: 1 }}
                            className="absolute inset-0 bg-black/40 backdrop-blur-[2px] flex items-center justify-center gap-2"
                          >
                            <button
                              onClick={() => setLightboxUrl(`${API_BASE_URL}${item.url}`)}
                              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white/90 dark:bg-white/15 text-slate-800 dark:text-white text-xs font-semibold hover:bg-white transition-colors"
                            >
                              <Eye className="w-3.5 h-3.5" /> Preview
                            </button>
                          </motion.div>
                        </div>

                        {/* Footer */}
                        <div className="flex items-center justify-between px-3 py-2.5">
                          <div>
                            <p className="text-[10px] text-slate-400 dark:text-slate-500 font-mono">
                              ID #{item.id}
                            </p>
                            {item.createdAt && (
                              <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-0.5">
                                {new Date(item.createdAt).toLocaleDateString()}
                              </p>
                            )}
                          </div>

                          <motion.button
                            type="button"
                            onClick={() => deleteImage(item.id)}
                            disabled={deletingId === item.id}
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl text-xs font-semibold
                              border border-red-200 dark:border-red-500/25
                              text-red-600 dark:text-red-400
                              bg-red-50/80 dark:bg-red-500/10
                              hover:bg-red-100 dark:hover:bg-red-500/20
                              disabled:opacity-50 disabled:cursor-not-allowed
                              transition-all duration-150"
                          >
                            {deletingId === item.id
                              ? <Loader2 className="w-3 h-3 animate-spin" />
                              : <Trash2 className="w-3 h-3" />
                            }
                            {deletingId === item.id ? "Deleting" : "Delete"}
                          </motion.button>
                        </div>
                      </motion.div>
                    ))}
                  </AnimatePresence>
                </div>
              )}
            </CardContent>
          </Card>
        </motion.div>

      </div>
    </>
  );
}

/* ── Fallback SVG (module-level so it's not recreated on render) ── */
const fallbackSvg =
  "data:image/svg+xml;utf8," +
  "<svg xmlns='http://www.w3.org/2000/svg' width='800' height='400'>" +
  "<rect width='100%' height='100%' fill='%23f1f5f9'/>" +
  "<text x='50%' y='50%' font-size='20' fill='%2394a3b8' text-anchor='middle' dominant-baseline='middle'>Image unavailable</text>" +
  "</svg>";
