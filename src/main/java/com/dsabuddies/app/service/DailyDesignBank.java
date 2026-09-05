package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto.DesignTopic;

import java.util.List;

public class DailyDesignBank {

    public static List<DesignTopic> getLldTopics() {
        return List.of(
            getParkingLotLld(),
            getRateLimiterLld(),
            getElevatorLld(),
            getMovieBookingLld()
        );
    }

    public static List<DesignTopic> getHldTopics() {
        return List.of(
            getTinyUrlHld(),
            getChatSystemHld(),
            getVideoStreamingHld(),
            getApiGatewayRateLimiterHld()
        );
    }

    // ==========================================
    // LLD 1: PARKING LOT
    // ==========================================
    private static DesignTopic getParkingLotLld() {
        return DesignTopic.builder()
            .id("LLD-01")
            .title("Design a Multi-Level Parking Lot System")
            .type("LLD")
            .difficulty("MEDIUM")
            .description("Design an object-oriented multi-level parking lot supporting different vehicle types (Motorcycle, Car, Truck), dynamic slot allocation, and automated fee calculation.")
            .coreRequirements(List.of(
                "Multiple parking levels with spots of various sizes (Compact, Large, Motorcycle).",
                "Entry gate issues ticket with timestamp and allocated spot ID.",
                "Exit gate calculates parking fee based on duration and vehicle rate strategy.",
                "Thread-safe concurrent parking spot allocation without race conditions."
            ))
            .designPatternsOrComponents(List.of(
                "Strategy Pattern for dynamic pricing calculation (hourly, peak surcharge, flat rate).",
                "Factory Pattern for instantiating ParkingSpot and Vehicle types.",
                "Singleton Pattern for central ParkingLot orchestrator.",
                "ReentrantLock / ConcurrentHashMap for thread-safe spot reservations."
            ))
            .architectureSummary("Classes: ParkingLot (Singleton), ParkingFloor, ParkingSpot, Vehicle (Car, Bike, Truck), Ticket, PricingStrategy.")
            .detailedSolution("""
### 🎯 1. Requirements Scoping & SOLID Principles
- **Functional**:
  1. Allocate closest available spot matching vehicle size (Bike -> MotorcycleSpot, Car -> CompactSpot, Truck -> LargeSpot).
  2. Issue a unique `Ticket` at entry with timestamp.
  3. Calculate fee on exit using pluggable pricing strategies.
- **Non-Functional**:
  1. **Thread-Safety**: Multiple entry/exit gates operating concurrently must NEVER assign the same spot to two vehicles.
  2. **Extensibility**: Adding EV charging spots or weekly passes should follow the Open/Closed Principle (OCP).

---

### 💻 2. Complete Production Java Implementation
```java
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// 1. Domain Enums
enum VehicleType { MOTORCYCLE, CAR, TRUCK }
enum SpotType { MOTORCYCLE, COMPACT, LARGE }

// 2. Vehicle Hierarchy
abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
}

class Car extends Vehicle { public Car(String plate) { super(plate, VehicleType.CAR); } }
class Motorcycle extends Vehicle { public Motorcycle(String plate) { super(plate, VehicleType.MOTORCYCLE); } }
class Truck extends Vehicle { public Truck(String plate) { super(plate, VehicleType.TRUCK); } }

// 3. Parking Spot with Thread-Safe State
class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;
    private volatile boolean occupied;
    private Vehicle currentVehicle;
    private final ReentrantLock lock = new ReentrantLock();

    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.occupied = false;
    }

    public boolean canFitVehicle(Vehicle vehicle) {
        return switch (vehicle.getType()) {
            case MOTORCYCLE -> true;
            case CAR -> spotType == SpotType.COMPACT || spotType == SpotType.LARGE;
            case TRUCK -> spotType == SpotType.LARGE;
        };
    }

    public boolean tryOccupy(Vehicle vehicle) {
        if (!occupied && canFitVehicle(vehicle)) {
            lock.lock();
            try {
                if (!occupied) {
                    this.occupied = true;
                    this.currentVehicle = vehicle;
                    return true;
                }
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    public void vacate() {
        lock.lock();
        try {
            this.occupied = false;
            this.currentVehicle = null;
        } finally {
            lock.unlock();
        }
    }

    public String getSpotId() { return spotId; }
    public boolean isOccupied() { return occupied; }
}

// 4. Ticket Domain Model
class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot allocatedSpot;
    private final Instant entryTime;

    public Ticket(Vehicle vehicle, ParkingSpot allocatedSpot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.allocatedSpot = allocatedSpot;
        this.entryTime = Instant.now();
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getAllocatedSpot() { return allocatedSpot; }
    public Instant getEntryTime() { return entryTime; }
}

// 5. Strategy Pattern for Pricing
interface PricingStrategy {
    double calculateFee(Duration duration, VehicleType vehicleType);
}

class HourlyPricingStrategy implements PricingStrategy {
    @Override
    public double calculateFee(Duration duration, VehicleType type) {
        long hours = Math.max(1, duration.toHours());
        double baseRate = switch (type) {
            case MOTORCYCLE -> 10.0;
            case CAR -> 20.0;
            case TRUCK -> 40.0;
        };
        return hours * baseRate;
    }
}

// 6. Central Thread-Safe ParkingLot Orchestrator
public class ParkingLot {
    private static volatile ParkingLot instance;
    private final List<ParkingSpot> allSpots = new ArrayList<>();
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private PricingStrategy pricingStrategy = new HourlyPricingStrategy();

    private ParkingLot() {}

    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) instance = new ParkingLot();
            }
        }
        return instance;
    }

    public void addSpot(ParkingSpot spot) {
        allSpots.add(spot);
    }

    public synchronized Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : allSpots) {
            if (spot.tryOccupy(vehicle)) {
                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(ticket.getTicketId(), ticket);
                return ticket;
            }
        }
        throw new IllegalStateException("Parking Lot Full for vehicle type: " + vehicle.getType());
    }

    public double exitVehicle(String ticketId) {
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Invalid ticket ID: " + ticketId);
        }

        ticket.getAllocatedSpot().vacate();
        Duration parkedDuration = Duration.between(ticket.getEntryTime(), Instant.now());
        return pricingStrategy.calculateFee(parkedDuration, ticket.getVehicle().getType());
    }
}
```

---

### ⚖️ 3. Concurrency & Production Bottlenecks
- **Spot Race Condition Prevention**: Each `ParkingSpot` uses fine-grained `ReentrantLock` with double-checked occupancy verification. Gates do not block each other unless contending for the exact same physical spot.
- **Google Interviewer Follow-Up**: *How do you allocate the nearest spot to the entrance in O(1) time?*
  *Answer*: Maintain a `PriorityQueue<ParkingSpot>` ordered by distance from gate for each `SpotType`. Remove the top spot upon entry; re-insert upon exit.
""")
            .build();
    }

