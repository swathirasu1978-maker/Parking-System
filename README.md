# 🚗 Parking Management System (Java + MySQL)

A **console-based Parking Management System** developed using **Core Java, JDBC, and MySQL**.  
This project is designed for **beginner-level Java learners** and is suitable for **IT placements**.

---

## 📌 Project Overview

This system manages parking operations such as vehicle entry, exit, slot allocation, fee calculation, income reports, and parking history using a MySQL database.

---

## 🛠️ Technologies Used

- Java (Core Java)
- JDBC (MySQL Connector)
- MySQL (XAMPP / phpMyAdmin)
- Command Line / VS Code
- Git & GitHub

---

## 🗄️ Database Details

**Database Name:** `parking_system`

Tables Used:
- `vehicles`
- `parking_history`

Database is created and managed using **phpMyAdmin (XAMPP)**.

---

## ▶️ How to Run the Project

### 1️⃣ Start MySQL Server
- Open **XAMPP**
- Start **MySQL**
- Open browser → `http://localhost/phpmyadmin`
- Create database `parking_system`
- Create required tables

---

### 2️⃣ Compile the Project

```bash
javac -cp ".;lib/mysql-connector-java-8.0.32.jar" -d bin src/app/Main.java src/dao/*.java src/model/*.java src/service/*.java
