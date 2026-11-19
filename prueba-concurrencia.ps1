
# ==========================================
# CAFE CONTABLE
# PRUEBA P18 - CONCURRENCIA E IDEMPOTENCIA
# ==========================================

$ErrorActionPreference = "Stop"

# 1. Endpoint de sincronización
$url = "https://192.168.101.5:7079/api/compras/sincronizar"

# 2. Leer JWT sin mostrarlo en pantalla
$seguro = Read-Host "Pega tu JWT vigente" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($seguro)

try {
    $token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}

# 3. Leer compra de prueba
$rutaJson = ".\compra-prueba.json"

if (-not (Test-Path $rutaJson)) {
    Write-Host "ERROR: No existe compra-prueba.json" -ForegroundColor Red
    exit 1
}

$compra = Get-Content $rutaJson -Raw -Encoding UTF8 |
    ConvertFrom-Json

# 4. Generar UUID nuevo
$uuid = [guid]::NewGuid().ToString()

$compra.operacionSyncId = $uuid

# Las dos solicitudes utilizan el mismo JSON
$json = $compra |
    ConvertTo-Json -Depth 20 -Compress

Write-Host ""
Write-Host "===================================="
Write-Host "PRUEBA P18 - CAFE CONTABLE"
Write-Host "UUID: $uuid"
Write-Host "===================================="

# 5. Preparar ejecución simultánea
$inicio = [DateTime]::UtcNow.AddSeconds(5)

$trabajo = {
    param($url, $token, $json, $inicio)

    while ([DateTime]::UtcNow -lt $inicio) {
        Start-Sleep -Milliseconds 20
    }

    try {
        $respuesta = Invoke-WebRequest `
            -Uri $url `
            -Method Post `
            -Headers @{
                Authorization = "Bearer $token"
            } `
            -ContentType "application/json" `
            -Body ([Text.Encoding]::UTF8.GetBytes($json)) `
            -UseBasicParsing `
            -ErrorAction Stop

        [PSCustomObject]@{
            Status = [int]$respuesta.StatusCode
            Body = $respuesta.Content
        }
    }
    catch {
        $codigo = 0
        $detalle = $_.Exception.Message

        if ($null -ne $_.Exception.Response) {
            $response = $_.Exception.Response
            $codigo = [int]$response.StatusCode

            try {
                $stream = $response.GetResponseStream()

                if ($null -ne $stream) {
                    $reader = New-Object System.IO.StreamReader($stream)

                    try {
                        $contenido = $reader.ReadToEnd()

                        if (-not [string]::IsNullOrWhiteSpace($contenido)) {
                            $detalle = $contenido
                        }
                    }
                    finally {
                        $reader.Dispose()
                    }
                }
            }
            catch {
                # Mantener el mensaje de error original
            }
        }

        [PSCustomObject]@{
            Status = $codigo
            Body = $detalle
        }
    }
}

# 6. Lanzar dos solicitudes concurrentes
$j1 = Start-Job `
    -ScriptBlock $trabajo `
    -ArgumentList $url, $token, $json, $inicio

$j2 = Start-Job `
    -ScriptBlock $trabajo `
    -ArgumentList $url, $token, $json, $inicio

# 7. Recibir las respuestas
try {
    $resultados = @(
        $j1, $j2 | Wait-Job | Receive-Job
    )

    Write-Host ""
    Write-Host "========= RESULTADOS ========="

    $resultados | Format-List Status, Body

    Write-Host "===================================="
    Write-Host "UUID evaluado: $uuid"
    Write-Host "===================================="
}
finally {
    $j1, $j2 | Remove-Job -Force -ErrorAction SilentlyContinue
    $token = $null
    $seguro.Dispose()
}
