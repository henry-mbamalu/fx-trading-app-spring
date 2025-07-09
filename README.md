# 📈 FX Trading Application

A robust and scalable **foreign exchange (FX) trading platform** built with **Spring Boot**, designed to handle real-time currency trading with high performance and fault tolerance.

---

## 🔧 Features

- ⚙️ **Spring Boot** backend for trading operations and REST APIs  
- 🚀 **Redis** for caching live FX rates and providing fallback during downtime  
- 📨 **RabbitMQ** for queueing trade requests and processing via background workers  
- 💾 Support for storing executed trades and transaction logs  
- 📊 Real-time exchange rate retrieval with minimal latency  

---

## 🛠️ Tech Stack

- **Java**, **Spring Boot**
- **Redis** – Caching and fallback mechanism for exchange rates
- **RabbitMQ** – Asynchronous message queue for background job processing
- **PostgreSQL / MySQL** – For persistent trade storage (optional)
- **Docker** – For containerized deployment (optional)

---

## ✅ Use Cases

- Perform real-time foreign exchange trades
- Efficiently fetch and cache live FX rates
- Decouple trade processing from user requests
- Build resilient and fault-tolerant trading workflows

---

## 🚀 Getting Started

```bash
# Clone the repository
git clone https://github.com/henry-mbamalu/fx-trading-app-spring.git
cd fx-trading-app

# Add your application.properties, clone application-example.properties

# Build and run
./mvnw spring-boot:run
