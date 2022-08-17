package com.example.redislearn.rankingboard.service.impl;

import com.example.redislearn.rankingboard.entity.UserScore;
import com.example.redislearn.rankingboard.service.RankBoardService;
import com.example.redislearn.rankingboard.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class RankBoardServiceImpl implements RankBoardService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static String REALTIME_POINT_RANKING_KEY = "REALTIME_POINT_RANKING_KEY";

    private static final int TIMESTAMP_BIT = 41;

    private static final String period = "WEEK";


    private int getPoint(long score) {
        return (int) (score >> TIMESTAMP_BIT);
    }

    private long toScore(int points, long periodEndTimestamp) {
        long score = 0L;
        score = (score | points) << TIMESTAMP_BIT;
        score = score | (periodEndTimestamp - System.currentTimeMillis());
        return score;
    }


    private long getPeriodEndDateTimestamp() {
        Long currentTime = System.currentTimeMillis();
        String timeZone = "GMT+8";
        switch (RankBoardServiceImpl.period) {
            case "DAY":
                return DateUtil.getDailyEndTime(currentTime, timeZone);
            case "WEEK":
                return DateUtil.getWeekEndTime(currentTime, timeZone);
            case "MONTH":
                return DateUtil.getMonthEndTime(currentTime, timeZone);
        }
        return 0L;
    }

    @Override
    public void updateRanking(Long userId, Integer addPoint) {
        String key = REALTIME_POINT_RANKING_KEY;
        Double score = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
        if (score == null) score = 0d;
        int oldPoint = getPoint(score.longValue());
        System.out.println("oldPoint:" + oldPoint + ", newPoint:" + (oldPoint + addPoint));
        long newScore = toScore(oldPoint + addPoint, getPeriodEndDateTimestamp());
        System.out.println("userId：" + userId + ", newScore:" + newScore);
        redisTemplate.opsForZSet().add(key, String.valueOf(userId), newScore);
    }

    @Override
    public List<UserScore> listUserScore() {
        String key = REALTIME_POINT_RANKING_KEY;
        // 倒序排序
        Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        List<UserScore> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Long> el : set) {
            list.add(new UserScore(Long.parseLong(String.valueOf(el.getValue())), getPoint(el.getScore().longValue())));
        }
        return list;
    }
}
