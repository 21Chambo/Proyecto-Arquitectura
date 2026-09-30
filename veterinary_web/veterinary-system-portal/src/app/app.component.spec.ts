import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { DashboardComponent } from './views/dashboard/dashboard.component';
import { ResourceIndexComponent } from './views/resources/resource-index/resource-index.component';
import { ResourceRegistryService } from './services/resource-registry.service';
import { HelperService } from './utils/services/helper.service';
import { LoginService } from './views/login/login.service';

class LoginServiceStub {
  isAuthenticated(): boolean {
    return true;
  }

  getFullName(): string {
    return 'System Administrator';
  }
}

class ResourceRegistryServiceStub {
  getNavigationItems(): any[] {
    return [
      {
        key: 'customers',
        label: 'Customers',
        path: '/customers',
        description: 'Gestiona clientes y propietarios.'
      }
    ];
  }

  getConfig(): any {
    return {
      key: 'customers',
      routeSegment: 'customers',
      path: '/customers',
      label: 'Customers',
      singularLabel: 'customer',
      description: 'Gestiona clientes y propietarios.',
      columns: [],
      fields: [],
      service: {
        findAll: () => of([]),
        delete: () => of(null)
      }
    };
  }
}

class HelperServiceStub {
  redirectApp(): void {}
  showMessage(): void {}
  getHttpErrorMessage(): string {
    return '';
  }
  confirmDelete(callback: () => void): void {
    callback();
  }
}

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'dashboard', component: DashboardComponent },
          { path: 'customers', component: ResourceIndexComponent, data: { resource: 'customers' } }
        ])
      ],
      declarations: [
        AppComponent,
        DashboardComponent,
        ResourceIndexComponent
      ],
      providers: [
        { provide: LoginService, useClass: LoginServiceStub },
        { provide: ResourceRegistryService, useClass: ResourceRegistryServiceStub },
        { provide: HelperService, useClass: HelperServiceStub }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should initialize navigation items', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.navItems).toBeTruthy();
  });

  it('should render a router outlet container', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('should render the resource screen when navigating to /customers', fakeAsync(() => {
    const router = TestBed.inject(Router);
    const location = TestBed.inject(Location);
    const fixture = TestBed.createComponent(AppComponent);

    router.initialNavigation();
    tick();

    router.navigateByUrl('/customers');
    tick();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(location.path()).toBe('/customers');
    expect(compiled.textContent).toContain('Customers');
    expect(compiled.textContent).not.toContain('Panel principal');
  }));
});
