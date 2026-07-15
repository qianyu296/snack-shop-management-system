package com.snackadmin.service;

import com.snackadmin.vo.StatisticsVO;

public interface StatisticsService {
    StatisticsVO getStatistics(String startDate, String endDate);
}
