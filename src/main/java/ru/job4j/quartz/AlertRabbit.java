package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * Class AlertRabbit
 * В классе создается планировщик, который выполняет задачу с заданым временным интервалом.
 * @author Dmitry Razumov
 * @version 1
 */
public class AlertRabbit {
    /**
     * Главный метод программы. Создается планировщик и задача которую он должен выполнить.
     * Временной интервал считывается из файла свойст.
     * @param args Параметры командной строки
     */
    public static void main(String[] args) {
        Properties property = new Properties();
        String config = "rabbit.properties";
        try (InputStream input = AlertRabbit.class.getClassLoader().getResourceAsStream(config)) {
            property.load(input);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(
                            Integer.parseInt(property.getProperty("rabbit.interval"))
                    )
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Класс представляет из себя задачу для планировщика.
     */
    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}
