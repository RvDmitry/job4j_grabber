package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
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
        String config = "app.properties";
        Properties property = new Properties();
        try (InputStream input = AlertRabbit.class.getClassLoader().getResourceAsStream(config)) {
            property.load(input);
            Class.forName(property.getProperty("db.driver"));
            try (Connection cn = DriverManager.getConnection(
                    property.getProperty("db.url"),
                    property.getProperty("db.username"),
                    property.getProperty("db.password")
            )) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", cn);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
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
            Thread.sleep(10000);
            scheduler.shutdown();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Класс представляет из себя задачу для планировщика.
     */
    public static class Rabbit implements Job {
        /**
         * Конструктор, выводит хеш-код объекта при каждом вызове задания из планировщика.
         */
        public Rabbit() {
            System.out.println(hashCode());
        }

        /**
         * Метод выполняет запись в базу данных при вызове планировщика через заданное время.
         * @param context Объект
         * @throws JobExecutionException Исключение
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("store");
            try (PreparedStatement st = cn.prepareStatement(
                    "insert into rabbit(create_date) values(?)"
            )) {
                st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                st.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Ошибка добавления записи в таблицу.");
            }
            System.out.println("Добавление записи в таблицу rabbit прошло успешно.");
        }
    }
}
