package app.store;

import app.model.Student;
import com.google.gson.Gson;
import redis.clients.jedis.*;

public class RedisStore {

    private static JedisPool pool;
    private static final Gson gson = new Gson();

    public static void init() {
        if (pool == null) {
            JedisPoolConfig cfg = new JedisPoolConfig();
            cfg.setMaxTotal(64);
            cfg.setMaxIdle(16);
            cfg.setMinIdle(4);
            cfg.setTestOnBorrow(true);
            cfg.setTestWhileIdle(true);

            pool = new JedisPool(cfg, "localhost", 6379); 
        }

        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < 10000; i++) {
                String id = "2025" + String.format("%06d", i);
                Student s = new Student(id, "Ad Soyad " + i, "Bilgisayar");
                jedis.set(id, gson.toJson(s));
            }
        }
    }

    public static Student get(String id) {
        try (Jedis jedis = pool.getResource()) {
            String json = jedis.get(id);
            if (json == null) return null;
            return gson.fromJson(json, Student.class);
        }
    }

    public static void close() {
        if (pool != null) pool.close();
    }
}
