import { HttpRequest, HttpResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { AuthInterceptor } from './auth.interceptor';
import { LoginService } from '../views/login/login.service';
import { Router } from '@angular/router';

describe('Bearer authentication interceptor', () => {
  let session: jasmine.SpyObj<LoginService>;
  let router: jasmine.SpyObj<Router>;
  let next: {handle: jasmine.Spy};
  let interceptor: AuthInterceptor;
  beforeEach(() => {
    session = jasmine.createSpyObj('LoginService', ['getToken', 'logout']);
    session.getToken.and.returnValue('signed.jwt.token');
    router = jasmine.createSpyObj('Router', ['navigate']);
    next = {handle: jasmine.createSpy('handle').and.returnValue(of(new HttpResponse({status: 200})))};
    interceptor = new AuthInterceptor(session, router);
  });

  it('attaches the Bearer token to API calls', () => {
    interceptor.intercept(new HttpRequest('GET', '/api/pets'), next).subscribe();
    expect(next.handle.calls.mostRecent().args[0].headers.get('Authorization')).toBe('Bearer signed.jwt.token');
  });

  it('does not disclose the token to external URLs or the login endpoint', () => {
    for (const url of ['https://external.example/api/pets', '/api/auth/login']) {
      interceptor.intercept(new HttpRequest('GET', url), next).subscribe();
      expect(next.handle.calls.mostRecent().args[0].headers.has('Authorization')).toBeFalse();
    }
  });

  it('clears the session and redirects on 401', () => {
    next.handle.and.returnValue(throwError(() => ({status: 401})));
    interceptor.intercept(new HttpRequest('GET', '/api/pets'), next).subscribe({error: () => {}});
    expect(session.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('also handles expiry discovered before making the API request', () => {
    session.getToken.and.returnValue(null);
    next.handle.and.returnValue(throwError(() => ({status: 401})));
    interceptor.intercept(new HttpRequest('GET', '/api/pets'), next).subscribe({error: () => {}});
    expect(session.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
