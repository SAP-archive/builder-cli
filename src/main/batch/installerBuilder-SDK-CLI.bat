cd %~dp0

mkdir "C:\hybris\BuilderSDK"
xcopy /y "builder-cli-shade.jar" "C:\hybris\BuilderSDK"
xcopy /y "builder.bat" "C:\hybris\BuilderSDK"

if exist C:\hybris\BuilderSDK\builder.exe del C:\hybris\BuilderSDK\builder.exe

@echo off
set pathStr=%PATH%
set empty=
set builderPath=C:\hybris\BuilderSDK

If "%pathStr%"=="%pathStr:BuilderSDK=%" (
	echo Update path ...
    setx PATH "%PATH%;C:\hybris\BuilderSDK" /M
)

pause