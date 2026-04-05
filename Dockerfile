# ── Stage 1: Build ───────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests

# ── Stage 2: Run ─────────────────────────────────────────────
FROM tomcat:10.1-jre17-temurin

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy as ROOT app (serves at /)
COPY --from=builder /build/target/dungeon-realm.war \
     /usr/local/tomcat/webapps/ROOT.war

# Entrypoint handles Railway's dynamic $PORT
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
