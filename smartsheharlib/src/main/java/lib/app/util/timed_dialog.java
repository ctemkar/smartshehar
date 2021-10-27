package lib.app.util;



public abstract class timed_dialog {
    private String title;
    private String message;
    private String positiveBttnText;
    private String negativeBttnText;
    private int time;//time in seconds

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPositiveBttnText() {
        return positiveBttnText;
    }

    public void setPositiveBttnText(String positiveBttnText) {
        this.positiveBttnText = positiveBttnText;
    }

    public String getNegativeBttnText() {
        return negativeBttnText + "(" + time + ") ";
    }

    public void setNegativeBttnText(String negativeBttnText) {
        this.negativeBttnText = negativeBttnText;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}