    // ==========================================
    // LLD 2: RATE LIMITER
    // ==========================================
    private static DesignTopic getRateLimiterLld() {
        return DesignTopic.builder()
            .id("LLD-02")
            .title("Design an In-Memory Distributed/Local Rate Limiter")
            .type("LLD")
            .difficulty("MEDIUM")
            .description("Design a thread-safe, high-concurrency rate limiting library to throttle incoming client requests based on API keys or IP addresses.")
            .coreRequirements(List.of(
                "Support Token Bucket algorithm with smooth refill over time.",
                "Sub-millisecond execution latency with thread-safe atomic CAS operations.",
                "Configurable limits per tenant (e.g., Free: 10 req/sec, Premium: 100 req/sec).",
                "Clean reject handling returning false when quota exhausted."
            ))
            .designPatternsOrComponents(List.of(
                "Strategy Pattern for swapping rate limiting algorithms seamlessly.",
                "Factory Pattern for creating rate limiters based on tenant tier.",
                "AtomicLong / CAS for lock-free high concurrency."
            ))
            .architectureSummary("Classes: RateLimiter (interface), TokenBucketRateLimiter, RateLimiterFactory.")
            .detailedSolution("""
### 🎯 1. Requirements & Algorithm Selection
- **Token Bucket Algorithm**:
  Tokens are continuously added to a bucket at a fixed refill rate up to `capacity`. Each incoming request consumes 1 token. If no tokens exist, the request is dropped.
- **Why Token Bucket?**: Allows bursts of traffic up to bucket capacity while strictly capping sustained rate. Uses O(1) memory per client without background cron threads!

---

### 💻 2. Complete Production Java Implementation (Lock-Free Token Bucket)
```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// 1. Rate Limiter Interface
interface RateLimiter {
    boolean allowRequest(String clientId);
}

// 2. High-Performance Token Bucket Implementation
class TokenBucketRateLimiter implements RateLimiter {
    private final long capacity;
    private final long refillRatePerSecond;

    private static class BucketState {
        final double tokens;
        final long lastRefillTimestampNanos;

        BucketState(double tokens, long lastRefillTimestampNanos) {
            this.tokens = tokens;
            this.lastRefillTimestampNanos = lastRefillTimestampNanos;
        }
    }

    // Stores per-client token bucket state
    private final ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicReference<BucketState>> clientBuckets
            = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(long capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    @Override
    public boolean allowRequest(String clientId) {
        long now = System.nanoTime();
        var atomicBucket = clientBuckets.computeIfAbsent(clientId,
            k -> new java.util.concurrent.atomic.AtomicReference<>(new BucketState(capacity, now))
        );

        // Lock-free CAS (Compare-And-Swap) loop
        while (true) {
            BucketState current = atomicBucket.get();
            long elapsedNanos = Math.max(0, now - current.lastRefillTimestampNanos);
            double tokensToAdd = (elapsedNanos / 1_000_000_000.0) * refillRatePerSecond;
            double currentTokens = Math.min(capacity, current.tokens + tokensToAdd);

            if (currentTokens < 1.0) {
                return false; // Quota exhausted -> HTTP 429
            }

            BucketState next = new BucketState(currentTokens - 1.0, now);
            if (atomicBucket.compareAndSet(current, next)) {
                return true; // Token acquired successfully!
            }
            // If CAS failed due to thread contention, retry immediately
        }
    }
}
```

---

### ⚖️ 3. Concurrency & Performance Analysis
- **Lock-Free via AtomicReference CAS**: Eliminates `synchronized` mutex overhead. Thousands of threads can evaluate limits concurrently without context switches.
- **Memory Footprint**: Memory is allocated only for active clients in `ConcurrentHashMap`. Unused clients can be evicted with a TTL cache (e.g. Caffeine).
- **Google Interviewer Follow-Up**: *How do you transition this to a distributed cluster?*
  *Answer*: In a distributed system, replace local `AtomicReference` with Redis running an atomic Lua script executing `ZREMRANGEBYSCORE` and `ZCARD` on a sliding window sorted set.
""")
            .build();
    }

