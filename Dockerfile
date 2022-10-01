#Build from gradle and run Java 18
#Name: Layercraft-Connector
FROM gradle:7.5.1-jdk18 as builder

ARG USERNAME
ARG TOKEN

#Print out the username and token
RUN echo "Username: $USERNAME"
RUN echo "Token: $TOKEN"

#Copy the source code to the container
COPY . /home/gradle/src
#Set the working directory
WORKDIR /home/gradle/src
#Build the project
RUN gradle distTar

FROM openjdk:18-jdk-bullseye as runtime
#Copy the built project to the container
COPY --from=builder /home/gradle/src/build/distributions/ /home/gradle/src/build/distributions/
#UnTar the project
RUN mkdir /home/runtime
RUN tar -xvf /home/gradle/src/build/distributions/*.tar -C /home/runtime/
RUN mv /home/runtime/* /home/runtime/layercraft-connector
RUN rm -rf /home/gradle
WORKDIR /home/runtime
#Run the project
RUN ls -la /home/runtime/layercraft-connector

CMD ["sh", "./layercraft-connector/bin/Connector"]
#Expose the port
EXPOSE 25565

