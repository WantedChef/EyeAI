# EyeAI Training Server Startup Script - Windows PowerShell Version
# JVM optimalisaties voor 8GB RAM systeem - MAX PERFORMANCE

Write-Host "🎯 Starting EyeAI Training Server..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# NVIDIA optimalisaties (indien beschikbaar)
$env:__GL_SHADER_DISK_CACHE = 1
$env:__GL_SHADER_DISK_CACHE_PATH = "$env:TEMP\nvidia-shaders"

# JVM optimalisaties voor 8GB RAM systeem - MAX PERFORMANCE
$JVM_FLAGS = @(
    "-Xms6G",
    "-Xmx6G",
    "-XX:+UseG1GC",
    "-XX:G1NewSizePercent=40",
    "-XX:G1MaxNewSizePercent=60",
    "-XX:G1HeapRegionSize=16M",
    "-XX:MaxGCPauseMillis=200",
    "-XX:+AlwaysPreTouch",
    "-XX:+DisableExplicitGC",
    "-XX:+ParallelRefProcEnabled",
    "-XX:+PerfDisableSharedMem",
    "-XX:G1MixedGCCountTarget=8",
    "-XX:G1HeapWastePercent=10",
    "-Dfile.encoding=UTF-8",
    "-Duser.timezone=UTC",
    "-Dcom.mojang.eula.agree=true"
)

# Server optimalisaties voor MAXIMUM performance
$JVM_FLAGS += @(
    "-XX:+UnlockExperimentalVMOptions",
    "-XX:+UseCGroupMemoryLimitForHeap",
    "-XX:MaxRAMPercentage=75.0",
    "-XX:+UseCompressedOops",
    "-XX:CompressedClassSpaceSize=256m",
    "-XX:+UseStringDeduplication",
    "-XX:+OptimizeStringConcat",
    "-XX:G1PeriodicGCInterval=30000",
    "-XX:G1PeriodicGCSystemLoadThreshold=0.8"
)

# Paper specifieke optimalisaties voor HEAVY training
$JVM_FLAGS += @(
    "-Dpaper.playerconnection.keepalive=60",
    "-Dpaper.max-joins-per-second=20",
    "-Dpaper.anti-xray.enabled=false",
    "-Dpaper.use-faster-eigencraft-redstone=false",
    "-Dpaper.fix-entity-position-desync=true",
    "-Dpaper.async-chunks.enable=true",
    "-Dpaper.async-chunks.threads=4"
)

Write-Host "JVM Flags: $JVM_FLAGS" -ForegroundColor Green
Write-Host "Starting server..." -ForegroundColor Green

# Ga naar server directory (pas aan naar je server locatie)
Set-Location -Path "server"

# Start de server
& java $JVM_FLAGS -jar purpur-1.20.6.jar nogui