    // ==========================================
    // LLD 3: ELEVATOR CONTROL SYSTEM
    // ==========================================
    private static DesignTopic getElevatorLld() {
        return DesignTopic.builder()
            .id("LLD-03")
            .title("Design an Elevator Control System")
            .type("LLD")
            .difficulty("HARD")
            .description("Design an optimal scheduling and control system for multiple elevators in a high-rise building with peak morning and evening traffic handling.")
            .coreRequirements(List.of(
                "Dispatch requests from floors (Up/Down) to the most optimal elevator car.",
                "Internal destination floor selection with door states.",
                "Optimal scheduling using SCAN / LOOK (Elevator algorithm).",
                "Handling elevator idle, moving, and maintenance states."
            ))
            .designPatternsOrComponents(List.of(
                "State Pattern for elevator car states (MOVING_UP, MOVING_DOWN, IDLE).",
                "Strategy Pattern for car dispatch algorithms (SCAN / LOOK vs SSTF).",
                "TreeSet / PriorityQueue for sorted pending stop requests."
            ))
            .architectureSummary("Classes: ElevatorController, ElevatorCar, Direction, ElevatorRequest.")
            .detailedSolution("""
### 🎯 1. Requirements & Scheduling Theory
- **SCAN / LOOK (Elevator Algorithm)**:
  An elevator moves continuously in its current direction (e.g., UP), servicing all pending floor requests along its path until no higher requests exist. It then switches direction to DOWN and services downward requests. This eliminates passenger starvation!

---

### 💻 2. Complete Production Java Implementation
```java
import java.util.*;

enum Direction { UP, DOWN, IDLE }
enum ElevatorState { MOVING, STOPPED, MAINTENANCE }

class ElevatorCar {
    private final int id;
    private int currentFloor = 0;
    private Direction direction = Direction.IDLE;
    private ElevatorState state = ElevatorState.STOPPED;

    // Ordered set of destination stops in each direction
    private final TreeSet<Integer> upStops = new TreeSet<>();
    private final TreeSet<Integer> downStops = new TreeSet<>(Collections.reverseOrder());

    public ElevatorCar(int id) { this.id = id; }

    public synchronized void addDestination(int floor) {
        if (floor > currentFloor) {
            upStops.add(floor);
            if (direction == Direction.IDLE) direction = Direction.UP;
        } else if (floor < currentFloor) {
            downStops.add(floor);
            if (direction == Direction.IDLE) direction = Direction.DOWN;
        }
    }

    public synchronized void step() {
        if (direction == Direction.UP) {
            Integer nextFloor = upStops.higher(currentFloor);
            if (nextFloor == null && upStops.contains(currentFloor)) nextFloor = currentFloor;

            if (nextFloor != null) {
                currentFloor = nextFloor;
                upStops.remove(currentFloor);
            } else {
                // No more up stops: reverse direction if down stops exist
                direction = downStops.isEmpty() ? Direction.IDLE : Direction.DOWN;
            }
        } else if (direction == Direction.DOWN) {
            Integer nextFloor = downStops.lower(currentFloor);
            if (nextFloor == null && downStops.contains(currentFloor)) nextFloor = currentFloor;

            if (nextFloor != null) {
                currentFloor = nextFloor;
                downStops.remove(currentFloor);
            } else {
                direction = upStops.isEmpty() ? Direction.IDLE : Direction.UP;
            }
        }
    }

    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public int getId() { return id; }
}

public class ElevatorController {
    private final List<ElevatorCar> cars;

    public ElevatorController(int numCars) {
        this.cars = new ArrayList<>();
        for (int i = 0; i < numCars; i++) cars.add(new ElevatorCar(i + 1));
    }

    // Nearest Car Dispatcher
    public ElevatorCar requestElevator(int floor, Direction dir) {
        ElevatorCar bestCar = null;
        int minDistance = Integer.MAX_VALUE;

        for (ElevatorCar car : cars) {
            int distance = Math.abs(car.getCurrentFloor() - floor);
            boolean onTheWay = (car.getDirection() == dir &&
                ((dir == Direction.UP && car.getCurrentFloor() <= floor) ||
                 (dir == Direction.DOWN && car.getCurrentFloor() >= floor)));

            if (car.getDirection() == Direction.IDLE) distance += 0;
            else if (onTheWay) distance += 2;
            else distance += 20; // Heavy penalty for cars moving in opposite direction

            if (distance < minDistance) {
                minDistance = distance;
                bestCar = car;
            }
        }

        if (bestCar != null) {
            bestCar.addDestination(floor);
        }
        return bestCar;
    }
}
```

---

### ⚖️ 3. Starvation Prevention & Edge Cases
- **Why `TreeSet` with directional comparator?**: `upStops` is sorted in ascending order (`1, 3, 5`), while `downStops` is sorted in descending order (`5, 3, 1`). This allows `O(log N)` stop insertion and ensures no passenger is passed by without being serviced.
""")
            .build();
    }

