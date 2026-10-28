Write-Host "========================================================" -ForegroundColor Red
Write-Host "  Iniciando Sala 4: El Espejo (Tunel del Terror)" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Red
$env:PATH = "C:\Users\anjla\apache-maven-3.9.6\bin;" + $env:PATH
mvn clean javafx:run
