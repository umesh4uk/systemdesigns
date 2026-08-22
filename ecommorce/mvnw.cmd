@REM ----------------------------------------------------------------------------
@REM Maven wrapper batch script for Windows
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0")

@SET WRAPPER_JAR="%BASE_DIR%.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET WRAPPER_URL=

@IF NOT EXIST %WRAPPER_JAR% (
    @ECHO Downloading Maven wrapper...
    @FOR /F "usebackq tokens=2,*" %%A IN (`FINDSTR /I "wrapperUrl" "%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"`) DO (
        SET WRAPPER_URL=%%B
    )
    @POWERSHELL -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile %WRAPPER_JAR%"
)

@"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

@IF "%ERRORLEVEL%"=="0" (EXIT /B 0)
@EXIT /B 1
