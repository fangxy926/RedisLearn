package com.example.redislearn.repeatSubmit.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 间隔时间， 小于此时间视为重复提交
     *
     * @return
     */
    public int interval() default 5000;

    /**
     * 提示消息
     *
     * @return
     */
    public String message() default "不允许重复提交，请稍后再试";
}
