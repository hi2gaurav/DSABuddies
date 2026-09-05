package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DesignTemplateDto;
import com.dsabuddies.app.dto.SaveDesignRequest;
import com.dsabuddies.app.dto.UserDesignDto;
import com.dsabuddies.app.model.DesignTemplate;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserDesign;
import com.dsabuddies.app.repository.DesignTemplateRepository;
import com.dsabuddies.app.repository.UserDesignRepository;
import com.dsabuddies.app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignTemplateService {

    private final DesignTemplateRepository templateRepository;
    private final UserDesignRepository userDesignRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initTemplates() {
        if (templateRepository.count() > 0) return;

        List<DesignTemplate> templates = List.of(
            // HLD 1: TinyURL
            DesignTemplate.builder()
                .title("Design a Scalable URL Shortener (TinyURL)")
                .category("HLD")
                .difficulty("BEGINNER")
                .tags("Hashing, Base62, Caching, Redis, SQL/NoSQL")
                .overview("Design a distributed service that converts long URLs into short 7-character aliases (e.g., https://tiny.url/abc123z) and redirects users with low latency.")
                .requirements("Functional:\n1. Given a long URL, generate a unique short alias.\n2. Redirect users to the original URL with < 20ms p99 latency.\n3. Custom aliases & expiration times.\n\nNon-Functional:\n1. 100M new URLs/month, 10B reads/month (100:1 read/write ratio).\n2. 99.99% availability, fault-tolerant, horizontally scalable.")
                .components("1. API Gateway / Load Balancer\n2. Shortener Service (Base62 generator + Snowflake ID / Key Generation Service)\n3. Relational DB (PostgreSQL) or Key-Value Store (DynamoDB)\n4. Distributed Cache (Redis) caching top 20% hot URLs\n5. Analytics Service (Kafka + ClickHouse for redirect metrics)")
                .diagramData("graph TD\n  Client[User Browser] --> LB[Load Balancer]\n  LB --> App[URL Shortener Service]\n  App --> Redis[(Redis Cache)]\n  App --> DB[(PostgreSQL / DynamoDB)]\n  App --> Kafka[Kafka Event Bus]\n  Kafka --> Analytics[(ClickHouse Analytics)]")
                .sampleSolution("Key Design Takeaways:\n- ID Generation: Pre-generate unique 64-bit integer tokens via Ticket Server or KGS (Key Generation Service) and encode into Base62 [a-zA-Z0-9].\n- Redirect Status Code: HTTP 302 (Found) allows real-time analytics tracking; HTTP 301 (Moved Permanently) leverages client-side caching to reduce server load.\n- Cache Eviction: LRU (Least Recently Used) cache strategy in Redis storing hash -> long_url.")
                .build(),

            // HLD 2: Distributed Rate Limiter
            DesignTemplate.builder()
                .title("Design a Distributed Rate Limiter")
                .category("HLD")
                .difficulty("INTERMEDIATE")
                .tags("Rate Limiting, Redis, Sliding Window, Concurrency")
                .overview("Design an enterprise-grade distributed rate limiter to protect backend microservices from DDoS attacks, scraping, and noisy neighbors.")
                .requirements("Functional:\n1. Limit requests per client IP or user API key (e.g. 1,000 requests/hour).\n2. Return HTTP 429 Too Many Requests with Retry-After header.\n\nNon-Functional:\n1. Sub-millisecond latency overhead (< 2ms).\n2. Low memory footprint, distributed across multiple edge proxies.")
                .components("1. Edge Proxy / API Gateway (Kong / Envoy)\n2. Rate Limiting Middleware (Lua script in Redis / Sentinel)\n3. In-Memory Data Store (Redis cluster for sliding window counters)\n4. Rules Configuration Service & Dashboard")
                .diagramData("graph LR\n  Client --> Gateway[API Gateway / Envoy]\n  Gateway --> Limiter[Rate Limiter Plugin]\n  Limiter --> Redis[(Redis Cluster)]\n  Gateway --> Backend[Target Microservices]")
                .sampleSolution("Algorithms comparison:\n- Token Bucket: Refills tokens at constant rate; supports bursts. Best for general API tiers.\n- Sliding Window Counter: Combines fixed window counter with previous window ratio; zero memory bloat while preventing boundary traffic spikes.\n- Redis Implementation: Execute atomic Lua scripts containing MULTI/EXEC to prevent race conditions during distributed increments.")
                .build(),

            // HLD 3: Real-Time Chat System
            DesignTemplate.builder()
                .title("Design a Real-Time Chat Application (WhatsApp/Slack)")
                .category("HLD")
                .difficulty("ADVANCED")
                .tags("WebSockets, Message Ordering, Cassandra, Kafka, Presence")
                .overview("Architect a real-time messaging system supporting 1-on-1 chats, group channels, online presence, and offline push delivery for 50M concurrent users.")
                .requirements("Functional:\n1. Real-time 1-on-1 and group messaging with delivery receipts (Sent, Delivered, Read).\n2. Online/offline user presence tracking.\n3. Offline push notifications.\n\nNon-Functional:\n1. Latency < 100ms globally.\n2. Strict message ordering per channel/chat.\n3. End-to-end data durability.")
                .components("1. WebSocket Connection Servers (Netty/Go)\n2. Chat Microservices (Authentication, Channel Manager)\n3. Message Broker (Apache Kafka for chat fanout)\n4. Distributed Wide-Column Store (Apache Cassandra / ScyllaDB for message history)\n5. Ephemeral Key-Value Store (Redis for user session & presence state)\n6. Push Notification Gateway (FCM / APNs)")
                .diagramData("graph TD\n  ClientA[User A] <--> WS[WebSocket Gateway]\n  WS <--> Redis[(Redis Presence)]\n  WS --> Kafka[Kafka Pipeline]\n  Kafka --> Cassandra[(Cassandra Message Store)]\n  Kafka --> Push[Push Notification Service]\n  Push --> ClientB[User B Device]")
                .sampleSolution("Architecture Highlights:\n- Connection Protocol: Persistent full-duplex WebSockets with heartbeats every 30s.\n- Data Partitioning: In Cassandra, partition key is `chat_id` and clustering key is `message_id` (Snowflake UUID strictly monotonic in time).\n- Group Fanout: For small groups (< 500 members), fanout on write. For massive broadcast channels (> 100k members), fanout on read with pull-based cursor.")
                .build(),

            // LLD 1: Parking Lot System
            DesignTemplate.builder()
                .title("Design a Low-Level Parking Lot System")
                .category("LLD")
                .difficulty("BEGINNER")
                .tags("OOP, Strategy Pattern, Factory, Concurrency")
                .overview("Design clean object-oriented classes and data structures for an automated multi-floor parking lot with different vehicle types, parking spots, and payment gates.")
                .requirements("Requirements:\n1. Support Motorcycle, Compact Car, Large Truck/SUV.\n2. Multiple floors with assigned spots (TwoWheelerSpot, CompactSpot, LargeSpot).\n3. Entry ticket generation and exit fee calculation (Hourly, Flat-rate, Vehicle-type multipliers).\n4. Thread-safe spot allocation.")
                .components("Classes:\n- Vehicle (abstract): Motorcycle, Car, Truck\n- ParkingSpot (abstract): TwoWheelerSpot, CompactSpot, LargeSpot\n- ParkingLot (Singleton): controls floors and overall occupancy\n- ParkingFloor: manages spots per level\n- ParkingTicket: timestamp, spotId, vehicleNumber\n- PricingStrategy (Strategy Pattern): FlatRateStrategy, HourlyRateStrategy\n- PaymentProcessor: CreditCard, Cash, UPI")
                .diagramData("classDiagram\n  class Vehicle {\n    +String licensePlate\n    +VehicleType type\n  }\n  class ParkingSpot {\n    +int spotId\n    +boolean isOccupied\n    +assignVehicle(Vehicle)\n    +vacate()\n  }\n  class ParkingLot {\n    -List~ParkingFloor~ floors\n    +parkVehicle(Vehicle)\n    +releaseVehicle(Ticket)\n  }\n  ParkingLot --> ParkingFloor\n  ParkingFloor --> ParkingSpot")
                .sampleSolution("Key Clean Code Patterns:\n- Singleton: `ParkingLot` orchestrates the singleton instance.\n- Factory Pattern: `VehicleFactory` creates vehicle instances.\n- Strategy Pattern: `ParkingFeeCalculationStrategy` decouples pricing algorithms from ticketing.\n- Thread-Safety: Use `ReentrantLock` or `ConcurrentSkipListSet` per floor to prevent race conditions during simultaneous spot acquisitions.")
                .build(),

            // LLD 2: Elevator Control System
            DesignTemplate.builder()
                .title("Design an Elevator Control System")
                .category("LLD")
                .difficulty("INTERMEDIATE")
                .tags("State Pattern, Observer, SCAN Algorithm, Priority Queue")
                .overview("Design an object-oriented elevator system with multiple elevator cars operating across N floors with optimal dispatching algorithms.")
                .requirements("Requirements:\n1. Multiple elevators (Car 1..M) servicing N floors.\n2. Up/Down requests from floors; destination button inside each elevator car.\n3. Elevators transition states: IDLE, MOVING_UP, MOVING_DOWN.\n4. Optimize wait time and energy efficiency.")
                .components("Classes:\n- ElevatorCar: carId, currentFloor, direction, state, floorQueue\n- ElevatorController: dispatches nearest/most efficient elevator\n- Request: sourceFloor, destinationFloor, direction\n- ElevatorState (State Pattern): IdleState, MovingUpState, MovingDownState\n- DispatchStrategy (Strategy Pattern): SCAN / LOOK algorithm")
                .diagramData("classDiagram\n  class ElevatorCar {\n    +int currentFloor\n    +Direction direction\n    +State state\n    +move()\n  }\n  class ElevatorController {\n    +List~ElevatorCar~ cars\n    +handleExternalRequest(Request)\n  }\n  ElevatorController --> ElevatorCar")
                .sampleSolution("Key Algorithms:\n- LOOK / SCAN Algorithm: Keep moving in the current direction until all pending requests in that direction are fulfilled before reversing.\n- Data Structures: Two priority queues per elevator (one min-heap for floors above, one max-heap for floors below).\n- Concurrency: `synchronized` floor scheduling and atomic status transitions.")
                .build()
        );

        templateRepository.saveAll(templates);
    }

    public List<DesignTemplateDto> getAllTemplates(String category) {
        List<DesignTemplate> list = (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category))
                ? templateRepository.findByCategoryIgnoreCase(category)
                : templateRepository.findAllByOrderByCreatedAtDesc();

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public DesignTemplateDto getTemplate(Long id) {
        return toDto(templateRepository.findById(id).orElseThrow());
    }

    public List<UserDesignDto> getUserDesigns(Long userId) {
        return userDesignRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toUserDesignDto).collect(Collectors.toList());
    }

    @Transactional
    public UserDesignDto saveUserDesign(Long userId, SaveDesignRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        DesignTemplate template = (req.templateId() != null)
                ? templateRepository.findById(req.templateId()).orElse(null)
                : null;

        UserDesign design = UserDesign.builder()
                .user(user)
                .template(template)
                .title(req.title() != null && !req.title().isBlank() ? req.title() : (template != null ? template.getTitle() : "Untitled Design"))
                .content(req.content())
                .diagramData(req.diagramData())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        design = userDesignRepository.save(design);
        return toUserDesignDto(design);
    }

    @Transactional
    public UserDesignDto updateUserDesign(Long id, Long userId, SaveDesignRequest req) {
        UserDesign design = userDesignRepository.findByIdAndUserId(id, userId).orElseThrow();
        if (req.title() != null) design.setTitle(req.title());
        if (req.content() != null) design.setContent(req.content());
        if (req.diagramData() != null) design.setDiagramData(req.diagramData());
        design.setUpdatedAt(LocalDateTime.now());
        design = userDesignRepository.save(design);
        return toUserDesignDto(design);
    }

    @Transactional
    public void deleteUserDesign(Long id, Long userId) {
        userDesignRepository.deleteByIdAndUserId(id, userId);
    }

    private DesignTemplateDto toDto(DesignTemplate t) {
        return new DesignTemplateDto(
                t.getId(),
                t.getTitle(),
                t.getCategory(),
                t.getOverview(),
                t.getRequirements(),
                t.getComponents(),
                t.getDiagramData(),
                t.getSampleSolution(),
                t.getDifficulty(),
                t.getTags(),
                t.getCreatedAt()
        );
    }

    private UserDesignDto toUserDesignDto(UserDesign d) {
        return new UserDesignDto(
                d.getId(),
                d.getUser().getId(),
                d.getTemplate() != null ? d.getTemplate().getId() : null,
                d.getTemplate() != null ? d.getTemplate().getTitle() : null,
                d.getTitle(),
                d.getContent(),
                d.getDiagramData(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
