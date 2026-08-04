package com.wipro.demp.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.wipro.demp.entity.Address;
import com.wipro.demp.entity.Event;

class CalendarInviteUtilTest {

    @Test
    void generateICSIncludesEventAndAddressDetails() {
        Event event = new Event();
        event.setEventName("AI Summit");
        event.setDescription("Annual AI Conference");
        event.setDate(LocalDate.of(2026, 7, 20));
        event.setTime(LocalTime.of(9, 30));

        Address address = new Address();
        address.setAddress("MG Road");
        address.setState("TS");
        address.setCountry("India");
        address.setPincode("500001");

        String ics = CalendarInviteUtil.generateICS(event, address);

        assertTrue(ics.contains("BEGIN:VCALENDAR"));
        assertTrue(ics.contains("SUMMARY:AI Summit"));
        assertTrue(ics.contains("DESCRIPTION:Annual AI Conference"));
        assertTrue(ics.contains("LOCATION:MG Road, TS, India - 500001"));
        assertTrue(ics.contains("DTSTART:20260720T093000"));
        assertTrue(ics.contains("DTEND:20260720T103000"));
    }

    @Test
    void generateICSHandlesNullAddress() {
        Event event = new Event();
        event.setEventName("AI Summit");
        event.setDescription("Annual AI Conference");
        event.setDate(LocalDate.of(2026, 7, 20));
        event.setTime(LocalTime.of(9, 30));

        String ics = CalendarInviteUtil.generateICS(event, null);

        assertTrue(ics.contains("LOCATION:"));
    }
}
