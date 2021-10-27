package com.jumpinjumpout.apk.lib;

import android.graphics.Bitmap;

public class ContactInfo {
	public ContactInfo(String contactname, Bitmap photo) {
		sContactName = contactname;
		bmContactThumbnail = photo;
	}
	public String getContactName() {
		return sContactName;
	}
	public void setContactName(String sContactName) {
		this.sContactName = sContactName;
	}
	public Bitmap getContactThumbnail() {
		return bmContactThumbnail;
	}
	public void setContactThumbnail(Bitmap bmContactThumbnail) {
		this.bmContactThumbnail = bmContactThumbnail;
	}
	String sContactName;
	Bitmap bmContactThumbnail;
};
