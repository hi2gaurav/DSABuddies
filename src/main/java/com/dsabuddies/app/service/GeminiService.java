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
            You are "Jeetu Bhaiya", the legendary, inspiring, and sharp tech mentor at DSA Buddies.
            
            Your Persona & Tone:
            - You talk like a beloved, caring elder brother and tech guru. Warm, pragmatic, inspiring, and sharp.
            - You frequently use natural, friendly mentor phrases (e.g. "Arre tension mat le!", "Bhai simple funda hai...", "Dekh, interview me direct code mat likhna, pehle interviewer se clarify karna", "Samjhe ya nahi?").
            - You never make the user feel intimidated. If they struggle with recursion, DP, or system design, you explain it with intuitive real-world analogies.
            - You are an expert across: Data Structures & Algorithms, Java 21, Spring Boot 3, System Design (HLD & LLD), PostgreSQL / MySQL, Redis, Kafka, Operating Systems, and Networking.
            - When answering technical or coding questions:
              1. Give a crisp intuition first.
              2. Provide clean, well-commented code snippets with exact Time & Space Complexity.
              3. Highlight common interview traps and edge cases.
              4. Give a pro-tip on how to impress the interviewer.
            - Keep your responses beautifully formatted in GitHub-style Markdown (code blocks, bold text, bullet points).
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
                    Arre tension mat le, LRU Cache ka simple funda samjhata hu! 💡
                    
                    **Kyu puchte hai interview me?**
                    Kyuki isme do data structures ka jugalbandi hota hai:
                    1. **HashMap<Key, Node>**: O(1) me element dhundne ke liye.
                    2. **Doubly Linked List**: O(1) me order maintain karne aur least-recently used element ko evict karne ke liye.
                    
                    ```java
                    class LRUCache {
                        class Node {
                            int key, val;
                            Node prev, next;
                            Node(int k, int v) { this.key = k; this.val = v; }
                        }
                        
                        private final int capacity;
                        private final Map<Integer, Node> map = new HashMap<>();
                        private final Node head = new Node(0, 0), tail = new Node(0, 0);
                        
                        public LRUCache(int capacity) {
                            this.capacity = capacity;
                            head.next = tail;
                            tail.prev = head;
                        }
                        // get() and put() both run in O(1) time!
                    }
                    ```
                    
                    **Interviewer Pro-Tip:**
                    Hamesha **dummy head** aur **dummy tail** nodes use karna whiteboard pe! Isse boundary conditions (`null` pointer checks) me fasne ka zero chance rehta hai.
                    
                    Aur koi doubt hai isme? Pucho bindass!
                    """;
        } else if (lastMessage.contains("spring") || lastMessage.contains("boot")) {
            return """
                    Spring Boot ka funda ekdum crystal clear rakho! 🚀
                    
                    **Core Principles:**
                    - **Auto-configuration (`@EnableAutoConfiguration`)**: Classpath me jo JARs hai (jaise H2 ya Postgres), Spring unka configuration khud bootstrap karta hai via `spring.factories` / `AutoConfiguration.imports`.
                    - **Dependency Injection & Inversion of Control (IoC)**: Tum classes banao, lifecycle Spring Container handle karega.
                    - **Spring Boot 3.4 Key Features**: Java 21 Virtual Threads (`spring.threads.virtual.enabled=true`), GraalVM Native Images, aur `RestClient` fluent HTTP client.
                    
                    **Interview Question:**
                    "Difference between `@Component`, `@Service`, and `@Repository`?"
                    *Answer:* Functionally sab spring-managed beans hain (`@Component` ki meta-annotations). Lekin `@Repository` automatic persistence exception translation karta hai Spring Data exceptions me!
                    
                    Bhai kaho to aur detail me samjhau?
                    """;
        } else if (lastMessage.contains("system design") || lastMessage.contains("hld") || lastMessage.contains("url")) {
            return """
                    System Design me sabse pehle requirements frame karo, sidhe architecture mat banana! 🏗️
                    
                    **Step-by-Step Interview Blueprint:**
                    1. **Scope Clarification**: Read-heavy ya Write-heavy? SLA kitna chahiye (99.99% availability)?
                    2. **Back-of-envelope Math**: Daily Active Users (DAU), Storage per day, Read QPS vs Write QPS.
                    3. **Core Components**:
                       - API Gateway / Load Balancer (Nginx/HAProxy)
                       - Stateless Application Servers (Horizontally scalable)
                       - Cache Layer (Redis / Memcached for fast lookups)
                       - Database (Postgres for ACID transactions, Cassandra/DynamoDB for high throughput writes)
                    
                    **Jeetu Bhaiya's Golden Rule:**
                    Single Point of Failure (SPOF) kabhi mat chhodna. Interviewer bolega "Database fail ho gaya to?", tum bolna "Master-Slave replication with automatic failover via Raft/Zookeeper"!
                    
                    Kya design karna chahte ho abhi?
                    """;
        } else {
            return """
                    Arre waah! Yeh question badhiya hai. 🎯
                    
                    Dekho, coding aur tech interviews me sabse bada secret hai **Consistency & Fundamentals**.
                    
                    1. **Problem ko pehle dry-run karo**: Sidhe IDE me code mat likho. 2-3 sample inputs banao, edge cases (empty array, nulls, negatives) pakdo.
                    2. **Time aur Space Complexity saath me socho**: Brute force se shuru karo, fir pucho *"Kya mai HashMap ya Two Pointers use karke complexity O(N^2) se O(N) laa sakta hu?"*.
                    3. **Speak out loud**: Interviewer tumhara dimag padhna chahta hai ki tum approach kaise karte ho.
                    
                    Bhai, jo specific topic ya question me phas rahe ho, code ya problem name paste karo yaha — mai pura step-by-step solve karwata hu! 🔥
                    """;
        }
    }
}
