.PHONY: all
all: build-jars build-audit-docker-image run

.PHONY: build-jars
build-jars:
	cd exporter; ./mvnw clean package
	cd auditapp; ./mvnw clean package
	
.PHONY: build-audit-docker-image
build-audit-docker-image:
	cd auditapp; docker build -t camunda-community/auditapp .
	
.PHONY: run
run:
	docker-compose up -d