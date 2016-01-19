
#protoc -I=app/res/protobuf/ --cpp_out=app/src/app/update/command/protobuf/ app/res/protobuf/*proto
protoc -I=app/res/protobuf/ --java_out=../ app/res/protobuf/*proto
protoc -I=app/res/protobuf/ --plugin=protoc-gen-lua=./protoc-gen-lua/protoc-gen-lua --lua_out=app/res/protobuf/lua_code/ app/res/protobuf/*proto
