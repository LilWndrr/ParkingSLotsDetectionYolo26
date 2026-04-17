package org.seyf.cardetection.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.seyf.cardetection.dto.DateRequestDto;
import org.seyf.cardetection.dto.HourlyOccupancy;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.OccupancySnapshot;
import org.seyf.cardetection.repository.OccupancySnapshotRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class OccupancySnapshotService {

    private final OccupancySnapshotRepository occupancySnapshotRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final GroundLevelService levelService;



    @Scheduled(fixedRate = 300000)
    @Transactional
    public void recordAndBroadcastSnapshot(){
        log.info("Start occupancyRate record");
        LocalDateTime currentTime = LocalDateTime.now();

        List<GroundLevel> allLevels = levelService.getAll();
        if(allLevels.isEmpty()){
            log.info("Ground Level Are Empty");
        }

        for(GroundLevel level : allLevels){
            String parkingName = level.getParking().getName();
            int totalSlots =0;
            int occupiedSlots=0;

            for(Camera cam:level.getCameras()){
                String redisKey = "parking:" + parkingName + ":ground_level:" + level.getName() + ":camera:" + cam.getName() + ":slots";
                Map<Object,Object> liveStats= redisTemplate.opsForHash().entries(redisKey);
                totalSlots+=liveStats.size();

                for(Object value : liveStats.values()){
                    if(!(Boolean) value){
                        occupiedSlots++;
                    }
                }
            }
            if(totalSlots == 0) continue;
            double occupancyRate = ((double) occupiedSlots/totalSlots)*100;
            OccupancySnapshot snapshot= OccupancySnapshot.builder()
                    .occupancyRate(occupancyRate)
                    .groundLevel(level)
                    .totalSlots(totalSlots)
                    .occupiedSlots(occupiedSlots)
                    .recordedAt(currentTime)
                    .hourOfDay(currentTime.getHour())
                    .dayOfWeek(currentTime.getDayOfWeek().getValue())
                    .isWeekend(currentTime.getDayOfWeek().getValue()>=6 )
                    .build();
            occupancySnapshotRepository.save(snapshot);
        }

    }


    public double getOccupancyRateByGroundLevel(String groundLevelId){

        GroundLevel groundLevel = levelService.get(groundLevelId).orElse(null);
        if(groundLevel==null){
            log.info("Ground level is not exist");
        }
        OccupancySnapshot snapshot= occupancySnapshotRepository.findFirstByGroundLevelOrderByRecordedAtDesc(groundLevel).orElse(null);

        return snapshot != null ? snapshot.getOccupancyRate() : 0;

    }

    public List<HourlyOccupancy >  getHourlyOccupancy(){
        return occupancySnapshotRepository.findAverageOccupancyByHour();
    }

    public List<OccupancySnapshot> getByTimeInterval(String groundLevelId, DateRequestDto date){
        GroundLevel level = levelService.get(groundLevelId).orElse(null);
        if(level==null){
            return new ArrayList<>();
        }

        return occupancySnapshotRepository.findByGroundLevelAndRecordedAtBetween(level,date.getFrom().atStartOfDay(),date.getTo().atTime(12,0));
    }



}
