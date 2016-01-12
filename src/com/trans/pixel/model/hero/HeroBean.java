package com.trans.pixel.model.hero;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.trans.pixel.constants.DirConst;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class HeroBean {
	private int id = 0;
	private String name = "";
	private int zhanli = 0;
	private int zhanli_lv = 0;
	private int ad = 0;
	private int ad_lv = 0;
	private int ap = 0;
	private int ap_lv = 0;
	private int arm  = 0;
	private int arm_lv = 0;
	private int mr = 0;
	private int mr_lv = 0;
	private List<HeroSkillBean> skillList = new ArrayList<HeroSkillBean>();
	private List<HeroStarBean> starList = new ArrayList<HeroStarBean>();
	private List<HeroEquipBean> equipList = new ArrayList<HeroEquipBean>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getZhanli() {
		return zhanli;
	}
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	public int getZhanli_lv() {
		return zhanli_lv;
	}
	public void setZhanli_lv(int zhanli_lv) {
		this.zhanli_lv = zhanli_lv;
	}
	public int getAd() {
		return ad;
	}
	public void setAd(int ad) {
		this.ad = ad;
	}
	public int getAd_lv() {
		return ad_lv;
	}
	public void setAd_lv(int ad_lv) {
		this.ad_lv = ad_lv;
	}
	public int getAp() {
		return ap;
	}
	public void setAp(int ap) {
		this.ap = ap;
	}
	public int getAp_lv() {
		return ap_lv;
	}
	public void setAp_lv(int ap_lv) {
		this.ap_lv = ap_lv;
	}
	public int getArm() {
		return arm;
	}
	public void setArm(int arm) {
		this.arm = arm;
	}
	public int getArm_lv() {
		return arm_lv;
	}
	public void setArm_lv(int arm_lv) {
		this.arm_lv = arm_lv;
	}
	public int getMr() {
		return mr;
	}
	public void setMr(int mr) {
		this.mr = mr;
	}
	public int getMr_lv() {
		return mr_lv;
	}
	public void setMr_lv(int mr_lv) {
		this.mr_lv = mr_lv;
	}
	public List<HeroSkillBean> getSkillList() {
		return skillList;
	}
	public void setSkillList(List<HeroSkillBean> skillList) {
		this.skillList = skillList;
	}
	public List<HeroStarBean> getStarList() {
		return starList;
	}
	public void setStarList(List<HeroStarBean> starList) {
		this.starList = starList;
	}
	public List<HeroEquipBean> getEquipList() {
		return equipList;
	}
	public void setEquipList(List<HeroEquipBean> equipList) {
		this.equipList = equipList;
	}
	public static String getZhanliLv() {
		return ZHANLI_LV;
	}
	public static String getAdLv() {
		return AD_LV;
	}
	public static String getApLv() {
		return AP_LV;
	}
	public static String getArmLv() {
		return ARM_LV;
	}
	public static String getMrLv() {
		return MR_LV;
	}

	public HeroEquipBean getEquip(int equipId) {
		for (HeroEquipBean equip : equipList) {
			if (equip.getEquipid() == equipId)
				return equip;
		}
		
		return null;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(NAME, name);
		json.put(ZHANLI, zhanli);
		json.put(ZHANLI_LV, zhanli_lv);
		json.put(AD, ad);
		json.put(AD_LV, ad_lv);
		json.put(AP, ap);
		json.put(AP_LV, ap_lv);
		json.put(ARM, arm);
		json.put(ARM_LV, arm_lv);
		json.put(MR, mr);
		json.put(MR_LV, mr_lv);
		json.put(SKILL, skillList);
		json.put(STAR, starList);
		json.put(EQUIP, equipList);
		
		return json.toString();
	}
	public static HeroBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		HeroBean bean = new HeroBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setName(json.getString(NAME));
		bean.setZhanli(json.getInt(ZHANLI));
		bean.setZhanli_lv(json.getInt(ZHANLI_LV));
		bean.setAd(json.getInt(AD));
		bean.setAd_lv(json.getInt(AD_LV));
		bean.setAp(json.getInt(AP));
		bean.setAp_lv(json.getInt(AP_LV));
		bean.setArm(json.getInt(ARM));
		bean.setArm_lv(json.getInt(ARM_LV));
		bean.setMr(json.getInt(MR));
		bean.setMr_lv(json.getInt(MR_LV));
		
		List<HeroSkillBean> skillList = new ArrayList<HeroSkillBean>();
		JSONArray skillArray = TypeTranslatedUtil.jsonGetArray(json, SKILL);
		for (int i = 0;i < skillArray.size(); ++i) {
			HeroSkillBean skill = HeroSkillBean.fromJson(skillArray.getString(i));
			skillList.add(skill);
		}
		bean.setSkillList(skillList);
		
		List<HeroStarBean> starList = new ArrayList<HeroStarBean>();
		JSONArray starArray = TypeTranslatedUtil.jsonGetArray(json, STAR);
		for (int i = 0;i < starArray.size(); ++i) {
			HeroStarBean star = HeroStarBean.fromJson(starArray.getString(i));
			starList.add(star);
		}
		bean.setStarList(starList);
		
		List<HeroEquipBean> equipList = new ArrayList<HeroEquipBean>();
		JSONArray equipArray = TypeTranslatedUtil.jsonGetArray(json, EQUIP);
		for (int i = 0;i < equipArray.size(); ++i) {
			HeroEquipBean equip = HeroEquipBean.fromJson(equipArray.getString(i));
			equipList.add(equip);
		}
		bean.setEquipList(equipList);
		
		return bean;
	}
	
	public static List<HeroBean> xmlParse() {
		Logger logger = Logger.getLogger(HeroBean.class);
		List<HeroBean> list = new ArrayList<HeroBean>();
		String fileName = FILE_NAME;
		try {
			String filePath = DirConst.getConfigXmlPath(fileName);
			SAXReader reader = new SAXReader();
			InputStream inStream = new FileInputStream(new File(filePath));
			Document doc = reader.read(inStream);
			// 获取根节点
			Element root = doc.getRootElement();
			List<?> heroList = root.elements();
			for (int i = 0; i < heroList.size(); i++) {
				HeroBean hero = new HeroBean();
				Element heroElement = (Element) heroList.get(i);
				hero.setId(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(ID)));
				hero.setName(heroElement.attributeValue(NAME));
				hero.setZhanli(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(ZHANLI)));
				hero.setZhanli_lv(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(ZHANLI_LV)));
				hero.setAd(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(AD)));
				hero.setAd_lv(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(AD_LV)));
				hero.setAp(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(AP)));
				hero.setAp_lv(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(AP_LV)));
				hero.setArm(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(ARM)));
				hero.setArm_lv(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(ARM_LV)));
				hero.setMr(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(MR)));
				hero.setMr_lv(TypeTranslatedUtil.stringToInt(heroElement.attributeValue(MR_LV)));
				hero.setSkillList(HeroSkillBean.xmlParse(heroElement.element(SKILL)));
				hero.setStarList(HeroStarBean.xmlParse(heroElement.element(STAR)));
				hero.setEquipList(HeroEquipBean.xmlParse(heroElement.element(EQUIP)));
				
				list.add(hero);
			}
		} catch (Exception e) {
			logger.error("parse " + fileName + " failed");
		}

		return list;
	}
	
	private static final String FILE_NAME = "lol_hero.xml";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String ZHANLI = "zhanli";
	private static final String ZHANLI_LV = "zhanli_lv";
	private static final String AD = "ad";
	private static final String AD_LV = "ad_lv";
	private static final String AP = "ap";
	private static final String AP_LV = "ap_lv";
	private static final String ARM = "arm";
	private static final String ARM_LV = "arm_lv";
	private static final String MR = "mr";
	private static final String MR_LV = "mr_lv";
	private static final String SKILL = "skill";
	private static final String STAR = "star";
	private static final String EQUIP = "equip";
}
