mvn clean package docker:build -Pdev
docker run -p 8080:8080 -t wennaisong/gag-spider