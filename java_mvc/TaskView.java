import java.util.List;
import java.util.Scanner;

/**
 * VIEW LAYER (MVC)
 * Renders both the Web Dashboard HTML (for browser/cloud deployment)
 * and the Console CLI view. Contains zero business or AI logic.
 */
public class TaskView {

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Renders a clean, responsive single-page HTML Dashboard for the browser.
     */
    public String renderWebDashboard(List<TaskModel.Task> tasks, String currentCity) {
        StringBuilder rows = new StringBuilder();
        if (tasks.isEmpty()) {
            rows.append("<div class='empty'>No tasks scheduled yet. Add a task above and let <b>simple_ai</b> check live weather & time!</div>");
        } else {
            for (TaskModel.Task t : tasks) {
                boolean outdoor = t.getCategory().toUpperCase().contains("OUTDOOR");
                String badgeClass = outdoor ? "badge-outdoor" : "badge-indoor";
                String cardClass = t.isCompleted() ? "task-card done" : "task-card";
                rows.append("<div class='").append(cardClass).append("'>")
                    .append("<div class='task-main'>")
                    .append("<div class='task-header'>")
                    .append("<span class='task-title'>#").append(t.getId()).append(" ").append(escapeHtml(t.getTitle())).append("</span>")
                    .append("<span class='badge ").append(badgeClass).append("'>").append(escapeHtml(t.getCategory())).append("</span>")
                    .append("<span class='badge badge-slot'>&#128338; ").append(escapeHtml(t.getRecommendedSlot())).append("</span>")
                    .append("<span class='badge badge-city'>&#128205; ").append(escapeHtml(t.getCity())).append("</span>")
                    .append("</div>")
                    .append("<div class='ai-advice'>&#10024; <b>AI Tip:</b> ").append(escapeHtml(t.getAiAdvice())).append("</div>")
                    .append("</div>")
                    .append("<div class='task-actions'>")
                    .append("<form method='POST' action='/toggle' style='margin:0;'>")
                    .append("<input type='hidden' name='id' value='").append(t.getId()).append("'/>")
                    .append("<button type='submit' class='btn-small ").append(t.isCompleted() ? "btn-undo" : "btn-done").append("'>")
                    .append(t.isCompleted() ? "Undo" : "Done &#10003;").append("</button>")
                    .append("</form>")
                    .append("<form method='POST' action='/delete' style='margin:0;'>")
                    .append("<input type='hidden' name='id' value='").append(t.getId()).append("'/>")
                    .append("<button type='submit' class='btn-small btn-del'>&#10005;</button>")
                    .append("</form>")
                    .append("</div>")
                    .append("</div>");
            }
        }

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
            + "<title>SmartSlot — Java MVC + Python simple_ai</title>"
            + "<style>"
            + "body { font-family: system-ui, -apple-system, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 32px 16px; }"
            + ".container { max-width: 760px; margin: 0 auto; }"
            + "header { margin-bottom: 24px; border-bottom: 1px solid #1e293b; padding-bottom: 16px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; }"
            + "h1 { margin: 0; font-size: 1.6rem; color: #38bdf8; }"
            + ".subtitle { color: #94a3b8; font-size: 0.9rem; margin-top: 4px; }"
            + ".add-box { background: #1e293b; padding: 18px; border-radius: 12px; display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 24px; border: 1px solid #334155; }"
            + "input[type=text] { background: #0f172a; border: 1px solid #334155; color: #f8fafc; padding: 10px 14px; border-radius: 8px; font-size: 0.95rem; }"
            + ".input-task { flex: 2; min-width: 220px; }"
            + ".input-city { flex: 1; min-width: 110px; }"
            + "button { cursor: pointer; border: none; border-radius: 8px; font-weight: 600; padding: 10px 16px; font-size: 0.9rem; transition: opacity 0.15s; }"
            + "button:hover { opacity: 0.9; }"
            + ".btn-primary { background: #0284c7; color: white; }"
            + ".btn-refresh { background: #334155; color: #e2e8f0; }"
            + ".task-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 16px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; gap: 14px; }"
            + ".task-card.done { opacity: 0.55; }"
            + ".task-card.done .task-title { text-decoration: line-through; }"
            + ".task-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }"
            + ".task-title { font-weight: 600; font-size: 1.05rem; }"
            + ".badge { font-size: 0.75rem; padding: 3px 8px; border-radius: 99px; font-weight: 600; }"
            + ".badge-outdoor { background: #065f46; color: #6ee7b7; }"
            + ".badge-indoor { background: #3730a3; color: #c7d2fe; }"
            + ".badge-slot { background: #0f172a; color: #38bdf8; border: 1px solid #0369a1; }"
            + ".badge-city { background: #0f172a; color: #cbd5e1; }"
            + ".ai-advice { color: #cbd5e1; font-size: 0.9rem; }"
            + ".task-actions { display: flex; gap: 6px; }"
            + ".btn-small { padding: 6px 11px; font-size: 0.8rem; }"
            + ".btn-done { background: #059669; color: white; }"
            + ".btn-undo { background: #475569; color: white; }"
            + ".btn-del { background: #991b1b; color: #fecaca; }"
            + ".empty { text-align: center; padding: 36px; background: #1e293b; border-radius: 10px; color: #94a3b8; border: 1px dashed #334155; }"
            + "</style></head><body><div class='container'>"
            + "<header><div><h1>&#9889; SmartSlot MVC</h1>"
            + "<div class='subtitle'>Weather &amp; Time-Aware Task Scheduler &bull; Java 21 MVC + Python <code>simple_ai</code></div></div>"
            + "<form method='POST' action='/refresh' style='margin:0;'>"
            + "<button type='submit' class='btn-refresh'>&#128260; Refresh Live Weather Tips</button></form></header>"
            + "<form class='add-box' method='POST' action='/add' onsubmit=\"this.querySelector('button').innerText='Asking simple_ai...';\">"
            + "<input class='input-task' type='text' name='title' placeholder='What do you need to do? (e.g. Evening run, Buy groceries, Study Java)' required/>"
            + "<input class='input-city' type='text' name='city' value='" + escapeHtml(currentCity) + "' placeholder='City' required/>"
            + "<button type='submit' class='btn-primary'>+ Add Smart Task</button>"
            + "</form>"
            + rows
            + "</div></body></html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    // --- CLI VIEW METHODS ---
    public void showWelcomeBanner(String defaultCity) {
        System.out.println("\n======================================================================");
        System.out.println("   SMARTSLOT : Weather & Time-Aware Daily Task Scheduler (Java MVC)   ");
        System.out.println("   Powered by Python simple_ai (Live wttr.in Weather + System Clock)  ");
        System.out.println("   Active City: " + defaultCity);
        System.out.println("======================================================================");
    }

    public void showMenu(String currentCity) {
        System.out.println("\nChoose an action:");
        System.out.println("  [1] + Add New Task (AI checks live weather & time in " + currentCity + ")");
        System.out.println("  [2] View All Scheduled Tasks");
        System.out.println("  [3] Toggle Task Done / Undone");
        System.out.println("  [4] Re-Analyze Pending Tasks with Current Weather & Time");
        System.out.println("  [5] Change City (Current: " + currentCity + ")");
        System.out.println("  [6] Delete a Task");
        System.out.println("  [0] Exit");
        System.out.print("\nYour choice: ");
    }

    public String readLine() { return scanner.nextLine().trim(); }

    public String promptTaskTitle() {
        System.out.print("Enter task description: ");
        return scanner.nextLine().trim();
    }

    public String promptCity(String currentCity) {
        System.out.print("Enter city name (Enter for '" + currentCity + "'): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentCity : input;
    }

    public int promptTaskId(String actionName) {
        System.out.print("Enter Task ID to " + actionName + ": ");
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    public void showAnalyzingLoading(String taskTitle, String city) {
        System.out.println("\n[simple_ai] Checking live weather in " + city + " & current clock for: \"" + taskTitle + "\"...");
    }

    public void showTaskAdded(TaskModel.Task task) {
        System.out.println("\n  Added #" + task.getId() + " [" + task.getCategory() + "] " + task.getTitle()
                + " | Best Slot: " + task.getRecommendedSlot() + " | AI Tip: " + task.getAiAdvice());
    }

    public void showTaskList(List<TaskModel.Task> tasks) {
        System.out.println("\n--- YOUR SMART DAILY SCHEDULE (" + tasks.size() + " tasks) ---");
        if (tasks.isEmpty()) {
            System.out.println("  No tasks yet!");
            return;
        }
        for (TaskModel.Task t : tasks) {
            String status = t.isCompleted() ? "[DONE]   " : "[PENDING]";
            System.out.printf("  #%d %s %-24s | [%s] | Slot: %s%n",
                    t.getId(), status, t.getTitle(), t.getCategory(), t.getRecommendedSlot());
            System.out.printf("     -> AI Tip (%s): %s%n", t.getCity(), t.getAiAdvice());
        }
    }

    public void showMessage(String message) {
        System.out.println("  " + message);
    }
}
