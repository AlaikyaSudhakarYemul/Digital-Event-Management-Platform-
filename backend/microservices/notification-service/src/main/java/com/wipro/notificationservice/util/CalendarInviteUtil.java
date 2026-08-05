package com.wipro.notificationservice.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CalendarInviteUtil {

    public static String generateICS(String eventName, String description,
                                     LocalDate date, LocalTime time, String location) {
        var startDateTime = date.atTime(time);
        var endDateTime = startDateTime.plusHours(1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        return "BEGIN:VCALENDAR\n" +
               "VERSION:2.0\n" +
               "BEGIN:VEVENT\n" +
               "SUMMARY:" + eventName + "\n" +
               "DTSTART:" + startDateTime.format(dtf) + "\n" +
               "DTEND:" + endDateTime.format(dtf) + "\n" +
               "LOCATION:" + (location != null ? location : "") + "\n" +
               "DESCRIPTION:" + description + "\n" +
               "END:VEVENT\n" +
               "END:VCALENDAR";
    }
}
