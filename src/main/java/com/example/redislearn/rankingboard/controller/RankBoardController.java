package com.example.redislearn.rankingboard.controller;

import com.example.redislearn.rankingboard.entity.UserScore;
import com.example.redislearn.rankingboard.service.RankBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rankboard")
public class RankBoardController {

    @Autowired
    private RankBoardService rankBoardService;

    @PostMapping("/update")
    public List<UserScore> updateRankBoard(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(String.valueOf(body.get("userId")));
        Integer point = (Integer) body.get("point");
        rankBoardService.updateRanking(userId, point);
        return rankBoardService.listUserScore();
    }

}
