@echo off

set PATH=%PATH%;C:\Cast3M\PCW_20\bin\

:: echo stop to fill console when castem ask for keyboard input
CALL echo "stop" | castem20.bat %1 > castem.out

::<nul timeout /t 1 
::for /F "TOKENS=1,2,*" %%a in ('tasklist /FI "IMAGENAME eq bin_Cast3M*"') do set PID_R=%%a
::echo %PID_R% > PID

:::loop
::tasklist | find " %PID_R% " > pid.txt
::if not errorlevel 1 (
::    timeout /t 1 >nul
::    goto :loop
::)

<nul timeout /t 10 

for /F "TOKENS=*" %%a in ('findstr "ERREUR" castem.out') do set exit_line=%%a

::echo %exit_line% > log2.txt

for /f "tokens=2 delims=:" %%a in ("%exit_line%") do (
  set exit_line_end=%%a
)

set exit_line_end=%exit_line_end: =%
::echo %exit_line_end% >> log2.txt

set exit_code=12
for /f "tokens=1,* delims=*" %%a in ("%exit_line_end%") do (set exit_code=%%a%%b)

::echo %exit_code% >> log2.txt

exit %exit_code%