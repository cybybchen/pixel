proto_dir=pixel_proto
# protoc -I=${proto_dir}/ --cpp_out=${proto_dir}/ ${proto_dir}/*proto
protoc -I=${proto_dir}/ --java_out=${proto_dir}/../src/ ${proto_dir}/*proto
#protoc -I=${proto_dir}/ --plugin=protoc-gen-lua=${proto_dir}/protoc-gen-lua/protoc-gen-lua --lua_out=${proto_dir}/ ${proto_dir}/*proto
