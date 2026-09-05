package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.FlashcardDto;
import com.dsabuddies.app.model.Flashcard;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserFlashcardProgress;
import com.dsabuddies.app.repository.FlashcardRepository;
import com.dsabuddies.app.repository.UserFlashcardProgressRepository;
import com.dsabuddies.app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final UserFlashcardProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LevelService levelService;
    private final ConsistencyService consistencyService;

    @PostConstruct
    public void initFlashcards() {
        if (flashcardRepository.count() > 0) return;

        List<Flashcard> cards = List.of(
            // JAVA
            Flashcard.builder()
                .category("JAVA")
                .topic("Concurrency")
                .difficulty("HARD")
                .question("How does ConcurrentHashMap achieve high concurrency in Java 8 compared to Java 7?")
                .answer("Java 7 used Segment-based locking (default 16 segments/ReentrantLocks).\nJava 8 eliminated Segments completely and uses Node-level synchronized blocks on the head of each bucket + CAS (Compare-And-Swap) for empty bucket insertions. When bucket collisions exceed 8, the linked list converts to a balanced Red-Black Tree (TreeBin) reducing lookup from O(N) to O(log N).")
                .codeSnippet("// Java 8 bucket insertion uses CAS first, then synchronized(firstNode):\nif (casTabAt(tab, i, null, new Node<K,V>(hash, key, value))) break;\nsynchronized (f) { /* Tree or LinkedList chaining */ }")
                .build(),

            Flashcard.builder()
                .category("JAVA")
                .topic("JVM Internals")
                .difficulty("MEDIUM")
                .question("What is the difference between JVM Metaspace and PermGen?")
                .answer("PermGen (removed in Java 8) was located in JVM contiguous heap memory with a fixed maximum size (leading to java.lang.OutOfMemoryError: PermGen space).\nMetaspace is allocated in off-heap native memory and auto-resizes by default up to available OS memory (constrained via -XX:MaxMetaspaceSize).")
                .codeSnippet("// JVM flag to bound metaspace:\njava -XX:MaxMetaspaceSize=256m -jar app.jar")
                .build(),

            Flashcard.builder()
                .category("JAVA")
                .topic("Garbage Collection")
                .difficulty("MEDIUM")
                .question("How does the G1 (Garbage-First) GC differ from standard CMS or Parallel GC?")
                .answer("G1 divides the entire JVM heap into 2,048 equal-sized contiguous memory regions (ranging from 1MB to 32MB) rather than static generational spaces.\nIt tracks Eden, Survivor, and Tenured states dynamically per region. G1 calculates which regions contain the most reclaimable garbage ('Garbage-First') and evacuates live objects concurrently within user-defined pause-time targets (-XX:MaxGCPauseMillis).")
                .codeSnippet("// Enable G1 GC with 100ms pause target:\njava -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -jar app.jar")
                .build(),

            // SPRING BOOT
            Flashcard.builder()
                .category("SPRING_BOOT")
                .topic("Transactions")
                .difficulty("HARD")
                .question("Why does calling a @Transactional method from another method in the SAME class fail to initiate a transaction?")
                .answer("Spring manages transactions using dynamic CGLIB or JDK dynamic AOP proxies.\nWhen a method calls another method within the same bean (e.g. this.innerMethod()), the invocation bypasses the proxy and directly calls the target object, so the transaction interceptor is never triggered.\nSolution: Inject self, use ApplicationContext, or extract the transactional logic to a separate bean.")
                .codeSnippet("@Service\npublic class OrderService {\n    @Autowired private OrderService self; // Self-injection workaround\n\n    public void placeOrder() {\n        self.executeTx(); // Passes through proxy!\n    }\n    @Transactional public void executeTx() { ... }\n}")
                .build(),

            Flashcard.builder()
                .category("SPRING_BOOT")
                .topic("Core Architecture")
                .difficulty("MEDIUM")
                .question("Explain the end-to-end request processing flow of Spring MVC DispatcherServlet.")
                .answer("1. Request arrives at DispatcherServlet.\n2. HandlerMapping determines the matching Controller method.\n3. HandlerAdapter invokes the controller with parameter binding.\n4. Controller executes business logic and returns Model/ResponseEntity.\n5. ViewResolver or HttpMessageConverter (Jackson) serializes response body to JSON.\n6. Handlers execute postHandle and afterCompletion interceptors.")
                .codeSnippet("// Standard REST request pipeline:\nClient -> DispatcherServlet -> HandlerMapping -> Controller -> HttpMessageConverter -> HTTP 200 JSON")
                .build(),

            // DATABASE
            Flashcard.builder()
                .category("DATABASE")
                .topic("Indexing")
                .difficulty("HARD")
                .question("What is a Database Composite Index and how does the Leftmost Prefix Rule apply?")
                .answer("A composite index is an index on multiple columns, e.g. INDEX(user_id, status, created_at).\nThe query optimizer can only use the index if the query constraints filter on the leading (leftmost) column.\n- WHERE user_id = 1 AND status = 'ACTIVE' -> Uses full index\n- WHERE user_id = 1 -> Uses index\n- WHERE status = 'ACTIVE' -> CANNOT use index; triggers full table scan!")
                .codeSnippet("CREATE INDEX idx_user_status_date ON orders(user_id, status, created_at);\n-- Valid:\nSELECT * FROM orders WHERE user_id = 10 AND status = 'SHIPPED';")
                .build(),

            Flashcard.builder()
                .category("DATABASE")
                .topic("ACID & Concurrency")
                .difficulty("MEDIUM")
                .question("What is the difference between Phantom Reads and Non-Repeatable Reads?")
                .answer("- Non-Repeatable Read: Transaction A reads row X. Transaction B modifies row X and commits. Transaction A re-reads row X and sees modified values.\n- Phantom Read: Transaction A queries a range of rows (e.g. WHERE price > 50). Transaction B inserts or deletes matching rows and commits. Transaction A re-runs the query and sees new 'phantom' rows.\nPrevented by Serializable isolation or Next-Key locks in InnoDB.")
                .codeSnippet("-- In MySQL InnoDB, REPEATABLE READ prevents phantom reads via Next-Key (gap) locking in MVCC:\nSET TRANSACTION ISOLATION LEVEL REPEATABLE READ;")
                .build(),

            // CS FUNDAMENTALS
            Flashcard.builder()
                .category("CS_FUNDAMENTALS")
                .topic("Networking")
                .difficulty("MEDIUM")
                .question("Explain the TCP 3-Way Handshake and 4-Way Termination sequence.")
                .answer("Handshake (Connection Establishment):\n1. Client -> SYN (Seq=X)\n2. Server -> SYN-ACK (Seq=Y, Ack=X+1)\n3. Client -> ACK (Ack=Y+1)\n\nTermination (Connection Teardown):\n1. Client -> FIN\n2. Server -> ACK\n3. Server -> FIN\n4. Client -> ACK (Client enters TIME_WAIT for 2*MSL to ensure final ACK delivery).")
                .codeSnippet("Client                Server\n  | --- SYN --------->  |  (1)\n  | <--- SYN-ACK -----  |  (2)\n  | --- ACK --------->  |  (3) Established!")
                .build()
        );

        flashcardRepository.saveAll(cards);
    }

    public List<FlashcardDto> getFlashcards(String category, Long userId) {
        List<Flashcard> cards = (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category))
                ? flashcardRepository.findByCategoryIgnoreCase(category)
                : flashcardRepository.findAll();

        Map<Long, UserFlashcardProgress> progressMap = new HashMap<>();
        if (userId != null) {
            progressRepository.findByUserId(userId).forEach(p -> progressMap.put(p.getFlashcard().getId(), p));
        }

        LocalDate today = LocalDate.now();

        return cards.stream().map(c -> {
            UserFlashcardProgress prog = progressMap.get(c.getId());
            boolean due = prog == null || prog.getNextReviewDate() == null || !prog.getNextReviewDate().isAfter(today);

            return new FlashcardDto(
                    c.getId(),
                    c.getCategory(),
                    c.getQuestion(),
                    c.getAnswer(),
                    c.getCodeSnippet(),
                    c.getDifficulty(),
                    c.getTopic(),
                    prog != null ? prog.getEaseFactor() : 2.5,
                    prog != null ? prog.getIntervalDays() : 0,
                    prog != null ? prog.getNextReviewDate() : today,
                    prog != null ? prog.getReviewCount() : 0,
                    due
            );
        }).collect(Collectors.toList());
    }

    public List<FlashcardDto> getDueFlashcards(Long userId) {
        return getFlashcards("ALL", userId).stream()
                .filter(FlashcardDto::due)
                .collect(Collectors.toList());
    }

    @Transactional
    public FlashcardDto submitFlashcardReview(Long flashcardId, Long userId, int rating) {
        User user = userRepository.findById(userId).orElseThrow();
        Flashcard card = flashcardRepository.findById(flashcardId).orElseThrow();

        UserFlashcardProgress progress = progressRepository.findByUserIdAndFlashcardId(userId, flashcardId)
                .orElseGet(() -> UserFlashcardProgress.builder()
                        .user(user)
                        .flashcard(card)
                        .easeFactor(2.5)
                        .intervalDays(1)
                        .reviewCount(0)
                        .build());

        int interval = progress.getIntervalDays();
        double ease = progress.getEaseFactor();

        // SM-2 algorithm calculation
        if (rating <= 2) {
            interval = 1;
            ease = Math.max(1.3, ease - (3 - rating) * 0.15);
        } else {
            if (progress.getReviewCount() == 0) {
                interval = 1;
            } else if (progress.getReviewCount() == 1) {
                interval = 3;
            } else {
                interval = Math.max(1, (int) Math.round(interval * ease));
            }

            if (rating == 4) ease += 0.1;
            else if (rating == 5) ease += 0.2;
            else if (rating == 3) ease -= 0.05;
        }

        ease = Math.round(Math.max(1.3, ease) * 100.0) / 100.0;
        progress.setEaseFactor(ease);
        progress.setIntervalDays(interval);
        progress.setNextReviewDate(LocalDate.now().plusDays(interval));
        progress.setReviewCount(progress.getReviewCount() + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        progressRepository.save(progress);

        // Award XP for review
        user.setTotalXp(user.getTotalXp() + 10);
        levelService.updateLevel(user);
        consistencyService.updateConsistencyScore(user);
        userRepository.save(user);

        return new FlashcardDto(
                card.getId(),
                card.getCategory(),
                card.getQuestion(),
                card.getAnswer(),
                card.getCodeSnippet(),
                card.getDifficulty(),
                card.getTopic(),
                ease,
                interval,
                progress.getNextReviewDate(),
                progress.getReviewCount(),
                false
        );
    }
}
