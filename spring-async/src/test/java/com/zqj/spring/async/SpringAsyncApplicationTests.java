package com.zqj.spring.async;

import com.zqj.spring.async.task.TaskDemo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAsyncApplicationTests {

    @Resource
    private TaskDemo taskDemo;

    @Test
    public void contextLoads() throws InterruptedException {
        long start = System.currentTimeMillis();
        taskDemo.task01(1);
        taskDemo.task01(2);
        taskDemo.task01(3);
        long end = System.currentTimeMillis();
        System.out.println("耗时 :" + (end - start) + "ms");
    }

}
