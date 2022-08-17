package com.example.redislearn.repeatSubmit.interceptor;

import com.example.redislearn.repeatSubmit.annotation.RepeatSubmit;
import com.example.redislearn.repeatSubmit.request.RepeatableReadRequestWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    public final static String REPEAT_PARAMS = "repeatParams";
    public final static String REPEAT_TIME = "repeatTime";
    public final static String REPEAT_SUBMIT_KEY = "REPEAT_SUBMIT_KEY";
    public static final String HEADER = "Authorization";


    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                if (isRepeatSubmit(request, annotation)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 500);
                    map.put("msg", annotation.message());
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(map));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request
     * @return
     * @throws Exception
     */
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit repeatSubmit) {
        //请求参数字符串
        String nowParams = "";
        if (request instanceof RepeatableReadRequestWrapper) {
            try {
                nowParams = ((RepeatableReadRequestWrapper) request).getReader().readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //否则说明请求参数是 key-value 格式的
        if (StringUtils.isEmpty(nowParams)) {
            try {
                nowParams = new ObjectMapper().writeValueAsString(request.getParameterMap());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Map<String, Object> nowDataMap = new HashMap<>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());
        String requestURI = request.getRequestURI();
        String header = request.getHeader(HEADER);
        // key 由固定前缀 + 请求 URL 地址 + 请求头的认证令牌组成，请求头的令牌还是非常重要需要有的，只有这样才能区分出来当前用户提交的数据
        String cacheKey = REPEAT_SUBMIT_KEY + requestURI + header.replace("Bearer ", "");
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        if (cacheObject != null) {
            Map<String, Object> map = (Map<String, Object>) cacheObject;
            if (compareParams(map, nowDataMap) && compareTime(map, nowDataMap, repeatSubmit.interval())) {
                return true;
            }
        }
        // 使用String 数据结构

        redisTemplate.opsForValue().set(cacheKey, nowDataMap, repeatSubmit.interval(), TimeUnit.MILLISECONDS);

        return false;
    }

    /**
     * 判断两次间隔时间
     */
    private boolean compareTime(Map<String, Object> map, Map<String, Object> nowDataMap, int interval) {
        Long time1 = (Long) map.get(REPEAT_TIME);
        Long time2 = (Long) nowDataMap.get(REPEAT_TIME);
        if ((time2 - time1) < interval) {
            return true;
        }
        return false;
    }

    /**
     * 判断参数是否相同
     */
    private boolean compareParams(Map<String, Object> map, Map<String, Object> nowDataMap) {
        String nowParams = (String) nowDataMap.get(REPEAT_PARAMS);
        String dataParams = (String) map.get(REPEAT_PARAMS);
        return nowParams.equals(dataParams);
    }


}
