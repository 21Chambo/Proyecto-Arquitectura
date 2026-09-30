import { TestBed } from '@angular/core/testing';
import { HelperService } from '../services/helper.service';
import { LoginService } from '../../views/login/login.service';
import { LoginGuard } from './login.guard';

describe('LoginGuard', () => {
  let guard: LoginGuard;
  let helperServiceSpy: { redirectApp: jasmine.Spy };
  let loginServiceSpy: { isAuthenticated: jasmine.Spy };

  beforeEach(() => {
    helperServiceSpy = {
      redirectApp: jasmine.createSpy('redirectApp')
    };
    loginServiceSpy = {
      isAuthenticated: jasmine.createSpy('isAuthenticated').and.returnValue(false)
    };

    TestBed.configureTestingModule({
      providers: [
        LoginGuard,
        { provide: HelperService, useValue: helperServiceSpy },
        { provide: LoginService, useValue: loginServiceSpy }
      ]
    });

    guard = TestBed.inject(LoginGuard);
    localStorage.clear();
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access when there is no token', () => {
    loginServiceSpy.isAuthenticated.and.returnValue(false);
    expect(guard.canActivate({} as any, {} as any)).toBe(true);
  });

  it('should redirect to dashboard when there is a valid session', () => {
    loginServiceSpy.isAuthenticated.and.returnValue(true);

    expect(guard.canActivate({} as any, {} as any)).toBe(false);
    expect(helperServiceSpy.redirectApp).toHaveBeenCalledWith('dashboard');
  });
});
