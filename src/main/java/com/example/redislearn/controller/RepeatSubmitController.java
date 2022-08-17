package com.example.redislearn.controller;

import com.example.redislearn.repeatSubmit.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepeatSubmitController {
    @PostMapping("/repeat_submit")
    @RepeatSubmit(interval = 10000)
    public String repeatSubmit(@RequestBody String json) {
        return json;
    }
}
