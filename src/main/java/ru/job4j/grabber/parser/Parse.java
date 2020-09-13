package ru.job4j.grabber.parser;

import ru.job4j.grabber.model.Post;

import java.util.List;

/**
 * Interface Parse
 * Интерфейс описывает парсинг сайта.
 * @author Dmitry Razumov
 * @version 1
 */
public interface Parse {
    /**
     * Метод загружает список всех постов.
     * @param link Ссылка на страницу постов
     * @return Список постов
     */
    List<Post> list(String link);

    /**
     * Метод загружает детали одного поста.
     * @param link Ссылка на пост
     * @return Объект Post
     */
    Post detail(String link);
}
