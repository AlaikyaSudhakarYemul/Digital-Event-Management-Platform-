package com.wipro.demp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Event;

public class CalendarInviteUtil {
    public static String generateICS(Event event, Address address) {
        LocalDateTime startDateTime = event.getDate().atTime(event.getTime());
        LocalDateTime endDateTime = startDateTime.plusHours(1); // Default 1 hour duration
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        String location = "";
        if (address != null) {
            location = address.getAddress() + ", " +
                       address.getState() + ", " +
                       address.getCountry() + " - " +
                       address.getPincode();
        }

        return "BEGIN:VCALENDAR\n" +
               "VERSION:2.0\n" +
               "BEGIN:VEVENT\n" +
               "SUMMARY:" + event.getEventName() + "\n" +
               "DTSTART:" + startDateTime.format(dtf) + "\n" +
               "DTEND:" + endDateTime.format(dtf) + "\n" +
               "LOCATION:" + location + "\n" +
               "DESCRIPTION:" + event.getDescription() + "\n" +
               "END:VEVENT\n" +
               "END:VCALENDAR";
    }
}
