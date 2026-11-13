import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoginService } from './login.service';

function jwt(exp = Math.floor(Date.now() / 1000) + 900): string {
  const encode = (value: unknown) => btoa(String.fromCharCode(...new TextEncoder().encode(JSON.stringify(value))))
    .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `${encode({alg: 'HS256'})}.${encode({sub: 'staff', fullName: 'José Test', roles: ['ROLE_STAFF'], exp})}.signature`;
}

describe('JWT session', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    localStorage.clear(); sessionStorage.clear();
    TestBed.configureTestingModule({imports: [HttpClientTestingModule]});
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => { http.verify(); localStorage.clear(); sessionStorage.clear(); });

  it('removes historical Basic credentials without migrating them', () => {
    localStorage.setItem('veterinary.token', 'Basic c3RhZmY6c2VjcmV0');
    localStorage.setItem('token', 'Basic c3RhZmY6c2VjcmV0');
    const service = TestBed.inject(LoginService);
    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('veterinary.token')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('stores only the JWT in sessionStorage and exposes its profile claims', () => {
    const service = TestBed.inject(LoginService); const token = jwt();
    service.login({username: 'staff', password: 'never-store-this-password'}).subscribe();
    http.expectOne('/api/auth/login').flush({username: 'staff', fullName: 'José Test', roles: ['ROLE_STAFF'], tokenType: 'Bearer', accessToken: token});
    expect(sessionStorage.length).toBe(1);
    expect(sessionStorage.getItem('veterinary.jwt')).toBe(token);
    expect(localStorage.length).toBe(0);
    expect(service.getToken()).toBe(token);
    expect(service.getFullName()).toBe('José Test');
    expect(service.getRoles()).toEqual(['ROLE_STAFF']);
  });

  it('clears a session when its JWT expires', () => {
    const service = TestBed.inject(LoginService);
    sessionStorage.setItem('veterinary.jwt', jwt());
    expect(service.isAuthenticated()).toBeTrue();
    const afterExpiry = Date.now() + 901000;
    spyOn(Date, 'now').and.returnValue(afterExpiry);
    expect(service.getToken()).toBeNull();
    expect(sessionStorage.getItem('veterinary.jwt')).toBeNull();
  });

  it('rejects malformed tokens and Basic tokens', () => {
    const service = TestBed.inject(LoginService);
    for (const token of ['Basic c3RhZmY6c2VjcmV0', 'abc.bad.signature', jwt(1)]) {
      sessionStorage.setItem('veterinary.jwt', token);
      expect(service.getToken()).toBeNull();
    }
  });

  it('clears the token on logout', () => {
    const service = TestBed.inject(LoginService);
    sessionStorage.setItem('veterinary.jwt', jwt()); service.logout();
    expect(service.isAuthenticated()).toBeFalse();
  });
});
