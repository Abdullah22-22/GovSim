# GovSim — Government Simulation

GovSim is a city management simulation game built with Java and JavaFX.
You play as a president managing a city through 5 ministries: Interior, Defense, Finance, Health, and Population.
Each day, random events happen across the city. Normal events are handled automatically by ministers, while dangerous events pause the simulation and require your decision.
An AI advisor (powered by Groq) suggests options for each situation.
At the end of every month you receive a report, and every year you review your ministers and decide to keep or fire them.

---

## Setup

### 1. Requirements

* Java 21+
* Maven
* MySQL 8+
* IntelliJ IDEA (recommended)

---

### 2. Groq API Keys

The project uses the Groq API for AI suggestions.
Get your free API key from: https://console.groq.com

Create a file called `.env` in the **root of the project** and add:

```env
GROQ_API_KEY_1=

GROQ_API_KEY_2=

GROQ_MODEL=llama-3.3-70b-versatile

DB_URL=jdbc:mysql://localhost:3306/govsim
DB_USER=root
DB_PASSWORD=
```

> You can use the same key for both, or get two keys to avoid rate limits.

---

### 3. Database

The project connects to MySQL automatically and creates the database and tables on first run.

Database credentials are loaded automatically from the `.env` file.

Make sure these values are correct:

```env
DB_URL=jdbc:mysql://localhost:3306/govsim
DB_USER=root
DB_PASSWORD=
```

---

### 4. Run the Project

Open the project in IntelliJ IDEA, then run `MainApp.java`.
