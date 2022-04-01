FROM openjdk:latest
LABEL maintainer Dev

COPY build/install/deletion_service-boot deletion_service

ENTRYPOINT ["/deletion_service/bin/deletion_service"]
