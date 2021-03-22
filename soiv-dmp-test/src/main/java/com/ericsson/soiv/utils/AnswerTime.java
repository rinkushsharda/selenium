package com.ericsson.soiv.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.ericsson.soiv.testbases.SoivTestBase;

public class AnswerTime extends SoivTestBase {
	
    public String determineAnswerTime() {
        String peakAnswerTime = "";
        String offpeakAnswerTime = "";
        String peakoffpeakAnswerTime = "";
        DateTime dateTime = new DateTime();
        String day = dateTime.dayOfWeek().getAsText();
        int peakDaysBack = 7; // How many days back to reach a peekDay
        int offPeakDaysBack = 0; // How many days back to reach a offpeekDay
        int peakOffPeakDaysBack = 0; // How many days to reach the day before an offpeakDay
        int peakStartHour = 5; // Start hour a peekDay so we don't end up in an offpeekDay when test is done
        int offPeakStartHour = 5; // Start hour a offpeekDay so we don't end up in a peekDay when test is done
        log("day:" + day);
        switch (day) {
            case "Monday":
                offPeakDaysBack = 2;
                peakOffPeakDaysBack = 3;
                break;
            case "Tuesday":
                offPeakDaysBack = 3;
                peakOffPeakDaysBack = 4;
                break;
            case "Wednesday":
                offPeakDaysBack = 4;
                peakOffPeakDaysBack = 5;
                break;
            case "Thursday":
                offPeakDaysBack = 5;
                peakOffPeakDaysBack = 6;
                break;
            case "Friday":
                offPeakDaysBack = 6;
                peakOffPeakDaysBack = 7;
                break;
            case "Saturday":
                peakDaysBack = 2;
                peakOffPeakDaysBack = 1;
                break;
            case "Sunday":
                peakDaysBack = 3;
                offPeakDaysBack = 1;
                peakOffPeakDaysBack = 2;
                break;
        }
        peakAnswerTime =
                dateTime.minusDays(peakDaysBack).hourOfDay().setCopy(peakStartHour).toString(DateTimeFormat.forPattern("yyMMddHHmmssZ"))
                    .replace("+", "2B").replace("-", "2D");
        offpeakAnswerTime =
                dateTime.minusDays(offPeakDaysBack).hourOfDay().setCopy(offPeakStartHour).toString(DateTimeFormat.forPattern(
                        "yyMMddHHmmssZ")).replace("+", "2B").replace("-", "2D");
        peakoffpeakAnswerTime =
                dateTime.minusDays(peakOffPeakDaysBack).hourOfDay().setCopy(23).minuteOfHour().setCopy(55).secondOfMinute().setCopy(00)
                    .toString(DateTimeFormat.forPattern("yyMMddHHmmssZ")).replace("+", "2B").replace("-", "2D");
        log("Peak AnswerTime:        " + peakAnswerTime);
        log("Off Peak AnswerTime:    " + offpeakAnswerTime);
        log("Peak-OffPeak AnswerTime:" + peakoffpeakAnswerTime);

        return peakAnswerTime;
    }
}
