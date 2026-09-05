# 🎯 DSA Buddies

> Track your DSA progress with your WhatsApp study group

A full-stack web application for DSA (Data Structures & Algorithms) task tracking, built for WhatsApp community study groups.

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/hi2gaurav/DSABuddies)

## ✨ Features

- 🔐 **Google Sign-In** — One-click authentication
- 📋 **Task Sheets** — Daily/weekly problem sets with LeetCode links
- ✅ **Progress Tracking** — Check off solved problems, earn XP
- 🏆 **Leaderboard** — Compete with group members by XP
- 🔥 **Streak Counter** — Track your daily solving streak
- 👤 **Profiles** — Personal stats, topic progress, activity heatmap
- ⚙️ **Admin Panel** — Create tasks, manage members
- 🌗 **Dark/Light Mode** — With system preference detection

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 + Spring Boot 3.4 |
| Auth | Spring Security + Google OAuth2 |
| Database | H2 (dev) / PostgreSQL (prod) |
| Frontend | React 18 + TypeScript + Tailwind CSS |
| Build | Maven + Vite (single JAR) |

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 20+ (for frontend development)

### Run Locally
```bash
# Clone the repo
git clone https://github.com/hi2gaurav/DSABuddies.git
cd DSABuddies

# Build frontend
cd frontend && npm install && npm run build && cd ..

# Run
mvn spring-boot:run -DskipTests -Dskip.npm -Dskip.installnodenpm
```
Open http://localhost:8080

### Google OAuth2 Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create OAuth 2.0 credentials
3. Set redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Set environment variables:
```bash
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret
```

## 📊 DSA Topics Covered

Arrays • Strings • Linked Lists • Stacks • Queues • Trees • Graphs • Hashing • Sorting • Searching • Dynamic Programming • Greedy • Backtracking

## 📄 License

MIT
