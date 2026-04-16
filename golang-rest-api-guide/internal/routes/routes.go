package routes

import (
	"github.com/gin-gonic/gin"
	"github.com/techyowls/golang-rest-api/internal/handlers"
	"github.com/techyowls/golang-rest-api/internal/middleware"
)

func SetupRoutes(r *gin.Engine, userHandler *handlers.UserHandler, healthHandler *handlers.HealthHandler) {
	// Health check (public)
	r.GET("/health", healthHandler.Health)

	// API v1 group
	v1 := r.Group("/api/v1")
	{
		// Public routes
		v1.POST("/register", userHandler.CreateUser)

		// Protected routes
		protected := v1.Group("/")
		protected.Use(middleware.AuthMiddleware())
		{
			protected.GET("/users", userHandler.GetAllUsers)
			protected.GET("/users/:id", userHandler.GetUserByID)
			protected.PUT("/users/:id", userHandler.UpdateUser)
			protected.DELETE("/users/:id", userHandler.DeleteUser)
		}
	}
}
