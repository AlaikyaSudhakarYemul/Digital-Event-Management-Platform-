package com.wipro.demp.service;

import com.wipro.demp.entity.CopiedEvents;
import java.util.List;
 
public interface CopiedEventsService {
      CopiedEvents createCopiedEvents(CopiedEvents copiedevents);
      CopiedEvents getCopiedEventsById(int id);
      List<CopiedEvents> getAllCopiedEvents();
    CopiedEvents updateCopiedEvents(int id, CopiedEvents updatedCopiedEvents);
      void deleteCopiedEvents(int id);
      List<CopiedEvents> findByCopiedEventsName(String copiedeventsName);
}

    
    

