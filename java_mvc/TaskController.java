import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER LAYER (MVC)
 * Connects the View and the Model, and integrates with Python simple_ai_assistant.py
 * using Java's ProcessBuilder. Supports both a Web Dashboard (Java 21 built-in HttpServer)
 * and a Console CLI mode.
 */
public class TaskController {

    private final TaskModel model;
    private final TaskView view;
    private final File pythonScriptDir;

    public TaskController(TaskModel model, TaskView view, File pythonScriptDir) {
        this.model = model;
        this.view = view;
        this.pythonScriptDir = pythonScriptDir;
    }

    /**
     * Starts the lightweight Web Dashboard on http://localhost:<port>
     * Uses Java 21's built-in HttpServer (zero external dependencies).
     */
    public void startWebServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // GET / -> Render MVC Dashboard View
        server.createContext("/", exchange -> {
            String html = view.renderWebDashboard(model.getAllTasks(), model.getDefaultCity());
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // POST /add -> Ask Python simple_ai & add task to Model
        server.createContext("/add", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                String title = form.getOrDefault("title", "").trim();
                String city = form.getOrDefault("city", model.getDefaultCity()).trim();
                if (!city.isEmpty()) {
                    model.setDefaultCity(city);
                }
                if (!title.isEmpty()) {
                    String[] ai = askPythonSimpleAi(title, model.getDefaultCity());
                    model.addTask(title, model.getDefaultCity(), ai[0], ai[1], ai[2]);
                }
            }
            redirectHome(exchange);
        });

        // POST /toggle -> Toggle task completion in Model
        server.createContext("/toggle", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                try {
                    int id = Integer.parseInt(form.getOrDefault("id", "-1"));
                    model.toggleComplete(id);
                } catch (NumberFormatException ignored) {}
            }
            redirectHome(exchange);
        });

        // POST /delete -> Delete task from Model
        server.createContext("/delete", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                try {
                    int id = Integer.parseInt(form.getOrDefault("id", "-1"));
                    model.removeTask(id);
                } catch (NumberFormatException ignored) {}
            }
            redirectHome(exchange);
        });

        // POST /refresh -> Re-check live weather & time for all pending tasks
        server.createContext("/refresh", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                for (TaskModel.Task t : model.getAllTasks()) {
                    if (!t.isCompleted()) {
                        String[] ai = askPythonSimpleAi(t.getTitle(), t.getCity());
                        t.setCategory(ai[0]);
                        t.setRecommendedSlot(ai[1]);
                        t.setAiAdvice(ai[2]);
                    }
                }
                model.saveToDisk();
            }
            redirectHome(exchange);
        });

        server.start();
        System.out.println("======================================================================");
        System.out.println("  SmartSlot Web Dashboard running at: http://localhost:" + port);
        System.out.println("  (Press Ctrl+C to stop, or run with --cli for terminal mode)");
        System.out.println("======================================================================");
    }

    private void redirectHome(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return map;
    }

    /**
     * Interactive CLI MVC loop
     */
    public void startCli() {
        view.showWelcomeBanner(model.getDefaultCity());
        view.showTaskList(model.getAllTasks());

        while (true) {
            view.showMenu(model.getDefaultCity());
            String choice = view.readLine();

            switch (choice) {
                case "1":
                    String title = view.promptTaskTitle();
                    if (!title.isEmpty()) {
                        view.showAnalyzingLoading(title, model.getDefaultCity());
                        String[] ai = askPythonSimpleAi(title, model.getDefaultCity());
                        view.showTaskAdded(model.addTask(title, model.getDefaultCity(), ai[0], ai[1], ai[2]));
                    }
                    break;
                case "2":
                    view.showTaskList(model.getAllTasks());
                    break;
                case "3":
                    int toggleId = view.promptTaskId("toggle Done/Undone");
                    if (model.toggleComplete(toggleId)) view.showTaskList(model.getAllTasks());
                    else view.showMessage("Task not found.");
                    break;
                case "4":
                    for (TaskModel.Task t : model.getAllTasks()) {
                        if (!t.isCompleted()) {
                            view.showAnalyzingLoading(t.getTitle(), t.getCity());
                            String[] ai = askPythonSimpleAi(t.getTitle(), t.getCity());
                            t.setCategory(ai[0]);
                            t.setRecommendedSlot(ai[1]);
                            t.setAiAdvice(ai[2]);
                        }
                    }
                    model.saveToDisk();
                    view.showTaskList(model.getAllTasks());
                    break;
                case "5":
                    model.setDefaultCity(view.promptCity(model.getDefaultCity()));
                    break;
                case "6":
                    int delId = view.promptTaskId("delete");
                    model.removeTask(delId);
                    break;
                case "0":
                case "exit":
                    return;
                default:
                    view.showMessage("Invalid option.");
            }
        }
    }

    /**
     * Bridges Java MVC -> Python simple_ai_assistant.py
     * Expects format: TYPE|BEST_SLOT|ADVICE
     */
    private String[] askPythonSimpleAi(String taskTitle, String city) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "simple_ai_assistant.py", taskTitle, city);
            pb.directory(pythonScriptDir);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String lastLine = "";
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lastLine = line.trim();
                    }
                }
            }
            process.waitFor();

            String[] parts = lastLine.split("\\|", 3);
            if (parts.length == 3) {
                String type = parts[0].trim().toUpperCase();
                if (!type.contains("OUTDOOR") && !type.contains("INDOOR")) {
                    type = "INDOOR/OUTDOOR";
                }
                return new String[]{type, parts[1].trim(), parts[2].trim()};
            } else if (!lastLine.isEmpty()) {
                return new String[]{"SMART", "Today", lastLine};
            }
        } catch (Exception e) {
            return new String[]{"FLEXIBLE", "Anytime", "Could not reach simple_ai (" + e.getMessage() + ")"};
        }
        return new String[]{"FLEXIBLE", "Today", "Check weather before heading out."};
    }
}