    // ==========================================
    // LLD 4: MOVIE TICKET BOOKING SYSTEM
    // ==========================================
    private static DesignTopic getMovieBookingLld() {
        return DesignTopic.builder()
            .id("LLD-04")
            .title("Design a Movie Ticket Booking System (BookMyShow / Fandango)")
            .type("LLD")
            .difficulty("HARD")
            .description("Design a concurrent movie ticket reservation engine handling seat selection, temporary locks, timeout expirations, and seat payment confirmation.")
            .coreRequirements(List.of(
                "Display cinema halls, shows, and real-time seat layouts.",
                "Temporarily lock selected seats for 10 minutes during checkout.",
                "Prevent double-booking under extreme concurrent requests.",
                "Automatically release locked seats if payment times out."
            ))
            .designPatternsOrComponents(List.of(
                "State Pattern for Seat status (AVAILABLE, TEMPORARILY_LOCKED, BOOKED).",
                "Distributed / In-Memory Lock Provider with TTL expiration.",
                "Unit of Work / Transaction Script for atomic seat reservations."
            ))
            .architectureSummary("Classes: Show, Seat, SeatStatus, SeatLockProvider, BookingManager.")
            .detailedSolution("""
### 🎯 1. Requirements & Concurrency Model
- **The Double-Booking Dilemma**: When thousands of users try to book the exact same front-row seat simultaneously, only ONE user must obtain the temporary lock.
- **Seat Lifecycle**:
  `AVAILABLE` -> (User selects seats) -> `TEMPORARILY_LOCKED` (10 min TTL) -> (Payment succeeds) -> `BOOKED`.
  If payment fails or timer expires: `TEMPORARILY_LOCKED` -> `AVAILABLE`.

---

### 💻 2. Complete Production Java Implementation
```java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

enum SeatStatus { AVAILABLE, TEMPORARILY_LOCKED, BOOKED }

class Seat {
    private final String seatId;
    private final int row;
    private final int col;
    private volatile SeatStatus status = SeatStatus.AVAILABLE;

    public Seat(String seatId, int row, int col) {
        this.seatId = seatId;
        this.row = row;
        this.col = col;
    }
    public String getSeatId() { return seatId; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
}

class SeatLock {
    private final String seatId;
    private final String lockedByUserId;
    private final Instant lockExpiryTime;

    public SeatLock(String seatId, String userId, long timeoutSeconds) {
        this.seatId = seatId;
        this.lockedByUserId = userId;
        this.lockExpiryTime = Instant.now().plusSeconds(timeoutSeconds);
    }
    public boolean isExpired() { return Instant.now().isAfter(lockExpiryTime); }
    public String getLockedByUserId() { return lockedByUserId; }
}

class SeatLockProvider {
    private final Map<String, SeatLock> activeLocks = new ConcurrentHashMap<>();
    private final ReentrantLock globalLock = new ReentrantLock();

    public boolean lockSeats(List<Seat> seats, String userId, long timeoutSeconds) {
        globalLock.lock();
        try {
            // 1. Verify all requested seats are available or expired
            for (Seat seat : seats) {
                if (seat.getStatus() == SeatStatus.BOOKED) return false;
                SeatLock existingLock = activeLocks.get(seat.getSeatId());
                if (existingLock != null && !existingLock.isExpired()) return false;
            }

            // 2. Atomically lock all seats
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.TEMPORARILY_LOCKED);
                activeLocks.put(seat.getSeatId(), new SeatLock(seat.getSeatId(), userId, timeoutSeconds));
            }
            return true;
        } finally {
            globalLock.unlock();
        }
    }

    public void releaseExpiredLocks(List<Seat> allSeats) {
        globalLock.lock();
        try {
            for (Seat seat : allSeats) {
                SeatLock lock = activeLocks.get(seat.getSeatId());
                if (lock != null && lock.isExpired() && seat.getStatus() == SeatStatus.TEMPORARILY_LOCKED) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    activeLocks.remove(seat.getSeatId());
                }
            }
        } finally {
            globalLock.unlock();
        }
    }
}
```

---

### ⚖️ 3. High-Scale Concurrency & Production Architecture
- **Pessimistic vs Optimistic Locking in DB**:
  In relational databases (MySQL/Postgres), use `SELECT * FROM seats WHERE id IN (...) FOR UPDATE` inside a transaction.
- **Distributed Locks with Redis**: Use Redis key `lock:show:{showId}:seat:{seatId}` with `SET key userId NX EX 600` (10-minute atomic lock).
""")
            .build();
    }

