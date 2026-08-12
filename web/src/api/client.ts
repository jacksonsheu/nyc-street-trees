/**
 * Thin fetch wrapper providing a shared base URL and consistent error handling
 * for all API calls. Keeping this isolated makes it easy to swap in another
 * HTTP client later without touching call sites.
 */

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080/api';

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Maps a mutation error to a user-facing message, special-casing the demo
 * interaction-capacity limit (503) so visitors get a clear explanation
 * instead of a generic failure message.
 */
export function getInteractionErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.status === 503) {
    return (
      error.message ||
      'This demo has reached its interaction limit for now. Thanks for checking it out!'
    );
  }
  return fallback;
}

interface RequestOptions {
  method?: 'GET' | 'POST';
  body?: unknown;
  params?: Record<string, string | number | undefined>;
}

function buildUrl(path: string, params?: RequestOptions['params']): string {
  const url = new URL(`${API_BASE_URL}${path}`);
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined) {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, params } = options;

  const response = await fetch(buildUrl(path, params), {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    // Surface backend error text when available so forms can show a useful message.
    const message = await response.text().catch(() => response.statusText);
    throw new ApiError(message || `Request failed with status ${response.status}`, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
