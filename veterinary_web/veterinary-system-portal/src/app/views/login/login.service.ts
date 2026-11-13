import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap, timeout } from 'rxjs/operators';
import { AuthLoginRequest } from '../../models/auth-login-request.model';
import { AuthLoginResponse } from '../../models/auth-login-response.model';
import { buildApiUrl } from '../../utils/api-url';

interface SessionClaims { exp: number; sub: string; fullName?: string; roles?: string[]; }

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly tokenKey = 'veterinary.jwt';

  constructor(private http: HttpClient) {
    // Never migrate the historical Basic tokens: they contain recoverable passwords.
    for (const key of ['veterinary.token', 'veterinary.full_name', 'veterinary.roles', 'token', 'full_name', 'roles']) {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    }
    this.getToken();
  }

  getApiUrl(): string { return buildApiUrl('/api'); }

  login(payload: AuthLoginRequest): Observable<AuthLoginResponse> {
    return this.http.post<AuthLoginResponse>(buildApiUrl('/api/auth/login'), payload).pipe(
      timeout(5000),
      tap(response => {
        if (response.tokenType !== 'Bearer' || !this.readClaims(response.accessToken)) {
          this.logout();
          throw new Error('La respuesta de autenticacion no contiene una sesion valida.');
        }
        sessionStorage.setItem(this.tokenKey, response.accessToken);
      })
    );
  }

  logout(): void { sessionStorage.removeItem(this.tokenKey); }
  isAuthenticated(): boolean { return !!this.getToken(); }

  getToken(): string | null {
    const token = sessionStorage.getItem(this.tokenKey);
    if (!this.readClaims(token)) { this.logout(); return null; }
    return token;
  }

  getFullName(): string {
    const claims = this.readClaims(this.getToken());
    return claims?.fullName || claims?.sub || '';
  }

  getRoles(): string[] {
    const roles = this.readClaims(this.getToken())?.roles;
    return Array.isArray(roles) ? roles.filter(role => typeof role === 'string') : [];
  }

  private readClaims(token: string | null): SessionClaims | null {
    try {
      if (!token || !/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(token)) { return null; }
      const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const bytes = Uint8Array.from(atob(part.padEnd(Math.ceil(part.length / 4) * 4, '=')), char => char.charCodeAt(0));
      const claims = JSON.parse(new TextDecoder().decode(bytes));
      // This is only a UI expiry check. The API verifies signature, issuer and timestamps.
      return typeof claims.exp === 'number' && claims.exp * 1000 > Date.now() && typeof claims.sub === 'string'
        ? claims : null;
    } catch { return null; }
  }
}
