param(
    [string]$BaseUrl = 'http://localhost:3000',
    [Parameter(Mandatory = $true)]
    [System.Management.Automation.PSCredential]$AdminCredential,
    [Parameter(Mandatory = $true)]
    [System.Management.Automation.PSCredential]$StaffCredential
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')

function Invoke-CheckedRequest {
    param([string]$Path, [string]$Method, [int]$ExpectedStatus, [string]$Body, [hashtable]$Headers = @{})
    $request = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $Headers
        UseBasicParsing = $true
        TimeoutSec = 20
        ErrorAction = 'Stop'
    }
    if ($Body) {
        $request.ContentType = 'application/json'
        $request.Body = $Body
    }
    $response = $null
    try {
        $response = Invoke-WebRequest @request
        $actualStatus = [int]$response.StatusCode
    } catch {
        if ($null -eq $_.Exception.Response) { throw }
        $actualStatus = [int]$_.Exception.Response.StatusCode
    }
    if ($actualStatus -ne $ExpectedStatus) {
        throw "$Method $Path returned HTTP $actualStatus; expected $ExpectedStatus."
    }
    if ($response) { return ($response.Content | ConvertFrom-Json) }
}

$health = Invoke-CheckedRequest -Path '/api/health' -Method Get -ExpectedStatus 200
if ($health.status -ne 'ok') { throw 'Database health check failed.' }
Write-Output 'PASS: API and database health.'

foreach ($account in @(
    @{ Credential = $AdminCredential; Role = 'ROLE_ADMIN' }
    @{ Credential = $StaffCredential; Role = 'ROLE_STAFF' }
)) {
    $credential = $account.Credential
    $body = @{ username = $credential.UserName; password = $credential.GetNetworkCredential().Password } | ConvertTo-Json
    $session = Invoke-CheckedRequest -Path '/api/auth/login' -Method Post -ExpectedStatus 200 -Body $body
    if ($session.username -ne $credential.UserName -or $session.roles -notcontains $account.Role) {
        throw 'Login returned an unexpected account or role.'
    }
    if ($session.tokenType -ne 'Bearer') { throw 'Login must return a Bearer JWT.' }
    $null = Invoke-CheckedRequest -Path '/api/customers' -Method Get -ExpectedStatus 200 -Headers @{ Authorization = "Bearer $($session.accessToken)" }
    Write-Output "PASS: $($credential.UserName) login, role and authenticated API access."

    $invalid = @{ username = $credential.UserName; password = [guid]::NewGuid().ToString('N') } | ConvertTo-Json
    $null = Invoke-CheckedRequest -Path '/api/auth/login' -Method Post -ExpectedStatus 401 -Body $invalid
    Write-Output "PASS: invalid password for $($credential.UserName) returns 401."
}

$unknown = @{ username = 'missing-' + [guid]::NewGuid().ToString('N'); password = 'invalid-test-password' } | ConvertTo-Json
$null = Invoke-CheckedRequest -Path '/api/auth/login' -Method Post -ExpectedStatus 401 -Body $unknown
$null = Invoke-CheckedRequest -Path '/api/auth/login' -Method Post -ExpectedStatus 400 -Body '{}'
Write-Output 'PASS: unknown account returns 401; empty credentials return 400.'
