package ru.job4j.grabber;

import java.util.List;

/**
 * Interface Store
 * Интерфейс определяет методы взаимодействия с базой данных.
 * @author Dmitry Razumov
 * @version 1
 */
public interface Store {
    /**
     * Метод сохраняет объявление в базе.
     * @param post Объявление
     */
    void save(Post post);

    /**
     * Метод извлекает все объявления из базы.
     * @return Список объявлений
     */
    List<Post> getAll();
}
