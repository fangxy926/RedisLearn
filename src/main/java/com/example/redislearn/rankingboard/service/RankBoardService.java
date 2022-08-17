package com.example.redislearn.rankingboard.service;

import com.example.redislearn.rankingboard.entity.UserScore;

import java.util.List;

public interface RankBoardService {
    void updateRanking(Long userId, Integer addPoints);

    List<UserScore> listUserScore();
}
