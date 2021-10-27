package com.jumpinjumpout.apk.user;

public class Contact {

	// private variables
	int _id;
	private String _name;
	private String _number;
	private byte[] _image;

	// Empty constructor
	public Contact() {

	}

	// constructor
	public Contact(int keyId, String name, String number, byte[] image) {
		this._id = keyId;
		this.set_name(name);
		this.set_number(number);
		this.set_image(image);

	}

	// constructor
	public Contact(String contactID, String name, String number, byte[] image) {
		this.set_name(name);
		this.set_number(number);
		this.set_image(image);

	}

	// constructor
	public Contact(String name, String number, byte[] image) {
		this.set_name(name);
		this.set_number(number);
		this.set_image(image);
	}

	// getting name
	public String getName() {
		return this.get_name();
	}

	// setting name
	public void setName(String name) {
		this.set_name(name);
	}

	// getting number
	public String getNumber() {
		return this.get_number();
	}

	// setting number
	public void setNumber(String number) {
		this.set_number(number);
	}

	// getting phone number
	public byte[] getImage() {
		return this.get_image();
	}

	// setting phone number
	public void setImage(byte[] image) {
		this.set_image(image);
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public String get_number() {
		return _number;
	}

	public void set_number(String _number) {
		this._number = _number;
	}

	public byte[] get_image() {
		return _image;
	}

	public void set_image(byte[] _image) {
		this._image = _image;
	}
}
