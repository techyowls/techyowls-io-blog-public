package main

import (
	"log"
	"os"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/techyowls/golang-rest-api/internal/handlers"
	"github.com/techyowls/golang-rest-api/internal/routes"
	"github.com/techyowls/golang-rest-api/pkg/database"
)

func main() {
	// Load environment variables
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using system environment")
	}

	// Initialize database
	db, err := database.Connect()
	if err != nil {
		log.Fatal("Failed to connect to database:", err)
	}

	// Run migrations
	if err := database.Migrate(db); err != nil {
		log.Fatal("Failed to run migrations:", err)
	}

	// Initialize handlers
	userHandler := handlers.NewUserHandler(db)
	healthHandler := handlers.NewHealthHandler()

	// Setup router
	router := gin.Default()
	routes.SetupRoutes(router, userHandler, healthHandler)

	// Start server
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("🚀 Server starting on port %s", port)
	if err := router.Run(":" + port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}
