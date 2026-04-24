package org.seyf.cardetection.repository;

import org.seyf.cardetection.dto.SlotHeatValue;
import org.seyf.cardetection.dto.SlotTransition;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.SlotEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotEventRepository extends JpaRepository<SlotEvent,String> {

    List<SlotEvent> findByGroundLevelAndOccurredAtBetween(GroundLevel level, LocalDateTime from, LocalDateTime to);

    @Query("select s.slotName as slotName, s.hourOfDay as hourOfDay, count (s.id) as transitionCount  from SlotEvent s where s.groundLevel=:level group by s.slotName,s.hourOfDay order by s.hourOfDay ASC")
    List<SlotTransition> countSlotEventAppearanceGroupBySlotAndByHourOfDay(@Param("level") GroundLevel level );

    @Query("select s.slotName as slotName, count(s.id) as totalTransitions from SlotEvent s where s.groundLevel = :level group by s.slotName")
    List<SlotHeatValue> sumTransitionsPerSlot(@Param("level") GroundLevel level);

    void deleteByGroundLevel(GroundLevel groundLevel);
}
