import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Router } from '@angular/router';
import { LoginService } from '../views/login/login.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private loginService: LoginService, private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!request.url.startsWith('/api/') || request.url.endsWith('/api/auth/login')) {
      return next.handle(request);
    }

    const token = this.loginService.getToken();

    const authenticatedRequest = token ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      }) : request;
    return next.handle(authenticatedRequest).pipe(catchError(error => {
      if (error.status === 401) {
        this.loginService.logout();
        this.router.navigate(['/login']);
      }
      return throwError(() => error);
    }));
  }
}
