# EyeAI TCP Transfer Script - Windows PowerShell Version
# SSH tunnels, file transfers en monitoring voor Windows

param(
    [string]$Action = "",
    [string]$Source = "",
    [string]$Dest = "",
    [string]$Desc = ""
)

# Config
$REMOTE_HOST = "192.168.1.100"  # Aanpassen naar IP van remote PC
$REMOTE_USER = "training"       # User op remote PC
$REMOTE_PORT = "22"
$LOCAL_PORT = "2222"           # Lokale tunnel poort
$TRANSFER_PORT = "2223"        # Data transfer poort
$MODEL_DIR = "plugins/ChefAI/models"
$CHECKPOINT_DIR = "plugins/ChefAI/checkpoints"

# Kleuren
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"

function Write-Status { param($Message) Write-Host "[INFO] $Message" -ForegroundColor $Green }
function Write-Error { param($Message) Write-Host "[ERROR] $Message" -ForegroundColor $Red; exit 1 }
function Write-Warning { param($Message) Write-Host "[WARN] $Message" -ForegroundColor $Yellow }

# Check SSH verbinding
function Test-Connection {
    Write-Status "Checking SSH connection to $REMOTE_HOST..."
    try {
        $result = ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no "$REMOTE_USER@$REMOTE_HOST" "echo 'SSH OK'" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Status "SSH connection OK ✓"
        } else {
            Write-Error "Kan geen SSH verbinding maken naar $REMOTE_HOST. Check IP, user en SSH keys!"
        }
    } catch {
        Write-Error "SSH command failed: $_"
    }
}

# Setup SSH tunnel
function New-Tunnel {
    Write-Status "Setting up SSH tunnel..."

    # Kill bestaande tunnels
    Get-Process ssh -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*$LOCAL_PORT*" } | Stop-Process -Force

    # Maak nieuwe tunnel
    Start-Process ssh -ArgumentList "-f", "-N", "-L", "$LOCAL_PORT`:localhost:$TRANSFER_PORT", "$REMOTE_USER@$REMOTE_HOST" -NoNewWindow -Wait

    # Wacht en check
    Start-Sleep 2

    $connection = Test-NetConnection -ComputerName localhost -Port $LOCAL_PORT -ErrorAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Status "SSH tunnel actief op localhost:$LOCAL_PORT ✓"
    } else {
        Write-Error "Tunnel niet beschikbaar!"
    }
}

# Transfer functie
function Send-File {
    param($Source, $Dest, $Desc)

    if (!(Test-Path $Source)) {
        Write-Warning "Bron bestand bestaat niet: $Source"
        return
    }

    Write-Status "Transferring $Desc..."
    Write-Status "From: $Source"
    Write-Status "To: $Dest"

    # Gebruik scp voor transfer (Windows heeft ingebouwde OpenSSH)
    try {
        scp -P $REMOTE_PORT "$Source" "$REMOTE_USER@$REMOTE_HOST`:$Dest"
        Write-Status "$Desc transferred successfully ✓"
    } catch {
        Write-Error "Transfer failed: $_"
    }
}

# Sync models (vereist rsync for Windows of alternatief)
function Sync-Models {
    Write-Status "Syncing complete models directory..."
    # Voor Windows: gebruik robocopy of installeer rsync for Windows
    Write-Warning "Voor volledige sync: installeer rsync for Windows (cwRsync) of gebruik scp"
    Write-Status "Models sync zou hier gebeuren..."
}

# Sync checkpoints
function Sync-Checkpoints {
    Write-Status "Syncing latest checkpoints..."
    Write-Warning "Voor volledige sync: installeer rsync for Windows"
    Write-Status "Checkpoints sync zou hier gebeuren..."
}

