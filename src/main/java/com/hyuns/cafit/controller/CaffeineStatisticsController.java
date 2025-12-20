package com.hyuns.cafit.controller;

import com.hyuns.cafit.auth.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.statistics.CaffeineTimelineResponse;
import com.hyuns.cafit.dto.statistics.DailyStatisticsResponse;
import com.hyuns.cafit.dto.statistics.TopBeverageStat;
import com.hyuns.cafit.service.CaffeineStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class CaffeineStatisticsController {
    private final CaffeineStatisticsService statisticsService;

    @GetMapping("/timeline")
    public ResponseEntity<CaffeineTimelineResponse> getTimeline(
            @Login User user,
            @RequestParam(defaultValue = "12") int hours
    ) {
        CaffeineTimelineResponse response = statisticsService.getTimeline(user, hours);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyStatisticsResponse> getDailyStatistics(
            @Login User user,
            @RequestParam(defaultValue = "7") int days
    ) {
        DailyStatisticsResponse response = statisticsService.getDailyStatistics(user, days);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-beverages")
    public ResponseEntity<List<TopBeverageStat>> getTopBeverages(
            @Login User user,
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(statisticsService.getTopBeverages(user, days));
    }
}
