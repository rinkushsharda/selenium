// **********************************************************************
// Copyright (c) 2016 Telefonaktiebolaget LM Ericsson, Sweden.
// All rights reserved.
// The Copyright to the computer program(s) herein is the property of
// Telefonaktiebolaget LM Ericsson, Sweden.
// The program(s) may be used and/or copied with the written permission
// from Telefonaktiebolaget LM Ericsson or in accordance with the terms
// and conditions stipulated in the agreement/contract under which the
// program(s) have been supplied.
// **********************************************************************
package com.ericsson.soiv.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class DateTimeFactory {
    private final DateTime myDateTime;

    private DateTimeFactory(Builder builder) {
        myDateTime = builder.setValue;
    }

    public DateTime getDateTime() {
        return new DateTime(myDateTime);
    }

    public String getDateTimeFormatted() {
        return myDateTime.toString(DateTimeFormat.forPattern("yyMMddHHmmssZ")).replace("+", "2B").replace("-", "2D");
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(DateTime dateTime) {
        return new Builder(dateTime);
    }

    public static class Builder {
        Weekday weekday;
        DateTime setValue;

        public Builder() {
            setValue = DateTime.now();
        }

        public Builder(DateTime initialDateTime) {
            setValue = new DateTime(initialDateTime);
        }

        public Builder withYear(int year) {
            setValue = setValue.year().setCopy(year);
            return this;
        }

        public Builder withMonth(int month) {
            setValue = setValue.monthOfYear().setCopy(month);
            return this;
        }

        public Builder withDay(Weekday day) {
            return withDay(day.getIndex());
        }

        public Builder withDay(int expectedDay) {

            int currentDay = setValue.getDayOfWeek();

            if (currentDay < expectedDay) {
                setValue = setValue.minusDays(7 - (expectedDay - currentDay));
            } else if (currentDay > expectedDay) {
                setValue = setValue.minusDays(currentDay - expectedDay);
            }

            return this;
        }

        public Builder withDayOfMonth(int dayOfMonth) {
            setValue = setValue.dayOfMonth().setCopy(dayOfMonth);
            return this;
        }

        public Builder withDayOffset(int days) {
            setValue = setValue.plusDays(days);
            return this;
        }

        public Builder withHour(int hour) {
            setValue = setValue.hourOfDay().setCopy(hour);
            return this;
        }

        public Builder withMinute(int minute) {
            setValue = setValue.minuteOfHour().setCopy(minute);
            return this;
        }

        public Builder withSecond(int second) {
            setValue = setValue.secondOfMinute().setCopy(second);
            return this;
        }

        public DateTimeFactory build() {
            return new DateTimeFactory(this);
        }
    }

    public enum Weekday {
        MONDAY(1),
        TUESDAY(2),
        WEDNESDAY(3),
        THURSDAY(4),
        FRIDAY(5),
        SATURDAY(6),
        SUNDAY(7);

        private int weekdayIndex;

        Weekday(int index) {
            weekdayIndex = index;
        }

        public int getIndex() {
            return weekdayIndex;
        }
    }

}
