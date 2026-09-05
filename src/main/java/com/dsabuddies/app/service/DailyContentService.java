package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto;
import com.dsabuddies.app.dto.DailyContentDto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DailyContentService {

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final Map<String, DailyContentDto> cache = new ConcurrentHashMap<>();

    public DailyContentDto getDailyContent(LocalDate date) {
        String key = date.toString();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        DailyContentDto content = generateDeterministicDailyContent(date);
        cache.put(key, content);
        return content;
    }

    private DailyContentDto generateDeterministicDailyContent(LocalDate date) {
        long seed = date.toEpochDay();
        Random rng = new Random(seed);
        int dayOfYear = date.getDayOfYear();

        List<LeetCodeProblem> leetCodeBank = getLeetCodeBank();
        List<DesignTopic> lldBank = getLldBank();
        List<DesignTopic> hldBank = getHldBank();
        List<InterviewQuestion> javaBank = getJavaQuestionsBank();
        List<InterviewQuestion> springBank = getSpringBootQuestionsBank();
        List<InterviewQuestion> dbBank = getDatabaseQuestionsBank();
        List<InterviewQuestion> csBank = getCsQuestionsBank();

        LeetCodeProblem leetCode = leetCodeBank.get((int) Math.abs(seed % leetCodeBank.size()));
        DesignTopic lld = lldBank.get((int) Math.abs(seed % lldBank.size()));
        DesignTopic hld = hldBank.get((int) Math.abs(seed % hldBank.size()));

        List<InterviewQuestion> dailyJava = pickDistinctQuestions(javaBank, 10, seed + 101);
        List<InterviewQuestion> dailySpring = pickDistinctQuestions(springBank, 10, seed + 202);
        List<InterviewQuestion> dailyDb = pickDistinctQuestions(dbBank, 10, seed + 303);
        List<InterviewQuestion> dailyCs = pickDistinctQuestions(csBank, 10, seed + 404);

        String[] quotes = {
            "First, solve the problem. Then, write the code. — John Johnson",
            "Simplicity is prerequisite for reliability. — Edsger W. Dijkstra",
            "Make it work, make it right, make it fast. — Kent Beck",
            "Programs must be written for people to read, and only incidentally for machines to execute. — Abelson & Sussman",
            "Any fool can write code that a computer can understand. Good programmers write code that humans can understand. — Martin Fowler",
            "The only way to go fast, is to go well. — Robert C. Martin"
        };
        String quote = quotes[(int) Math.abs(seed % quotes.length)];

        return DailyContentDto.builder()
                .date(date.toString())
                .dayOfYear(dayOfYear)
                .quoteOfTheDay(quote)
                .leetCodeProblem(leetCode)
                .lldTopic(lld)
                .hldTopic(hld)
                .javaQuestions(dailyJava)
                .springBootQuestions(dailySpring)
                .databaseQuestions(dailyDb)
                .csSubjectsQuestions(dailyCs)
                .build();
    }

    private List<InterviewQuestion> pickDistinctQuestions(List<InterviewQuestion> pool, int count, long seed) {
        List<InterviewQuestion> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, new Random(seed));
        List<InterviewQuestion> selected = new ArrayList<>();
        int take = Math.min(count, copy.size());
        for (int i = 0; i < take; i++) {
            InterviewQuestion q = copy.get(i);
            selected.add(InterviewQuestion.builder()
                    .id(i + 1)
                    .category(q.getCategory())
                    .topic(q.getTopic())
                    .question(q.getQuestion())
                    .answer(q.getAnswer())
                    .keyPoints(q.getKeyPoints())
                    .codeSnippet(q.getCodeSnippet())
                    .build());
        }
        return selected;
    }

    // ==========================================
    // 1. LEETCODE PROBLEMS BANK
    // ==========================================
    private List<LeetCodeProblem> getLeetCodeBank() {
        return List.of(
            LeetCodeProblem.builder()
                .id("LC-146")
                .title("LRU Cache")
                .difficulty("MEDIUM")
                .topic("Hash Table & Doubly Linked List")
                .url("https://leetcode.com/problems/lru-cache/")
                .problemSummary("Design a data structure that follows the constraints of a Least Recently Used (LRU) cache with O(1) get and put operations.")
                .optimalApproach("Combine a HashMap<Integer, Node> for O(1) key lookups with a Doubly Linked List with dummy head and tail. On get/update, splice the node and move it directly to head. On eviction, remove node right before dummy tail.")
                .timeComplexity("O(1) for both get() and put()")
                .spaceComplexity("O(capacity) to store maximum elements in list and map")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-42")
                .title("Trapping Rain Water")
                .difficulty("HARD")
                .topic("Two Pointers / Monotonic Stack")
                .url("https://leetcode.com/problems/trapping-rain-water/")
                .problemSummary("Given n non-negative integers representing an elevation map where width of each bar is 1, compute how much water it can trap after raining.")
                .optimalApproach("Use Two Pointers (left = 0, right = n-1) tracking leftMax and rightMax. Water trapped at any index is bounded by min(leftMax, rightMax) - height[i]. Move the pointer with the smaller max bar inwards.")
                .timeComplexity("O(N) single pass")
                .spaceComplexity("O(1) auxiliary memory")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-207")
                .title("Course Schedule")
                .difficulty("MEDIUM")
                .topic("Graph / Topological Sort / Kahn's Algorithm")
                .url("https://leetcode.com/problems/course-schedule/")
                .problemSummary("There are numCourses you have to take, labeled from 0 to numCourses - 1. Given prerequisites array, determine if it is possible to finish all courses.")
                .optimalApproach("Model as a Directed Graph where courses are nodes. Detect cycles using Kahn's Algorithm (BFS with in-degree array) or DFS with 3 states (UNVISITED, VISITING, VISITED). If cycle exists, return false.")
                .timeComplexity("O(V + E) where V = courses, E = prerequisites")
                .spaceComplexity("O(V + E) for adjacency list and queue/in-degree array")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-76")
                .title("Minimum Window Substring")
                .difficulty("HARD")
                .topic("Sliding Window / Hash Map")
                .url("https://leetcode.com/problems/minimum-window-substring/")
                .problemSummary("Given two strings s and t, return the minimum window substring of s such that every character in t (including duplicates) is included in the window.")
                .optimalApproach("Maintain a frequency map of string t and a sliding window [left, right] in s. Expand right until all characters in t are satisfied (matchedCount == required). Then contract left to find the minimum valid window, updating smallest length recorded.")
                .timeComplexity("O(M + N) where M = s.length(), N = t.length()")
                .spaceComplexity("O(K) where K is unique character set (at most 128 for ASCII)")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-300")
                .title("Longest Increasing Subsequence")
                .difficulty("MEDIUM")
                .topic("Binary Search / Patience Sorting")
                .url("https://leetcode.com/problems/longest-increasing-subsequence/")
                .problemSummary("Given an integer array nums, return the length of the longest strictly increasing subsequence.")
                .optimalApproach("Use Patience Sorting: maintain a tails array where tails[i] stores the smallest tail of all increasing subsequences of length i+1. For each num, binary search for its insertion slot in tails. If it's larger than all, append it; else replace.")
                .timeComplexity("O(N log N)")
                .spaceComplexity("O(N) to store tails array")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-295")
                .title("Find Median from Data Stream")
                .difficulty("HARD")
                .topic("Heap / Priority Queue")
                .url("https://leetcode.com/problems/find-median-from-data-stream/")
                .problemSummary("Design a data structure that supports adding integers from a stream and finding the median of all elements seen so far in O(1) time.")
                .optimalApproach("Maintain two Heaps: a Max-Heap for the lower half of numbers, and a Min-Heap for the upper half. Keep heaps balanced such that maxHeap.size() == minHeap.size() or maxHeap.size() == minHeap.size() + 1. Median is either maxHeap.peek() or the average of both roots.")
                .timeComplexity("O(log N) for addNum(), O(1) for findMedian()")
                .spaceComplexity("O(N) total storage for all elements")
                .build(),
            LeetCodeProblem.builder()
                .id("LC-200")
                .title("Number of Islands")
                .difficulty("MEDIUM")
                .topic("Depth-First Search / Breadth-First Search / Disjoint Set Union")
                .url("https://leetcode.com/problems/number-of-islands/")
                .problemSummary("Given an m x n 2D binary grid grid which represents a map of '1's (land) and '0's (water), return the number of islands.")
                .optimalApproach("Iterate through every cell. When a '1' is encountered, increment island counter and initiate a DFS/BFS traversal to sink all connected land cells by marking them as '0' or visited.")
                .timeComplexity("O(M * N)")
                .spaceComplexity("O(M * N) recursion stack in worst case")
                .build()
        );
    }

    // ==========================================
    // 2. LLD TOPICS BANK
    // ==========================================
    private List<DesignTopic> getLldBank() {
        return List.of(
            DesignTopic.builder()
                .id("LLD-01")
                .title("Design a Multi-Level Parking Lot System")
                .type("LLD")
                .difficulty("MEDIUM")
                .description("Design an object-oriented multi-level parking lot supporting different vehicle types (Motorcycle, Car, Truck), dynamic slot allocation, and automated fee calculation.")
                .coreRequirements(List.of(
                    "Multiple parking levels with slots of various sizes (Compact, Large, Handicapped, Electric).",
                    "Entry gate issues ticket with timestamp and allocated slot ID.",
                    "Exit gate calculates parking fee based on duration and vehicle rate strategy.",
                    "Real-time spot availability display per floor."
                ))
                .designPatternsOrComponents(List.of(
                    "Strategy Pattern for hourly vs flat fee pricing calculation.",
                    "Factory Pattern for instantiating ParkingSpot and Vehicle types.",
                    "Observer Pattern for notifying display boards when spots become vacant/occupied.",
                    "Singleton Pattern for ParkingLot management instance."
                ))
                .architectureSummary("Classes: ParkingLot (Singleton), Level, ParkingSpot (CompactSpot, LargeSpot), Vehicle (Car, Bike, Truck), Ticket, PaymentService, RateCalculationStrategy.")
                .build(),
            DesignTopic.builder()
                .id("LLD-02")
                .title("Design an In-Memory Distributed/Local Rate Limiter")
                .type("LLD")
                .difficulty("MEDIUM")
                .description("Design a thread-safe, high-concurrency rate limiting library to throttle incoming client requests based on API keys or IP addresses.")
                .coreRequirements(List.of(
                    "Support multiple algorithms: Token Bucket, Leaky Bucket, and Sliding Window Counter.",
                    "High throughput with thread-safe atomic operations (AtomicInteger, ReentrantLock, or CAS).",
                    "Configurable limits per tier (Free: 10 req/min, Premium: 100 req/min).",
                    "Graceful 429 Too Many Requests response with Retry-After header metadata."
                ))
                .designPatternsOrComponents(List.of(
                    "Strategy Pattern for swapping rate limiting algorithms seamlessly.",
                    "Factory Pattern for creating rate limiters based on tenant policy.",
                    "Decorator / Filter Pattern to hook into HTTP request pipeline."
                ))
                .architectureSummary("Classes: RateLimiter (interface), TokenBucketRateLimiter, SlidingWindowRateLimiter, RateLimitConfig, ClientRuleRepository, RateLimiterFactory.")
                .build(),
            DesignTopic.builder()
                .id("LLD-03")
                .title("Design an Elevator Control System")
                .type("LLD")
                .difficulty("HARD")
                .description("Design an optimal scheduling and control system for multiple elevators in a high-rise building with peak morning and evening traffic handling.")
                .coreRequirements(List.of(
                    "Dispatch requests from floors (Up/Down) to the most optimal elevator car.",
                    "Internal elevator destination selection with door open/close/emergency states.",
                    "Scheduling algorithms: SCAN / LOOK (Elevator algorithm), Nearest Elevator First.",
                    "Handling overload weight limits and maintenance mode."
                ))
                .designPatternsOrComponents(List.of(
                    "State Pattern for elevator states (MOVING_UP, MOVING_DOWN, IDLE, DOOR_OPEN, MAINTENANCE).",
                    "Strategy Pattern for elevator car dispatch algorithms (SCAN, SSTF).",
                    "Observer Pattern to alert floor display indicators of elevator position."
                ))
                .architectureSummary("Classes: ElevatorController, ElevatorCar, Direction (UP, DOWN, IDLE), InternalButton, ExternalButton, Floor, RequestDispatcher.")
                .build(),
            DesignTopic.builder()
                .id("LLD-04")
                .title("Design a Movie Ticket Booking System (BookMyShow / Fandango)")
                .type("LLD")
                .difficulty("HARD")
                .description("Design a concurrent movie ticket reservation engine handling seat selection, temporary locks, timeout expirations, and seat payment confirmation.")
                .coreRequirements(List.of(
                    "View cities, cinemas, halls, shows, and real-time seat layouts.",
                    "Lock seats for 10 minutes when user enters checkout to prevent double-booking.",
                    "Release locked seats automatically if payment is not completed within grace period.",
                    "Concurrency handling using optimistic or pessimistic locks."
                ))
                .designPatternsOrComponents(List.of(
                    "State Pattern for Seat status (AVAILABLE, RESERVED_TEMP, BOOKED).",
                    "Unit of Work / Transaction Script for atomic seat reservations.",
                    "Scheduler / Timer task for releasing expired locks.",
                    "Payment Gateway Adapter pattern."
                ))
                .architectureSummary("Classes: Movie, Cinema, Screen, Show, Seat, Booking, Payment, SeatLockProvider, BookingManager.")
                .build()
        );
    }

    // ==========================================
    // 3. HLD TOPICS BANK
    // ==========================================
    private List<DesignTopic> getHldBank() {
        return List.of(
            DesignTopic.builder()
                .id("HLD-01")
                .title("Design a Globally Scalable URL Shortener (TinyURL)")
                .type("HLD")
                .difficulty("MEDIUM")
                .description("Design a system that converts long URLs into compact 7-character aliases with high read-to-write ratio (100:1), low latency, and 99.999% availability.")
                .coreRequirements(List.of(
                    "Shorten long URLs to 7 alphanumeric characters (Base62: [a-zA-Z0-9]).",
                    "Redirection via HTTP 301 (Permanent) or 302 (Temporary for analytics) in < 15ms.",
                    "Handle 100M new URLs/month and 10B clicks/month (approx 4,000 read QPS).",
                    "Custom aliases and expiration TTL."
                ))
                .designPatternsOrComponents(List.of(
                    "Pre-generated Token Service (Ticket Server / ZooKeeper / Snowflake) to avoid hash collisions.",
                    "Redis Cluster caching top 20% most accessed URLs (80-20 Pareto rule).",
                    "NoSQL (Cassandra/DynamoDB) or Sharded PostgreSQL keyed on shortKey.",
                    "Geo-distributed CDN & Anycast DNS for edge redirection."
                ))
                .architectureSummary("Client -> Anycast DNS -> Cloudflare CDN -> NGINX Load Balancer -> URL Shortener Service -> Redis Cache -> Cassandra DB Cluster. Key Generation Service asynchronously fills Redis queue with unused 7-char keys.")
                .build(),
            DesignTopic.builder()
                .id("HLD-02")
                .title("Design a Real-Time Scalable Chat System (WhatsApp / Slack)")
                .type("HLD")
                .difficulty("HARD")
                .description("Design an end-to-end messaging infrastructure capable of handling 1 billion daily active users, 1-on-1 chats, group messaging, and real-time online presence.")
                .coreRequirements(List.of(
                    "Bi-directional real-time communication with sub-100ms message delivery.",
                    "Message statuses: Sent, Delivered, Read (ticks).",
                    "Offline message storage and push notifications via FCM/APNs.",
                    "Group chat with member fanout and read receipts."
                ))
                .designPatternsOrComponents(List.of(
                    "WebSocket Gateways for persistent TCP full-duplex connections.",
                    "Redis Pub/Sub or Apache Kafka for routing messages across gateway clusters.",
                    "ScyllaDB / Cassandra for high-write-throughput append-only message history.",
                    "Distributed Presence Service with heartbeat leases."
                ))
                .architectureSummary("Client -> WebSocket Gateway (Stateful) -> Message Service -> Kafka Topic -> Consumer Workers -> ScyllaDB. Presence Server tracks heartbeats in Redis Hash. Media files uploaded to S3 via pre-signed URLs.")
                .build(),
            DesignTopic.builder()
                .id("HLD-03")
                .title("Design a Video Streaming Platform (Netflix / YouTube)")
                .type("HLD")
                .difficulty("HARD")
                .description("Design a massive video ingestion, transcoding, and content delivery system streaming billions of video chunks globally without buffering.")
                .coreRequirements(List.of(
                    "Upload raw video files of up to 100GB.",
                    "Asynchronous transcoding into multiple resolutions (1080p, 720p, 480p) and adaptive bitrates (HLS / MPEG-DASH).",
                    "Global low-latency delivery using Content Delivery Networks (CDNs).",
                    "Video recommendation, search, and user resume watch-state tracking."
                ))
                .designPatternsOrComponents(List.of(
                    "Object Storage (AWS S3 / GCS) partitioned by videoId.",
                    "Distributed DAG Job Pipeline (Apache Airflow / Temporal) orchestrating FFmpeg worker pods.",
                    "Edge CDN PoPs caching video segments (.ts/.m4s chunked files).",
                    "Cassandra / CockroachDB for watch-progress state and metadata."
                ))
                .architectureSummary("Creator Upload -> S3 raw bucket -> S3 Event -> Transcoding Pipeline -> Multi-resolution chunks in S3 -> CDN Open Connect PoP -> Client Player adapts bitrate based on bandwidth.")
                .build(),
            DesignTopic.builder()
                .id("HLD-04")
                .title("Design a Distributed Rate Limiter & API Gateway")
                .type("HLD")
                .difficulty("HARD")
                .description("Design a central rate limiting layer protecting microservices from DDoS attacks, cascading failures, and noisy neighbor problems.")
                .coreRequirements(List.of(
                    "Accurate sliding window rate limiting across thousands of microservice instances.",
                    "Sub-2ms evaluation latency with negligible CPU overhead.",
                    "Multi-tier quotas: per-IP, per-user, per-endpoint.",
                    "Graceful degradation when rate limiter storage is degraded."
                ))
                .designPatternsOrComponents(List.of(
                    "Redis with Lua scripting for atomic sliding-log and sliding-window counter execution.",
                    "Envoy / Kong API Gateway filter plugins.",
                    "Token bucket synchronization across regional Redis clusters."
                ))
                .architectureSummary("Client -> Edge Gateway (Envoy) -> Redis Lua script for atomic sliding window count increment. If quota exceeded, returns HTTP 429 immediately; else forwards to downstream microservices.")
                .build()
        );
    }

    // ==========================================
    // 4. JAVA INTERVIEW QUESTIONS BANK (15+ items to rotate)
    // ==========================================
    private List<InterviewQuestion> getJavaQuestionsBank() {
        return List.of(
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Collections & Concurrency")
                .question("How does ConcurrentHashMap work internally in Java 8+, and how does it achieve high concurrency without full table locking?")
                .answer("In Java 8+, ConcurrentHashMap discarded the Segment array lock mechanism used in Java 7. It now uses a synchronized block on the head node of each bucket along with CAS (Compare-And-Swap) operations via Unsafe/VarHandle for node insertions. Reads are completely lock-free because Node.val and Node.next are declared volatile. When bucket count exceeds 8 and total capacity >= 64, buckets treeify into Red-Black Trees (TreeNodes). Resizing uses a TransferIndex with multi-threaded collaborative resizing.")
                .keyPoints(List.of(
                    "CAS for table initialization and empty bucket insertions.",
                    "Synchronized locking restricted solely to the specific bucket head node.",
                    "Volatile val and next pointers guarantee memory visibility without read locks.",
                    "Treeification to Red-Black Tree when collision depth exceeds threshold."
                ))
                .codeSnippet("// ConcurrentHashMap head node locking simplified:\nsynchronized (firstNode) {\n    if (tabAt(tab, i) == firstNode) {\n        // traverse and insert or update\n    }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("JVM Memory & Garbage Collection")
                .question("Explain the difference between ZGC, G1GC, and Shenandoah collectors, and how ZGC achieves sub-millisecond pause times.")
                .answer("G1GC divides the heap into equal-sized regions and performs generational garbage collection with pause times typically in tens of milliseconds. ZGC (Z Garbage Collector) and Shenandoah are ultra-low latency concurrent collectors. ZGC achieves max pause times under 1ms regardless of heap size (even terabytes) by executing marking, relocation, and reference processing concurrently with mutator threads. It uses Colored Pointers (storing GC metadata in top reference bits) and Load Barriers to fix up pointers on-the-fly when application threads dereference objects.")
                .keyPoints(List.of(
                    "Colored Pointers: 44-bit object address + 4 metadata bits (Finalizable, Remapped, Marked0, Marked1).",
                    "Load Barrier: JIT-injected check during object reference reads that relocates/updates references concurrently.",
                    "Generational ZGC (Java 21) separates young and old objects for 4x higher throughput."
                ))
                .codeSnippet("// Enable Generational ZGC in Java 21+:\njava -XX:+UseZGC -XX:+ZGenerational -jar app.jar")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Concurrency / Java 21")
                .question("What are Virtual Threads (Project Loom) in Java 21, and how do they differ from Platform (OS) Threads?")
                .answer("Platform threads are 1:1 wrappers around operating system kernel threads with high memory footprint (~1MB stack) and costly OS context switching, limiting servers to thousands of threads. Virtual threads (JVM-managed green threads) are M:N mapped onto a small pool of carrier ForkJoinPool worker threads. When a virtual thread executes a blocking I/O call (e.g., socket read, DB query), the JVM unmounts its Continuation from the carrier thread and parks it in heap memory, allowing the carrier thread to execute another virtual thread. This enables millions of concurrent threads with standard synchronous imperative code.")
                .keyPoints(List.of(
                    "Stack memory stored in JVM heap and expands/contracts dynamically (~few hundred bytes initially).",
                    "Blocking calls unmount the continuation, liberating carrier threads.",
                    "Eliminates the need for reactive programming (RxJava/WebFlux) for I/O bound workloads.",
                    "Avoid pinning carrier threads: do not hold synchronized locks across blocking calls; use ReentrantLock instead."
                ))
                .codeSnippet("// Spawning virtual threads:\nThread.startVirtualThread(() -> {\n    System.out.println(\"Running on: \" + Thread.currentThread());\n});\n\ntry (var executor = Executors.newVirtualThreadPerTaskExecutor()) {\n    IntStream.range(0, 10_000).forEach(i -> executor.submit(task));\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Memory Model")
                .question("What is the Java Memory Model (JMM) happens-before relationship, and why does volatile prevent instruction reordering?")
                .answer("The JMM specifies how the JVM and CPU caches interact with main memory. A 'happens-before' guarantee ensures that memory writes by one thread are visible to subsequent reads by another thread. Volatile variables enforce this via CPU memory barriers (LoadLoad, LoadStore, StoreStore, StoreLoad). Writing to a volatile variable flushes the CPU store buffer to main memory and prevents compiler/CPU instruction reordering across the write; reading from volatile invalidates the local CPU cache and reads directly from main memory.")
                .keyPoints(List.of(
                    "Volatile write happens-before any subsequent volatile read of the same variable.",
                    "Monitor unlock happens-before subsequent lock on the same monitor.",
                    "Thread start() happens-before any action in the started thread.",
                    "Prevents reordering: eliminates half-initialized object publishing in Double-Checked Locking."
                ))
                .codeSnippet("// Thread-safe Double-Checked Locking Singleton:\nprivate static volatile Singleton instance;\npublic static Singleton getInstance() {\n    if (instance == null) {\n        synchronized (Singleton.class) {\n            if (instance == null) instance = new Singleton();\n        }\n    }\n    return instance;\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("OOP & Contract")
                .question("Why must equals() and hashCode() be overridden together, and what disastrous bugs happen if hashCode() is violated?")
                .answer("The contract between equals() and hashCode() dictates that if two objects are equal according to equals(Object), they MUST produce the exact same integer hashCode(). If you override equals() without overriding hashCode(), two logically identical objects will hash to different buckets in a HashSet or HashMap. As a consequence, map.get(key) or set.contains(obj) will return false or null even when the object is present, leading to duplicate entries and memory leaks.")
                .keyPoints(List.of(
                    "Equal objects => same hashCode.",
                    "Same hashCode !=> equal objects (hash collisions are valid).",
                    "Always use the exact same fields in both equals() and hashCode().",
                    "Record classes automatically generate correct equals() and hashCode() based on all components."
                ))
                .codeSnippet("@Override\npublic boolean equals(Object o) {\n    if (this == o) return true;\n    if (!(o instanceof Person p)) return false;\n    return id == p.id && Objects.equals(name, p.name);\n}\n@Override\npublic int hashCode() {\n    return Objects.hash(id, name);\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("ClassLoading")
                .question("Explain the ClassLoader Delegation Hierarchy and how to break the parent-delegation model.")
                .answer("Java uses a delegation model: Bootstrap ClassLoader (loads rt.jar/java.base) -> Platform/Extension ClassLoader -> Application/System ClassLoader -> Custom ClassLoaders. When a class is requested, a ClassLoader first delegates to its parent. Only if the parent cannot find the class does the child attempt to load it. This prevents malicious classes from overriding core java.lang.String. The delegation model is deliberately broken in web servers (Tomcat loads webapp WEB-INF/classes first) and OSGi plugins by overriding loadClass() instead of findClass().")
                .keyPoints(List.of(
                    "Parent delegation prevents core JDK classes from being shadowed.",
                    "Override findClass() to maintain parent delegation.",
                    "Override loadClass() to break parent delegation (Child-first loading in Tomcat/OSGi).",
                    "ContextClassLoader (Thread.currentThread().getContextClassLoader()) allows core classes to access user classes."
                ))
                .codeSnippet("// Breaking parent delegation (Child-First):\n@Override\nprotected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {\n    try {\n        return findClass(name); // Try child first\n    } catch (ClassNotFoundException e) {\n        return super.loadClass(name, resolve); // Fallback to parent\n    }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Generics")
                .question("Explain Type Erasure in Java and the difference between <? extends T> and <? super T> (PECS principle).")
                .answer("Java generics were implemented using Type Erasure for backwards compatibility with pre-Java 5 bytecode. At compile-time, generic type parameters are verified and then replaced by their bounds (or Object). Type arguments are not available at runtime. The PECS rule stands for Producer Extends, Consumer Super: use <? extends T> when reading elements from a collection (it acts as a producer of T); use <? super T> when adding elements to a collection (it acts as a consumer of T).")
                .keyPoints(List.of(
                    "Producer Extends: List<? extends Number> allows reading Numbers, but cannot add (except null).",
                    "Consumer Super: List<? super Integer> allows adding Integers, but reading returns Object.",
                    "Cannot instantiate generic arrays (new T[]) or check instanceof T at runtime.",
                    "Type tokens (e.g. Class<T> clazz or TypeReference) preserve type info via subclassing."
                ))
                .codeSnippet("// PECS in Collections.copy():\npublic static <T> void copy(List<? super T> dest, List<? extends T> src) {\n    for (int i = 0; i < src.size(); i++) dest.set(i, src.get(i));\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("CompletableFuture")
                .question("What is the difference between thenApply, thenCompose, and thenCombine in CompletableFuture?")
                .answer("thenApply transforms the result of the future synchronously or asynchronously: T -> R (like Stream.map). thenCompose flattens nested futures: T -> CompletableFuture<R> (like Stream.flatMap), chaining two dependent asynchronous stages sequentially. thenCombine joins two independent futures running concurrently and applies a BiFunction to their results once both complete.")
                .keyPoints(List.of(
                    "thenApply: f(x) -> y. Returns CompletableFuture<y>.",
                    "thenCompose: f(x) -> CompletableFuture<y>. Prevents nested CompletableFuture<CompletableFuture<y>>.",
                    "thenCombine: waits for both futureA and futureB and merges their results.",
                    "exceptionally() or handle() for graceful error recovery in async pipelines."
                ))
                .codeSnippet("// Combining two parallel async operations:\nCompletableFuture<User> userFuture = fetchUserAsync(userId);\nCompletableFuture<Orders> ordersFuture = fetchOrdersAsync(userId);\n\nCompletableFuture<Dashboard> dashboard = userFuture.thenCombine(\n    ordersFuture, (user, orders) -> new Dashboard(user, orders)\n);")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Modern Java")
                .question("What are Records, Sealed Classes, and Pattern Matching in modern Java (17/21)?")
                .answer("Records (Java 16) are transparent, immutable data carriers that automatically generate constructor, getters, equals, hashCode, and toString. Sealed Classes (Java 17) restrict which other classes or interfaces may extend or implement them using the 'permits' keyword, providing closed algebraic data types. Pattern Matching (Java 16-21) enhances instanceof and switch expressions to deconstruct records and types exhaustively without casting.")
                .keyPoints(List.of(
                    "Records are final and cannot extend classes (already extend java.lang.Record).",
                    "Sealed classes provide compiler-checked exhaustive switch matching without needing a default branch.",
                    "Record patterns allow direct destructuring of components inside switch."
                ))
                .codeSnippet("// Sealed hierarchy with Pattern Matching switch:\npublic sealed interface Shape permits Circle, Rectangle {}\n\npublic double area(Shape s) {\n    return switch (s) {\n        case Circle c -> Math.PI * c.radius() * c.radius();\n        case Rectangle r -> r.width() * r.height();\n    };\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Java Core")
                .topic("Performance & Reference Types")
                .question("Explain the four types of Object References in Java: Strong, Soft, Weak, and Phantom references.")
                .answer("1. Strong Reference: standard reference (Object obj = new Object()). Never garbage collected while reachable.\n2. SoftReference: collected only when JVM runs critically low on heap memory before OutOfMemoryError occurs; ideal for memory-sensitive caches.\n3. WeakReference: collected eagerly in the very next GC cycle when no strong references remain; used in WeakHashMap to prevent key memory leaks.\n4. PhantomReference: enqueued in a ReferenceQueue after the object has been finalized and memory is ready to be reclaimed; used for cleanup operations instead of finalize().")
                .keyPoints(List.of(
                    "SoftReference: Out-of-memory prevention cache.",
                    "WeakReference: Canonical mapping caches (WeakHashMap).",
                    "Cleaner API (Java 9+) uses PhantomReferences to replace deprecated finalize()."
                ))
                .codeSnippet("WeakReference<HeavyObject> weakRef = new WeakReference<>(new HeavyObject());\nHeavyObject obj = weakRef.get(); // returns null if GC collected it")
                .build()
        );
    }

    // ==========================================
    // 5. SPRING BOOT QUESTIONS BANK
    // ==========================================
    private List<InterviewQuestion> getSpringBootQuestionsBank() {
        return List.of(
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Core & Bean Lifecycle")
                .question("Detail the entire Spring Bean Lifecycle from instantiation to destruction.")
                .answer("1. Bean Definition scanning (@Component, @Bean).\n2. Instantiation (constructor execution via reflection).\n3. Dependency Injection (populating properties / @Autowired).\n4. BeanNameAware, BeanClassLoaderAware, BeanFactoryAware callbacks.\n5. BeanPostProcessor.postProcessBeforeInitialization().\n6. Initialization (@PostConstruct, InitializingBean.afterPropertiesSet(), custom initMethod).\n7. BeanPostProcessor.postProcessAfterInitialization() (Crucial: Proxies like AOP, @Transactional, and Security are generated here!).\n8. Bean is ready for use in ApplicationContext.\n9. Destruction: @PreDestroy, DisposableBean.destroy(), custom destroyMethod.")
                .keyPoints(List.of(
                    "Dynamic Proxies (CGLIB / JDK Dynamic Proxy) are created in postProcessAfterInitialization.",
                    "BeanFactoryPostProcessor runs before any beans are created (e.g. PropertySourcesPlaceholderConfigurer).",
                    "@PostConstruct executes before AOP proxies wrap the bean."
                ))
                .codeSnippet("@Component\npublic class CustomBean implements InitializingBean {\n    @PostConstruct\n    public void postConstruct() { /* Step 6 */ }\n    \n    @Override\n    public void afterPropertiesSet() { /* Step 6 */ }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Transactions & AOP")
                .question("How does @Transactional work internally, and why does self-invocation bypass transaction management?")
                .answer("Spring @Transactional works via Spring AOP proxies (CGLIB by default in Boot 2+). When a client invokes a method on a transactional bean, it calls the proxy object. The TransactionInterceptor opens a transaction, executes the target method via reflection, and commits or rolls back on RuntimeException. When a method calls another transactional method within the same class (this.methodB()), the call bypasses the proxy and executes directly on the target instance ('this'). Therefore, the transaction advice is never triggered.")
                .keyPoints(List.of(
                    "AOP proxy intercepts external calls only; self-invocation calls target object directly.",
                    "Fixes: Move method to a separate helper bean, self-inject the proxy (@Autowired private MyService self), or use AspectJ compile-time weaving.",
                    "Default rollback is only for unchecked exceptions (RuntimeException and Error); set rollbackFor = Exception.class for checked exceptions."
                ))
                .codeSnippet("// Fix self-invocation:\n@Service\npublic class OrderService {\n    @Autowired private OrderService self; // Injects CGLIB proxy\n\n    public void process() {\n        self.saveWithTx(); // Goes through proxy!\n    }\n    \n    @Transactional\n    public void saveWithTx() { /* ... */ }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Auto-Configuration")
                .question("How does Spring Boot Auto-Configuration work under the hood (@EnableAutoConfiguration)?")
                .answer("Spring Boot inspects classpath dependencies via META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (in Boot 3+, formerly spring.factories). During context startup, AutoConfigurationImportSelector evaluates conditions using @Conditional annotations (@ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty). If a required library is present on classpath and the user hasn't defined their own bean, the default configuration bean is registered automatically.")
                .keyPoints(List.of(
                    "@SpringBootApplication includes @EnableAutoConfiguration, @ComponentScan, and @Configuration.",
                    "@ConditionalOnMissingBean guarantees developer-defined beans always override defaults.",
                    "@AutoConfiguration classes define order via @AutoConfigureBefore / @AutoConfigureAfter."
                ))
                .codeSnippet("@AutoConfiguration\n@ConditionalOnClass(DataSource.class)\n@ConditionalOnMissingBean(DataSource.class)\npublic class DataSourceAutoConfiguration {\n    @Bean\n    public DataSource dataSource() { return ...; }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Spring Security 6")
                .question("Explain the Spring Security 6 SecurityFilterChain architecture and how authentication works.")
                .answer("Spring Security operates as a chain of servlet filters registered inside DelegatingFilterProxy -> FilterChainProxy. In Spring Security 6, WebSecurityConfigurerAdapter is removed in favor of component-based SecurityFilterChain bean definitions. Key filters include: SecurityContextHolderFilter, UsernamePasswordAuthenticationFilter / OAuth2LoginAuthenticationFilter, ExceptionTranslationFilter, and AuthorizationFilter. AuthenticationManager uses AuthenticationProvider to validate credentials and populate SecurityContext with an Authentication token.")
                .keyPoints(List.of(
                    "SecurityFilterChain registered as a @Bean with HttpSecurity DSL.",
                    "SecurityContextHolder stores Authentication in ThreadLocal (or TransmittableThreadLocal).",
                    "Spring Security 6 enforces authorization at the end of the chain via AuthorizationFilter."
                ))
                .codeSnippet("@Bean\npublic SecurityFilterChain filterChain(HttpSecurity http) throws Exception {\n    return http\n        .csrf(AbstractHttpConfigurer::disable)\n        .authorizeHttpRequests(auth -> auth\n            .requestMatchers(\"/api/admin/**\").hasRole(\"ADMIN\")\n            .anyRequest().authenticated()\n        )\n        .build();\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Spring Data JPA")
                .question("What is the N+1 query problem in Spring Data JPA, and what are all the ways to resolve it?")
                .answer("The N+1 problem occurs when fetching an entity with lazy-loaded relationships (e.g. Order has many OrderItems). Loading N orders generates 1 query to fetch the orders, and then N separate queries when iterating order.getItems(), leading to massive DB roundtrips. Solutions include:\n1. JOIN FETCH in JPQL (@Query(\"SELECT o FROM Order o JOIN FETCH o.items\")).\n2. @EntityGraph on the repository method specifying attribute paths.\n3. DTO projection directly selecting only needed fields.\n4. Hibernate @BatchSize(size = 25) to batch lazy loading using SQL IN clauses.")
                .keyPoints(List.of(
                    "JOIN FETCH forces an inner/left join to load associations in a single query.",
                    "@EntityGraph is declarative and avoids hardcoded JPQL queries.",
                    "DTO projections bypass Hibernate entity tracking overhead entirely for read-heavy APIs."
                ))
                .codeSnippet("@EntityGraph(attributePaths = {\"items\", \"customer\"})\nList<Order> findAllByStatus(OrderStatus status);")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Exception Handling")
                .question("How do @ControllerAdvice, @ExceptionHandler, and ProblemDetail (RFC 7807) work in Spring Boot 3?")
                .answer("@ControllerAdvice is a specialization of @Component that applies cross-cutting exception handling across all @RequestMapping controllers. In Spring Boot 3, ProblemDetail (implementing RFC 7807) was introduced as the standard JSON error payload specification containing status, title, detail, type URI, and custom properties. ResponseEntityExceptionHandler can be extended to standardize error formatting across all standard Spring MVC exceptions.")
                .keyPoints(List.of(
                    "@ControllerAdvice intercepts exceptions thrown from any controller.",
                    "ProblemDetail provides standardized machine-readable HTTP error details.",
                    "Spring Boot 3 property 'spring.mvc.problemdetails.enabled=true' enables standard RFC 7807 responses."
                ))
                .codeSnippet("@RestControllerAdvice\npublic class GlobalExceptionHandler {\n    @ExceptionHandler(ResourceNotFoundException.class)\n    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {\n        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());\n    }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Caching")
                .question("How does Spring Cache abstraction work (@Cacheable, @CachePut, @CacheEvict), and how do you configure Redis as the cache manager?")
                .answer("Spring Cache abstraction uses AOP to intercept method invocations. When @Cacheable is present, Spring checks the underlying CacheManager (e.g. RedisCacheManager, ConcurrentMapCacheManager). If a cached value exists for the generated key (SpEL expression), the method body is skipped and the cached value returned. If not, the method executes and its return value is stored in cache. @CachePut always executes the method and updates the cache. @CacheEvict removes entries to keep data consistent.")
                .keyPoints(List.of(
                    "@Cacheable: cache hit returns cached value; cache miss runs method and writes to cache.",
                    "@CacheEvict(allEntries = true): flushes entire cache namespace on mutations.",
                    "RedisCacheConfiguration sets TTL, key serialization (StringRedisSerializer), and value serialization (GenericJackson2JsonRedisSerializer)."
                ))
                .codeSnippet("@Cacheable(value = \"products\", key = \"#id\", unless = \"#result == null\")\npublic Product getProductById(Long id) {\n    return repository.findById(id).orElse(null);\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Microservices Communication")
                .question("What are the architectural differences between RestTemplate, WebClient, and HTTP Interfaces in Spring Boot 3?")
                .answer("RestTemplate is the classic synchronous, blocking HTTP client where each request blocks a thread. WebClient (from Spring WebFlux) is a non-blocking, reactive client based on Project Reactor and Netty, supporting high concurrency with few threads. In Spring Boot 3 / Spring 6, HTTP Interfaces allow defining declarative HTTP client interfaces with @HttpExchange annotations (similar to Feign), which can be backed by either WebClient or the new non-reactive RestClient.")
                .keyPoints(List.of(
                    "RestTemplate is in maintenance mode; RestClient is the modern synchronous alternative in Spring 6.1+.",
                    "WebClient is ideal for streaming and reactive pipelines.",
                    "HttpInterfaces eliminate boilerplate HTTP client code with declarative methods."
                ))
                .codeSnippet("// Spring 3.2 RestClient HTTP Interface:\npublic interface UserClient {\n    @GetExchange(\"/users/{id}\")\n    User getUser(@PathVariable(\"id\") Long id);\n}")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Actuator & Observability")
                .question("What is Micrometer, and how does Spring Boot 3 implement distributed tracing with OpenTelemetry?")
                .answer("Spring Boot 3 replaced Spring Cloud Sleuth with Micrometer Tracing. Micrometer provides a vendor-neutral facade for dimensional metrics (Prometheus, Datadog) and distributed tracing (Zipkin, OpenTelemetry). Every incoming request is assigned a Trace ID (representing the overall transaction across microservices) and Span ID (representing a single hop). Micrometer automatically propagates these IDs in HTTP headers (W3C TraceContext) and MDC for log correlation.")
                .keyPoints(List.of(
                    "Trace ID correlates logs across distributed services in ELK / Grafana Loki.",
                    "management.endpoints.web.exposure.include configures accessible Actuator endpoints.",
                    "Never expose sensitive actuator endpoints (/env, /heapdump) publicly in production."
                ))
                .codeSnippet("// Log format with trace and span IDs:\nlogging.pattern.level = \"%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]\"")
                .build(),
            InterviewQuestion.builder()
                .category("Spring Boot")
                .topic("Database Migrations")
                .question("Why should you use Flyway or Liquibase instead of 'spring.jpa.hibernate.ddl-auto=update' in production?")
                .answer("Using ddl-auto=update in production is extremely hazardous: Hibernate can only create tables or add columns; it cannot safely rename columns, drop outdated indexes, or perform data backfills. It can lock tables unpredictably or corrupt schemas during multi-instance deployments. Flyway/Liquibase provide version-controlled, repeatable SQL migration scripts tracked in a flyway_schema_history table. Migrations run deterministically in order, support rollback strategies, and ensure every environment (dev, staging, prod) has the exact same database schema state.")
                .keyPoints(List.of(
                    "Deterministic, audited SQL scripts checked into version control.",
                    "Checksum validation prevents manual tampering with applied scripts.",
                    "ddl-auto must strictly be set to 'validate' or 'none' in production."
                ))
                .codeSnippet("// Flyway migration naming convention:\n// V1__init_schema.sql\n// V2__add_index_on_user_email.sql")
                .build()
        );
    }

    // ==========================================
    // 6. DATABASE & SQL QUESTIONS BANK
    // ==========================================
    private List<InterviewQuestion> getDatabaseQuestionsBank() {
        return List.of(
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Indexing Internals")
                .question("Explain the internal structure of a B+ Tree index and why relational databases prefer B+ Trees over B-Trees and Hash indexes.")
                .answer("In a B+ Tree, all data records/pointers are stored exclusively in the leaf nodes, while internal nodes store only routing keys and child pointers. Furthermore, leaf nodes are linked together as a doubly-linked list. B+ Trees outperform B-Trees for databases because:\n1. Internal nodes hold many more keys since they carry no data payloads, resulting in higher fanout and shallower tree height (typically 3-4 levels for millions of rows), minimizing disk I/O.\n2. Range queries (BETWEEN, >, <, ORDER BY) are extremely fast: locate the starting leaf node via O(log N) tree traversal, then scan horizontally across linked leaf nodes.\nHash indexes are O(1) for exact lookups but cannot do range scans or prefix searches.")
                .keyPoints(List.of(
                    "High fanout reduces disk seek operations (I/O depth).",
                    "Doubly-linked leaves allow linear sequential scans without tree re-traversal.",
                    "Consistent O(log N) lookup time for all keys since all leaves are at the exact same depth."
                ))
                .codeSnippet("-- Composite index leftmost prefix rule:\nCREATE INDEX idx_user_status_created ON users (status, created_at);\n-- Uses index: WHERE status = 'ACTIVE' AND created_at > '2026-01-01'\n-- Cannot use index efficiently: WHERE created_at > '2026-01-01'")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("ACID & Isolation Levels")
                .question("What are the 4 SQL Transaction Isolation Levels and the 3 read anomalies they prevent?")
                .answer("Read Anomalies:\n- Dirty Read: Reading uncommitted changes made by another transaction that may be rolled back.\n- Non-Repeatable Read: Re-reading the same row within a transaction returns different values because another transaction updated and committed it.\n- Phantom Read: Re-running a range query returns newly inserted/deleted rows committed by another transaction.\n\nIsolation Levels:\n1. READ UNCOMMITTED: All anomalies possible.\n2. READ COMMITTED: Prevents Dirty Reads (uses snapshot at statement start in MVCC).\n3. REPEATABLE READ (Default in MySQL InnoDB): Prevents Dirty and Non-Repeatable Reads (uses snapshot at transaction start; InnoDB uses Next-Key Locks to also prevent phantoms).\n4. SERIALIZABLE: Prevents all anomalies using strict two-phase locking (2PL).")
                .keyPoints(List.of(
                    "PostgreSQL default: READ COMMITTED. MySQL default: REPEATABLE READ.",
                    "Higher isolation levels degrade concurrency and increase deadlock probabilities."
                ))
                .codeSnippet("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Concurrency Control")
                .question("What is the difference between Optimistic Locking and Pessimistic Locking, and when should you use each?")
                .answer("Pessimistic Locking assumes conflicts will occur and locks the database record upfront using SQL 'SELECT ... FOR UPDATE'. Other transactions attempting to access the row must wait until the lock is released on commit/rollback. Best for high-contention, critical write operations (e.g. reserving the last flight seat or processing bank withdrawals).\nOptimistic Locking assumes conflicts are rare and does not lock records during read. Instead, it adds a @Version column. When updating, it executes 'UPDATE table SET ..., version = version + 1 WHERE id = ? AND version = current_version'. If the row count is 0, another transaction updated it first, throwing OptimisticLockException. Best for read-heavy workloads with low collision rates.")
                .keyPoints(List.of(
                    "Optimistic: No DB locks; relies on version check; better throughput for read-heavy systems.",
                    "Pessimistic: SELECT FOR UPDATE; prevents conflicts upfront; risks deadlocks and throttles concurrency.",
                    "In JPA: @Version for optimistic; LockModeType.PESSIMISTIC_WRITE for pessimistic."
                ))
                .codeSnippet("// JPA Optimistic:\n@Version private Long version;\n\n// JPA Pessimistic:\n@Lock(LockModeType.PESSIMISTIC_WRITE)\nOptional<Account> findByIdForUpdate(Long id);")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Clustered vs Non-Clustered Indexes")
                .question("What is the fundamental difference between a Clustered Index and a Non-Clustered (Secondary) Index?")
                .answer("A Clustered Index dictates the physical order in which rows are stored on disk. The leaf nodes of a clustered index contain the actual data rows (table pages). Therefore, a table can have ONLY ONE clustered index (usually the PRIMARY KEY in MySQL InnoDB). A Non-Clustered (Secondary) Index is a separate B+ Tree where leaf nodes store the indexed key along with a pointer to the clustered index key (Primary Key). Looking up a row using a secondary index requires an additional 'Bookmark Lookup' (searching the clustered index) unless the query is satisfied entirely by a 'Covering Index'.")
                .keyPoints(List.of(
                    "Clustered Index: Leaf nodes = actual table rows (1 per table).",
                    "Secondary Index: Leaf nodes = indexed columns + primary key pointer.",
                    "Covering Index: When all columns in SELECT and WHERE exist in the secondary index, bookmark lookup is avoided."
                ))
                .codeSnippet("-- Covering index example:\nCREATE INDEX idx_emp_dept_salary ON employee(dept_id, salary);\n-- Fully satisfied by index (Using index in EXPLAIN):\nSELECT dept_id, salary FROM employee WHERE dept_id = 10;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Normalization")
                .question("Explain Database Normalization forms from 1NF to BCNF, and when is intentional Denormalization justified?")
                .answer("- 1NF: Atomic values (no repeating groups or arrays in columns), primary key defined.\n- 2NF: In 1NF and no partial dependencies (every non-key attribute is fully dependent on the entire composite primary key).\n- 3NF: In 2NF and no transitive dependencies (non-key attributes do not depend on other non-key attributes: X -> Y -> Z).\n- BCNF (Boyce-Codd): Stricter 3NF where every determinant X in X -> Y must be a candidate key.\nDenormalization is intentionally adding redundant data (e.g., storing total_orders on customer table) in high-scale read-heavy systems or analytical data warehouses to eliminate expensive multi-table JOINs at query time, traded off against extra write complexity.")
                .keyPoints(List.of(
                    "1NF: Atomicity.",
                    "2NF: No partial key dependency.",
                    "3NF: No transitive dependency.",
                    "Denormalize for read performance at scale when read-to-write ratio is 100:1."
                ))
                .codeSnippet("-- Denormalized cache counter:\nALTER TABLE users ADD COLUMN cached_followers_count INT DEFAULT 0;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Sharding & Partitioning")
                .question("What is the difference between Horizontal Partitioning and Database Sharding, and how does Consistent Hashing help?")
                .answer("Horizontal Partitioning splits a large table into smaller physical partitions on the same database server instance (e.g. partition by RANGE on created_at date). Database Sharding distributes rows horizontally across multiple physically distinct database servers/nodes, each operating independently (Shared-Nothing architecture).\nA Shard Key (e.g. userId) determines which database node stores the record. Standard hash(key) % N causes massive data migration when adding a node. Consistent Hashing maps nodes and keys onto a virtual ring (0 to 2^32-1); adding or removing a shard node requires remapping only k/N keys instead of the entire dataset.")
                .keyPoints(List.of(
                    "Partitioning = single database server; Sharding = distributed across multiple database servers.",
                    "Challenges in sharding: Cross-shard JOINs, distributed transactions (Two-Phase Commit), rebalancing.",
                    "Virtual nodes in consistent hashing ensure even key distribution across shards."
                ))
                .codeSnippet("// Shard routing logic concept:\nint hash = MurmurHash3.hash32(userId);\nint shardId = hashRing.getFloorNode(hash);")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Query Optimization")
                .question("How do you read and interpret an EXPLAIN ANALYZE execution plan in PostgreSQL/MySQL?")
                .answer("EXPLAIN shows the query optimizer's estimated execution strategy, while EXPLAIN ANALYZE actually executes the query and prints real elapsed times, row counts, and buffer hits. Key indicators to check:\n1. Access Type: ALL (full table scan - bad), index (full index scan), range (index range scan - good), ref / eq_ref (indexed lookup - best).\n2. Join Algorithms: Nested Loop (great for small outer tables), Hash Join (fast for large unindexed joins), Merge Join (requires sorted inputs).\n3. Rows: Difference between estimated vs actual rows indicates stale table statistics (run ANALYZE).\n4. Buffers: Check shared read vs shared hit (cache hit ratio).")
                .keyPoints(List.of(
                    "Avoid 'Using filesort' and 'Using temporary' in MySQL EXPLAIN.",
                    "Look for 'Seq Scan' on large tables in PostgreSQL - indicates missing index.",
                    "Composite indexes must align with leftmost prefix rules."
                ))
                .codeSnippet("EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 42 ORDER BY order_date DESC;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Distributed Systems")
                .question("Explain the CAP Theorem and the PACELC Theorem with real database examples.")
                .answer("The CAP Theorem states that in a distributed data store experiencing a Network Partition (P), you must choose between Consistency (C - every read receives most recent write) or Availability (A - every non-failing node returns a response). You cannot guarantee both simultaneously.\nPACELC theorem extends CAP: If there is a Partition (P), how does system trade off Availability (A) and Consistency (C)? Else (E), when system runs normally without partitions, how does it trade off Latency (L) and Consistency (C)?\nExamples:\n- MongoDB / HBase: PC/EC (chooses Consistency in partitions, low latency consistency normally).\n- Cassandra / DynamoDB: PA/EL (chooses Availability in partitions, Eventual consistency for low Latency normally).")
                .keyPoints(List.of(
                    "Network partitions are inevitable in distributed systems; CA distributed systems do not exist in reality.",
                    "PACELC highlights the trade-off during normal, non-partitioned operation (Latency vs Consistency)."
                ))
                .codeSnippet("// Cassandra tunable consistency:\nSELECT * FROM user_profile USING CONSISTENCY LOCAL_QUORUM;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Storage Engine Internals")
                .question("What is Write-Ahead Logging (WAL) and how does it guarantee Durability without hurting write performance?")
                .answer("Disk writes are orders of magnitude faster when sequential rather than random. In databases (PostgreSQL WAL, MySQL InnoDB Redo Log), when a transaction commits, the database does NOT immediately write dirty data pages to random disk locations in the main table files. Instead, it sequentially appends the change delta to an append-only Write-Ahead Log on disk and marks the page as dirty in the buffer pool in RAM. In the event of a power outage or crash, the recovery engine replays the WAL to restore unwritten data. Background threads (Checkpointing) flush dirty buffer pool pages to table files asynchronously.")
                .keyPoints(List.of(
                    "Sequential disk I/O ensures ultra-fast commit times.",
                    "Guarantees the 'D' (Durability) in ACID.",
                    "Fsync flushes WAL buffer to physical disk upon transaction commit."
                ))
                .codeSnippet("-- Force a checkpoint in PostgreSQL:\nCHECKPOINT;")
                .build(),
            InterviewQuestion.builder()
                .category("Database & SQL")
                .topic("Deadlocks")
                .question("How do database deadlocks occur, how does the database detect them, and how should application code handle them?")
                .answer("A deadlock occurs when two or more transactions hold locks that the other needs in a circular dependency: Tx1 holds row A and waits for row B; Tx2 holds row B and waits for row A. Neither can progress. The database engine periodically runs a Deadlock Detector that traverses the 'Wait-For Graph'. When a cycle is detected, the engine picks the transaction with the least work done (the 'deadlock victim') and aborts/rolls it back with an error (e.g. ORA-00060, MySQL error 1213). Application code should implement exponential backoff retry mechanisms (@Retryable).")
                .keyPoints(List.of(
                    "Prevent deadlocks by always acquiring locks in the exact same deterministic order across all queries.",
                    "Keep transactions as short as possible; never make network/HTTP calls inside a database transaction.",
                    "Handle deadlock exceptions by automatically retrying the transaction."
                ))
                .codeSnippet("@Retryable(retryFor = DeadlockLoserDataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))")
                .build()
        );
    }

    // ==========================================
    // 7. CS SUBJECTS (OS & NETWORKING) BANK
    // ==========================================
    private List<InterviewQuestion> getCsQuestionsBank() {
        return List.of(
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Operating Systems - Concurrency")
                .question("What is the difference between a Process and a Thread, and what resources are shared vs private?")
                .answer("A Process is an independent executing program with its own isolated virtual memory address space (Text, Data, Heap, Stack) managed by the OS. Processes communicate via Inter-Process Communication (IPC: pipes, sockets, shared memory). A Thread is the smallest unit of CPU scheduling within a process. All threads in a process share the same Code segment, Data segment (globals), Heap memory, and open file descriptors. However, each thread has its own private Stack, Program Counter (PC), and CPU Registers.")
                .keyPoints(List.of(
                    "Shared by threads: Heap, Global Variables, Open Files, Address Space.",
                    "Private to each thread: Thread Stack, Stack Pointer, Program Counter, CPU Registers.",
                    "Context switching between processes is much slower than between threads due to TLB (Translation Lookaside Buffer) invalidation."
                ))
                .codeSnippet("// Thread-private stack frame vs shared heap:\nint localStackVar = 42; // private to thread stack\nUser sharedObj = new User(); // allocated on shared process heap")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Operating Systems - Deadlocks")
                .question("What are the 4 Coffman Conditions required for a Deadlock, and how can they be broken?")
                .answer("All 4 conditions must hold simultaneously for a deadlock to occur:\n1. Mutual Exclusion: At least one resource must be non-shareable.\n2. Hold and Wait: A process holds at least one resource and is waiting to acquire additional resources held by other processes.\n3. No Preemption: Resources cannot be forcibly seized from a process until released voluntarily.\n4. Circular Wait: A closed loop exists where P0 waits for P1, P1 waits for P2... Pn waits for P0.\nBreaking any single condition prevents deadlocks. The most practical is breaking Circular Wait by imposing a strict global ordering on all resource acquisitions (e.g. always lock account with lower ID first).")
                .keyPoints(List.of(
                    "Break Circular Wait: Enforce hierarchical lock ordering.",
                    "Break Hold and Wait: Request all required resources simultaneously upfront.",
                    "Banker's Algorithm: Dynamically evaluates resource requests against safe states."
                ))
                .codeSnippet("// Breaking circular wait by ordering:\nlong first = Math.min(idA, idB);\nlong second = Math.max(idA, idB);\nsynchronized (getLock(first)) {\n    synchronized (getLock(second)) {\n        // transfer\n    }\n}")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Operating Systems - Memory Management")
                .question("Explain Virtual Memory, Paging, Page Faults, and the role of the TLB (Translation Lookaside Buffer).")
                .answer("Virtual Memory decouples application memory from physical RAM. The virtual address space is divided into fixed-size chunks called Pages (typically 4KB). Physical RAM is divided into matching Page Frames. The CPU Memory Management Unit (MMU) uses a Page Table to translate virtual addresses to physical addresses. The TLB is a high-speed hardware cache inside the CPU storing recent virtual-to-physical mappings. If a translation misses the TLB, the MMU walks the multi-level page table. If the page is not loaded in RAM (swapped out to disk), a Page Fault hardware interrupt is triggered, prompting the OS kernel to load the page from disk into a free RAM frame.")
                .keyPoints(List.of(
                    "TLB Hit -> ~1ns. Page Table Walk -> ~10-20ns. Page Fault (Disk I/O) -> ~5-10ms (huge performance hit!).",
                    "Thrashing: When processes spend more time handling page faults than executing instructions."
                ))
                .codeSnippet("# Linux check page faults for process:\nps -o min_flt,maj_flt -p <PID>")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Operating Systems - Synchronization")
                .question("What is the fundamental difference between a Mutex and a Semaphore (Binary vs Counting)?")
                .answer("A Mutex (Mutual Exclusion object) is a locking mechanism with ownership semantics: ONLY the thread that acquired the mutex can unlock it. It is designed to serialize access to a critical section. A Semaphore is a signaling mechanism with an integer counter: it does not have ownership semantics, meaning Thread A can signal (release) a semaphore that was decremented (acquired) by Thread B. A Binary Semaphore (0 or 1) can act as a lock, while a Counting Semaphore regulates access to a finite pool of N identical resources (e.g., a database connection pool of size 20).")
                .keyPoints(List.of(
                    "Mutex has ownership: 'I locked it, I must unlock it'.",
                    "Semaphore is a signal: 'Wait until permit available; signal permit released'.",
                    "Spinlock vs Mutex: Spinlock busy-waits in a loop on the CPU (good for tiny critical sections); mutex puts the thread to sleep."
                ))
                .codeSnippet("// Semaphore as connection limiter:\nSemaphore pool = new Semaphore(10); // max 10 concurrent permits\npool.acquire();\ntry { useConnection(); } finally { pool.release(); }")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Computer Networks - Transport Layer")
                .question("Explain the TCP 3-Way Handshake and 4-Way Teardown, and why the TIME_WAIT state exists.")
                .answer("Connection Establishment (3-Way):\n1. Client -> SYN (Seq=X)\n2. Server -> SYN-ACK (Seq=Y, Ack=X+1)\n3. Client -> ACK (Ack=Y+1). Connection ESTABLISHED.\n\nConnection Termination (4-Way):\n1. Client -> FIN (I'm done sending)\n2. Server -> ACK (I acknowledge your FIN; I may still have data to send)\n3. Server -> FIN (I'm done sending too)\n4. Client -> ACK -> Enters TIME_WAIT state (typically 2 * Maximum Segment Lifetime / 2MSL = 60-120 seconds).\n\nWhy TIME_WAIT exists:\n1. To ensure the final ACK reliably reached the server (if lost, server retransmits FIN).\n2. To allow lingering duplicate packets from the old connection to expire in the network so they don't corrupt a new connection using the same IP:Port tuple.")
                .keyPoints(List.of(
                    "3-Way: SYN -> SYN-ACK -> ACK.",
                    "4-Way: FIN -> ACK -> FIN -> ACK.",
                    "TIME_WAIT prevents duplicate segment corruption and ensures graceful remote close."
                ))
                .codeSnippet("# Check TIME_WAIT sockets:\nnetstat -nat | grep TIME_WAIT | wc -l")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Computer Networks - Application Layer")
                .question("What are the key evolutionary differences between HTTP/1.1, HTTP/2, and HTTP/3 (QUIC)?")
                .answer("- HTTP/1.1: Plain text protocol with persistent TCP connections (Keep-Alive). Suffers from Head-of-Line (HoL) Blocking at the application layer: only one request/response can be processed per TCP connection at a time.\n- HTTP/2: Binary protocol introducing Multiplexing: multiple bidirectional streams interleaved over a single TCP connection. Adds Header Compression (HPACK) and Server Push. However, it still suffers from TCP-level Head-of-Line blocking: if a single packet is lost, the OS TCP stack stalls ALL multiplexed streams until the packet is retransmitted.\n- HTTP/3: Completely replaces TCP with QUIC (built on top of UDP). Solves TCP HoL blocking: packet loss on one stream does NOT pause other streams. Features 0-RTT connection resumption and connection migration across networks (e.g. WiFi to 5G seamlessly).")
                .keyPoints(List.of(
                    "HTTP/1.1: Text, 1 request per TCP socket at a time.",
                    "HTTP/2: Binary, Multiplexing over single TCP, HPACK.",
                    "HTTP/3: QUIC over UDP, independent streams (no TCP HoL blocking), 0-RTT handshake."
                ))
                .codeSnippet("HTTP/1.1 -> TCP + TLS\nHTTP/2   -> TCP + TLS (Multiplexed Streams)\nHTTP/3   -> UDP + QUIC (Native TLS 1.3 encryption)")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Computer Networks - DNS & Web")
                .question("What happens under the hood from the moment you type 'https://google.com' in the browser and hit Enter?")
                .answer("1. URL parsing & HSTS check.\n2. DNS Resolution: Browser cache -> OS hosts/cache -> Local DNS Resolver -> Root Nameserver (.) -> TLD Nameserver (.com) -> Authoritative Nameserver (google.com) -> Returns IP.\n3. TCP Handshake: 3-way handshake with target IP on port 443.\n4. TLS Handshake: ClientHello (supported ciphers, SNI) -> ServerHello (chosen cipher, SSL Certificate) -> Client verifies cert against CA roots -> Key exchange (ECDHE) derives symmetric session keys.\n5. HTTP Request sent encrypted.\n6. Load Balancer / Reverse Proxy terminates TLS and forwards to backend service.\n7. Server generates response and returns HTML/JSON with HTTP 200.\n8. Browser parses DOM, CSSOM, renders Render Tree, executes JS.")
                .keyPoints(List.of(
                    "DNS lookup traverses hierarchy: Root -> TLD -> Authoritative.",
                    "TLS 1.3 reduces TLS handshake latency to 1 RTT (or 0-RTT for resumed sessions).",
                    "Browser rendering pipeline: DOM + CSSOM -> Render Tree -> Layout -> Paint -> Composite."
                ))
                .codeSnippet("# Trace DNS lookup:\ndig +trace google.com")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Computer Networks - Security")
                .question("How does TLS 1.3 work, and what is the difference between Symmetric and Asymmetric Encryption in HTTPS?")
                .answer("Asymmetric Encryption (RSA, ECC) uses a Public Key to encrypt and a Private Key to decrypt. It is computationally expensive and is used ONLY during the TLS Handshake to verify server identity (via digital certificates signed by trusted Certificate Authorities) and safely exchange a shared secret. Symmetric Encryption (AES-GCM, ChaCha20) uses the EXACT same secret key for both encryption and decryption, and is orders of magnitude faster. Once the TLS handshake derives the shared symmetric key, all subsequent HTTP data is encrypted with fast symmetric encryption. TLS 1.3 mandates Ephemeral Diffie-Hellman (PFS - Perfect Forward Secrecy) so compromising the server's private key in the future cannot decrypt past recorded sessions.")
                .keyPoints(List.of(
                    "Asymmetric encryption for handshake and authentication; Symmetric encryption for bulk data transfer.",
                    "Perfect Forward Secrecy (PFS) ensures past sessions remain secure even if private keys leak.",
                    "TLS 1.3 completes in 1 round trip (1-RTT) compared to 2-RTT in TLS 1.2."
                ))
                .codeSnippet("# Inspect TLS certificate and handshake:\nopenssl s_client -connect google.com:443 -tls1_3")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Operating Systems - File Systems")
                .question("What is an Inode in Unix/Linux file systems, and how do Hard Links differ from Soft (Symbolic) Links?")
                .answer("An Inode (Index Node) is a data structure on disk that stores all metadata about a file (file size, permissions, owner, timestamps, pointers to disk data blocks) EXCEPT the filename. A directory is simply a table mapping filenames to Inode numbers.\n- Hard Link: A new directory entry pointing directly to the SAME Inode number. Both filenames share the same metadata and data blocks. Deleting one hard link decrements the inode's link count; data is only freed when link count reaches 0. Cannot cross filesystem boundaries or link to directories.\n- Soft Link (Symlink): A distinct file with its OWN unique Inode whose data block contains the text string path to the target file. If the original target is deleted, the symlink breaks ('dangling link').")
                .keyPoints(List.of(
                    "Inode stores metadata and block pointers; filename is stored in directory entry.",
                    "Hard link shares the same Inode number (ln file.txt hard.txt).",
                    "Soft link has its own Inode containing target path (ln -s file.txt soft.txt)."
                ))
                .codeSnippet("# View inode numbers:\nls -li myfile.txt")
                .build(),
            InterviewQuestion.builder()
                .category("CS Subjects")
                .topic("Computer Networks - Protocols")
                .question("Compare WebSockets, Server-Sent Events (SSE), and Long Polling for real-time applications.")
                .answer("- Long Polling: Client sends an HTTP request; server holds it open until new data is available, sends response, and client immediately opens a new request. High overhead due to repeated HTTP header parsing and TCP re-connections.\n- Server-Sent Events (SSE): Unidirectional (Server -> Client) streaming over standard HTTP/1.1 or HTTP/2. Lightweight, automatic reconnection, text-based (Content-Type: text/event-stream), works through proxies and firewalls without special configuration. Ideal for live stock tickers, news feeds, and LLM text generation streaming.\n- WebSockets: Full-duplex, bidirectional communication over a single persistent TCP connection initiated via HTTP 101 Switching Protocols. Binary and text support, minimal framing overhead (2-10 bytes per frame). Best for multiplayer games, collaborative editing (Figma), and chat apps.")
                .keyPoints(List.of(
                    "SSE: Unidirectional (Server to Client), built-in reconnection, standard HTTP.",
                    "WebSockets: Bidirectional (Client <-> Server), full-duplex, low latency frame overhead.",
                    "Use SSE for LLM streaming & dashboards; use WebSockets for chat & collaborative canvas."
                ))
                .codeSnippet("// Server-Sent Events header:\nContent-Type: text/event-stream\nCache-Control: no-cache\nConnection: keep-alive\n\ndata: {\"message\": \"live update\"}\n\n")
                .build()
        );
    }
}
