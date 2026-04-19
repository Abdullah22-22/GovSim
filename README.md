# SmartCity — Government Simulation

A Java + JavaFX simulation where the player manages a city as government president.
Every day, random incidents occur across 5 ministries. Normal events are handled automatically.
Dangerous events halt the simulation and force the president to make an immediate decision.
At the end of each month, ministers report to the president. Every year, ministers are promoted, warned, or fired.
Game state is saved in SQLite so the player can continue where they left off.



## Service Points

| SP  | Class                  | Role                              |
|-----|------------------------|-----------------------------------|
| SP1 | EventGenerator.java    | Generates random daily events     |
| SP2 | EventRouter.java       | Routes event to correct ministry  |
| SP3 | Ministry/*.java        | 5 ministries handle their events  |
| SP4 | President.java         | Handles dangerous events only     |
| SP5 | ReportGenerator.java   | Builds monthly and annual reports |

---

## Paths Through the System

```
Normal   → SP1 → SP2 → SP3 → SP5
Dangerous→ SP1 → SP2 → SP3 → SP4 → SP5
Monthly  → SP3 × 5 → SP4 → SP5
```

---

## Project Structure

```
GovSim/
│
├── src/main/java/com/govsim/govsim/
│   │
│   ├── model/                         # Shared data classes — used everywhere
│   │   ├── Event.java                 # One incident: ministry, severity, day, resolved
│   │   ├── Report.java                # Monthly report: list of events + rating
│   │   ├── Minister.java              # Minister: name, score, warnings, status
│   │   ├── Decision.java              # President decision: choice, cost, outcome
│   │   ├── City.java                  # City state: budget, satisfaction, month
│   │   └── Severity.java              # ENUM: NORMAL / DANGEROUS
│   │
│   ├── simulation/                    # Core simulation engine
│   │   ├── SimuEngine.java            # Main loop: runs days 1-30
│   │   ├── EventGenerator.java        # SP1: generates random Events
│   │   ├── EventRouter.java           # SP2: routes Event to correct Ministry
│   │   ├── ReportGenerator.java       # SP5: builds monthly & annual reports
│   │   ├── AnnualReview.java          # Yearly minister evaluation
│   │   └── AIAdvisor.java             # Suggests decisions to president
│   │
│   ├── ministry/                      # SP3 — 5 Ministry service points
│   │   ├── Ministry.java              # Abstract base class
│   │   ├── InteriorMinistry.java      # SP3-A: police, prison events
│   │   ├── DefenseMinistry.java       # SP3-B: army, border events
│   │   ├── FinanceMinistry.java       # SP3-C: budget, tax events
│   │   ├── PopulationMinistry.java    # SP3-D: citizens, housing events
│   │   └── HealthMinistry.java        # SP3-E: hospital, epidemic events
│   │
│   ├── president/                     # SP4 — President decisions
│   │   └── President.java             # Handles dangerous events only
│   │
│   ├── gui/                           # JavaFX screens
│   │   ├── MainApp.java               # Entry point: init(), start()
│   │   ├── LoginController.java       # Login: new game / continue
│   │   ├── DashboardController.java   # Main view: stats + controls
│   │   └── AlertController.java       # Danger popup: president decides
│   │
│   └── database/                      # SQLite save & load
│       ├── DBManager.java             # Opens connection, creates tables
│       ├── CityDAO.java               # Save/load city state
│       ├── MinisterDAO.java           # Save/load ministers
│       └── EventDAO.java              # Save/load events log
│
├── src/main/resources/
│   ├── fxml/                          # JavaFX screen layouts
│   │   ├── login.fxml
│   │   ├── dashboard.fxml
│   │   └── alert.fxml
│   ├── style.css                      # App stylesheet
│   ├── govsim.db                      # SQLite database file
│   └── config.properties             # Default simulation settings
│
├── pom.xml                            # Maven: JavaFX + SQLite dependencies
└── README.md                          # Project description
```




*Smart City · Government Simulation · Java OOP Project*
