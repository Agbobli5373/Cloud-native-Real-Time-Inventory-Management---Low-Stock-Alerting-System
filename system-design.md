---
config:
  look: neo
---
graph TD
classDef service fill:#dbe9f4,stroke:#333,stroke-width:2px;
classDef database fill:#e9e9e9,stroke:#333,stroke-width:2px;
classDef cache fill:#fdf5e6,stroke:#333,stroke-width:2px;
classDef broker fill:#ffd27f,stroke:#333,stroke-width:2px;
classDef monitoring fill:#d4edda,stroke:#333,stroke-width:2px;
classDef external fill:#ffffff,stroke:#666,stroke-width:1px,stroke-dasharray: 5 5;
classDef gateway fill:#f8d7da,stroke:#333,stroke-width:2px;
classDef config fill:#e2e3e5,stroke:#333,stroke-width:2px;
subgraph SystemBoundary["System Boundary (Kubernetes Cluster)"]
direction TB
subgraph APILayer["API Gateway Layer"]
apiGateway["API Gateway\n(Spring Cloud Gateway)"]:::gateway
authService["Authentication Service\n(OAuth2/JWT)"]:::service
end
subgraph CoreServices["Core Services"]
inventorySvc["Inventory Service\n(Java/Spring Boot)"]:::service
orderSvc["Order Service\n(Java/Spring Boot)"]:::service
alerterSvc["Stock Alerter Service\n(Java/Spring Boot)"]:::service
notificationSvc["Notification Service\n(Java/Spring Boot)"]:::service
end
subgraph DataLayer["Data Layer"]
inventoryDb["PostgreSQL\n(Inventory Items)\n- Sharded for Scale\n- Read Replicas"]:::database
orderDb["PostgreSQL\n(Orders)"]:::database
alertConfigDb["PostgreSQL\n(Alert Configurations)"]:::database
redisCache["Redis Cluster\n(Distributed Cache)\n- Stock Counts\n- Rate Limiting"]:::cache
end
subgraph Messaging["Event Streaming Platform"]
kafkaBroker["Apache Kafka\n(Event Streaming)\n- High Throughput\n- Persistence\n- Exactly-once delivery"]:::broker
schemaRegistry["Schema Registry\n(Avro Schemas)"]:::service
end
subgraph ConfigManagement["Configuration Management"]
configServer["Spring Cloud Config Server"]:::config
serviceRegistry["Service Registry\n(Eureka/Consul)"]:::config
end
subgraph Monitoring["Observability Stack"]
prometheus["Prometheus\n(Metrics Collection)"]:::monitoring
grafana["Grafana\n(Dashboards)"]:::monitoring
alertmanager["Alertmanager\n(Alert Routing)"]:::monitoring
jaeger["Jaeger\n(Distributed Tracing)"]:::monitoring
elk["ELK Stack\n(Log Aggregation)"]:::monitoring
end
subgraph Resilience["Resilience Patterns"]
circuitBreaker["Circuit Breaker\n(Resilience4j)"]:::service
rateLimiter["Rate Limiter\n(Redis-based)"]:::service
bulkhead["Bulkhead\n(Thread Isolation)"]:::service
end
end
extClient["External Clients\n(Web/Mobile Apps, B2B)"]:::external
emailSvc["Email Service\n(AWS SES)"]:::external
smsSvc["SMS Service\n(Twilio)"]:::external
pushSvc["Push Notification\n(Firebase)"]:::external
opsTeam["Operations Team\n(PagerDuty)"]:::external
cicd["CI/CD Pipeline\n(GitHub Actions/Jenkins)"]:::external
extClient -- "HTTPS Requests" --> apiGateway
apiGateway -- "Authenticate" --> authService
apiGateway -- "Route Requests" --> inventorySvc
apiGateway -- "Route Requests" --> orderSvc
inventorySvc -- "Read/Write" --> inventoryDb
inventorySvc -- "Cache Operations" --> redisCache
inventorySvc -- "Publish Events" --> kafkaBroker
orderSvc -- "Read/Write" --> orderDb
orderSvc -- "Publish Events" --> kafkaBroker
orderSvc -- "Check Inventory" --> inventorySvc
kafkaBroker -- "Consume Events" --> alerterSvc
alerterSvc -- "Read Configurations" --> alertConfigDb
alerterSvc -- "Send Notification Requests" --> kafkaBroker
kafkaBroker -- "Consume Notification Events" --> notificationSvc
notificationSvc -- "Send Email" --> emailSvc
notificationSvc -- "Send SMS" --> smsSvc
notificationSvc -- "Send Push" --> pushSvc
configServer -- "Provide Configurations" --> inventorySvc
configServer -- "Provide Configurations" --> orderSvc
configServer -- "Provide Configurations" --> alerterSvc
configServer -- "Provide Configurations" --> notificationSvc
inventorySvc -- "Register" --> serviceRegistry
orderSvc -- "Register" --> serviceRegistry
alerterSvc -- "Register" --> serviceRegistry
notificationSvc -- "Register" --> serviceRegistry
prometheus -- "Scrape Metrics" --> inventorySvc
prometheus -- "Scrape Metrics" --> orderSvc
prometheus -- "Scrape Metrics" --> alerterSvc
prometheus -- "Scrape Metrics" --> notificationSvc
prometheus -- "Scrape Metrics" --> apiGateway
grafana -- "Query Metrics" --> prometheus
prometheus -- "Fire Alerts" --> alertmanager
alertmanager -- "Send Notifications" --> opsTeam
inventorySvc -- "Implements" --> circuitBreaker
orderSvc -- "Implements" --> circuitBreaker
apiGateway -- "Implements" --> rateLimiter
inventorySvc -- "Implements" --> bulkhead
cicd -- "Deploy" --> SystemBoundary
class apiGateway,authService gateway;
class inventorySvc,orderSvc,alerterSvc,notificationSvc service;
class inventoryDb,orderDb,alertConfigDb database;
class redisCache cache;
class kafkaBroker,schemaRegistry broker;
class configServer,serviceRegistry config;
class prometheus,grafana,alertmanager,jaeger,elk monitoring;
class circuitBreaker,rateLimiter,bulkhead service;
class extClient,emailSvc,smsSvc,pushSvc,opsTeam,cicd external;
