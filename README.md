# ⚡ SmartSlot — Weather & Time-Aware Task Scheduler (Java 21 MVC + Python `simple_ai`)

**SmartSlot** is a lightweight **Java 21 MVC** task scheduler integrated with a **Python LLM + Tool-Calling assistant (`simple_ai`)**.

Whenever you add a daily task (e.g., *"Evening run"*, *"Buy groceries"*, *"Study Java MVC"*), the Java `TaskController` calls `simple_ai_assistant.py`, which invokes live tools (`get_current_time` and `get_city_weather` via `wttr.in`) to automatically:
1. Classify the task as **`[INDOOR]`** or **`[OUTDOOR]`**
2. Recommend the **Best Time Slot** based on current time & weather
3. Generate a short, practical **AI Weather & Time Tip**

---

## 🏗️ Project Architecture (Java MVC + Python Tool-Calling)

```text
SmartSlot/
├── simple_ai_assistant.py   # LLM + Tool-calling assistant (supports CLI & Interactive modes)
├── tools.py                 # Live AI tools: get_current_time() & get_city_weather()
├── requirements.txt         # Python dependencies (openai, python-dotenv, requests)
├── Dockerfile               # Ready for cloud deployment (Render / Railway / Hugging Face)
└── java_mvc/
    ├── TaskModel.java       # MODEL: Task data structure & local file persistence (tasks_db.txt)
    ├── TaskView.java        # VIEW: Console CLI UI + Built-in Web Dashboard HTML
    ├── TaskController.java  # CONTROLLER: Bridges View <-> Model & invokes Python via ProcessBuilder
    └── SmartSlotApp.java    # MAIN: Entry point (supports both Web Dashboard & CLI modes)
```

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Prachi01Yadav/SmartSlot.git
cd SmartSlot
```

### 2. Set Up `.env`
Create a `.env` file in the root directory:
```env
BASE_URL=http://localhost:11434/v1
API_KEY=ollama
MODEL=llama3.2
```
*(You can use local Ollama or any OpenAI-compatible API such as Groq, OpenRouter, or OpenAI).*

### 3. Install Python Dependencies
```bash
pip install -r requirements.txt
```

### 4. Compile & Run (Java 21)
Compile the Java MVC classes:
```bash
javac java_mvc/*.java
```

* **Option A — Run the Web Dashboard (`http://localhost:8080`):**
  ```bash
  java -cp java_mvc SmartSlotApp
  ```
* **Option B — Run in Terminal CLI Mode:**
  ```bash
  java -cp java_mvc SmartSlotApp --cli
  ```
* **Option C — Run Standalone Python Assistant:**
  ```bash
  python simple_ai_assistant.py
  ```

---

## 🛠️ Tech Stack
* **Backend & Architecture**: Java 21 (Pure MVC — `Model`, `View`, `Controller`, `HttpServer`, `ProcessBuilder`)
* **AI & Tool Calling**: Python, OpenAI SDK / Ollama, `wttr.in` Live Weather API
