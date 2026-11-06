import java.io.File;

/**
 * ENTRY POINT (Wires Model + View + Controller)
 *
 * 1. Compile (Java 21):
 *    javac java_mvc/*.java
 *
 * 2. Run Web Dashboard (http://localhost:8080):
 *    java -cp java_mvc SmartSlotApp
 *
 * 3. Run Terminal CLI Mode:
 *    java -cp java_mvc SmartSlotApp --cli
 */
public class SmartSlotApp {

    public static void main(String[] args) throws Exception {
        TaskModel model = new TaskModel("tasks_db.txt");
        TaskView view = new TaskView();
        TaskController controller = new TaskController(model, view, new File("."));

        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            controller.startCli();
        } else {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            controller.startWebServer(port);
        }
    }
}
