package com.ericsson.soiv.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalculateDateTime {
    public String getCurrentTimeInMilliSeconds() {
        Instant instant = Instant.now();
        long currentTimeStamp = instant.getEpochSecond();
        return String.valueOf(currentTimeStamp);
    }

    public String getCurrentDate() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ss+01:00");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        return df.format(date);
    }

    public String getCurrentDateTime() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        return df.format(date);
    }

    public String getNextDayDate() {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, 1);
        Date currentDatePlusOne = c.getTime();
        return dateFormat.format(currentDatePlusOne);

    }
}