    // ==========================================
    // HLD 1: TINYURL SHORTENER
    // ==========================================
    private static DesignTopic getTinyUrlHld() {
        return DesignTopic.builder()
            .id("HLD-01")
            .title("Design a Globally Scalable URL Shortener (TinyURL)")
            .type("HLD")
            .difficulty("MEDIUM")
            .description("Design a system that converts long URLs into compact 7-character aliases with high read-to-write ratio (100:1), low latency, and 99.999% availability.")
            .coreRequirements(List.of(
                "Shorten long URLs to 7 alphanumeric characters (Base62: [a-zA-Z0-9]).",
                "Redirection via HTTP 301/302 in sub-15ms latency.",
                "Handle 100M new URLs/month and 10B clicks/month (approx 4,000 read QPS).",
                "High availability (99.999%) and fault tolerance across global regions."
            ))
            .designPatternsOrComponents(List.of(
                "Pre-generated Token Service (Ticket Server / ZooKeeper range allocator) to avoid collisions.",
                "Redis Cluster caching top 20% hottest URLs (80/20 Pareto rule).",
                "NoSQL (Cassandra / DynamoDB) partitioned by shortKey.",
                "Geo-distributed CDN & Anycast DNS for edge redirection."
            ))
            .architectureSummary("Client -> Anycast DNS -> Cloudflare CDN -> NGINX LB -> URL Shortener Service -> Redis Cache -> Cassandra DB Cluster. Key Generation Service pre-fills Redis buffer with unused 7-character Base62 keys.")
            .detailedSolution("""
### 🎯 1. Scale & Back-of-the-Envelope Estimation
- **Write QPS**: 100M URLs / month = `100,000,000 / (30 * 86,400) ≈ 40 writes/sec`. Peak = 200 writes/sec.
- **Read QPS**: 10B clicks / month = `10,000,000,000 / (30 * 86,400) ≈ 3,860 reads/sec`. Peak = 10,000 reads/sec.
- **Storage over 5 Years**: `100M * 12 * 5 = 6 Billion URLs`. At ~500 bytes per record = `3 TB total storage`. Very manageable!
- **Cache Memory (80/20 Rule)**: 20% of daily reads generate 80% traffic. Daily reads = 330M * 500 bytes = 165 GB * 20% ≈ **33 GB Redis Cache RAM**.

---

### 🏛️ 2. Key Generation & Base62 Encoding
- **Why Base62?**: Using `[a-zA-Z0-9]` gives `62^7 ≈ 3.52 Trillion` unique combinations for a 7-character key!
- **How to avoid collision?**:
  Never hash the URL (MD5/SHA256 will collide on 7 chars and require retry queries).
  Instead, use a **Key Generation Service (KGS)** with range allocation. ZooKeeper assigns distinct numeric ID ranges (e.g. Server 1 gets 1M-2M, Server 2 gets 2M-3M). Each worker server converts its atomic counter to Base62 without contacting a central database during write requests!

---

### 💻 3. Database Schema (NoSQL Cassandra / DynamoDB)
```sql
CREATE KEYSPACE tinyurl WITH replication = {'class': 'NetworkTopologyStrategy', 'us-east': 3, 'us-west': 3};

CREATE TABLE tinyurl.urls (
    short_key varchar,
    long_url text,
    user_id uuid,
    created_at timestamp,
    expires_at timestamp,
    click_count counter,
    PRIMARY KEY (short_key)
);
```

---

### 📡 4. HTTP Redirection API Contract
- `POST /api/v1/shorten` -> Request: `{"longUrl": "https://google.com/search?q=..."}` -> Response: `{"shortUrl": "https://tiny.url/x7B2k9q"}`
- `GET /{shortCode}` -> Response: **HTTP 301 Permanent Redirect** (cached by client browser to reduce server load) OR **HTTP 302 Found** (if tracking click analytics on every visit).
""")
            .build();
    }

