/**
 * Sanitizes and validates external URLs to prevent link-based XSS (e.g. javascript: or data: URIs)
 */
export function toSafeUrl(url: string | null | undefined): string {
  if (!url) return '#';
  const trimmed = url.trim();
  
  // Strictly allow only http:// and https:// URLs
  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed;
  }
  
  return '#';
}
