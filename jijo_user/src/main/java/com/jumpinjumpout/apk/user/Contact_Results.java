package com.jumpinjumpout.apk.user;

public class Contact_Results {

	String iD = "";
	String contact_Name = "";
	String contact_phone = "";
	byte[] contact_image;
	String contact_Email = "";
	String contact_Member_ID = "";
	String contact_Member_Trip_ID = "";
	String community_name = "";

	public void setID(String id) {
		this.iD = id;
	}

	public String getID() {
		return iD;
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

	public void setContactImage(byte[] cImage) {
		this.contact_image = cImage;
	}

	public void setContactEmail(String cPhoneEmail) {
		this.contact_Email = cPhoneEmail;
	}

	public String getContactEmail() {
		return contact_Email;
	}

	public void setMemberID(String cMemberID) {
		this.contact_Member_ID = cMemberID;
	}

	public void setMemberTripID(String cMemberTripID) {
		this.contact_Member_Trip_ID = cMemberTripID;
	}

	public void setCommunityName(String scommunityname) {
		this.community_name = scommunityname;
	}

	public String getMCommunityName() {
		return community_name;
	}

}
