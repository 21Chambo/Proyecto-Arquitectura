import { environment } from '../../environments/environment';

/**
 * Devuelve la URL base del API.
 * Si environment.apiBaseUrl está vacío (producción Docker), las peticiones
 * son relativas al mismo origen y Nginx hace el proxy hacia el API.
 * Durante el desarrollo, Angular CLI reenvía /api mediante proxy.conf.js.
 */
export function resolveApiBaseUrl(): string {
  return (environment.apiBaseUrl || '').replace(/\/+$/, '');
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
