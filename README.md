# Yapay Zeka Destekli Akıllı Otopark Doluluk Tespit ve Analitik Sistemi
## AI-Powered Smart Parking Occupancy Detection & Analytics System

An end-to-end intelligent parking occupancy detection and analytics system combining computer vision, asynchronous messaging architectures, and real-time dashboards. This project was developed as a graduation thesis (ENG402) for Istanbul Commerce University.

You can access to CCTV simulation https://github.com/LilWndrr/Carla-Simulator-CCTV and test frontend https://github.com/LilWndrr/ParkingSLotsDetectionFrontend

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [System Architecture](#%EF%B8%8F-system-architecture)
- [Tech Stack](#-tech-stack)
- [Folder Structure](#-folder-structure)
- [Getting Started & Installation](#-getting-started--installation)
  - [Prerequisites](#prerequisites)
  - [Docker Compose Quick Start (Recommended)](#docker-compose-quick-start-recommended)
  - [Manual Local Setup](#manual-local-setup)
- [Database Setup & Seeding](#-database-setup--seeding)
- [Backend REST API & WebSockets](#-backend-rest-api--websockets)
- [Performance & Stabilization](#-performance--stabilization)
- [Authors & License](#-authors--license)

---

## 🔍 Project Overview

Finding empty parking spaces in congested urban areas leads to significant fuel and time waste. Traditional solutions involving physical slot sensors are capital-intensive to install, maintain, and scale.

This project provides a software-based, **vision-centric alternative**. By utilizing existing security camera feeds, the system detects parking slot occupancy without any physical in-ground sensors.
- It consumes incoming camera frames via a REST Ingestion API.
- Leverages **RabbitMQ** for load balancing and asynchronous frame ingestion to handle high incoming throughput.
- Runs **YOLO-based object detection** on the CPU via **ONNX Runtime** to locate vehicles in real-time.
- Matches detected vehicle coordinate points (ground contact points) against predefined parking slot polygons using **OpenCV** geometry (`pointPolygonTest`).
- Employs a **multi-frame validation** buffer (3 consecutive frames) to prevent state flickering caused by shadows, temporary occlusion, or passing cars.
- Distributes updates in real-time to clients using **STOMP WebSockets** and caches live states in **Redis** for ultra-low latency reads.
- Aggregates long-term analytics and saves snapshots in **PostgreSQL** for historical reporting.

---

## ✨ Key Features

1. **YOLO + ONNX Model Inference**: Runs object detection using `yolo26xSmall.onnx` inside the JVM on CPU without needing heavy GPU library configurations.
2. **Interactive Admin Slot Editor**: A custom canvas interface built on React & Fabric.js that allows administrators to overlay and draw polygon slots directly on live camera views.
3. **Live Parking Map**: Shows real-time occupancy updates on the parking lot layout using Leaflet and React WebSockets.
4. **Analytics Dashboard**: Visualizes hourly trends, occupancy heatmaps, and slot state transitions over time using Recharts.
5. **Robust State Engine**: Implements a `REQUIRED_CONSECUTIVE_FRAMES` threshold to ensure transitions only happen when status changes are confirmed over multiple frames.
6. **Containerized Environment**: The entire ecosystem (PostgreSQL, Redis, RabbitMQ, Spring Boot) can be launched using a single Docker Compose command.

---

## 🗺️ System Architecture

```
                                  +-----------------------+
                                  |   Camera / Simulator  |
                                  +-----------+-----------+
                                              |
                                              | HTTP POST (Image bytes + metadata)
                                              v
                                  +-----------+-----------+
                                  |    Spring Backend     |
                                  |  (Ingestion API /v1)  |
                                  +-----------+-----------+
                                              |
                                              | Publish Raw Frames
                                              v
                                  +-----------+-----------+
                                  |        RabbitMQ       |
                                  | (raw-camera-frames q) |
                                  +-----------+-----------+
                                              |
                                              | Listen & Process (Async)
                                              v
                                  +-----------+-----------+
                                  |      YoloService      |
                                  | (ONNX Runtime + OpenCV)
                                  +-----+-----+-----+-----+
                                        |     |     |
                 Update Cache           |     |     | Broadcast Websocket Updates
        +-------------------------------+     |     +-------------------------------+
        |                                     |                                     |
        v                                     v Persist Events                      v
+-------+-------+                      +------+------+                      +-------+-------+
|  Redis Cache  |                      | PostgreSQL  |                      | WebSockets    |
| (Live States) |                      |  Database   |                      | (STOMP /ws)   |
+---------------+                      +------+------+                      +-------+-------+
                                              |                                     |
                                              | Query Historical Metrics            | Stream updates
                                              v                                     v
                                  +-----------+-----------+               +---------+---------+
                                  |  Analytics Dashboard  |               |  Live Parking Map |
                                  |     (React App)       |               |     (React App)   |
                                  +-----------------------+               +-------------------+
```

---

## 🛠️ Tech Stack

### Backend
- **Core Framework**: Spring Boot 4.0.3, Java 17
- **Database & JPA**: PostgreSQL, Hibernate
- **Message Broker**: RabbitMQ
- **Cache**: Redis
- **Computer Vision & Inference**: ONNX Runtime Java (v1.20.0), OpenCV (OpenPNP bindings v4.9.0)
- **Image Storage**: Cloudinary integration (for archiving detected frames)

### Frontend
- **Framework**: React 19 (Vite)
- **State & Routing**: React Router DOM v7
- **WebSockets**: StompJS, SockJS
- **Visuals & Charts**: Leaflet (Maps), Recharts (Analytics charts), Fabric.js (Admin canvas editor)

---

## 📁 Folder Structure

```
carDetection/
├── src/main/java/org/seyf/cardetection/
│   ├── config/              # WebSocket, Web, and RabbitMQ configs
│   ├── controller/          # Ingestion, Display, Occupancy, Parking, and Slot REST Controllers
│   ├── dto/                 # Data transfer objects for APIs
│   ├── helper/              # Utility classes and image helpers
│   ├── model/               # JPA Entities (Slot, Camera, GroundLevel, SlotEvent, etc.)
│   ├── repository/          # JPA Repositories
│   └── service/             # Business logic (YoloService, IngestionService, SnapshotService)
├── src/main/resources/
│   ├── application.properties # Main application properties
│   └── yolo26xSmall.onnx    # YOLO model weights used for CPU inference
├── parking-frontend/        # React + Vite application
│   ├── src/
│   │   ├── api/             # Axios API integration
│   │   ├── components/      # Shared UI components
│   │   ├── pages/           # Pages (Dashboard, LiveMap, ParkingMap, AdminSlotEditor)
│   │   └── index.css        # Main stylesheet
│   └── package.json         # Frontend dependencies and npm scripts
├── Dockerfile               # Multi-stage build configuration for Spring Boot
├── docker-compose.yml       # Configuration for infrastructure (DB, Rabbit, Redis, App)
└── car_detection_dump.sql   # PostgreSQL database dump file
```

---

## 🚀 Getting Started & Installation

### Prerequisites
Make sure you have the following installed on your machine:
- [Docker & Docker Compose](https://www.docker.com/)
- [Java 17 JDK](https://adoptium.net/) (if running locally without Docker)
- [Maven 3.8+](https://maven.apache.org/) (if compiling locally)
- [Node.js v18+](https://nodejs.org/) (for running the frontend)

---

### Docker Compose Quick Start (Recommended)

1. Clone or download this repository.
2. Navigate to the root directory.
3. Start the entire container stack:
   ```bash
   docker-compose up --build
   ```
4. This command spins up:
   - **PostgreSQL** on port `5432`
   - **Redis** on port `6379`
   - **RabbitMQ** on port `5672` (Management console on `http://localhost:15672` using user/pass: `guest`/`guest`)
   - **Spring Boot Backend** on port `8085`

---

### Manual Local Setup

If you prefer to run services manually for debugging or active development:

#### Step 1: Start Infrastructure Services
Run only PostgreSQL, Redis, and RabbitMQ via Docker:
```bash
docker-compose up postgres redis rabbitmq -d
```

#### Step 2: Restore the Database Schema & Mock Data
A PostgreSQL backup dump is provided at the root directory (`car_detection_dump.sql`). Restore it to seed your local database:
```bash
# Example command using psql
psql -h localhost -U postgres -d car_detection -f car_detection_dump.sql
```
*(The default database password is set to `12345` in `application.properties`)*

#### Step 3: Run the Spring Boot Backend
From the root directory, run using the Maven Wrapper:
```bash
./mvnw spring-boot:run
```
The backend server runs on `http://localhost:8085`.

#### Step 4: Run the Frontend
1. Navigate to the frontend directory:
   ```bash
   cd parking-frontend
   ```
2. Install the node packages:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to `http://localhost:5173`.

---

## 🗄️ Database Setup & Seeding

The database requires initial configurations for Otopark, GroundLevels, and Camera entities before it can process frames. 
- You can populate this data by importing the `car_detection_dump.sql` backup.
- Alternatively, you can use the **Admin Slot Editor** at `http://localhost:5173/admin` to set up camera views, draw polygons for slots, and configure layouts.

---

## 🔌 Backend REST API & WebSockets

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/api/v1/ingestion/forward` | Ingests multipart camera frames and sends them to RabbitMQ queue `raw-camera-frames` |
| **GET** | `/api/v1/display/map` | Retrieves real-time slot lists and status details |
| **GET** | `/api/v1/occupancy/byHour` | Analytics endpoint returning hourly occupancy rates |
| **GET** | `/api/v1/events/countByHours` | Analytical overview of state change transitions |
| **POST** | `/api/v2/slots` | Imports a batch of slot coordinate definitions |

### WebSockets (STOMP Broker)
- **Broker Endpoint**: `ws://localhost:8085/ws`
- **Topic Subscription**: `/topic/parking-updates`
- **Payload Format**:
  ```json
  {
    "parkingName": "Main Parking Lot",
    "groundLevelName": "Floor 1",
    "slotId": 12,
    "isEmpty": false
  }
  ```

---

## ⚡ Performance & Stabilization

- **Asynchronous Processing**: Camera frames are stored in RabbitMQ and processed asynchronously. To prevent processing lag under heavy load, frames older than 5 seconds are skipped automatically.
- **Consecutive Confirmation Buffer**: Before updating a slot's status in the database/Redis, the system requires a slot to be consistently identified as occupied or empty across 3 consecutive frames (`REQUIRED_CONSECUTIVE_FRAMES = 3`).
- **Ground Contact Heuristic**: Instead of using the center of the bounding box, the detection engine tests the **bottom 15% center point** of the vehicle bounding box. This aligns with where the vehicle touches the ground, avoiding perspective errors.
- **Resource Management**: Native OpenCV memory objects (`Mat`) are strictly closed inside `finally` blocks to prevent memory leaks in the JVM.
