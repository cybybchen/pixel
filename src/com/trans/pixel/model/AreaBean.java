package com.trans.pixel.model;

import com.google.protobuf.ByteString;
import com.trans.pixel.protoc.Commands.AreaInfo;

public class AreaBean {
	private int id;
	private long ownerId;
	private int rewardId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getOwner() {
		return ownerId;
	}
	public void setOwner(long owner) {
		this.ownerId = owner;
	}
	public int getRewardId() {
		return rewardId;
	}
	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}
	public static AreaBean parseFrom(String string) {
		AreaBean area = new AreaBean();
//		AreaInfo a = AreaInfo.parseFrom(data);
//		AreaInfo.Builder builder = AreaInfo.newBuilder();
//		AreaInfo a = builder.build();
//		ByteString str = a.toByteString();
		return area;
	}
}
