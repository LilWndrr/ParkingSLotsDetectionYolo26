package org.seyf.cardetection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seyf.cardetection.model.*;
import org.seyf.cardetection.repository.OccupancySnapshotRepository;
import org.seyf.cardetection.repository.SlotEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {

    private final GroundLevelService groundLevelService;
    private final SlotService slotService;
    private final OccupancySnapshotRepository snapshotRepository;
    private final SlotEventRepository slotEventRepository;

    private final Random random = new Random(42);

    /**
     * Generates realistic occupancy data for a given ground level.
     * Reads existing slots from the database automatically.
     */
    @Transactional
    public String seedData(String groundLevelId, int weeksBack) {
        GroundLevel level = groundLevelService.get(groundLevelId).orElse(null);
        if (level == null) {
            return "Ground level not found: " + groundLevelId;
        }

        String parkingName = level.getParking().getName();

        List<Slot> slots = slotService.getByGrounfLevel(level);
        if (slots.isEmpty()) {
            return "No slots found for ground level: " + groundLevelId;
        }

        List<String> slotNames = slots.stream().map(Slot::getName).toList();
        int totalSlots = slotNames.size();

        // Map each slot name to its camera name
        Map<String, String> slotCameraMap = new HashMap<>();
        for (Slot slot : slots) {
            String camName = (slot.getCamera() != null) ? slot.getCamera().getName() : "cam1";
            slotCameraMap.put(slot.getName(), camName);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusWeeks(weeksBack).withHour(0).withMinute(0).withSecond(0);

        List<OccupancySnapshot> snapshots = new ArrayList<>();
        List<SlotEvent> events = new ArrayList<>();

        int previousOccupied = 0;
        LocalDateTime cursor = startTime;

        while (cursor.isBefore(now)) {
            int dayOfWeek = cursor.getDayOfWeek().getValue();
            int hour = cursor.getHour();
            int minute = cursor.getMinute();
            boolean isWeekend = dayOfWeek >= 6;

            double baseRate = getBaseOccupancyRate(hour, minute, isWeekend, dayOfWeek);

            long daysFromStart = java.time.Duration.between(startTime, cursor).toDays();
            double trendFactor = 1.0 + (daysFromStart * 0.001);
            baseRate = Math.min(baseRate * trendFactor, 98.0);

            double noise = (random.nextGaussian() * 3.5);
            double occupancyRate = Math.max(2.0, Math.min(98.0, baseRate + noise));

            int occupiedSlots = (int) Math.round(occupancyRate / 100.0 * totalSlots);
            occupiedSlots = Math.max(0, Math.min(totalSlots, occupiedSlots));
            double actualRate = ((double) occupiedSlots / totalSlots) * 100.0;

            OccupancySnapshot snapshot = OccupancySnapshot.builder()
                    .groundLevel(level)
                    .totalSlots(totalSlots)
                    .occupiedSlots(occupiedSlots)
                    .occupancyRate(actualRate)
                    .recordedAt(cursor)
                    .hourOfDay(hour)
                    .dayOfWeek(dayOfWeek)
                    .isWeekend(isWeekend)
                    .build();
            snapshots.add(snapshot);

            int delta = occupiedSlots - previousOccupied;
            if (delta != 0) {
                int transitionCount = Math.min(Math.abs(delta), slotNames.size());
                List<String> shuffled = new ArrayList<>(slotNames);
                Collections.shuffle(shuffled, random);

                for (int i = 0; i < transitionCount; i++) {
                    boolean slotIsEmpty = delta < 0;
                    String slotName = shuffled.get(i);
                    SlotEvent event = SlotEvent.builder()
                            .slotName(slotName)
                            .cameraName(slotCameraMap.getOrDefault(slotName, "cam1"))
                            .groundLevel(level)
                            .parkingName(parkingName)
                            .isEmpty(slotIsEmpty)
                            .occurredAt(cursor.plusSeconds(random.nextInt(290)))
                            .hourOfDay(hour)
                            .dayOfWeek(dayOfWeek)
                            .build();
                    events.add(event);
                }
            }

            previousOccupied = occupiedSlots;
            cursor = cursor.plusMinutes(15);

            if (snapshots.size() >= 2000) {
                snapshotRepository.saveAll(snapshots);
                snapshots.clear();
                log.info("Saved batch of snapshots, cursor at {}", cursor);
            }
            if (events.size() >= 2000) {
                slotEventRepository.saveAll(events);
                events.clear();
            }
        }

        if (!snapshots.isEmpty()) snapshotRepository.saveAll(snapshots);
        if (!events.isEmpty()) slotEventRepository.saveAll(events);

        long totalSnapshots = java.time.Duration.between(startTime, now).toMinutes() / 5;
        return String.format("Seeded %d weeks: ~%d snapshots + slot events for %d slots (%s)",
                weeksBack, totalSnapshots, totalSlots, slotNames);
    }

    private double getBaseOccupancyRate(int hour, int minute, boolean isWeekend, int dayOfWeek) {
        double timeDecimal = hour + minute / 60.0;
        if (isWeekend) {
            return getWeekendRate(timeDecimal, dayOfWeek);
        } else {
            return getWeekdayRate(timeDecimal, dayOfWeek);
        }
    }

    private double getWeekdayRate(double time, int dayOfWeek) {
        double dayModifier = switch (dayOfWeek) {
            case 1 -> -3.0;
            case 5 -> -5.0;
            default -> 0.0;
        };

        double rate;
        if (time < 5) {
            rate = 5.0 + time * 1.0;
        } else if (time < 7) {
            rate = lerp(8.0, 30.0, (time - 5) / 2.0);
        } else if (time < 9) {
            rate = lerp(30.0, 82.0, (time - 7) / 2.0);
        } else if (time < 11) {
            rate = lerp(82.0, 88.0, (time - 9) / 2.0);
        } else if (time < 14) {
            rate = lerp(88.0, 92.0, (time - 11) / 3.0);
            if (time > 12 && time < 13.5) {
                rate -= 4.0;
            }
        } else if (time < 16) {
            rate = lerp(88.0, 82.0, (time - 14) / 2.0);
        } else if (time < 18) {
            rate = lerp(82.0, 42.0, (time - 16) / 2.0);
        } else if (time < 20) {
            rate = lerp(42.0, 18.0, (time - 18) / 2.0);
        } else {
            rate = lerp(18.0, 6.0, (time - 20) / 4.0);
        }

        return Math.max(3.0, rate + dayModifier);
    }

    private double getWeekendRate(double time, int dayOfWeek) {
        double dayModifier = (dayOfWeek == 6) ? 5.0 : 0.0;

        double rate;
        if (time < 8) {
            rate = 3.0 + time * 0.6;
        } else if (time < 10) {
            rate = lerp(8.0, 25.0, (time - 8) / 2.0);
        } else if (time < 12) {
            rate = lerp(25.0, 55.0, (time - 10) / 2.0);
        } else if (time < 15) {
            rate = lerp(55.0, 65.0, (time - 12) / 3.0);
        } else if (time < 17) {
            rate = lerp(65.0, 45.0, (time - 15) / 2.0);
        } else if (time < 20) {
            rate = lerp(45.0, 15.0, (time - 17) / 3.0);
        } else {
            rate = lerp(15.0, 5.0, (time - 20) / 4.0);
        }

        return Math.max(2.0, rate + dayModifier);
    }

    private double lerp(double a, double b, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return a + (b - a) * t;
    }
}
