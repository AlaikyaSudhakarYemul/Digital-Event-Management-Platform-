package com.wipro.event.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wipro.event.entity.Event;
import com.wipro.event.entity.EventStatus;
import com.wipro.event.repository.EventRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventStatusUpdaterService {

    private final EventRepository eventRepository;

    public EventStatusUpdaterService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void updateCompletedEvents() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            if (event.getDate() != null && event.getDate().isBefore(today) && event.getActiveStatus() != EventStatus.COMPLETED) {
                event.setActiveStatus(EventStatus.COMPLETED);
                eventRepository.save(event);
            }
        }
    }
}