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

				append1="import com.trans.pixel.protoc.Commands.$class;\n"
				sed -i "s/$tag1/$append1$tag1/g" src/com/trans/pixel/controller/chain/RequestScreen.java
				append2="\n	protected abstract boolean handleCommand($class cmd, Builder responseBuilder, UserBean user);\n	"
				sed -i "s/$tag2/$append2$tag2/g" src/com/trans/pixel/controller/chain/RequestScreen.java
				append3="if (request.has$name()) {\n        	$class cmd = request.get$name();\n            if (result)\n                result = handleCommand(cmd, responseBuilder, user);\n        }\n		"
				sed -i "s/$tag3/$append3$tag3/g" src/com/trans/pixel/controller/chain/RequestScreen.java

				append1="import com.trans.pixel.protoc.Commands.$class;\n"
				sed -i "s/$tag1/$append1$tag1/g" src/com/trans/pixel/controller/chain/GameDataScreen.java
				append2="\n	\@Override\n	protected boolean handleCommand($class cmd, Builder responseBuilder, UserBean user) {\n		\/\/ TODO Auto-generated method stub\n		return true;\n	}\n	"
				sed -i "s/$tag2/$append2$tag2/g" src/com/trans/pixel/controller/chain/GameDataScreen.java

				append1="import com.trans.pixel.protoc.Commands.$class;\n"
				sed -i "s/$tag1/$append1$tag1/g" src/com/trans/pixel/controller/chain/HeadScreen.java
				append2="\n	\@Override\n	protected boolean handleCommand($class cmd, Builder responseBuilder, UserBean user) {\n		\/\/ TODO Auto-generated method stub\n		return true;\n	}\n	"
				sed -i "s/$tag2/$append2$tag2/g" src/com/trans/pixel/controller/chain/HeadScreen.java
			fi
		fi
	    shift
	done
	return 0;
}
AddCommands `grep "Request.*Command.*=" pixel_proto/Commands.proto`