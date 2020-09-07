package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        for (int i = 1; i <= 5; i++) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                Element time = td.parent().child(5);
                System.out.println(href.attr("href"));
                System.out.println(href.text());
                System.out.println(parse(time.text()));
            }
        }
    }

    /**
     * Метод осуществляет преобразование даты в формате заданном на сайте в формат Timestamp,
     * с целью дальнейшей возможности записи даты в базу данных.
     * @param time Дата и время в формате сайта
     * @return Дата и время в формате Timestamp
     * @throws ParseException Исключение, при неудачном преобразовании даты и времени
     */
    private static Timestamp parse(String time) throws ParseException {
        String today = "сегодня";
        String yesterday = "вчера";
        String fullTime = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yy, HH:mm");
        String[] shortMonths = {"янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен",
                "окт", "ноя", "дек"};
        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля",
                "августа", "сентября", "октября", "ноября", "декабря"};
        for (int i = 0; i < shortMonths.length; i++) {
            if (time.contains(shortMonths[i])) {
                fullTime = time.replace(shortMonths[i], months[i]);
                break;
            }
        }
        if (time.contains(today)) {
            fullTime = time.replace(today, new SimpleDateFormat("dd MMMM yy").format(new Date()));
        }
        if (time.contains(yesterday)) {
            fullTime = time.replace(yesterday,
                    new SimpleDateFormat("dd MMMM yy")
                            .format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)));
        }
        Date date = formatter.parse(fullTime);
        return new Timestamp(date.getTime());
    }
}
