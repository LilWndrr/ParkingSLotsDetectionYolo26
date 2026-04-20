package org.seyf.cardetection.dto;

import org.seyf.cardetection.model.GroundLevel;

public interface SlotTransition {

   String getSlotName();
   int getTransitionCount();
   int getHourOfDay();

}
