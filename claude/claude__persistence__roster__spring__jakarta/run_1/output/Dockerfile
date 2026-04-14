FROM maven:3.9.12-ibm-semeru-21-noble

USER root
# install uv
RUN apt-get update && apt-get install -y --no-install-recommends curl python3-venv python3-full \
    && curl -LsSf https://astral.sh/uv/install.sh | sh

ENV PATH="/root/.local/bin:$PATH"

# create venv and install
RUN uv venv /opt/venv \
    && uv pip install --python /opt/venv/bin/python pytest

ENV PATH="/opt/venv/bin:$PATH"

# Set working directory
WORKDIR /app

# Copy all the assets
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi && if [ -f smoke.py ]; then chmod +x smoke.py; fi && if [ -f test.sh ]; then chmod +x test.sh; fi
RUN mvn clean install -pl roster-ear -am

CMD ["mvn", "liberty:run", "-pl", "roster-ear"]
