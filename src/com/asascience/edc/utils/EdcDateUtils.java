package com.asascience.edc.utils;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class EdcDateUtils {
	private String[] dateFormats;
	public EdcDateUtils(){
		initDateFormats();
	}
	private void initDateFormats(){
		 // Create DateFormats
	    dateFormats = new String[12];
	    dateFormats[0] = "yyyy-MM-dd'T'HH:mm:ss'.'S'Z'";// Zulu with Millis
	    dateFormats[1] = "yyyy-MM-dd'T'HH:mm:ss'.'SZ";  // Millis, RFC822 zone
	    dateFormats[2] = "yyyy-MM-dd'T'HH:mm:ss'.'Sz";  // Millis, long zone
	    dateFormats[3] = "yyyy-MM-dd'T'HH:mm:ss'.'S";   // Millis, no zone
	    dateFormats[4] = "yyyy-MM-dd'T'HH:mm:ssZ";      // ISO8601 long, RFC822 zone
	    dateFormats[5] = "yyyy-MM-dd'T'HH:mm:ssz";      // ISO8601 long, long zone
	    dateFormats[6] = "yyyy-MM-dd'T'HH:mm:ss'Z'";    // Zulu
	    dateFormats[7] = "yyyy-MM-dd'T'HH:mm:ss";       // No 'Z', No zone
	    dateFormats[8] = "yyyy-MM-dd'T'HH:mm'Z'";         // No seconds, RFC822 zone
	    dateFormats[9] = "yyyy-MM-dd'T'HH:mmz";         // No seconds, long zone
	    dateFormats[10] = "yyyy-MM-dd'T'HH:mm";         // No seconds, no zone
	    dateFormats[11] = "yyyyMMddHHmmssZ";            // ISO8601 sho
	}
	public Date parseDate(String dateToParse){
		 Date parsedDate = null;
		    try {
		      parsedDate = DateUtils.parseDate(dateToParse, dateFormats);
		    } catch (ParseException pe) {
		      pe.printStackTrace();
		    }
		    return parsedDate;
	}
}
