SNOMED Release Service: API
============================

The API is built as a self serving executable jar and as a traditional war.

To launch the executable jar:
`cd snomed-release-service`
`mvn package`
`cd api`
`java -jar target/exec-api.jar -httpPort=8085`

The API will be available here: http://localhost:8085/api/v1/
```sh
cd snomed-release-service
mvn clean install
cd api/target
java -jar exec-api.jar -httpPort=8085
```

The API will then be available on your machine here: http://localhost:8085/api/v1/
