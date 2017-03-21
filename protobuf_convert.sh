#!/bin/sh
declare -A map=()
packagepath=""
package=""
packages=()
content=""
function pushCommand()
{
	while [ 0 -lt $# ];
	do
		if [[ $1 == "message" ]]
		then
	    	shift
	    	map[$1]=$1
	    elif [[ $1 == "//////////" ]]
	    then
	    	shift
	    	if [[ $package != "" ]]
	    	then
	    		echo  "$package"
	    		echo -e "$packagepath;\n" > pixel_proto/$package.proto
	    		if [[ $package != "Base" ]] && [[ $package != "Request" ]] && [[ $package != "Response" ]]
    			then
		    		echo "import \"Base.proto\";" >> pixel_proto/$package.proto
		    	fi
	    		if [[ $package != "Request" ]] && [[ $package != "Response" ]]
    			then
	    			packages[${#packages[@]}]=$package
			    else
		    		for i in ${packages[*]}
		    		do
			    		echo "import \"$i.proto\";" >> pixel_proto/$package.proto
			    	done
			    fi
	    		echo -e $content >> pixel_proto/$package.proto
	    		content=""
	    	fi
	    	package=$1
    		# package=${package:0:${#package}-1}
		fi
	    shift
	done
	return 0;
}
cat Commands.proto|while read line
do
	if [[ $line == "message "* ]]
		then
		pushCommand $line
	elif [[ $line == "////////// "* ]]
	then
		pushCommand $line
	fi
	if [[ $line == "package "* ]]
	then
		packagepath=${line:0:${#line}-1}
	else
		content+=$line"\n"
	fi
	# echo $line
	# sleep 1
done
proto_dir=pixel_proto
# protoc -I=${proto_dir}/ --cpp_out=${proto_dir}/ ${proto_dir}/*proto
protoc -I=${proto_dir}/ --java_out=${proto_dir}/../src/ ${proto_dir}/*proto
#protoc -I=${proto_dir}/ --plugin=protoc-gen-lua=${proto_dir}/protoc-gen-lua/protoc-gen-lua --lua_out=${proto_dir}/ ${proto_dir}/*proto