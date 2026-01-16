package app.store;

import app.model.Student;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastStore {
    private static HazelcastInstance hz;
    private static IMap<String, Student> map;

    public static void init() {
        ClientConfig config = new ClientConfig();
        config.setClusterName("dev");

        config.getNetworkConfig().addAddress("127.0.0.1:5701");

        hz = HazelcastClient.newHazelcastClient(config);

        map = hz.getMap("ogrenciler");

        // 10.000 seed
        for (int i = 0; i < 10000; i++) {
            String id = "2025" + String.format("%06d", i);
            Student s = new Student(id, "Ad Soyad " + i, "Bilgisayar");
            map.put(id, s);
        }
    }

    public static Student get(String id) {
        return map.get(id);
    }
}
