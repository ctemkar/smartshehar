package in.bestbus.app;

import android.annotation.SuppressLint;
import android.graphics.Color;

public class CBus {
    public String msBusLabel, msBusNo, msBusNoDevanagari, msBusNoHTML;
    public String msPrefixBus = "", msSuffixBus = "";
    public int miBusNo, miBusType;
    public int busColor;
    public String msStopName;
    public String msFirstStop, msLastStop;
    public int miFromStopCode, miToStopCode;
    public String msRouteCode;
    public double stopLat, stopLon;
    public String msEtaUp = "", msEtaDn = "";
    public double mdLatu, mdLonu, mdLatd, mdLond;
    public double mdDistUp, mdDistDn;
    public String msFrequencyUp, msFrequencyDn;
    public String msDirection;
    public String msAcTimeUp, msAcTimeDn,msLastAccess;
    public double miMyDistanceBusup = 0.0, miMyDistanceBusdown = 0.0,miSpeed;
    CStop moStop;
    int miStopId;
    int miStopSerial;
    int iFirstStopId, iLastStopId;



    public CBus() {

    }

    CBus(String sRouteCode) {
        msRouteCode = sRouteCode;
        miBusNo = Integer.parseInt(sRouteCode.substring(0, 3));
        miBusType = Integer.parseInt(sRouteCode.substring(3, 4));
        setBusNo(miBusNo, miBusType);
        busColor = getBusColor(miBusType);
        msBusNoHTML = "<font size=\"5\" face=\"arial\">" + msBusLabel + "</font>" +
                "<br><font size=\"10\" face=\"arial\" >" + msBusNoDevanagari + "</font>";
    }

    CBus(int busno, int bustype) {
        setBusNo(busno, bustype);
        busColor = getBusColor(bustype);
        msBusNoHTML = "<font size=\"5\" face=\"arial\">" + msBusLabel + "</font>" +
                "<br><font size=\"10\" face=\"arial\" >" + msBusNoDevanagari + "</font>";
    }

    private void setBusNo(int busno, int bustype) {
        miBusNo = busno;
        msPrefixBus = "";
        msSuffixBus = "";
        mdDistUp = 0;
        mdDistDn = 0;
        miBusType = bustype;
        msBusNoDevanagari = TranslateNumber(Integer.toString(busno));
        msBusNo = Integer.toString(busno);
        switch (bustype) {
            case 1:
                msSuffixBus = " L";
                msBusLabel = msPrefixBus + msBusNo + msSuffixBus;
                msBusNoHTML = msBusLabel + "<br><h1>" + msBusNoDevanagari + "</h1>";
                break;
            case 2:
                msSuffixBus = " Ext";
                msBusLabel = msBusNo + "\nExt";
                break;
            case 3:
                msSuffixBus = " L Ext";
                msBusLabel = msBusNo + "\nL Ext";
                msBusNoHTML = msBusLabel + "<br><h1>" + msBusNoDevanagari + "</h1>";
                break;
            case 5:
                msSuffixBus = " L RR";
                msBusLabel = msBusNo + "\nL RR";
                break;
            case 6:
                msPrefixBus = "C-";
                msSuffixBus = " E";
                msBusLabel = msPrefixBus + msBusNo + msSuffixBus;
//				msBusLabel = "C-" + msBusNo + "\nL Ext";
                break;
            case 7:
                msPrefixBus = "AS-";
                msBusLabel = "AS-" + msBusNo;
                msBusNoHTML = msBusLabel + "<br><font-size='6'>" + msBusNoDevanagari + "</font>";
                break;
            case 8:
            case 9:
                msPrefixBus = "A-";
                msSuffixBus = "E";
                ;
                msBusLabel = "A-" + msBusNo + "\nExp";
                break;
            default:
                msBusLabel = Integer.toString(busno);
                msBusNoHTML = msBusLabel + "<br><font-size=40dp>" + msBusNoDevanagari + "";

        }
        msBusNo = msPrefixBus + msBusNo + msSuffixBus;
    }

    private int getBusColor(int bustype) {
        switch (bustype) {
            case 1:
            case 3:
            case 5:
                return Color.RED;
            case 6:
                return Color.BLUE;
            case 7:
            case 8:
            case 9:
                return Color.MAGENTA;
            default:
                return Color.BLACK;
        }
    }

    // Translate digits to Devanagari
    @SuppressLint("UseValueOf")
    String TranslateNumber(String sNum) {
        StringBuilder s = new StringBuilder();
//			String sNum = Integer.toString(num);
        sNum = sNum.replaceAll("[^0-9]", "");

        int nDigits = sNum.length();
        int n;
        for (int i = 0; i < nDigits; i++) {
//				c = sNum.charAt(nDigits);
            n = new Integer(sNum.substring(i, i + 1));
            switch (n) {
                case 0:
                    s.append("\u0966");
                    break;
                case 1:
                    s.append("\u0967");
                    break;
                case 2:
                    s.append("\u0968");
                    break;
                case 3:
                    s.append("\u0969");
                    break;
                case 4:
                    s.append("\u096A");
                    break;
                case 5:
                    s.append("\u096B");
                    break;
                case 6:
                    s.append("\u096C");
                    break;
                case 7:
                    s.append("\u096D");
                    break;
                case 8:
                    s.append("\u096E");
                    break;
                case 9:
                    s.append("\u096F");
                    break;

            }
//				s.append(new String(buffer, 0, 1, "UTF-16"));
//				s.appendCodePoint (967 + n);

        }
        return s.toString();
    }

    public double getMyDistanceBusUP() {
        return miMyDistanceBusup;
    }

    public void setMyDistanceBusUP(double iMyDistanceBusup) {
        this.miMyDistanceBusup = iMyDistanceBusup;
    }

    public double getMyDistanceBusDOWN() {
        return miMyDistanceBusdown;
    }

    public void setMyDistanceBusDOWN(double iMyDistanceBusdown) {
        this.miMyDistanceBusdown = iMyDistanceBusdown;
    }

    public double getSpeed() {
        return miSpeed;
    }

    public void setSpeed(double iSpeed) {
        this.miSpeed = iSpeed;
    }

    public String getLastAccess() {
        return msLastAccess;
    }

    public void setLastAccess(String sLastAccess) {
        this.msLastAccess = sLastAccess;
    }


}

