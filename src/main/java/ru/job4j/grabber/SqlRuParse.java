package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

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
        Post post = extract("https://www.sql.ru/forum/1325330/"
                + "lidy-be-fe-senior-cistemnye-analitiki-qa-i-devops-moskva-do-200t");
        System.out.println(System.lineSeparator() + post.getLink());
        System.out.println(post.getTitle());
        System.out.println(post.getDescription());
        System.out.println(post.getDate());
    }

    /**
     * Метод осуществляет извлечение информации из поста через переданную ссылку на данный пост.
     * Извлекается заголовок поста, его содержание, дата создания поста.
     * Из полученной информации собирается и вовзращается объект Post.
     * @param url Ссылка на пост
     * @return Объект Post
     * @throws Exception Исключение
     */
    public static Post extract(String url) throws Exception {
        Post post = new Post();
        StringJoiner join = new StringJoiner(System.lineSeparator());
        Document doc = Jsoup.connect(url).get();
        Elements header = doc.select(".messageHeader");
        String title = header.get(0).textNodes().get(0).text();
        Elements bodies = doc.select(".msgBody");
        Element body = bodies.get(1);
        for (var element : body.childNodes()) {
            if (element instanceof TextNode) {
                join.add(((TextNode) element).text());
            } else {
                for (TextNode node : ((Element) element).textNodes()) {
                    join.add(node.text());
                }
            }
        }
        String footer = doc.selectFirst(".msgFooter").textNodes().get(0).text();
        String date = footer.replaceAll("[\\[]", "");
        post.setTitle(title);
        post.setDescription(join.toString());
        post.setLink(url);
        post.setDate(parse(date));
        return post;
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
