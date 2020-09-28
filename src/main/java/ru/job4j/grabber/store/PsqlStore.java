package ru.job4j.grabber.store;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.parser.SqlRuParse;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Class PsqlStore
 * Класс осуществляет работу с базой данных.
 * @author Dmitry Razumov
 * @version 1
 */
public class PsqlStore implements Store, AutoCloseable {
    /**
     * Поле содержит параметры соединения в базой.
     */
    private final Connection cnn;

    /**
     * Конструктор инициализирует соединение с базой
     * @param cfg Параметры соединения
     */
    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("db.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("db.url"),
                    cfg.getProperty("db.username"),
                    cfg.getProperty("db.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Метод сохраняет объявление в базе.
     * @param post Объявление
     * @return Объявление
     */
    @Override
    public Post save(Post post) {
        try (PreparedStatement st = cnn.prepareStatement(
                "insert into posts(name, text, link, created) values(?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, post.getTitle());
            st.setString(2, post.getDescription());
            st.setString(3, post.getLink());
            st.setTimestamp(4, new Timestamp(post.getDate().getTime()));
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                post.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    /**
     * Метод осуществляет поиск всех объявлений в базе.
     * @return Список объявлений
     */
    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (Statement st = cnn.createStatement();
             ResultSet rs = st.executeQuery("select * from posts")) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("name"));
                post.setDescription(rs.getString("text"));
                post.setLink(rs.getString("link"));
                post.setDate(rs.getTimestamp("created"));
                posts.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    /**
     * Метод ищет объявление по его ИД.
     * @param id идентификационный номер
     * @return Объявление
     */
    @Override
    public Post findById(String id) {
        Post post = null;
        try (PreparedStatement st = cnn.prepareStatement("select * from posts where id = ?")) {
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("name"));
                post.setDescription(rs.getString("text"));
                post.setLink(rs.getString("link"));
                post.setDate(rs.getTimestamp("created"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    /**
     * Метод закрывает соединение с базой
     * @throws Exception Исключение при ошибке закрытия соединения с базой
     */
    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    /**
     * Главный метод программы.
     * В методе демонстрируется работа класса.
     * @param args Параметры командной строки
     */
    public static void main(String[] args) {
        String config = "app.properties";
        Properties cfg = new Properties();
        try (InputStream input = PsqlStore.class.getClassLoader().getResourceAsStream(config)) {
            cfg.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Ошибка чтения файла конфигурации.");
        }
        PsqlStore store = new PsqlStore(cfg);
        List<Post> first = new SqlRuParse().list("https://www.sql.ru/forum/job-offers");
        first.forEach(store::save);
        List<Post> second = store.getAll();
        Post post = store.findById(String.valueOf(first.get(0).getId()));
    }
}
