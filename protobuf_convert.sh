#!/bin/sh
declare -A map=()
packagepath=""
package=""
packages=()
content=""
proto_dir=pixel_proto/pb/
mkdir -p $proto_dir
function pushCommand()
{
	while [ 0 -lt $# ];
	do
		if [[ $1 == "message" ]]
		then
			shift
			if [[ $1 == *"Command" ]]
			then
				map[$1]=$package.$1
			fi
		elif [[ $1 == "//////////" ]]
		then
			shift
			if [[ $package != "" ]]
			then
				echo  "$package"
				echo "$packagepath;" > ${proto_dir}/$package.proto
				if [[ $package != "Base" ]] && [[ $package != "Request" ]] && [[ $package != "Response" ]] && [[ $package != "Commands" ]]
				then
					echo "import \"Base.proto\";" >> ${proto_dir}/$package.proto
				fi
				if [[ $package != "Request" ]] && [[ $package != "Response" ]] && [[ $package != "Commands" ]]
				then
					if [[ $package != "Base" ]]
					then
						packages[${#packages[@]}]=$package
					fi
				else
					for i in ${packages[*]}
					do
						echo "import \"$i.proto\";" >> ${proto_dir}/$package.proto
					done
				fi
				echo -e $content >> ${proto_dir}/$package.proto
				content=""
			fi
			package=$1
			# package=${package:0:${#package}-1}
		fi
		shift
	done
	return 0;
}
while read line
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
	# echo ${map[@]} 
	# echo $line
	# sleep 1
done < pixel_proto/Commands.proto
# echo ${map[@]}

# protoc -I=${proto_dir}/ --cpp_out=${proto_dir}/ ${proto_dir}/*proto
protoc -I=${proto_dir}/ --java_out=${proto_dir}/../../src/ ${proto_dir}/*proto
#protoc -I=${proto_dir}/ --plugin=protoc-gen-lua=${proto_dir}/protoc-gen-lua/protoc-gen-lua --lua_out=${proto_dir}/ ${proto_dir}/*proto

proto_java=src/com/trans/pixel/controller/chain/RequestScreen.java



declare -A commandNames=()
for line in `cat pixel_proto/pb/Request.proto`
# while read line
do
	if [[ $line == "optional" ]]
	then
		getcommand=0
	elif [[ $getcommand == 0 ]]
	then
		myCommand=$line
		getcommand=1
	elif [[ $getcommand == 1 ]]
	then
		commandNames[$myCommand]=`echo $line|sed 's/^\(.\)/\U\1/g'`
		getcommand=2
	fi
done
echo -e "package com.trans.pixel.controller.chain;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.ServerBean;
import com.trans.pixel.model.ServerTitleBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.ServerTitleService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.utils.TypeTranslatedUtil;
import com.trans.pixel.protoc.ServerProto.HeadInfo.SERVER_STATUS;
import com.trans.pixel.protoc.ServerProto.HeadInfo;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;" > $proto_java

for key in ${!commandNames[@]}  
do  
    echo "import com.trans.pixel.protoc.${map[$key]};" >> $proto_java
done 

echo -e "
public abstract class RequestScreen implements RequestHandle {
	private static final Logger log = LoggerFactory.getLogger(RequestScreen.class);
	@Resource
	private UserService userService;
	@Resource
	private ServerService serverService;
	@Resource
	private ServerTitleService serverTitleService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private LootService lootService;

	protected abstract boolean handleRegisterCommand(RequestCommand cmd, Builder responseBuilder);
	protected abstract boolean handleLoginCommand(RequestCommand cmd, Builder responseBuilder);
	protected abstract boolean pushNoticeCommand(Builder responseBuilder, UserBean user);" >> $proto_java

for key in ${!commandNames[@]}  
do  
    echo "	protected abstract boolean handleCommand($key cmd, Builder responseBuilder, UserBean user);" >> $proto_java
done 

echo -e "	private HeadInfo buildHeadInfo(HeadInfo head) {
		HeadInfo.Builder nHead = HeadInfo.newBuilder(head);
		ServerBean server = serverService.getServer(head.getServerId());
		if (server == null)
			return null;
		nHead.setServerstarttime(server.getKaifuTime());
		nHead.setDatetime(System.currentTimeMillis() / 1000);
		nHead.setOnlineStatus(serverService.getOnlineStatus(head.getVersion()+\"\"));
		nHead.setGameVersion(serverService.getGameVersion());
		nHead.setServerStatus(server.getStatus());
		
		return nHead.build();
	}

	@Override
	public boolean handleRequest(PixelRequest req, PixelResponse rep) {
		RequestCommand request = req.command;
		HeadInfo head = buildHeadInfo(request.getHead());
		if (head == null && !request.hasLogCommand()) {
			rep.command.setHead(request.getHead());
			ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
			erBuilder.setCode(String.valueOf(ErrorConst.SRVER_NOT_OPEN_ERROR.getCode()));
			erBuilder.setMessage(ErrorConst.SRVER_NOT_OPEN_ERROR.getMesssage());
			rep.command.setErrorCommand(erBuilder.build());
			log.error(\"cmd server not open:\" + req);
			return false;
		}
	
		if (head != null)
			rep.command.setHead(head);
		else 
			rep.command.setHead(request.getHead());	
		
		if (head != null && head.getServerStatus() == SERVER_STATUS.SERVER_MAINTENANCE_VALUE) {
			ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
			erBuilder.setCode(String.valueOf(ErrorConst.SRVER_MAINTENANCE_OPEN_ERROR.getCode()));
			erBuilder.setMessage(ErrorConst.SRVER_MAINTENANCE_OPEN_ERROR.getMesssage());
			rep.command.setErrorCommand(erBuilder.build());
			log.error(\"cmd server maintenance:\" + req);
			return false;
		}

		ServerTitleInfo.Builder serverTitleBuilder = ServerTitleInfo.newBuilder();
		serverTitleBuilder.addAllTitle(serverTitleService.selectServerTileListByServerId(request.getHead().getServerId()));
		rep.command.setTitle(serverTitleBuilder.build());

		ResponseCommand.Builder responseBuilder = rep.command;
		UserBean user = null;

		if (request.hasRegisterCommand()) {
			handleRegisterCommand(request, responseBuilder);
			return false;
		} else if (request.hasLoginCommand()) {
			handleLoginCommand(request, responseBuilder);
			return false;
		} else {
		    long userId = head != null ? head.getUserId() : 0;
		    req.user = userService.getUser(userId);
			user = req.user;

		    if (request.hasLogCommand()) {
		        RequestLogCommand cmd = request.getLogCommand();
		        handleCommand(cmd, responseBuilder, user);//LogCommand
		    }//LogCommand
		    
			if (user == null || !user.getSession().equals(head.getSession())) {
		    	ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
		        erBuilder.setCode(String.valueOf(ErrorConst.USER_NEED_LOGIN.getCode()));
		        erBuilder.setMessage(ErrorConst.USER_NEED_LOGIN.getMesssage());
		        if(!request.hasLogCommand())
		    		rep.command.setErrorCommand(erBuilder.build());
		        log.info(\"cmd user need login:\" + req);
		        return false;
		    }
		}

		boolean result = true;" >> $proto_java

for key in ${!commandNames[@]}  
do  
    echo -e "		if (request.has${commandNames[$key]}()) {
			$key cmd = request.get${commandNames[$key]}();
			if (result) result = handleCommand(cmd, responseBuilder, user);
		}" >> $proto_java
done 

echo -e "		if (result && user != null && !request.hasQueryRechargeCommand() && !request.hasLogCommand())
			pushNoticeCommand(responseBuilder, user);

		UserLevelBean userLevel = lootService.calLoot(user);
		if (userLevel != null) {
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			rep.command.setLevelLootCommand(userLevel.build());
		}

		return result;
	}
}" >> $proto_java
