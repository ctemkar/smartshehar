package in.bestbus.app;

import java.text.DecimalFormat;

public class FrequencyInfo {
    String sRouteCode;
    String sFirstStop, sLastStop;
    float fFirstFrom, fLastFrom, fFirstTo, fLastTo;
    String sFirstFrom, sLastFrom, sFirstTo, sLastTo;
    int iAm, iPm, iNoon;
    String sAm, sPm, sNoon;

    FrequencyInfo(String fs, String ls, float ff, float lf, float ft, float lt, int am, int pm, int noon) {
        DecimalFormat dec = new DecimalFormat("#.00");

        sFirstStop = fs;
        sLastStop = ls;
        sFirstFrom = dec.format(ff);
        sLastFrom = dec.format(lf);
        sFirstTo = dec.format(ft);
        sLastTo = dec.format(lt);
        iAm = am;
        iPm = pm;
        iNoon = noon;
        sAm = dec.format(am);
        sPm = dec.format(pm);
        sNoon = dec.format(noon);

    }
}

