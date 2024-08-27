package net.maku.quartz.config;

import net.maku.framework.common.constant.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 定时任务配置
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 *
 * 创建和启动 Scheduler 的时机
 * SchedulerFactoryBean 创建和启动 Scheduler 的时机取决于几个因素：
 * Spring 容器启动时:
 * 当 Spring 容器开始初始化时，它会创建并配置所有的 Bean。
 * SchedulerFactoryBean 作为一个 Spring Bean，也会在这个过程中被创建和配置。
 *
 * 如果 SchedulerFactoryBean 的 autoStartup 属性被设置为 true（默认值），那么 Scheduler 将在 Spring 容器启动时自动启动。
 *
 * SchedulerFactoryBean 初始化时:
 * SchedulerFactoryBean 实现了 FactoryBean 接口，这意味着它有自己的初始化逻辑。
 * 当 SchedulerFactoryBean 被 Spring 容器初始化时，它会调用 SchedulerFactoryBean 的 afterPropertiesSet 方法。
 * 在 afterPropertiesSet 方法中，SchedulerFactoryBean 会创建 Scheduler 实例，并进行必要的配置。
 * 如果 autoStartup 为 true，Scheduler 将在这个阶段启动。
 * 显式启动 Scheduler:
 * 如果 SchedulerFactoryBean 的 autoStartup 属性被设置为 false，那么 Scheduler 不会自动启动。
 * 你可以通过调用 SchedulerFactoryBean 的 start() 方法来显式地启动 Scheduler。
 * 例如，你可以在应用启动后某个特定时刻调用 start() 方法来启动 Scheduler。
 *
 */
@Configuration
public class ScheduleConfig {
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
    private String driver;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        // quartz参数
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "MakuScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        // 线程池配置
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "20");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        // jobStore配置
        prop.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        // 集群配置
        prop.put("org.quartz.jobStore.isClustered", "true");
        prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");

        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");

        // PostgreSQL数据库配置
        if (Constant.PGSQL_DRIVER.equals(driver)) {
            prop.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        }

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setSchedulerName("MakuScheduler");
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(prop);
        // 延时启动
        factory.setStartupDelay(10);
        factory.setApplicationContextSchedulerContextKey("applicationContextKey");
        // 启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
        factory.setOverwriteExistingJobs(true);

        return factory;
    }
}
