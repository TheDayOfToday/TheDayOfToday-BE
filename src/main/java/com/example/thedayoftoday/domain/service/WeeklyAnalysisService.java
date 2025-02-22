package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WeeklyAnalysisService {
    private final WeeklyDataRepository weeklyDataRepository;

    public WeeklyAnalysisService(WeeklyDataRepository weeklyDataRepository) {
        this.weeklyDataRepository = weeklyDataRepository;
    }

    public Map<String, Object> getWeeklyAnalysis(int year, int month, int week) {
        List<WeeklyData> weeklyDataList = weeklyDataRepository.findAll();

        WeeklyData targetWeekData = weeklyDataList.stream()
                .filter(data -> isMatchingWeek(data, year, month, week))
                .findFirst()
                .orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("week", week);

        if (targetWeekData != null) {
            response.put("title", targetWeekData.getTitle());
            response.put("analysisMoodmeter", targetWeekData.getAnalysisMoodmeter());
            response.put("feedback", targetWeekData.getFeedback());
            response.put("startDate", targetWeekData.getStartDate());
            response.put("endDate", targetWeekData.getEndDate());
        } else {
            response.put("message", "해당 주차에 대한 분석 데이터가 없습니다.");
        }

        return response;
    }

    private boolean isMatchingWeek(WeeklyData data, int year, int month, int week) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(java.sql.Date.valueOf(data.getStartDate()));
        int dataYear = calendar.get(Calendar.YEAR);
        int dataMonth = calendar.get(Calendar.MONTH) + 1;
        int dataWeek = calendar.get(Calendar.WEEK_OF_MONTH);

        return dataYear == year && dataMonth == month && dataWeek == week;
    }
}
