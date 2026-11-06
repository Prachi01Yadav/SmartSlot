FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y python3 python3-pip && ln -sf /usr/bin/python3 /usr/bin/python

WORKDIR /app
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY . .
RUN javac java_mvc/*.java

EXPOSE 8080
CMD ["java", "-cp", "java_mvc", "SmartSlotApp"]
