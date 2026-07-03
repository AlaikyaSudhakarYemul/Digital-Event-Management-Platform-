package com.wipro.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wipro.admin.entity.Speaker;

public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
