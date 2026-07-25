import { showToast } from "@/lib/toast";

export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export function getAuthToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("authToken") || localStorage.getItem("token");
}

export function getAuthHeaders(): Record<string, string> {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function handleAuthErrors(response: Response) {
  if (response.status === 401) {
    if (typeof window !== "undefined") {
      localStorage.removeItem("authToken");
      localStorage.removeItem("token");
      localStorage.removeItem("userRole");
      localStorage.removeItem("role");
      window.location.href = "/login";
    }
    showToast({ title: "Session expired", description: "Please log in again.", variant: "error" });
    throw new Error("Please log in again.");
  }
  if (response.status === 403) {
    showToast({ title: "Access denied", description: "You do not have permission for this action.", variant: "error" });
    throw new Error("Access denied");
  }
  if (response.status === 404) {
    showToast({ title: "Not found", description: "The requested resource was not found.", variant: "info" });
    throw new Error("Requested resource not found.");
  }
}

export async function readErrorMessage(response: Response): Promise<string> {
  const text = await response.text();
  if (!text) {
    return response.statusText;
  }
  try {
    const payload = JSON.parse(text);
    return (payload && (payload.message || payload.error)) || response.statusText;
  } catch {
    const normalized = text.trim();
    if (normalized.startsWith("<!DOCTYPE") || normalized.startsWith("<html")) {
      return response.statusText;
    }
    return normalized.length > 200 ? response.statusText : normalized;
  }
}

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders()
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  return response.json();
}
export async function getJson<T>(
  path: string,
  options?: { allow404?: boolean }
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "GET",
    headers: {
      ...getAuthHeaders()
    }
  });

  if (response.status === 404 && options?.allow404) {
    return null as T;
  }

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) {
    return null as T;
  }

  const text = await response.text();
  if (!text) {
    return null as T;
  }

  return JSON.parse(text) as T;
}
export async function patchJson<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders()
    },
    body: body ? JSON.stringify(body) : "{}"
  });

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  return response.json();
}

export async function putJson<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders()
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  return response.json();
}

export async function postForm<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      ...getAuthHeaders()
    },
    body: formData
  });

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  return response.json();
}

export async function deleteJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: {
      ...getAuthHeaders()
    }
  });

  if (!response.ok) {
    handleAuthErrors(response);
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}
