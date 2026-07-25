import { useState } from "react";
import Cropper from "react-easy-crop";
import { Button } from "@/components/ui/button";
import { getCroppedImage } from "@/lib/imageCrop";

type CarouselCropModalProps = {
  open: boolean;
  imageSrc: string | null;
  onCancel: () => void;
  onComplete: (file: File, previewUrl: string) => void;
};

export default function CarouselCropModal({ open, imageSrc, onCancel, onComplete }: CarouselCropModalProps) {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [aspect, setAspect] = useState(16 / 9);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<{
    x: number;
    y: number;
    width: number;
    height: number;
  } | null>(null);
  const [processing, setProcessing] = useState(false);

  if (!open || !imageSrc) {
    return null;
  }

  const handleComplete = async () => {
    if (!croppedAreaPixels) return;
    setProcessing(true);
    try {
      const targetWidth = aspect === 1 ? 800 : 1200;
      const targetHeight = aspect === 1 ? 800 : 675;
      const blob = await getCroppedImage(imageSrc, croppedAreaPixels, targetWidth, targetHeight);
      const file = new File([blob], "carousel.jpg", { type: blob.type });
      const previewUrl = URL.createObjectURL(blob);
      onComplete(file, previewUrl);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4">
      <div className="w-full max-w-3xl rounded-2xl bg-white p-4 shadow-2xl">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <p className="text-sm font-semibold text-slate-800">Crop Carousel Image</p>
            <p className="text-xs text-slate-500">Adjust framing before upload.</p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setAspect(16 / 9)}>
              16:9
            </Button>
            <Button variant="outline" onClick={() => setAspect(1)}>
              1:1
            </Button>
          </div>
        </div>

        <div className="relative mt-4 h-72 w-full overflow-hidden rounded-xl bg-slate-100">
          <Cropper
            image={imageSrc}
            crop={crop}
            zoom={zoom}
            aspect={aspect}
            onCropChange={setCrop}
            onZoomChange={setZoom}
            onCropComplete={(_, croppedPixels) => setCroppedAreaPixels(croppedPixels)}
          />
        </div>

        <div className="mt-4 flex items-center gap-3">
          <label className="text-xs text-slate-500">Zoom</label>
          <input
            type="range"
            min={1}
            max={3}
            step={0.1}
            value={zoom}
            onChange={(event) => setZoom(Number(event.target.value))}
            className="flex-1"
          />
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button onClick={handleComplete} disabled={processing}>
            {processing ? "Processing..." : "Use Cropped Image"}
          </Button>
        </div>
      </div>
    </div>
  );
}
