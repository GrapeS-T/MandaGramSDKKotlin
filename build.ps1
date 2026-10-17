[CmdletBinding()]
param(
    [string]$KotlinNativeHome = $env:KOTLIN_NATIVE_HOME
)
$ErrorActionPreference = 'Stop'
if (-not $KotlinNativeHome) {
    throw 'Set KOTLIN_NATIVE_HOME to the Windows x86_64 Kotlin/Native 2.1.21 distribution.'
}
$compiler = Join-Path $KotlinNativeHome 'bin/kotlinc-native.bat'
$interop = Join-Path $KotlinNativeHome 'bin/cinterop.bat'
# Native tools write version banners to stderr. Collect without treating the
# banner as a PowerShell 5.1 terminating error; check the native exit code.
$previousPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$version = (& $compiler -version 2>&1 | Out-String)
$compilerExit = $LASTEXITCODE
$ErrorActionPreference = $previousPreference
if ($compilerExit -ne 0 -or $version -notmatch '(?<![\d.])2\.1\.21(?![\d.])') {
    throw "Expected Kotlin/Native 2.1.21, got: $version"
}
$out = Join-Path $PSScriptRoot 'build/mingw_x64'
New-Item -ItemType Directory -Force -Path $out | Out-Null
& $interop -def (Join-Path $PSScriptRoot 'interop/mandagram.def') `
    -compiler-option "-I$(Join-Path $PSScriptRoot 'include')" `
    -target mingw_x64 -o (Join-Path $out 'mandagram')
if ($LASTEXITCODE -ne 0) { throw "cinterop failed ($LASTEXITCODE)." }
& $compiler (Join-Path $PSScriptRoot 'examples/PlainText.kt') `
    -library (Join-Path $out 'mandagram.klib') `
    -target mingw_x64 -produce dynamic -opt -o (Join-Path $out 'plain_text')
if ($LASTEXITCODE -ne 0) { throw "Kotlin/Native compilation failed ($LASTEXITCODE)." }
Write-Output "Built Kotlin/Native SDK and plugin in $out"
