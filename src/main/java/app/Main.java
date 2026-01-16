package app;

import static spark.Spark.*;

import com.google.gson.Gson;
import app.store.*;

public class Main {

    static class Err {
        String error;
        Err(String error) { this.error = error; }
    }

    public static void main(String[] args) {
        port(8080);

        Gson gson = new Gson();

        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(new Err(
                    "Internal error: " + e.getClass().getSimpleName() +
                            (e.getMessage() != null ? (": " + e.getMessage()) : "")
            )));
        });

        try { RedisStore.init(); }
        catch (Exception e) { System.err.println("Redis init FAILED: " + e.getMessage()); e.printStackTrace(); }

        try { HazelcastStore.init(); }
        catch (Exception e) { System.err.println("Hazelcast init FAILED: " + e.getMessage()); e.printStackTrace(); }

        try { MongoStore.init(); }
        catch (Exception e) { System.err.println("Mongo init FAILED: " + e.getMessage()); e.printStackTrace(); }

        get("/health", (req, res) -> {
            res.type("application/json");
            return "{\"ok\":true}";
        });

        get("/routes", (req, res) -> {
            res.type("application/json");
            return gson.toJson(new String[]{
                    "GET /health",
                    "GET /routes",
                    "GET /nosql-lab-rd/student_no=2025000001",
                    "GET /nosql-lab-hz/student_no=2025000001",
                    "GET /nosql-lab-mon/student_no=2025000001"
            });
        });

        get("/nosql-lab-rd/*", (req, res) -> {
            res.type("application/json");
            String id = extractId(req.splat()[0]);
            if (id.isEmpty()) { res.status(400); return gson.toJson(new Err("Bad request: expected student_no=XXXXXXXXXX")); }

            var s = RedisStore.get(id);
            if (s == null) { res.status(404); return gson.toJson(new Err("Not found: " + id)); }
            return gson.toJson(s);
        });

        get("/nosql-lab-hz/*", (req, res) -> {
            res.type("application/json");
            String id = extractId(req.splat()[0]);
            if (id.isEmpty()) { res.status(400); return gson.toJson(new Err("Bad request: expected student_no=XXXXXXXXXX")); }

            var s = HazelcastStore.get(id);
            if (s == null) { res.status(404); return gson.toJson(new Err("Not found: " + id)); }
            return gson.toJson(s);
        });

        get("/nosql-lab-mon/*", (req, res) -> {
            res.type("application/json");
            String id = extractId(req.splat()[0]);
            if (id.isEmpty()) { res.status(400); return gson.toJson(new Err("Bad request: expected student_no=XXXXXXXXXX")); }

            var s = MongoStore.get(id);
            if (s == null) { res.status(404); return gson.toJson(new Err("Not found: " + id)); }
            return gson.toJson(s);
        });
    }

    private static String extractId(String tail) {
        if (tail == null) return "";
        tail = tail.trim();
        if (!tail.startsWith("student_no=")) return "";
        String id = tail.substring("student_no=".length()).trim();
        return id.matches("\\d{10}") ? id : "";
    }
}
