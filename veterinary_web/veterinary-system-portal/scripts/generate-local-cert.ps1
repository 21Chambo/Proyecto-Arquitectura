New-Item -ItemType Directory -Force -Path "nginx/certs" | Out-Null

$openssl = Get-Command openssl -ErrorAction SilentlyContinue

$keyPath = "nginx/certs/localhost.key"
$crtPath = "nginx/certs/localhost.crt"

if ($openssl) {
    & $openssl.Source req `
        -x509 `
        -nodes `
        -days 365 `
        -newkey rsa:2048 `
        -keyout $keyPath `
        -out $crtPath `
        -subj "/CN=localhost"

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to generate the local self-signed certificate with the local OpenSSL binary."
        exit $LASTEXITCODE
    }

    Write-Output "Generated $crtPath and $keyPath using local OpenSSL"
    exit 0
}

$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
    Write-Error "Neither OpenSSL nor Docker is available to generate the local certificate."
    exit 1
}

$dockerImage = "maven:3.9.15-eclipse-temurin-11"
$opensslCheck = & $docker.Source run --rm --entrypoint /bin/sh $dockerImage -c "command -v openssl" 2>$null

if ($LASTEXITCODE -ne 0 -or -not $opensslCheck) {
    Write-Error "No usable OpenSSL binary was found locally, and the fallback Docker image does not provide OpenSSL."
    exit 1
}

$projectPath = (Resolve-Path ".").Path

& $docker.Source run --rm `
    -v "${projectPath}:/workspace" `
    -w /workspace `
    --entrypoint /bin/sh `
    $dockerImage `
    -c "openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout '$keyPath' -out '$crtPath' -subj '/CN=localhost'"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to generate the local self-signed certificate using Docker."
    exit $LASTEXITCODE
}

Write-Output "Generated $crtPath and $keyPath using Docker ($dockerImage)"
