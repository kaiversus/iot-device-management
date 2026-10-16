$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

$jarPath = Join-Path $PSScriptRoot 'target\management-0.0.1-SNAPSHOT.jar'
$configPath = Join-Path $PSScriptRoot 'config\application-local.properties'
if (-not (Test-Path -LiteralPath $jarPath)) {
    throw 'Chua co file JAR. Hay chay mvn package truoc.'
}
if (-not (Test-Path -LiteralPath $configPath)) {
    throw 'Thieu config/application-local.properties. Xem docs/DAT_DEVICE_MODULE.md.'
}

$previousPassword = $env:SPRING_DATASOURCE_PASSWORD
try {
    $passwordLine = Get-Content -LiteralPath $configPath | Where-Object { $_ -match '^spring.datasource.password=' } | Select-Object -First 1
    if (-not $previousPassword -and ($passwordLine -eq 'spring.datasource.password=CHANGE_ME' -or $passwordLine -eq 'spring.datasource.password=')) {
        $securePassword = Read-Host 'Nhap mat khau root MySQL (ky tu se duoc an)' -AsSecureString
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
            $securePassword.Dispose()
        }
    }
    $port = '8080'
    $portLine = Get-Content -LiteralPath $configPath | Where-Object { $_ -match '^server.port=\d+$' } | Select-Object -Last 1
    if ($portLine) { $port = ($portLine -split '=', 2)[1] }
    Write-Host "Dang khoi dong IoT Hub. Khi khoi dong xong, mo http://localhost:$port"
    & java -jar $jarPath '--spring.profiles.active=local'
    if ($LASTEXITCODE -ne 0) { throw 'Khoi dong that bai. Xem thong bao loi phia tren.' }
} finally {
    if ($null -eq $previousPassword) {
        Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
    } else {
        $env:SPRING_DATASOURCE_PASSWORD = $previousPassword
    }
}
