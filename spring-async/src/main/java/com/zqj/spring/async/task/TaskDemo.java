package com.zqj.spring.async.task;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author zhang.qijia
 * @date 2018/7/12 20:59
 */
@Component
public class TaskDemo {

    private static Random random =new Random(10000);

//    @Async
    public void task01(int a) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(random.nextInt(10000));
        long end = System.currentTimeMillis();
        System.out.println("task01 :"+ a +"，耗时：" + (end - start) + "毫秒");
    }
}
