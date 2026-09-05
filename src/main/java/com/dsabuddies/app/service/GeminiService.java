package com.dsabuddies.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String JEETU_BHAIYA_SYSTEM_PROMPT = """
            You are "Jeetu Bhaiya", the world-class tech mentor and elite coding coach at DSA Buddies, powered by Google Gemini.
            
            Persona & Communication Standards:
            - Professional yet friendly, warm, and inspiring. You mentor with authority, precision, and genuine encouragement.
            - Direct and concise introduction: Avoid lengthy greetings or unnecessary conversational fluff. Get straight to the solution with crystal clarity.
            - Provide comprehensive, structured explanations as demanded by the user — exactly like a state-of-the-art Google Gemini model.
            - Structure coding and technical answers clearly:
              1. **Intuition & Approach**: Clear, insightful conceptual walkthrough before diving into code.
              2. **Optimal Code Solution**: Clean, production-ready, well-commented code in the user's requested language (default to Java 21).
              3. **Complexity Analysis**: Big-O Time and Space Complexity with reasoning.
              4. **Edge Cases & Interview Tips**: Boundary conditions, common pitfalls, and FAANG interview expectations.
            - Format answers with clean GitHub-flavored Markdown (code blocks, bold highlights, concise bulleted points).
            """;

    public GeminiService() {
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role; // "user" or "model"
        private String content;
    }

    public String chatWithJeetuBhaiya(List<ChatMessage> conversationHistory) {
        if (geminiApiKey != null && !geminiApiKey.isBlank() && !"your-gemini-api-key".equalsIgnoreCase(geminiApiKey.trim())) {
            try {
                return callGeminiApi(conversationHistory);
            } catch (Exception e) {
                log.warn("Failed to call Gemini API directly, using Jeetu Bhaiya fallback: {}", e.getMessage());
            }
        }

        // Fallback simulated Jeetu Bhaiya response
        return generateFallbackResponse(conversationHistory);
    }

    private String callGeminiApi(List<ChatMessage> conversationHistory) throws Exception {
        List<Map<String, Object>> contents = new ArrayList<>();

        for (ChatMessage msg : conversationHistory) {
            String role = "model".equalsIgnoreCase(msg.getRole()) ? "model" : "user";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", JEETU_BHAIYA_SYSTEM_PROMPT))
                ),
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 2048
                )
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        String response = restClient.post()
                .uri(GEMINI_API_URL + "?key=" + geminiApiKey.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonPayload)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            if (!textNode.isMissingNode()) {
                return textNode.asText();
            }
        }

        throw new IllegalStateException("Empty response from Gemini API");
    }

    private String generateFallbackResponse(List<ChatMessage> history) {
        String lastMessage = "";
        if (history != null && !history.isEmpty()) {
            lastMessage = history.get(history.size() - 1).getContent().toLowerCase();
        }

        if (lastMessage.contains("lru") || lastMessage.contains("cache")) {
            return """
                    ### LRU Cache Implementation & Walkthrough

                    **1. Intuition & Design**
                    To achieve strictly **O(1)** time complexity for both `get()` and `put()`, we combine two complementary data structures:
                    - **HashMap<Key, Node>**: Provides instantaneous $O(1)$ key lookup.
                    - **Doubly Linked List (DLL)**: Tracks usage recency. The most recently accessed items sit at `head.next`, while the least recently used item sits at `tail.prev`. Re-ordering nodes in a DLL takes $O(1)$ pointer manipulations.

                    **2. Production-Grade Java Implementation**
                    ```java
                    import java.util.HashMap;
                    import java.util.Map;

                    public class LRUCache {
                        private static class Node {
                            int key, value;
                            Node prev, next;
                            Node(int k, int v) { this.key = k; this.value = v; }
                        }

                        private final int capacity;
                        private final Map<Integer, Node> cache;
                        private final Node head;
                        private final Node tail;

                        public LRUCache(int capacity) {
                            this.capacity = capacity;
                            this.cache = new HashMap<>();
                            this.head = new Node(0, 0);
                            this.tail = new Node(0, 0);
                            head.next = tail;
                            tail.prev = head;
                        }

                        public int get(int key) {
                            Node node = cache.get(key);
                            if (node == null) return -1;
                            moveToHead(node);
                            return node.value;
                        }

                        public void put(int key, int value) {
                            Node node = cache.get(key);
                            if (node != null) {
                                node.value = value;
                                moveToHead(node);
                            } else {
                                if (cache.size() >= capacity) {
                                    Node lru = tail.prev;
                                    removeNode(lru);
                                    cache.remove(lru.key);
                                }
                                Node newNode = new Node(key, value);
                                cache.put(key, newNode);
                                addToHead(newNode);
                            }
                        }

                        private void addToHead(Node node) {
                            node.next = head.next;
                            node.prev = head;
                            head.next.prev = node;
                            head.next = node;
                        }

                        private void removeNode(Node node) {
                            node.prev.next = node.next;
                            node.next.prev = node.prev;
                        }

                        private void moveToHead(Node node) {
                            removeNode(node);
                            addToHead(node);
                        }
                    }
                    ```

                    **3. Complexity Analysis**
                    - **Time Complexity**: **O(1)** for both `get(key)` and `put(key, value)`.
                    - **Space Complexity**: **O(capacity)** to store at most `capacity` entries in the HashMap and DLL.

                    **4. Interview Traps & Best Practices**
                    - **Sentinel Dummy Nodes**: Always introduce dummy `head` and `tail` nodes to eliminate null checks when inserting or deleting at boundaries.
                    - **Thread Safety**: In concurrent environments, wrap operations in `ReentrantReadWriteLock` or use Java's `ConcurrentLinkedDeque`.
                    """;
        } else if (lastMessage.contains("spring") || lastMessage.contains("boot") || lastMessage.contains("thread")) {
            return """
                    ### Java 21 Virtual Threads & Spring Boot 3 Architecture

                    **1. Core Intuition**
                    - **Platform Threads**: 1:1 mapping with Operating System kernel threads. Each platform thread consumes ~1MB stack memory and incurs expensive OS context switches. A typical server caps out around 5,000–10,000 platform threads.
                    - **Virtual Threads (Project Loom)**: Lightweight JVM-managed threads ($M:N$ mapping). The JVM parks virtual threads upon blocking I/O (database query, network call) and unmounts them from carrier threads, allowing millions of concurrent tasks with near-zero overhead.

                    **2. Enabling Virtual Threads in Spring Boot 3.2+**
                    ```yaml
                    spring:
                      threads:
                        virtual:
                          enabled: true
                    ```

                    **3. Code Example: Asynchronous Parallel Execution**
                    ```java
                    @RestController
                    @RequestMapping("/api/orders")
                    public class OrderController {

                        @GetMapping("/process")
                        public ResponseEntity<String> processConcurrentOrders() throws Exception {
                            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                                Future<String> inventory = executor.submit(() -> fetchInventory());
                                Future<String> payment = executor.submit(() -> verifyPayment());

                                return ResponseEntity.ok("Result: " + inventory.get() + " & " + payment.get());
                            }
                        }

                        private String fetchInventory() throws InterruptedException {
                            Thread.sleep(50); // Unmounts virtual thread from carrier OS thread
                            return "In-Stock";
                        }

                        private String verifyPayment() throws InterruptedException {
                            Thread.sleep(50);
                            return "Authorized";
                        }
                    }
                    ```

                    **4. Interview Pro-Tip**
                    Beware of **pinning**: Avoid `synchronized` blocks around blocking I/O operations as they prevent virtual threads from unmounting from the carrier thread. Prefer `ReentrantLock`.
                    """;
        } else if (lastMessage.contains("system design") || lastMessage.contains("hld") || lastMessage.contains("url")) {
            return """
                    ### High-Level System Design: Scalable URL Shortener (TinyURL)

                    **1. Requirements & Scale Estimation**
                    - **Traffic**: 100 Million URLs created/month ($~40$ writes/sec peak $200$ writes/sec). Read-to-Write ratio $100:1$ ($4,000$ reads/sec).
                    - **Availability**: High availability with sub-50ms read latency.

                    **2. Architecture Components**
                    1. **API Gateway / Load Balancer**: Nginx / Envoy distributing incoming requests with round-robin or least-connections.
                    2. **Application Layer**: Stateless Spring Boot microservices horizontally scaled.
                    3. **Short Code Generation**:
                       - Base62 encoding (`[a-zA-Z0-9]`, $62^7 \\approx 3.5$ Trillion unique URLs).
                       - Range-based Key Generation Service (KGS) via Redis / ZooKeeper to ensure zero collision without locks.
                    4. **Caching Layer**: Redis cluster storing hot URLs using LRU eviction policy (serves 80% of read traffic directly).
                    5. **Database**: Distributed NoSQL (Cassandra / DynamoDB) or partitioned PostgreSQL sharded by `hash(short_key)`.

                    **3. Core Data Schema**
                    ```sql
                    CREATE TABLE url_mapping (
                        short_key VARCHAR(8) PRIMARY KEY,
                        long_url TEXT NOT NULL,
                        user_id BIGINT,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                        expires_at TIMESTAMP WITH TIME ZONE
                    );
                    CREATE INDEX idx_user_id ON url_mapping(user_id);
                    ```

                    **4. Trade-offs & Resilience**
                    - Return **HTTP 302 (Temporary Redirect)** if click tracking/analytics is required; return **HTTP 301 (Permanent Redirect)** to allow browser caching and minimize server load.
                    """;
        } else {
            return """
                    ### Problem Solving & Interview Strategy

                    **1. Four-Step Technical Interview Framework**
                    1. **Clarify Constraints**: Ask about inputs (e.g. Can array be empty? Negative numbers? Integer overflow?).
                    2. **Formulate Intuition**: Start with a baseline brute force approach, analyze its bottleneck, and deduce the optimal algorithm.
                    3. **Dry Run on Edge Cases**: Test with single-element, duplicates, and boundary inputs before finalizing code.
                    4. **Complexity Discussion**: Explicitly articulate Time and Space complexities using Big-O notation.

                    Feel free to share the exact problem statement or topic you are working on, and we will break down the intuition, proof, and full implementation step-by-step.
                    """;
        }
    }
}
