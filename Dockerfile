FROM public.ecr.aws/docker/library/maven:3.8.5-openjdk-17-slim AS build
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package spring-boot:repackage

FROM public.ecr.aws/docker/library/maven:3.8.5-openjdk-17-slim
COPY --from=build /tmp/target/waypoint-authorization-api.jar waypoint-authorization-api.jar
EXPOSE 8080

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh

ENTRYPOINT [ "sh","/entrypoint.sh" ]