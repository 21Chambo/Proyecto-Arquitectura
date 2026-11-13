import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA, NgZone, provideZoneChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, HttpClientTestingModule, ReactiveFormsModule, RouterTestingModule],
      declarations: [ LoginComponent ],
      providers: [provideZoneChangeDetection()],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('renders an asynchronous login rejection and enables retry', async () => {
    // Toast dismissal uses a five-second timer; keep this test focused on the login view.
    spyOn(component.helperService, 'showMessage');
    fixture.autoDetectChanges();
    component.frmLogin.setValue({ username: 'staff', password: 'wrong-password' });
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    const request = TestBed.inject(HttpTestingController).expectOne('/api/auth/login');
    TestBed.inject(NgZone).run(() => {
      request.flush({ message: 'Credenciales invalidas' }, { status: 401, statusText: 'Unauthorized' });
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.auth-feedback').textContent).toContain('Usuario o contrasena invalidos');
    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBeFalse();
    TestBed.inject(HttpTestingController).verify();
  });

});
