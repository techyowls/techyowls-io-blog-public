# Golang REST API Guide

A complete example of building a production-ready REST API with Go, Gin, PostgreSQL, and JWT authentication.

## 🚀 Quick Start

```bash
# Clone and run with Docker
docker-compose up -d

# API is running at http://localhost:8080
```

## 📌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/api/v1/register` | Create user |
| GET | `/api/v1/users` | Get all users (auth) |
| GET | `/api/v1/users/:id` | Get user by ID (auth) |
| PUT | `/api/v1/users/:id` | Update user (auth) |
| DELETE | `/api/v1/users/:id` | Delete user (auth) |

## 🛠️ Tech Stack

- **Go 1.21** - Programming language
- **Gin** - Web framework
- **GORM** - ORM for PostgreSQL
- **JWT** - Authentication
- **Docker** - Containerization

## 📁 Project Structure

```
golang-rest-api-guide/
├── main.go                 # Entry point
├── internal/
│   ├── handlers/          # HTTP handlers
│   ├── models/            # Data models
│   ├── middleware/        # JWT auth
│   └── routes/            # Route setup
├── pkg/
│   └── database/          # DB connection
├── Dockerfile
└── docker-compose.yml
```

## 📚 Tutorial

See the complete guide at [techyowls.io/blog/golang-rest-api-guide](https://techyowls.io/blog/golang-rest-api-guide)
