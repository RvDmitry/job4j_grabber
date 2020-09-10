package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * Class SqlRuParse
 * Класс осуществляет парсинг сайта вакансий.
 * @author Dmitry Razumov
 * @version 1
 */
public class SqlRuParse implements Parse {
    /**
     * Главный метод программы.
     * @param args Параметры командной строки
     */
    public static void main(String[] args) {
        List<Post> posts = new SqlRuParse().list("https://www.sql.ru/forum/job-offers");
        posts.stream().map(post -> post.getTitle() + " /дата поста: " + post.getDate())
                .forEach(System.out::println);
    }

    /**
     * Метод осуществляет парсинг сайта вакансий sql.ru. Формирует список постов.
     * @param link Ссылка на страницу постов
     * @return Список постов
     */
    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        Document doc;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Ошибка загрузки списка постов со страницы вакансий.");
        }
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            posts.add(detail(href.attr("href")));
        }
        return posts;
    }

    /**
     * Метод осуществляет извлечение информации из поста через переданную ссылку на данный пост.
     * Извлекается заголовок поста, его содержание, дата создания поста.
     * Из полученной информации собирается и вовзращается объект Post.
     * @param link Ссылка на пост
     * @return Объект Post
     */
    @Override
    public Post detail(String link) {
        Post post = new Post();
        StringJoiner join = new StringJoiner(System.lineSeparator());
        Document doc;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Ошибка загрузки деталей поста.");
        }
        Elements header = doc.select(".messageHeader");
        String title = header.get(0).textNodes().get(0).text();
        Elements bodies = doc.select(".msgBody");
        Element body = bodies.get(1);
        for (var element : body.childNodes()) {
            if (element instanceof TextNode) {
                join.add(((TextNode) element).text());
            } else if (element instanceof Element) {
                for (TextNode node : ((Element) element).textNodes()) {
                    join.add(node.text());
                }
            }
        }
        String footer = doc.selectFirst(".msgFooter").textNodes().get(0).text();
        String date = footer.replaceAll("[\\[]", "");
        post.setTitle(title);
        post.setDescription(join.toString());
        post.setLink(link);
        post.setDate(parse(date));
        return post;
    }

    /**
     * Метод осуществляет преобразование даты в формате заданном на сайте в формат Timestamp,
     * с целью дальнейшей возможности записи даты в базу данных.
     * @param time Дата и время в формате сайта
     * @return Дата и время в формате Timestamp
     */
    private Timestamp parse(String time) {
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
        Date date;
        try {
            date = formatter.parse(fullTime);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalStateException("Ошибка преобразования времени.");
        }
        return new Timestamp(date.getTime());
    }
}
