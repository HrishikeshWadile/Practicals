# Runs Maven with the JDK found via JAVA_HOME / PATH. JDK 17 or 21 is required.
# Usage:  .\run-corba.ps1 clean compile
#         .\run-corba.ps1 compile exec:exec@server
#         .\run-corba.ps1 compile exec:exec@client

$versionLine = (& java -version 2>&1 | Select-Object -First 1).ToString()
$major = 0
if ($versionLine -match '"(\d+)(\.(\d+))?') {
    $major = [int]$Matches[1]
    if ($major -eq 1) { $major = [int]$Matches[3] }   # old style 1.8 -> 8
}
if ($major -lt 17) {
    Write-Error "Java 17 or newer is required (found: $versionLine). Point JAVA_HOME / PATH to JDK 17 or 21."
    exit 1
}

mvn @args
