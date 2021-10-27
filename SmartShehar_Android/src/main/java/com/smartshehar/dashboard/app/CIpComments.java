package com.smartshehar.dashboard.app;

/**
 * Created by asmita on 27/01/2016.
 */
public class CIpComments {
    String comment, date, username,
            email, phoneno, sImagePath, sImageName;

    public CIpComments(String comment, String date, String username,
                       String email, String phoneno, String sImagePath, String sImageName) {
        this.comment = comment;
        this.date = date;
        this.username = username;
        this.email = email;
        this.phoneno = phoneno;
        this.sImagePath = sImagePath;
        this.sImageName = sImageName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getsImagePath() {
        return sImagePath;
    }

    public void setsImagePath(String sImagePath) {
        this.sImagePath = sImagePath;
    }

    public String getsImageName() {
        return sImageName;
    }

    public void setsImageName(String sImageName) {
        this.sImageName = sImageName;
    }
}
