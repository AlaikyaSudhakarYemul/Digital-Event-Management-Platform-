package com.wipro.demp.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventStatusUpdaterServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventStatusUpdaterService eventStatusUpdaterService;

    @Test
    void updateCompletedEventsMarksPastActiveEventsAsCompleted() {
        Event pastActive = new Event();
        pastActive.setDate(LocalDate.now().minusDays(2));
        pastActive.setActiveStatus(EventStatus.ACTIVE);

        Event futureActive = new Event();
        futureActive.setDate(LocalDate.now().plusDays(2));
        futureActive.setActiveStatus(EventStatus.ACTIVE);

        Event pastCompleted = new Event();
        pastCompleted.setDate(LocalDate.now().minusDays(1));
        pastCompleted.setActiveStatus(EventStatus.COMPLETED);

        when(eventRepository.findAll()).thenReturn(List.of(pastActive, futureActive, pastCompleted));

        eventStatusUpdaterService.updateCompletedEvents();

        verify(eventRepository).save(pastActive);
        verify(eventRepository, never()).save(futureActive);
        verify(eventRepository, never()).save(pastCompleted);
    }

    @Test
    void updateCompletedEventsSkipsEventsWithNullDate() {
        Event event = new Event();
        event.setDate(null);
        event.setActiveStatus(EventStatus.ACTIVE);

        when(eventRepository.findAll()).thenReturn(List.of(event));

        eventStatusUpdaterService.updateCompletedEvents();

        verify(eventRepository, never()).save(event);
    }
}
