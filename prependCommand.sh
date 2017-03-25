#!/bin/sh 
function AddCommands()
{
	tag1="\/\/add import here"
	tag2="\/\/add handleCommand here"
	tag3="\/\/call handleCommand here"
	while [ 0 -lt $# ];
	do
		if grep $1 src/com/trans/pixel/controller/chain/RequestScreen.java>/tmp/null
		then
			# echo "$class exsited"
	    	shift
		else
			if grep "Request.*Command"<<<$1>/tmp/null
			then
				class=$1
				name=`echo $2|sed 's/^\(.\)/\U\1/g'`
				shift
				echo "adding $class $name"

				append1="import com.trans.pixel.protoc.${package}.$class;"
				sed -i "s/$tag1/$append1\n$tag1/g" src/com/trans/pixel/controller/chain/RequestScreen.java
				append2="protected abstract boolean handleCommand($class cmd, Builder responseBuilder, UserBean user);"
				sed -i "s/$tag2/$append2\n	$tag2/g" src/com/trans/pixel/controller/chain/RequestScreen.java
				append3="if (request.has$name()) {\n            $class cmd = request.get$name();\n            if (result)\/\/$name\n                result = handleCommand(cmd, responseBuilder, user);\/\/$name\n        }\/\/$name"
				sed -i "s/$tag3/$append3\n        $tag3/g" src/com/trans/pixel/controller/chain/RequestScreen.java

				append1="import com.trans.pixel.protoc.${package}.$class;"
				sed -i "s/$tag1/$append1\n$tag1/g" src/com/trans/pixel/controller/chain/GameDataScreen.java
				append2="\@Override\/\/$name\n	protected boolean handleCommand($class cmd, Builder responseBuilder, UserBean user) {\n		\/\/ TODO $name method\n		return true;\/\/$name\n	}\/\/$name"
				sed -i "s/$tag2/$append2\n	$tag2/g" src/com/trans/pixel/controller/chain/GameDataScreen.java
			fi
		fi
	    shift
	done
	return 0;
}

function getClassName() {
	while [ 0 -lt $# ];	
	do
		if [[ $1 == "//////////" ]]
		then
		shift
		else
			package=$1
		shift
		fi
	done
	return 0;
}

function handleRequestCommand {
	while [ 0 -lt $# ];
	do
		if [[ $1 == "message" ]]
		then
		shift
		elif [[ $1 == "Request"*Command ]]
		then
			AddCommands `grep $1 pixel_proto/pb/Request.proto`
		shift
		else
		shift
		fi
	done
	return 0;
}
package=""
#AddCommands `grep "Request.*Command.*=" pixel_proto/Commands.proto`
cat pixel_proto/Commands.proto | while read line
do
	if [[ $line == "////////// Request" ]]
	then
		continue
	elif [[ $line == "////////// "* ]]
	then
		getClassName $line
	elif [[ $line == "message Request"* ]]
	then
		handleRequestCommand $line
	fi
done
