@echo OFF

REM #################################################################################
REM ##
REM ## Copyright © 2014 Cask Data, Inc.
REM ##
REM ## Licensed under the Apache License, Version 2.0 (the "License"); you may not
REM ## use this file except in compliance with the License. You may obtain a copy of
REM ## the License at
REM ##
REM ## http://www.apache.org/licenses/LICENSE-2.0
REM ##
REM ## Unless required by applicable law or agreed to in writing, software
REM ## distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
REM ## WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
REM ## License for the specific language governing permissions and limitations under
REM ## the License.
REM ##
REM #################################################################################

SET CDAP_HOME=%~dp0
SET CDAP_HOME=%CDAP_HOME:~0,-5%
SET JAVACMD=%JAVA_HOME%\bin\java.exe

SET CLASSPATH=%CDAP_HOME%\lib\co.cask.cdap.cdap-cli-@@project.version@@.jar;%CDAP_HOME%\conf

REM Check for 64-bit version of OS. Currently not supporting 32-bit Windows
IF NOT EXIST "%PROGRAMFILES(X86)%" (
  echo 32-bit Windows operating system is currently not supported
  GOTO :FINALLY
)

REM Check for correct setting for JAVA_HOME path
if "%JAVA_HOME%" == "" (
  echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
  echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
  GOTO :FINALLY
)

REM Check for Java version
setlocal ENABLEDELAYEDEXPANSION
set /a counter=0
for /f "tokens=* delims= " %%f in ('"%JAVACMD%" -version 2^>^&1') do @(
  if "!counter!"=="0" set line=%%f
  set /a counter+=1
)
set line=%line:java version "1.=!!%
set line=%line:~0,1%
if NOT "%line%" == "7" (
  if NOT "%line%" == "8" (
    echo ERROR: Java version not supported. Please install Java 7 or 8 - other versions of Java are not supported.
    GOTO :FINALLY
  )
)
endlocal

"%JAVACMD%" -classpath "%CLASSPATH%" co.cask.cdap.cli.CLIMain %*

:FINALLY
