### Quarkus Makefile
APP_NAME        := jobs-quarkus
IMAGE_NAME      := $(APP_NAME):$(shell uuidgen | tr "[:upper:]" "[:lower:]" | head -c 8)
CONTAINER_NAME  := $(APP_NAME)-$(shell uuidgen | tr "[:upper:]" "[:lower:]" | head -c 8)
APP_PORT        := 8080
SUCCESS_PATTERN := Listening on:
FAILURE_PATTERN := BUILD FAILURE|\[ERROR\] COMPILATION ERROR|COMPILATION ERROR|\[ERROR\] Failed to execute goal|\[ERROR\] \*\*\*|\[FATAL\]|MojoExecutionException|ProjectBuildingException|ModelParseException|cannot find symbol|package .* does not exist| error:

compile:
	@echo "=== COMPILE START ==="
	@docker build -f Dockerfile -t $(IMAGE_NAME) . || { echo "=== COMPILE FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1 || true; exit 1; }
	@echo "[INFO] Built image: $(IMAGE_NAME)"
	@echo "=== COMPILE OK ==="

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

deploy: compile
	@echo "=== DEPLOY START ==="
	-@docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true

	### Run the container in detached mode
	docker run -d --name $(CONTAINER_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(CONTAINER_NAME), waiting for app to start..."

	@docker logs -f $(CONTAINER_NAME) 2>&1 & LOG_PID=$$!; \
	while ! docker logs $(CONTAINER_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)|$(SUCCESS_PATTERN)"; do sleep 1; done; \
	kill $$LOG_PID 2>/dev/null; wait $$LOG_PID 2>/dev/null || true; \
	if docker logs $(CONTAINER_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
		echo "\[ERROR\] Build failed in container:"; \
		docker logs $(CONTAINER_NAME) 2>&1; \
		echo "=== DEPLOY FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1; exit 1; \
	fi
	@echo "[INFO] Quarkus application started and ready."
	@echo "=== DEPLOY OK ==="
logs:
	docker logs $(CONTAINER_NAME)

down:
	- docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true
	@echo "[INFO] Container removed (if it existed)"

clean: down
	- docker rmi $$(docker images -q $(APP_NAME)) 2>/dev/null
	- docker image prune -f
	@echo "[INFO] Removed app image(s) and pruned dangling Docker layers"

test: deploy
	@echo "=== TEST START ==="
	@echo "[INFO] === Container build/startup logs ==="
	@docker logs $(CONTAINER_NAME)
	@docker exec $(CONTAINER_NAME) sh -c 'until curl -sS "http://localhost:$(APP_PORT)" >/dev/null; do sleep 1; done'
	@docker exec $(CONTAINER_NAME) sh -c 'python3 /app/smoke.py' || { echo "=== TEST FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1; exit 1; }
	@echo "=== TEST OK ==="
	@echo "[INFO] === Container logs after test ==="
	@docker logs $(CONTAINER_NAME)
	- docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true
	- docker rmi $$(docker images -q $(APP_NAME)) 2>/dev/null
	- docker image prune -f
	@echo "[INFO] Cleaned up container and image"

local:
	./mvnw clean package quarkus:run
