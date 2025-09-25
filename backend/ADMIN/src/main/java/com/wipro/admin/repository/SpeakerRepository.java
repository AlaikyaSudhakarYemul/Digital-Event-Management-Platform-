package com.wipro.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wipro.admin.entity.Speaker;

@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
 