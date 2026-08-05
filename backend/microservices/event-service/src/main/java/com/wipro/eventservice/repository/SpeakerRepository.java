package com.wipro.eventservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.eventservice.entity.Speaker;

public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
