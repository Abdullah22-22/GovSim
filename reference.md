# GovSim — File Reference

## What each file does and where it's used

---

## model/ — Shared Classes (used everywhere)

### `Severity.java`
**What it does:** ENUM with two values — NORMAL or DANGEROUS  
**Used in:** Event, EventGenerator, Ministry, EventRouter

---

### `Event.java`
**What it does:** Represents one daily incident (ministry, description, severity, day, resolved)  
**Used in:** EventGenerator, EventRouter, Ministry, President, Report, Database, GUI

---

### `City.java`
**What it does:** Holds city state — budget, satisfaction, month, year  
**Used in:** SimuEngine, President, AIAdvisor, Database, GUI Dashboard

---

### `Minister.java`
**What it does:** Holds minister data — name, ministry, score, warnings, status  
**Used in:** SimuEngine, AnnualReview, President, Database, GUI Dashboard

---

### `Decision.java`
**What it does:** Represents a president decision — choice, cost, outcome  
**Used in:** President, AIAdvisor, Database, GUI Alert

---

### `Report.java`
**What it does:** Monthly ministry report — list of events, rating, resolved count  
**Used in:** Ministry, ReportGenerator, President, Database, GUI

---

## simulation/ — Core Engine

### `EventGenerator.java` — SP1
**What it does:** Generates random daily events for each ministry based on probability  
**Used in:** SimuEngine  
**Uses:** Event, Severity

---

### `EventRouter.java` — SP2
**What it does:** Receives events and routes them to the correct ministry  
**Used in:** SimuEngine  
**Uses:** Event, Ministry

---

### `SimuEngine.java` — Main Loop
**What it does:** Runs the simulation — day loop, monthly meeting, annual review  
**Used in:** MainApp (entry point)  
**Uses:** EventGenerator, EventRouter, Ministry, President, City, Minister

---

### `AIAdvisor.java`
**What it does:** Analyzes situation and suggests decisions to the president  
**Used in:** President, SimuEngine (monthly meeting)  
**Uses:** Event, City, Minister, Decision

---

### `ReportGenerator.java` — SP5
**What it does:** Collects ministry reports and builds monthly + annual summaries  
**Used in:** SimuEngine  
**Uses:** Report, Ministry, City

---

### `AnnualReview.java`
**What it does:** Evaluates ministers every year — promote, warn, or fire  
**Used in:** SimuEngine  
**Uses:** Minister, Report

---

## ministry/ — SP3 Service Points

### `Ministry.java` — Abstract Base
**What it does:** Template for all ministries — receiveEvent(), handleNormal(), handleDangerous(), generateReport()  
**Used in:** EventRouter, SimuEngine  
**Extended by:** All 5 ministries

---

### `InteriorMinistry.java` — SP3-A
**What it does:** Handles police, prison, security events  
**Uses:** Ministry, Event, Report

---

### `DefenseMinistry.java` — SP3-B
**What it does:** Handles army, border, military threat events  
**Uses:** Ministry, Event, Report

---

### `FinanceMinistry.java` — SP3-C
**What it does:** Handles budget, tax, corruption events  
**Uses:** Ministry, Event, Report

---

### `PopulationMinistry.java` — SP3-D
**What it does:** Handles citizen, housing, protest events  
**Uses:** Ministry, Event, Report

---

### `HealthMinistry.java` — SP3-E
**What it does:** Handles hospital, epidemic, doctor events  
**Uses:** Ministry, Event, Report

---

## president/ — SP4

### `President.java`
**What it does:** Receives dangerous events, calls AIAdvisor, applies decision to city  
**Used in:** SimuEngine, Ministry (handleDangerous)  
**Uses:** Event, Decision, AIAdvisor, City

---

## database/ — Save & Load

### `DBManager.java`
**What it does:** Opens SQLite connection, creates tables on first run  
**Used in:** All DAO classes

---

### `CityDAO.java`
**What it does:** Saves and loads city state (budget, satisfaction, month, year)  
**Uses:** City, DBManager

---

### `MinisterDAO.java`
**What it does:** Saves and loads minister data  
**Uses:** Minister, DBManager

---

### `EventDAO.java`
**What it does:** Saves and loads event log  
**Uses:** Event, DBManager

---

## gui/ — JavaFX Screens

### `MainApp.java`
**What it does:** Entry point — init(), start(), launches JavaFX  
**Uses:** SimuEngine, LoginController

---

### `LoginController.java`
**What it does:** Login screen — new game, continue, logout  
**Uses:** City, DBManager

---

### `DashboardController.java`
**What it does:** Main simulation view — city stats, ministry panels, controls  
**Uses:** City, Minister, SimuEngine

---

### `AlertController.java`
**What it does:** Dangerous event popup — shows AI suggestions, president decides  
**Uses:** Event, Decision, AIAdvisor

---

## Most Used Files (used in 5+ places)

| File | Used in |
|------|---------|
| `Event.java` | Generator, Router, Ministry, President, Report, DB, GUI |
| `City.java` | SimuEngine, President, AI, DB, GUI |
| `Minister.java` | SimuEngine, AnnualReview, President, DB, GUI |
| `Report.java` | Ministry, ReportGenerator, President, DB, GUI |
| `Decision.java` | President, AI, DB, GUI |

---

*GovSim · Government Simulation · Java OOP Project*
