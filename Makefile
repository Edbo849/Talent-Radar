.PHONY: help dev stop clean logs shell db-shell build test

# Default help command
help:
		@echo "Talent Radar Development Commands:"
		@echo "  make dev      - Start development environment"
		@echo "  make stop     - Stop all containers"
		@echo "  make clean    - Remove all containers and volumes"
		@echo "  make logs     - Show logs from all services"
		@echo "  make build    - Build development Docker images"
		@echo "  make test     - Run all tests"
		@echo "  make db-shell - Open PostgreSQL shell"

# Development environment
dev:
		@echo "🚀 Starting development environment..."
		@cp .env.example .env 2>/dev/null || true
		docker-compose -f docker-compose.dev.yml up -d
		@echo "✅ Development environment started!"
		@echo "📱 Frontend: http://localhost:3000"
		@echo "🔧 Backend: http://localhost:8080"
		@echo "🐘 Database: localhost:5432"

# Stop all services
stop:
		@echo "🛑 Stopping all services..."
		docker-compose -f docker-compose.dev.yml down
		@echo "✅ All services stopped!"

# Clean everything
clean: stop
		@echo "🧹 Cleaning Docker resources..."
		docker-compose -f docker-compose.dev.yml down -v --remove-orphans
		docker system prune -f
		@echo "✅ Cleanup completed!"

# Build all images
build:
		@echo "🔨 Building Docker images..."
		docker-compose -f docker-compose.dev.yml build
		@echo "✅ Build completed!"

# Show logs
logs:
		docker-compose -f docker-compose.dev.yml logs -f

# Database shell
db-shell:
		docker-compose -f docker-compose.dev.yml exec postgres psql -U talent_radar_user -d talent_radar_dev

# Run tests
test:
		@echo "🧪 Running tests..."
		docker-compose -f docker-compose.dev.yml exec backend ./mvnw test
		docker-compose -f docker-compose.dev.yml exec frontend npm test -- --coverage --watchAll=false
		@echo "✅ Tests completed!"