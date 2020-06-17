# Billing Service
## Docker 
### PostgreSQL
```
docker run -d --name platform-postgres -e POSTGRES_USER=platformuser -e POSTGRES_PASSWORD=H3lpM#Plz -e POSTGRES_DB=platform -p 5432:5432 postgres:9.6
```
### Zipkin Server
```
docker run -d --name platform-zipkin-server -p 9411:9411 openzipkin/zipkin
```
