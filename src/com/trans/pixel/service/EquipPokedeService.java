package com.trans.pixel.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;

@Service
public class EquipPokedeService {
	private static final Logger log = LoggerFactory.getLogger(EquipPokedeService.class);
	 
	public ResultConst heroStrengthen(UserEquipPokedeBean pokede, UserBean user, List<UserPropBean> propList) {
		pokede.setLevel(pokede.getLevel() + 1);
		
		return SuccessConst.HERO_STRENGTHEN_FAILED_SUCCESS;
	}
}
