# Claude Code hook: 에이전트 작업 결과와 파일 수정 내역을 docs/agent-log/ 에 자동 기록한다.
#   subagent : SubagentStop 이벤트 -> docs/agent-log/raw-log.md 에 에이전트 최종 보고서 추가
#   edit     : PostToolUse(Edit|Write) 이벤트 -> docs/agent-log/file-changes.md 에 수정 파일 한 줄 추가
# 기록 실패가 작업을 막으면 안 되므로 모든 오류는 삼키고 exit 0 한다.
param([string]$Kind = 'subagent', [string]$InputFile = '', [string]$AgentTypeOverride = '')

$ErrorActionPreference = 'Stop'
$utf8 = New-Object System.Text.UTF8Encoding $false

function Append-Utf8([string]$Path, [string]$Text) {
    [System.IO.File]::AppendAllText($Path, $Text, $utf8)
}

# 서브에이전트 transcript(JSONL)에서 최종 보고서를 꺼낸다.
# 최종 보고서는 SubagentHandback 도구 호출의 message로 전달되므로 그것을 먼저 찾고,
# 없으면(예전 형식) 마지막 assistant 텍스트를 쓴다.
function Get-LastAssistantText([string]$TranscriptPath) {
    if (-not $TranscriptPath -or -not (Test-Path -LiteralPath $TranscriptPath)) { return $null }
    $lines = [System.IO.File]::ReadAllLines($TranscriptPath, $utf8)
    $lastText = $null
    for ($i = $lines.Length - 1; $i -ge 0; $i--) {
        try { $entry = $lines[$i] | ConvertFrom-Json } catch { continue }
        if ($entry.type -ne 'assistant') { continue }
        foreach ($part in @($entry.message.content)) {
            if ($part.type -eq 'tool_use' -and $part.name -eq 'SubagentHandback' -and $part.input.message) {
                return $part.input.message
            }
        }
        if (-not $lastText) {
            $texts = @($entry.message.content | Where-Object { $_.type -eq 'text' } | ForEach-Object { $_.text })
            if ($texts.Count -gt 0) { $lastText = ($texts -join "`n") }
        }
    }
    return $lastText
}

try {
    if ($InputFile) {
        $raw = [System.IO.File]::ReadAllText($InputFile, $utf8)
    } else {
        [Console]::InputEncoding = $utf8
        $raw = [Console]::In.ReadToEnd()
    }
    $j = $raw | ConvertFrom-Json

    # 한글 경로는 환경 변수로 넘어오며 깨질 수 있어, UTF-8 JSON의 cwd와 현재 위치를 우선한다.
    $root = (Get-Location).Path
    if (-not (Test-Path -LiteralPath (Join-Path $root '.claude'))) {
        if ($j.cwd) { $root = $j.cwd } elseif ($env:CLAUDE_PROJECT_DIR) { $root = $env:CLAUDE_PROJECT_DIR }
    }
    $logDir = Join-Path $root 'docs\agent-log'
    if (-not (Test-Path -LiteralPath $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }
    $ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $session = if ($j.session_id) { $j.session_id.Substring(0, [Math]::Min(8, $j.session_id.Length)) } else { '-' }

    if ($Kind -eq 'subagent') {
        # 이름(agent_type)이 없는 SubagentStop은 앱 내부 보조 작업(다음 입력 추천 등)이라 기록하지 않는다.
        $agent = if ($AgentTypeOverride) { $AgentTypeOverride } elseif ($j.agent_type) { $j.agent_type } else { $null }
        if (-not $agent) { exit 0 }
        $msg = Get-LastAssistantText $j.agent_transcript_path
        if (-not $msg) { $msg = $j.last_assistant_message }
        if (-not $msg) { $msg = '(최종 보고서를 찾지 못함)' }
        $entry = "## [$ts] $agent`n`n- session: ``$session```n`n$msg`n`n---`n`n"
        Append-Utf8 (Join-Path $logDir 'raw-log.md') $entry
    }
    elseif ($Kind -eq 'edit') {
        $file = $j.tool_input.file_path
        if (-not $file) { $file = $j.tool_response.filePath }
        if (-not $file) { exit 0 }
        # 로그 파일 자체의 수정은 기록하지 않는다(무한 기록 방지).
        if ($file -match 'docs[\\/]agent-log[\\/]') { exit 0 }
        $rel = $file -replace '\\', '/'
        $rootNorm = ($root -replace '\\', '/').TrimEnd('/')
        if ($rel.StartsWith($rootNorm + '/', [System.StringComparison]::OrdinalIgnoreCase)) {
            $rel = $rel.Substring($rootNorm.Length + 1)
        }
        $who = if ($j.agent_type) { $j.agent_type } else { 'main' }
        $line = "- $ts | $who | $($j.tool_name) | ``$rel```n"
        Append-Utf8 (Join-Path $logDir 'file-changes.md') $line
    }
} catch {
    # 기록 실패는 무시한다.
}
exit 0
