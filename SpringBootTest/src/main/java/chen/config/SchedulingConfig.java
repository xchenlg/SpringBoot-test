package chen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务
 * @author Administrator
 *
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
   
//    @Scheduled(cron = "0/2000 * * * * ?") // 每2000秒执行一次
    public void scheduler() {
        System.out.println(">>>>>>>>>定时任务 SchedulingConfig.scheduler()");
    }
}