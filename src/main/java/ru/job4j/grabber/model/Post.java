package ru.job4j.grabber.model;

import java.util.Date;

/**
 * Class Post
 * Класс описывает объявление на сайте sql.ru
 * @author Dmitry Razumov
 * @version 1
 */
public class Post {
    /**
     * Поле содержит заголовок вакансии.
     */
    private String title;
    /**
     * Поле содержит описание вакансии.
     */
    private String description;
    /**
     * Поле содержит ссылку на вакансию.
     */
    private String link;
    /**
     * Поле содержит дату и время создания вакансии.
     */
    private Date date;

    /**
     * Метод вовзращает название вакансии.
     * @return Название вакансии
     */
    public String getTitle() {
        return title;
    }

    /**
     * Метод задает название вакансии.
     * @param title Название вакансии
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Метод возвращает описание вакансии.
     * @return Описание вакансии
     */
    public String getDescription() {
        return description;
    }

    /**
     * Метод задает описание вакансии
     * @param description Описание вакансии
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Метод возвращает ссылку на вакансию.
     * @return Ссылка на вакансию
     */
    public String getLink() {
        return link;
    }

    /**
     * Метод задает ссылку на вакансию.
     * @param link Ссылка на вакансию
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Метод возвращает дату создания вакансии.
     * @return Дата создания вакансии
     */
    public Date getDate() {
        return date;
    }

    /**
     * Метод задает дату создания вакансии.
     * @param date Дата создания вакансии
     */
    public void setDate(Date date) {
        this.date = date;
    }
}
