package com.example.redislearn.rankingboard.entity;

public class UserScore {
    private Long userId;
    private Integer score;

    public UserScore(Long userId, Integer score) {
        this.userId = userId;
        this.score = score;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
