#!/bin/sh
proto_dir=pixel_proto/pb/
path=${proto_dir}/../../src/
for i in `find ${proto_dir} -name *.proto`;do
protoc -I=${proto_dir}/ --java_out=${path} $i
done