    // ==========================================
    // HLD 2: CHAT SYSTEM (WHATSAPP)
    // ==========================================
    private static DesignTopic getChatSystemHld() {
        return DesignTopic.builder()
            .id("HLD-02")
            .title("Design a Real-Time Scalable Chat System (WhatsApp / Slack)")
            .type("HLD")
            .difficulty("HARD")
            .description("Design an end-to-end messaging infrastructure capable of handling 1 billion daily active users, 1-on-1 chats, group messaging, and real-time online presence.")
            .coreRequirements(List.of(
                "Bi-directional real-time communication with sub-100ms message delivery.",
                "Message delivery status: Sent, Delivered, Read (ticks).",
                "Offline message storage and push notifications via FCM/APNs.",
                "Real-time online presence tracking with heartbeats."
            ))
            .designPatternsOrComponents(List.of(
                "WebSocket Gateways for persistent TCP full-duplex connections.",
                "Apache Kafka partitioned by conversation_id for ordered message streaming.",
                "ScyllaDB / Cassandra for append-only high write throughput.",
                "Redis Presence Service with heartbeat leases (30s TTL)."
            ))
            .architectureSummary("Client -> WebSocket Gateway (Stateful) -> Message Service -> Kafka Topic -> Consumer Workers -> ScyllaDB. Presence Server tracks heartbeats in Redis Hash. Media files uploaded to S3 via pre-signed URLs.")
            .detailedSolution("""
### 🎯 1. Scale & Back-of-the-Envelope Estimation
- **Users**: 1 Billion DAU.
- **Messages**: 100 messages/user/day = `100 Billion messages/day ≈ 1,150,000 messages/sec (1.15M write QPS)!`
- **Peak QPS**: ~3,000,000 messages/sec.
- **Storage**: At 100 bytes/message = `10 TB/day ≈ 3.6 PB/year`. Requires an append-only distributed wide-column store (ScyllaDB/Cassandra).

---

### 🏛️ 2. Architectural Blueprint & WebSocket Gateway
1. **Connection Tier**: Clients establish persistent full-duplex **WebSocket connections** terminated at WebSocket Gateways. Gateways maintain an in-memory session map `Map<UserId, WebSocketSession>`.
2. **Presence Service**: Clients ping heartbeat every 20s. Presence Service sets Redis key `presence:{userId}` with a 30s TTL. If heartbeat misses, user is marked offline.
3. **Message Ordering with Kafka**: To guarantee strictly sequential message delivery, Kafka topics are partitioned by `conversation_id`. All messages within the same conversation land on the exact same Kafka partition and consumer worker!

---

### 💻 3. ScyllaDB Message Schema
```sql
CREATE TABLE chat.messages (
    conversation_id uuid,
    message_id timeuuid, -- Guarantees time-ordered clustering
    sender_id uuid,
    content text,
    media_url text,
    status text, -- SENT, DELIVERED, READ
    PRIMARY KEY (conversation_id, message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);
```
""")
            .build();
    }

