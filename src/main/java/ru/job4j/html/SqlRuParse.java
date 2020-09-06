package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class SqlRuParse
 * Класс осуществляет парсинг сайта вакансий.
 * @author Dmitry Razumov
 * @version 1
 */
public class SqlRuParse {
    /**
     * Главный метод программы.
     * Загружается страница с сайта вакансий.
     * Выводится информация о вакансиях.
     * @param args Параметры командной строки
     * @throws Exception Исключение
     */
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            Element time = td.parent().child(5);
            System.out.println(href.attr("href"));
            System.out.println(href.text());
            System.out.println(time.text());
        }
    }
}
