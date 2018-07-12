# SpringBoot
Some practice about SpringBoot 

# spring boot 异步任务
## Preface
- 在Java应用中，绝大多数情况下都是通过同步的方法来实现交互处理的；但是这种方式在与第三方系统交互的时候容易造成响应迟缓，解决响应迟缓多采用多线程的处理方式。

- Spring 通过 TaskExecutor 实现多线程和并发编程。我们可以通过注解 **@EnableSync** 来开启对异步任务的支持，使用 **@Async** 来标注异步任务类或者方法。

## 简单的实现异步任务

- 启用@Async注解支持
```
@SpringBootApplication
@EnableAsync
public class SpringAsyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAsyncApplication.class, args);
	}
}
```

- 基于@Async实现无返回值异步任务
```
@Component
public class TaskDemo {

    private static Random random =new Random(10000);

    @Async
    public void task01(int a) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(random.nextInt(10000));
        long end = System.currentTimeMillis();
        System.out.println("task01 :"+ a +"，耗时：" + (end - start) + "毫秒");
    }
}

```
- 基于@Async有返回异步任务
```
@Component
public class TaskDemo {

   
    @Async
    public Future<String> asyncInvokeReturnFuture(int i) {
        log.info("asyncInvokeReturnFuture, parementer={}", i);
        Future<String> future;
        try {
            Thread.sleep(1000 * 1);
            future = new AsyncResult<String>("success:" + i);
        } catch (InterruptedException e) {
            future = new AsyncResult<String>("error");
        }
        return future;
    }

}
```

## 自定义线程池
- 我们可以先了解一下官方文档对 **@EnableAsync** 注解的注释。
- https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html

- **@EnableAsync** 用来开启Spring的异步方法执行能力，和XML配置中的 **<task:\*>** 功能相同。
 
- 默认情况下，Spring会按照以下条件搜寻已定义的线程池：
    - 容器中唯一的 TaskExecutor 类型的 bean。
    - 容器中命名为“taskExecutor” Executor 类型的 bean
- 如果以上情况均不满足，Spring 会用 SimpleAsyncTaskExecutor 来执行异步方法。

- 除此之外，void 返回值类型的异步方法如果抛出异常的话，没有办法将异常反馈给调用方。默认情况下这种未捕获的异常只会打印日志。

- 这些都可以通过实现 **AsyncConfigurer** 来进行自定义。
    - **getAsyncExecutor()** 方法自定义 **Executor** 实现；
    - **getAsyncUncaughtExceptionHandler()** 方法自定义异常处理。
    
```
@Configuration
@EnableAsync
 public class AppConfig implements AsyncConfigurer {

     @Override
     public Executor getAsyncExecutor() {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(7);
         executor.setMaxPoolSize(42);
         executor.setQueueCapacity(11);
         executor.setThreadNamePrefix("MyExecutor-");
         /*注意这一句*/
         executor.initialize();
         return executor;
     }

     @Override
     public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
         return MyAsyncUncaughtExceptionHandler();
     }
     
 }
 
 /**
 * 自定义异常处理类
 *
 */
class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    //手动处理捕获的异常
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        System.out.println("-------------》》》捕获线程异常信息");
        log.info("Exception message - " + throwable.getMessage());
        log.info("Method name - " + method.getName());
        for (Object param : obj) {
            log.info("Parameter value - " + param);
        }
    }

}
```

### 注意
- 上述示例中 **ThreadPoolTaskExecutor** 并不是一个完全托管的 Spring bean。

- 在 **getAsyncExecutor()** 方法上增加 **@Bean** 可以实现完全托管。采取这种方式可以不用显示的调用 ```executor.initialize() ```方法，因为容器初始化 bean 的时候会自动调用。
- 
```
@Configuration
public class MyAsyncConfigurer implements AsyncConfigurer {
    
    @Bean("taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("MyExecutor-");
        return executor;
    }
    ...
    
}
```

- 以上注解配置对应的 XML 配置可参考下方：
 
 ```
 <beans>
     <task:annotation-driven executor="myExecutor" exception-handler="exceptionHandler"/>

     <task:executor id="myExecutor" pool-size="7-42" queue-capacity="11"/>

     <bean id="asyncBean" class="com.foo.MyAsyncBean"/>

     <bean id="exceptionHandler" class="com.foo.MyAsyncUncaughtExceptionHandler"/>
 </beans>
 ```
 
- XML配置对比注解配置缺少了对 Executor 中 thread name prefix的配置。

## 了解 Spring 线程池 TaskExecutor
- Spring异步线程池的接口类，其实质是 **java.util.concurrent.Executor**。

- Spring已经实现的异常线程池：
    - **SimpleAsyncTaskExecutor**：不是真的线程池，该类不重用线程，每次调用都会创建一个新的线程。
    - **SyncTaskExecutor**：该类没有实现异步调用，只是一个同步操作。只适用于不需要多线程的场景。
    - **ConcurrentTaskExecutor**：Executor 的适配类，不推荐。尽当 ThreadPoolTaskExecutor 不满足要求时，才考虑使用。
    - **SimpleThreadPoolTaskExecutor**：是Quartz的SimpleThreadPool的类。线程池同时被quartz和非quartz使用，才需要使用此类 。
    - **ThreadPoolTaskExecutor**：最常使用，推荐。 其实质是对java.util.concurrent.ThreadPoolExecutor的包装

> 参考：   
[Spring中@Async用法总结](https://blog.csdn.net/blueheart20/article/details/44648667)   
[Spring boot 使用@Async实现异步调用](https://github.com/timebusker/spring-boot/tree/master/spring-boot-5-Async#%E4%BC%98%E5%8C%96%E5%BC%82%E6%AD%A5%E8%B0%83%E7%94%A8)   
[使用Future以及定义超时](http://blog.didispace.com/springbootasync-4/)   
[Spring @Async异步线程池用法总结](https://blog.csdn.net/hry2015/article/details/67640534)     
[SpringBoot 线程池配置 实现AsyncConfigurer接口方法](https://www.cnblogs.com/memoryXudy/p/7737190.html)
