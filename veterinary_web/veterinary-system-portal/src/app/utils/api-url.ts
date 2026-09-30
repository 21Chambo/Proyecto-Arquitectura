import { environment } from '../../environments/environment';

export function resolveApiBaseUrl(): string {
  const configuredBaseUrl = (environment.apiBaseUrl || '').trim();

  if (configuredBaseUrl) {
    return configuredBaseUrl.replace(/\/+$/, '');
  }

  const protocol = typeof window !== 'undefined' && window.location?.protocol
    ? window.location.protocol
    : 'http:';
  const hostname = typeof window !== 'undefined' && window.location?.hostname
    ? window.location.hostname
    : 'localhost';
  const isLocalHost = hostname === 'localhost' || hostname === '13.223.141.227';
  const isHttps = protocol === 'https:';

  // When the portal is served over local HTTPS, keep API calls relative so
  // Nginx can proxy them and we avoid mixed-content requests to port 9090.
  if (isHttps) {
    return '';
  }

  // In local development and local Docker, the backend is exposed on port 9090.
  // In future static production deployments, the API can stay relative under /api/*.
  if (!environment.production || isLocalHost) {
    return '';
  }

  return '';
}

export function buildApiUrl(endpoint: string): string {
  const baseUrl = resolveApiBaseUrl();
  const normalizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;

  if (!baseUrl) {
    return normalizedEndpoint;
  }

  if (baseUrl.endsWith('/api') && normalizedEndpoint.startsWith('/api/')) {
    return `${baseUrl}${normalizedEndpoint.slice(4)}`;
  }

  return `${baseUrl}${normalizedEndpoint}`;
}
