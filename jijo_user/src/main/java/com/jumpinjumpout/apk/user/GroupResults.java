package com.jumpinjumpout.apk.user;

public class GroupResults {
	private String group_Id = "";
	private String group_Name = "";
	private String group_Desc = "";
	private String group_Type = "";
	private String group_Fromadd = "";
	private String group_Toadd = "";
	private String group_ID_One = "";
	private String mine_flag = "";
	private String active_user = "";
	private String full_Nmae = "";

	public void setID(String iD) {
		this.group_Id = iD;
	}

	public String getID() {
		return group_Id;
	}

	public void setGroupName(String gName) {
		this.group_Name = gName;
	}

	public String getGroupName() {
		return group_Name;
	}

	public void setGroupDesc(String gDesc) {
		this.group_Desc = gDesc;
	}

	public String getGroupDesc() {
		return group_Desc;
	}

	public void setGroupType(String gType) {
		this.group_Type = gType;
	}

	public String getGroupType() {
		return group_Type;
	}

	public void setMine(String fMine) {
		this.mine_flag = fMine;
	}

	public String getMine() {
		return mine_flag;
	}

	public void setActive(String activeU) {
		this.active_user = activeU;
	}

	public String getActive() {
		return active_user;
	}

	public void setFullName(String fullName) {
		this.full_Nmae = fullName;
	}

	public String getFullName() {
		return full_Nmae;
	}
}