    // ==========================================
    // HLD 3: VIDEO STREAMING (NETFLIX)
    // ==========================================
    private static DesignTopic getVideoStreamingHld() {
        return DesignTopic.builder()
            .id("HLD-03")
            .title("Design a Video Streaming Platform (Netflix / YouTube)")
            .type("HLD")
            .difficulty("HARD")
            .description("Design a massive video ingestion, transcoding, and content delivery system streaming billions of video chunks globally without buffering.")
            .coreRequirements(List.of(
                "Upload and ingest raw video files up to 100GB.",
                "Asynchronous transcoding into multiple resolutions (4K, 1080p, 720p, 480p) and codecs (H.264, VP9, AV1).",
                "Global low-latency streaming using Content Delivery Networks (CDNs) with Adaptive Bitrate Streaming (HLS / DASH).",
                "Track user watch-progress and resume playback across devices."
            ))
            .designPatternsOrComponents(List.of(
                "Object Storage (AWS S3 / GCS) partitioned by videoId.",
                "Distributed DAG Job Pipeline (Apache Airflow / Temporal) orchestrating FFmpeg worker pods.",
                "Open Connect CDN Edge Caching for chunked `.ts` / `.m4s` video segments.",
                "Cassandra for user watch-state tracking."
            ))
            .architectureSummary("Creator Upload -> S3 Raw Bucket -> S3 Event Trigger -> Transcoding Pipeline -> Multi-resolution chunks in S3 -> CDN Open Connect PoP -> Client Player adapts bitrate based on bandwidth.")
            .detailedSolution("""
### 🎯 1. Scale & Back-of-the-Envelope Estimation
- **Concurrent Streamers**: 200M daily active users streaming an average of 1 hour/day.
- **Peak Egress Bandwidth**: At an average 1080p bitrate of 5 Mbps: `200M * 1 hr = 7.2 * 10^11 seconds * 5 Mbps ≈ 41.6 Terabits/sec (Tbps)` at peak!
- **Ingestion & Storage**: 100,000 new video hours uploaded daily. At 10GB/hour (master ProRes/DNxHD) + encoded profiles (4K, 1080p, 720p, 480p) = `~2 Petabytes (PB) new storage per day`.
- **CDN Offload**: 95% of requests must be served directly from CDN Edge PoPs (Netflix Open Connect) to keep origin bandwidth feasible.

---

### 🏛️ 2. Core Architecture & Ingestion Pipeline
1. **Direct-to-S3 Chunked Upload**: Creators obtain a Pre-Signed S3 URL and upload video chunks directly to S3 via multipart upload, bypassing application servers.
2. **Transcoding DAG (Airflow/Temporal)**:
   - S3 event triggers a job coordinator.
   - Video is split into 6-second GOP (Group of Pictures) chunks.
   - Hundreds of distributed GPU/CPU worker pods transcode chunks concurrently into 4K, 1080p, 720p, 480p across H.264 (high compatibility) and AV1 (high compression).
   - Generates `.m3u8` master playlist files for HLS (HTTP Live Streaming).
3. **Edge Caching via CDN**: 95% of video requests are served directly from ISP edge CDN nodes (Netflix Open Connect appliances).

---

### 💻 3. Adaptive Bitrate Streaming (ABR) & Cassandra Schema
```sql
-- Cassandra user watch history & resume state:
CREATE TABLE streaming.user_watch_progress (
    user_id uuid,
    video_id uuid,
    last_playback_position_seconds int,
    device_id text,
    completed boolean,
    updated_at timestamp,
    PRIMARY KEY ((user_id), video_id)
);
```
The client video player measures instantaneous network bandwidth and buffer fullness every few seconds. If bandwidth drops, it switches to the 480p `.ts` chunk stream; if bandwidth increases, it seamlessly shifts to 1080p without stopping video playback!
""")
            .build();
    }

