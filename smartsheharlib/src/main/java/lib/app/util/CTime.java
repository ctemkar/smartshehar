package lib.app.util;

import java.text.DecimalFormat;


public class CTime {
	public int miHr, miMin;
	public double mdTm;
	public String msTm;
	public String msHrMin;
	public int miTimeMin;
	public CTime(int iHr, int iMin) {
		setTime(iHr, iMin);
	}

	public CTime(int iTm) {
		int iHr =  (iTm/60);
		int iMin = (iTm % 60);
		setTime(iHr, iMin);
		
	}
	
	public void add(int increment) {
		int i = miMin + increment;
		if(i >= 60 ) {
			miHr = miHr + increment / 60;
			miMin = i - increment % 60;
		}
		setTime(miHr, miMin);
		
	}
	private void setTime(int iHr, int iMin) {
		miTimeMin = iHr * 60 + iMin;
		miHr = iHr; miMin = iMin;
		String sAmPm = " a";
		if(iHr >= 12 && iHr < 24)
			sAmPm = " p";
		mdTm = (iHr + iMin / 100.00);
		mdTm = mdTm < 1 ? mdTm + 12 : mdTm;
		if(mdTm >= 24) {
			mdTm = mdTm - 24;
		} 
		DecimalFormat dec = new DecimalFormat("#0.00");
		msTm = dec.format(mdTm >= 13 ? mdTm - 12 : mdTm);
		msTm += sAmPm;
		if(miHr > 0) 
			msHrMin = miHr + " hr " + (miMin > 0 ? miMin + " mn." : "");
		else
			msHrMin = miMin + " mn.";
		
	}
/*	public CTime timeDiff(CTime ctm) {
		int diff = (miHr * 60 + miMin) - (ctm.miHr * 60 - ctm.miMin);
		return new CTime((int)diff/60, (int)(diff % 60 * 100));
		
	}*/
	

} // class CTime
