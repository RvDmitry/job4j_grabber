package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

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
     * @param scheduler Объект определяющий время выполнения задачи
     * @throws SchedulerException Исключение
     */
    void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException;
}
