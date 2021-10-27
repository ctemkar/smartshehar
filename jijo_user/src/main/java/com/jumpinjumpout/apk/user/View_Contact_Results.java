package com.jumpinjumpout.apk.user;

public class View_Contact_Results {

	String iD = "";
	String contact_Name = "";
	String contact_phone = "";
	private byte[] contact_image;
	String contact_Email = "";
	int contact_Member_ID;

	public void setID(String id) {
		this.iD = id;
	}

	public void setContactName(String cName) {
		this.contact_Name = cName;
	}

	public String getContactName() {
		return contact_Name;
	}

	public void setContactPhone(String cPhone) {
		this.contact_phone = cPhone;
	}

	public String getContactPhone() {
		return contact_phone;
	}

	public void setContactEmail(String cPhoneEmail) {
		this.contact_Email = cPhoneEmail;
	}

	public String getContactEmail() {
		return contact_Email;
	}

	public void setMemberID(int contact_memberId_list) {
		this.contact_Member_ID = contact_memberId_list;
	}
}