# Live monitoring
function Start-Monitor {
    Write-Status "Starting live training monitor..."
    Write-Status "Press Ctrl+C to stop"

    while ($true) {
        Clear-Host
        Write-Host "=== EyeAI Training Monitor ===" -ForegroundColor $Cyan
        Get-Date
        Write-Host ""

        # Remote GPU status
        Write-Host "🎮 Remote GPU Status:" -ForegroundColor $Cyan
        try {
            ssh "$REMOTE_USER@$REMOTE_HOST" "nvidia-smi --query-gpu=utilization.gpu,utilization.memory,memory.used,memory.free,temperature.gpu --format=csv,noheader,nounits" 2>$null
        } catch {
            Write-Host "GPU info niet beschikbaar"
        }

        Write-Host ""
        Write-Host "🖥️ Remote Server Status:" -ForegroundColor $Cyan
        try {
            ssh "$REMOTE_USER@$REMOTE_HOST" "ps aux | grep -E '(java|purpur)' | grep -v grep" 2>$null
        } catch {
            Write-Host "Server info niet beschikbaar"
        }

        Write-Host ""
        Write-Host "📊 Transfer Status:" -ForegroundColor $Cyan
        $connection = Test-NetConnection -ComputerName localhost -Port $LOCAL_PORT -ErrorAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "Tunnel: ACTIVE"
        } else {
            Write-Host "Tunnel: INACTIVE"
        }

        Write-Host ""
        Write-Host "📋 Latest Training Logs:" -ForegroundColor $Cyan
        try {
            ssh "$REMOTE_USER@$REMOTE_HOST" "tail -5 ~/minecraft-training/server/logs/latest.log" 2>$null
        } catch {
            Write-Host "Logs niet beschikbaar"
        }

        Start-Sleep 10
    }
}

# Cleanup
function Clear-Tunnels {
    Write-Status "Cleaning up..."
    Get-Process ssh -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*$LOCAL_PORT*" } | Stop-Process -Force
    Write-Status "Cleanup complete ✓"
}

# Main menu
function Show-Menu {
    Clear-Host
    Write-Host "🚀 EyeAI TCP Transfer - Windows PowerShell" -ForegroundColor $Cyan
    Write-Host "==========================================" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "1. Check SSH Connection"
    Write-Host "2. Setup SSH Tunnel"
    Write-Host "3. Transfer Specific File"
    Write-Host "4. Sync All Models (vereist rsync)"
    Write-Host "5. Sync Latest Checkpoints (vereist rsync)"
    Write-Host "6. Start Live Training Monitor"
    Write-Host "0. Exit"
    Write-Host ""
    $choice = Read-Host "Choose option (0-6)"
    return $choice
}

# Main logic
if ($Action -eq "") {
    # Interactive mode
    while ($true) {
        $choice = Show-Menu

        switch ($choice) {
            1 { Test-Connection }
            2 { Test-Connection; New-Tunnel }
            3 {
                $source = Read-Host "Enter source file path"
                $dest = Read-Host "Enter destination path"
                $desc = Read-Host "Description"
                Send-File -Source $source -Dest $dest -Desc $desc
            }
            4 { Sync-Models }
            5 { Sync-Checkpoints }
            6 { Start-Monitor }
            0 {
                Write-Status "Goodbye! 👋"
                exit 0
            }
            default { Write-Warning "Invalid option!" }
        }

        Read-Host "Press Enter to continue..."
    }
} else {
    # Command line mode
    switch ($Action) {
        "check" { Test-Connection }
        "tunnel" { Test-Connection; New-Tunnel }
        "transfer" {
            if ($Source -eq "" -or $Dest -eq "" -or $Desc -eq "") {
                Write-Error "Usage: .\tcp-transfer.ps1 -Action transfer -Source <path> -Dest <path> -Desc <description>"
            }
            Send-File -Source $Source -Dest $Dest -Desc $Desc
        }
        "sync-models" { Sync-Models }
        "sync-checkpoints" { Sync-Checkpoints }
        "monitor" { Start-Monitor }
        default { Write-Error "Invalid action. Use: check, tunnel, transfer, sync-models, sync-checkpoints, monitor" }
    }
}
