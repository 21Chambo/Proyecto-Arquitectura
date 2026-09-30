# FrontendUnab

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 10.2.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Production deployment

The preferred low-cost AWS deployment for this frontend is:

- static Angular build
- Amazon S3 for hosting
- Amazon CloudFront for CDN and SPA delivery
- backend routed through `/api/*` at the edge layer

In production, the frontend does not require an Nginx container. The generated app is expected to call `/api/...` on the same public domain, with CloudFront forwarding that path to the backend origin.

Recommended production flow:

1. Build the Angular app:
```bash
npm run build -- --configuration production
```

2. Publish the generated `dist/frontend-unab` files to S3.

3. Configure CloudFront to:
- serve the SPA from the S3 origin
- forward `/api/*` to the backend origin
- return `index.html` for SPA routes such as `/dashboard` or `/customers`

The self-signed local TLS validation described below is not the production deployment model for AWS. It is only an isolated local check for ADR-011.

## Local HTTPS with self-signed certificate

For local validation of ADR-011, the Nginx container can terminate TLS with a self-signed certificate through a dedicated compose override. The base `docker-compose.yml` remains HTTP-only to avoid coupling local `localhost` behavior to future AWS deployment settings.

1. Generate the local certificate:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\generate-local-cert.ps1
```

2. Make sure the shared Docker network exists:
```bash
docker network create veterinary-system-network
```

3. Start the portal with the local HTTPS override:
```bash
docker compose -f docker-compose.yml -f docker-compose.local-https.yml up -d --build
```

4. Open:
```text
https://localhost:3443
```

The browser will warn that the certificate is self-signed. This setup is only for local or pre-production validation and is not intended for real public deployments.

For the default local HTTP-only mode:
```bash
docker compose up -d
```

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
