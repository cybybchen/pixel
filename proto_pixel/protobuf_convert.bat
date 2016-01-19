@echo off
rem cd .\app\res\protobuf\
rem ..\..\..\tools\protobuf\protoc.exe --cpp_out=..\..\src\app\update\command\protobuf\ *.proto
.\tools\protobuf\protoc.exe -I=.\app\res\protobuf\ --cpp_out=.\app\src\app\update\command\protobuf\ .\app\res\protobuf\*.proto
.\tools\protobuf\protoc.exe -I=.\app\res\protobuf\ --java_out .\app\res\protobuf\java_code\ .\app\res\protobuf\*.proto
rem .\tools\protobuf\protoc.exe -h

@echo Convert finished!
pause