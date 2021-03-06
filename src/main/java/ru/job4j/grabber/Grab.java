package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import ru.job4j.grabber.parser.Parse;
import ru.job4j.grabber.store.Store;

/**
 * Interface Grab
 * Интерфейс определяет параметры выпонения задачи парсинга сайта вакансий.
 * @author Dmitry Razumov
 * @version 1
 */
public interface Grab {
    /**
     * Метод определяет параметры запуска задачи парсинга сайта вакансий.
     * @param parse Объект осуществляющий парсинг сайта вакансий
     * @param store Хранилище, в которое сохраняются извлеченные объявления
     * @param scheduler Планировщик заданий
     * @throws SchedulerException Исключение
     */
    void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException;
}
