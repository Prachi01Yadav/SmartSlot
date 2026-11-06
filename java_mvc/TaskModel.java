import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL LAYER (MVC)
 * Holds the Task data structure and handles local file persistence (tasks_db.txt)
 * so your daily schedule is saved across runs without needing a heavy SQL database.
 */
public class TaskModel {

    public static class Task {
        private int id;
        private String title;
        private String city;
        private String category;        // INDOOR or OUTDOOR
        private String recommendedSlot; // e.g., "Today 5:30 PM" or "Tomorrow 8 AM"
        private String aiAdvice;        // Weather & time-aware tip from simple_ai
        private boolean completed;

        public Task(int id, String title, String city, String category, String recommendedSlot, String aiAdvice, boolean completed) {
            this.id = id;
            this.title = title;
            this.city = city;
            this.category = category;
            this.recommendedSlot = recommendedSlot;
            this.aiAdvice = aiAdvice;
            this.completed = completed;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCity() { return city; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getRecommendedSlot() { return recommendedSlot; }
        public void setRecommendedSlot(String recommendedSlot) { this.recommendedSlot = recommendedSlot; }
        public String getAiAdvice() { return aiAdvice; }
        public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    private final List<Task> tasks = new ArrayList<>();
    private final Path storagePath;
    private String defaultCity = "Pune";
    private int nextId = 1;

    public TaskModel(String storageFile) {
        this.storagePath = Paths.get(storageFile);
        loadFromDisk();
    }

    public String getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(String city) {
        if (city != null && !city.trim().isEmpty()) {
            this.defaultCity = city.trim();
        }
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public Task addTask(String title, String city, String category, String recommendedSlot, String aiAdvice) {
        Task task = new Task(nextId++, title, city, category, recommendedSlot, aiAdvice, false);
        tasks.add(task);
        saveToDisk();
        return task;
    }

    public boolean toggleComplete(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) {
                t.setCompleted(!t.isCompleted());
                saveToDisk();
                return true;
            }
        }
        return false;
    }

    public boolean removeTask(int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) {
            saveToDisk();
        }
        return removed;
    }

    public void saveToDisk() {
        List<String> lines = new ArrayList<>();
        for (Task t : tasks) {
            String line = String.join("\t",
                    String.valueOf(t.getId()),
                    sanitize(t.getTitle()),
                    sanitize(t.getCity()),
                    sanitize(t.getCategory()),
                    sanitize(t.getRecommendedSlot()),
                    sanitize(t.getAiAdvice()),
                    String.valueOf(t.isCompleted())
            );
            lines.add(line);
        }
        try {
            Files.write(storagePath, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private void loadFromDisk() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length >= 7) {
                    int id = Integer.parseInt(parts[0]);
                    Task t = new Task(id, parts[1], parts[2], parts[3], parts[4], parts[5], Boolean.parseBoolean(parts[6]));
                    tasks.add(t);
                    if (id >= nextId) {
                        nextId = id + 1;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String sanitize(String input) {
        return input == null ? "" : input.replace("\t", " ").replace("\n", " ").replace("\r", "").trim();
    }
}

