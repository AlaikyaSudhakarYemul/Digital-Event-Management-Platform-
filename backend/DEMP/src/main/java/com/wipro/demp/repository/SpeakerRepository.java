package com.wipro.demp.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import com.wipro.demp.entity.Speaker;
 
@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
 