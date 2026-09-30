import { TestBed } from '@angular/core/testing';
import { HelperService } from '../services/helper.service';
import { LoginService } from '../../views/login/login.service';
import { UserAuthGuard } from './user-auth.guard';

describe('UserAuthGuard', () => {
  let guard: UserAuthGuard;
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
        UserAuthGuard,
        { provide: HelperService, useValue: helperServiceSpy },
        { provide: LoginService, useValue: loginServiceSpy }
      ]
    });

    guard = TestBed.inject(UserAuthGuard);
    localStorage.clear();
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access when there is a token', () => {
    loginServiceSpy.isAuthenticated.and.returnValue(true);

    expect(guard.canActivate({} as any, {} as any)).toBe(true);
    expect(helperServiceSpy.redirectApp).not.toHaveBeenCalled();
  });

  it('should redirect to login when there is no token', () => {
    loginServiceSpy.isAuthenticated.and.returnValue(false);

    expect(guard.canActivate({} as any, {} as any)).toBe(false);
    expect(helperServiceSpy.redirectApp).toHaveBeenCalledWith('login');
  });
});
