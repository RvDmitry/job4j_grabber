package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.parser.Parse;
import ru.job4j.grabber.parser.SqlRuParse;
import ru.job4j.grabber.store.PsqlStore;
import ru.job4j.grabber.store.Store;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Class Grabber
 * Класс осуществляет парсинг переданного сайта вакансий с заданной периодичностью.
 * Вакансии в которых фигурирует слово 'Java' записываются в базу данных.
 * @author Dmitry Razumov
 * @version 1
 */
public class Grabber implements Grab {
    /**
     * В поле содержатся настройки приложения.
     */
    private final Properties cfg = new Properties();

    /**
     * Метод возвращает объект, который взаимодействует с хранилищем объявлений.
     * @return Хранилище объявлений
     */
    public Store store() {
        return new PsqlStore(cfg);
    }

    /**
     * Метод возвращает планировщик заданий.
     * @return Планировщик заданий
     * @throws SchedulerException Исключение при ошибке создания планировщика
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    /**
     * Метод осуществляет загрузку параметров из файла конфигурации.
     * @throws IOException Исключение, при ошибке чтения файла
     */
    public void cfg() throws IOException {
        String config = "app.properties";
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream(config)) {
            cfg.load(in);
        }
    }

    /**
     * Метод осуществляет инициализацию планировщика заданий.
     * @param parse Объект осуществляющий парсинг сайта вакансий
     * @param store Хранилище, в которое сохраняются извлеченные объявления
     * @param scheduler Планировщик заданий
     * @throws SchedulerException Исключение
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    /**
     * Класс характеризует задание, которое будет выполняться планировщиком.
     */
    public static class GrabJob implements Job {
        /**
         * Метод принимает параметры среды выполнения задачи и периодичность ее выполнения.
         * @param context Контекст задачи
         * @throws JobExecutionException Исключение
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            String url = "https://www.sql.ru/forum/job-offers/";
            for (int i = 1; i <= 702; i++) {
                List<Post> posts = parse.list(url + i);
                posts.stream()
                        .filter(post -> post.getTitle().contains("Java"))
                        .forEach(store::save);
            }
        }
    }

    /**
     * Метод загружает объявления из базы данных и отправляет их серверу.
     * Сервер выводит полученые данные на странице браузера по указанному порту.
     * @param store Хранилище из которого нужно загрузить объявления
     */
    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(
                    Integer.parseInt(cfg.getProperty("port"))
            )) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes("CP1251"));
                            for (int i = 0; i < 5; i++) {
                                out.write(System.lineSeparator().getBytes());
                            }
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Главный метод программы. Запускает планировщик заданий.
     * @param args Параметры командной строки
     * @throws Exception Исключение
     */
    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(), store, scheduler);
        grab.web(store);
    }
}