    // ==========================================
    // HLD 4: DISTRIBUTED RATE LIMITER & API GATEWAY
    // ==========================================
    private static DesignTopic getApiGatewayRateLimiterHld() {
        return DesignTopic.builder()
            .id("HLD-04")
            .title("Design a Distributed Rate Limiter & API Gateway")
            .type("HLD")
            .difficulty("HARD")
            .description("Design a central rate limiting layer protecting microservices from DDoS attacks, cascading failures, and noisy neighbor problems.")
            .coreRequirements(List.of(
                "Accurate sliding window rate limiting across thousands of microservice instances.",
                "Sub-2ms evaluation latency with negligible CPU overhead.",
                "Multi-tier quotas: per-IP, per-user, per-endpoint.",
                "Graceful degradation when rate limiter storage is degraded (Fail-Open)."
            ))
            .designPatternsOrComponents(List.of(
                "Redis with Lua scripting for atomic sliding-window counter execution.",
                "Envoy / Kong API Gateway filter plugins.",
                "Resilience4j Circuit Breaker for graceful degradation."
            ))
            .architectureSummary("Client -> Edge Gateway (Envoy) -> Redis Lua script for atomic sliding window count increment. If quota exceeded, returns HTTP 429 immediately; else forwards to downstream microservices.")
            .detailedSolution("""
### 🎯 1. Scale & Back-of-the-Envelope Estimation
- **Gateway Traffic**: 500,000 peak requests/sec across 200 microservices.
- **Latency Budget**: Rate limiting evaluation must complete in **< 2ms** to avoid adding latency to upstream client requests.
- **Memory Footprint**: Tracking 100M active daily users with a 1-minute sliding window counter at 64 bytes/key in Redis Cluster requires `~6.4 GB RAM` (well within standard single-node Redis cluster sizes).

---

### 🏛️ 2. Architecture & Redis Sliding Window Lua Script
Using Redis standalone commands (`ZADD`, `ZCARD`) causes race conditions between multiple gateway instances. We execute the sliding window counter atomically inside Redis using a **Lua Script**:

```lua
-- KEYS[1]: rate limit key (e.g. ratelimit:user:123:endpoint:/checkout)
-- ARGV[1]: current timestamp in milliseconds
-- ARGV[2]: window size in milliseconds (e.g. 60000 for 1 min)
-- ARGV[3]: limit (e.g. 100 requests)

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local clearBefore = now - window

-- 1. Remove expired request timestamps older than window
redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)

-- 2. Count requests in current sliding window
local currentRequests = redis.call('ZCARD', key)

if currentRequests < limit then
    -- 3. Add current request and reset TTL
    redis.call('ZADD', key, now, now)
    redis.call('PEXPIRE', key, window)
    return 1 -- Allowed
else
    return 0 -- Rejected (HTTP 429)
end
```

---

### ⚖️ 3. High-Availability & Disaster Recovery (Fail-Open Policy)
- **Circuit Breaker**: If Redis latency spikes beyond 10ms or cluster is unreachable, the Gateway circuit breaker opens and falls back to **Fail-Open mode** (allows traffic through and logs a high-severity alert) rather than taking down the entire business.
""")
            .build();
    }
}
