package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto.InterviewQuestion;

import java.util.List;

public class DailyInterviewQuestionsBank {

    // ==========================================
    // 1. JAVA CORE INTERVIEW QUESTIONS (10 Items)
    // ==========================================
    public static List<InterviewQuestion> getJavaQuestions() {
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
                .codeSnippet("""
// Java 8+ ConcurrentHashMap bucket insertion logic simplified:
Node<K,V> f = tabAt(tab, i = (n - 1) & hash);
if (f == null) {
    if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value)))
        break; // CAS succeeded without acquiring any lock!
} else {
    synchronized (f) { // Locks ONLY the head node of bucket i
        if (tabAt(tab, i) == f) {
            // traverse linked list or TreeBin to update/insert
        }
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In Java 8, `ConcurrentHashMap` achieved a 3x-5x throughput increase over Java 7 by eliminating the coarse `Segment` array (which used 16 reentrant locks). Instead, it adopts a **lock-free CAS + bucket-level synchronized lock** design:
1. When a bucket is empty, insertion uses a single non-blocking CPU CAS (`Compare-And-Swap`) instruction.
2. When a collision occurs, synchronization is locked strictly on the **head node of that specific bucket**, allowing thousands of other threads to read and write to different buckets concurrently.
3. Reads (`get()`) are 100% lock-free because all node values and `next` references are marked `volatile`."

---

### ⚙️ 2. Under-The-Hood Architecture & Internals
- **Collision Treeification**: When a bucket's linked-list depth exceeds `TREEIFY_THRESHOLD = 8` and table capacity >= 64, the bucket is transformed into a balanced Red-Black Tree (`TreeBin`), guaranteeing O(log N) worst-case search time instead of O(N) hash flood DDoS vulnerability.
- **Concurrent Resizing**: When scaling from size `N` to `2N`, threads encountering a `ForwardingNode` (hash = -1) collaborate in parallel via `transferIndex`, copying distinct bucket chunks concurrently without blocking the entire table.

---

### 💻 3. Production Reference Implementation
```java
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentCounterRepository {
    private final ConcurrentHashMap<String, Long> userVisitCount = new ConcurrentHashMap<>();

    // Thread-safe atomic read-modify-write without external locking
    public void incrementVisit(String userId) {
        userVisitCount.compute(userId, (key, currentCount) -> 
            (currentCount == null) ? 1L : currentCount + 1L
        );
    }

    public long getVisits(String userId) {
        return userVisitCount.getOrDefault(userId, 0L); // Completely lock-free read
    }
}
```

---

### 🚀 4. Google Interviewer Follow-Up & Real-World Edge Cases
- **Q: Why does `ConcurrentHashMap` disallow `null` keys and `null` values while `HashMap` allows them?**
  *Answer*: In a concurrent environment, `map.get(key) == null` creates fatal ambiguity: did the key not exist, or was the key explicitly mapped to `null`? In a single-threaded `HashMap`, you can check `map.containsKey(key)`. In a concurrent map, another thread could insert or delete the key between `get()` and `containsKey()`, causing race conditions. Therefore, Doug Lea strictly banned `null`.
""")
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
                .codeSnippet("""
// Enabling Generational ZGC in Java 21+ Production:
// java -XX:+UseZGC -XX:+ZGenerational -Xms16g -Xmx16g -XX:+AlwaysPreTouch -jar app.jar
// Result: Pause times strictly < 1ms across terabyte heaps
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Traditional collectors like G1GC stop application threads (Stop-The-World / STW) to relocate live objects and update memory references, leading to pause times proportional to heap size (20ms - 200ms).
**ZGC** achieves sub-millisecond pauses (< 1ms) even on multi-terabyte heaps because it performs **Concurrent Marking and Concurrent Relocation** while application threads are actively executing.
It accomplishes this via two core innovations: **Colored Pointers** and **JIT Load Barriers**."

---

### ⚙️ 2. Under-The-Hood Architecture: Colored Pointers & Load Barriers
- **Colored Pointers**: On 64-bit platforms, ZGC uses 44 bits for object memory addressing (up to 16 TB heap) and reserves 4 bits in the reference itself for GC metadata flags: `Marked0`, `Marked1`, `Remapped`, and `Finalizable`.
- **Load Barrier (Self-Healing)**: When an application thread executes an object reference read (e.g. `obj.field`), the JIT-compiled load barrier inspects the pointer's color bits. If the pointer has not yet been updated to the relocated address, the thread itself updates the pointer immediately (self-healing) and returns the new location in nanoseconds!

---

### 💻 3. Production Reference Configuration
```bash
# High-Throughput Production JVM Flag Profile (Java 21 LTS):
java -XX:+UseZGC \
     -XX:+ZGenerational \
     -Xms32g -Xmx32g \
     -XX:+AlwaysPreTouch \
     -XX:+UseNUMA \
     -jar microservice.jar
```

---

### 🚀 4. Google Interviewer Follow-Up & Real-World Edge Cases
- **Q: What is an 'Allocation Stall' in ZGC and how do you prevent it?**
  *Answer*: If mutator threads allocate memory faster than ZGC can concurrently reclaim and compact garbage, free memory is exhausted. In this scenario, threads are forced to stall. Solution: Allocate sufficient headroom, set `-XX:ZAllocationSpikeTolerance=5`, and enable Generational ZGC (`-XX:+ZGenerational`) in Java 21 so short-lived objects are collected rapidly in the young generation.
""")
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
                .codeSnippet("""
// Spawning 100,000 concurrent virtual threads with structured executor:
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        final int taskId = i;
        executor.submit(() -> {
            // Blocking I/O unmounts continuation without blocking OS carrier thread!
            String response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return process(response);
        });
    }
} // Auto-awaits completion of all virtual threads
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Platform threads wrap kernel threads 1:1. Each consumes ~1MB of off-heap stack memory and costs ~1-2 microseconds per context switch. A server running 5,000 platform threads runs out of memory or thrashes the OS scheduler.
**Virtual Threads (Project Loom)** are user-mode threads managed by the JVM. They start with a minimal stack footprint of only a few hundred bytes on the JVM heap. The JVM multiplexes millions of virtual threads onto a tiny pool of carrier OS threads (equal to CPU cores). When a virtual thread performs blocking I/O (sockets, DB calls), its continuation is unmounted and parked in heap, freeing the carrier thread to run other tasks immediately."

---

### ⚙️ 2. Carrier Pinning Caveat
When a virtual thread executes inside a `synchronized` block or native JNI call and blocks on I/O, it **pins** the carrier OS thread. To avoid pinning in Java 21+, replace `synchronized` with `ReentrantLock`.

---

### 💻 3. Production Reference Implementation
```java
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.*;

public class VirtualThreadScraper {
    private final HttpClient client = HttpClient.newHttpClient();

    public void processAllUrls(List<String> urls) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String url : urls) {
                executor.submit(() -> {
                    var req = HttpRequest.newBuilder(URI.create(url)).GET().build();
                    var res = client.send(req, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Fetched " + url + " on " + Thread.currentThread());
                });
            }
        }
    }
}
```
""")
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
                .codeSnippet("""
// Thread-Safe Double-Checked Locking Singleton:
public class SafeSingleton {
    // volatile is MANDATORY to prevent instruction reordering!
    private static volatile SafeSingleton instance;

    public static SafeSingleton getInstance() {
        if (instance == null) {
            synchronized (SafeSingleton.class) {
                if (instance == null) {
                    instance = new SafeSingleton(); 
                    // Without volatile: 1. Alloc memory -> 2. Assign ref -> 3. Run constructor
                    // Another thread could see non-null instance before constructor completes!
                }
            }
        }
        return instance;
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Modern CPUs have multiple cores with independent L1/L2 caches and out-of-order execution pipelines. Without explicit memory barriers, writes in Core 1 sit in a store buffer and are invisible to Core 2.
The **Java Memory Model (JMM)** defines formal **happens-before** rules. If action A happens-before action B, the memory effects of A are guaranteed to be visible to B.
Marking a variable `volatile` does two things:
1. Emits CPU memory fences (`StoreStore`, `StoreLoad`) that flush write buffers immediately to L3/RAM.
2. Prevents the compiler and CPU from reordering instructions across the barrier."

---

### 💻 3. Full Production Implementation (Thread-Safe Publication)
```java
public class SafeStatePublisher {
    private int dataA;
    private int dataB;
    private volatile boolean ready = false;

    public void writerThread() {
        dataA = 42;
        dataB = 84;
        ready = true; // Volatile write: flushes dataA and dataB to memory!
    }

    public void readerThread() {
        if (ready) { // Volatile read: invalidates cache, reads latest dataA and dataB!
            assert (dataA == 42 && dataB == 84);
        }
    }
}
```
""")
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
                .codeSnippet("""
// Correct equals and hashCode implementation in standard Java:
public class CustomerId {
    private final String tenant;
    private final long id;

    public CustomerId(String tenant, long id) {
        this.tenant = Objects.requireNonNull(tenant);
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerId that)) return false;
        return id == that.id && tenant.equals(that.tenant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, id); // Uses same fields!
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Overriding `equals()` without `hashCode()` breaks the fundamental contract of Java collections.
When `map.get(key)` is called:
1. It computes `key.hashCode() % bucketCount` to locate the bucket array index.
2. Only after finding the bucket does it call `equals()` along the node chain.
If two logically equal objects produce different hash codes, `get()` searches the wrong bucket, fails to find the entry, and returns `null`! This causes duplicate entries in `HashSet` and severe memory leaks."

---

### 💻 3. Modern Java 16+ Best Practice (Records)
```java
// Java Records automatically generate byte-for-byte compliant equals() & hashCode():
public record UserSession(String sessionId, long userId, String ipAddress) {}
```
""")
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
                .codeSnippet("""
// Breaking parent delegation: Child-First ClassLoader (Tomcat pattern):
public class ChildFirstClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. Check if already loaded in this classloader
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // 2. Try loading in child first (bypassing parent delegation!)
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // 3. Fallback to parent only if child fails
                c = super.loadClass(name, resolve);
            }
        }
        if (resolve) resolveClass(c);
        return c;
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Java's default class loading mechanism follows **Parent Delegation**: a ClassLoader always asks its parent to search first before searching itself. This enforces security and single-definition guarantees (ensuring nobody can replace `java.lang.Object`).
However, application servers (like Apache Tomcat, OSGi, or plugin frameworks) break this model intentionally by implementing **Child-First (Webapp-First) ClassLoading**. This allows two independent web applications to use different versions of the same library (e.g. App A uses Jackson 2.12, App B uses Jackson 2.15) without classpath collisions."
""")
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
                .codeSnippet("""
// PECS demonstrated in standard Java Collections.copy():
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    // src PRODUCES elements of type T (so extends T)
    // dest CONSUMES elements of type T (so super T)
    for (int i = 0; i < src.size(); i++) {
        dest.set(i, src.get(i));
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Due to Type Erasure, `List<String>` and `List<Integer>` compile to the exact same raw `List` in bytecode, with the compiler automatically inserting synthetic cast instructions.
The **PECS rule (Producer Extends, Consumer Super)** governs covariance and contravariance:
- **Producer Extends (`<? extends T>`)**: If your method only READS data from the collection, use `extends`. The collection produces `T`. You cannot add anything (except `null`).
- **Consumer Super (`<? super T>`)**: If your method WRITES data into the collection, use `super`. The collection consumes `T`. You can safely add `T` and its subclasses."
""")
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
                .codeSnippet("""
// Combining parallel async operations with CompletableFuture:
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> userService.getUser(id));
CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> orderService.getOrders(id));

// thenCombine joins two independent futures running concurrently:
CompletableFuture<UserProfileDto> profileFuture = userFuture.thenCombine(ordersFuture, 
    (user, orders) -> new UserProfileDto(user, orders)
).orTimeout(3, TimeUnit.SECONDS)
 .exceptionally(ex -> UserProfileDto.empty());
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In asynchronous reactive programming with `CompletableFuture`:
- `thenApply(T -> U)` is **map**: transforms a completed value synchronously without creating a new async task.
- `thenCompose(T -> CompletableFuture<U>)` is **flatMap**: chains two dependent asynchronous operations sequentially without creating nested `CompletableFuture<CompletableFuture<U>>`.
- `thenCombine(CompletableFuture<U>, (T, U) -> V)` is **fork-join**: kicks off two independent operations concurrently, waiting for both to finish before combining their results with zero blocking."
""")
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
                .codeSnippet("""
// Algebraic Data Types in Modern Java (Java 21):
public sealed interface PaymentStatus permits Pending, Approved, Failed {}

public record Pending(long transactionId, Instant initiatedAt) implements PaymentStatus {}
public record Approved(long transactionId, String authCode) implements PaymentStatus {}
public record Failed(long transactionId, String errorCode) implements PaymentStatus {}

// Pattern Matching Switch with Record Destructuring (No default branch required!):
public String describe(PaymentStatus status) {
    return switch (status) {
        case Pending(var id, var time) -> "Processing tx #" + id + " since " + time;
        case Approved(var id, var code) -> "Success tx #" + id + " auth: " + code;
        case Failed(var id, var err)    -> "Failed tx #" + id + " reason: " + err;
    };
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Modern Java brings **Algebraic Data Types (ADTs)** into the language:
- `Records` provide product types (immutable value data carriers).
- `Sealed Classes` provide sum types (strictly enumerated subclass hierarchies).
When combined with **Pattern Matching for Switch**, the Java compiler enforces exhaustiveness at compile time. If a developer adds a new subclass (e.g. `Refunded`) to the sealed interface, all switch expressions across the codebase immediately fail compilation until the new case is handled, eliminating runtime `UnhandledException` bugs."
""")
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
                .codeSnippet("""
// Using WeakHashMap to prevent ThreadLocal / Key memory leaks:
Map<KeyObject, ExpensiveMetadata> weakCache = new WeakHashMap<>();

KeyObject key = new KeyObject("user-1");
weakCache.put(key, new ExpensiveMetadata());

// Once 'key' has no strong references remaining in the application,
// the garbage collector automatically evicts the entry in the next GC!
key = null;
System.gc(); // weakCache will become empty
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In Java, reference types dictate when the Garbage Collector reclaims an object:
1. **Strong (`Object a = new Object()`)**: Never collected while reachable from GC roots.
2. **SoftReference**: Reclaimed ONLY when heap memory is critically low right before an `OutOfMemoryError`. Ideal for memory-sensitive caches.
3. **WeakReference**: Reclaimed eagerly during the very next GC cycle as soon as all strong references are gone. Used in `WeakHashMap` and `ThreadLocal` map keys.
4. **PhantomReference**: Enqueued into a `ReferenceQueue` after the object has been finalized, enabling safe, off-heap native memory cleanup (e.g. DirectByteBuffers) via the modern `java.lang.ref.Cleaner` API."
""")
                .build()
        );
    }

    // ==========================================
    // 2. SPRING BOOT INTERVIEW QUESTIONS (10 Items)
    // ==========================================
    public static List<InterviewQuestion> getSpringBootQuestions() {
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
                .codeSnippet("""
@Component
public class LifecycleDemonstrationBean implements InitializingBean, DisposableBean {

    @Autowired
    private Environment env; // 1. Dependency Injection

    @PostConstruct
    public void init() {
        // 2. @PostConstruct runs before proxying
        System.out.println("Step 6: @PostConstruct initialized");
    }

    @Override
    public void afterPropertiesSet() {
        // 3. InitializingBean callback
        System.out.println("Step 6: afterPropertiesSet executed");
    }

    @PreDestroy
    public void cleanup() {
        // 4. PreDestroy hook before bean destroyed
        System.out.println("Step 9: @PreDestroy teardown");
    }

    @Override
    public void destroy() {
        System.out.println("Step 9: DisposableBean destroyed");
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"The Spring bean lifecycle progresses through 4 phases: **Creation**, **Awareness**, **Initialization**, and **Destruction**.
The most critical architectural moment occurs in `BeanPostProcessor.postProcessAfterInitialization()`: this is where Spring wraps the raw instantiated bean with a **CGLIB dynamic bytecode proxy** to enable `@Transactional`, `@Async`, `@Cacheable`, and Spring Security authorization. Calling `@Transactional` methods from within `@PostConstruct` will fail to use transactions because the proxy has not yet been wrapped!"
""")
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
                .codeSnippet("""
@Service
public class OrderService {

    // SOLUTION: Self-injection injects the CGLIB proxy rather than 'this'!
    @Autowired
    @Lazy
    private OrderService self;

    public void processOrder() {
        // WRONG: this.createTransaction() -> Bypasses proxy! No transaction!
        // CORRECT: Call via proxy reference:
        self.createTransaction();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void createTransaction() {
        // Enclosed in real DB transaction!
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Spring's declarative `@Transactional` is implemented via **Dynamic Proxies (CGLIB)**. When an external caller invokes an annotated method, the proxy's `TransactionInterceptor` opens a DB transaction connection, binds it to the current `ThreadLocal`, and delegates to the real class.
When method A calls method B in the same class (`this.methodB()`), the invocation executes against the raw internal `this` pointer, completely bypassing the surrounding proxy. Thus, no transaction advice is executed!
To solve this:
1. Extract method B into a separate delegate service (preferred clean architecture).
2. Self-inject the bean using `@Autowired @Lazy private MyService self;`.
3. Switch to AspectJ compile-time/load-time weaving (LTT)."
""")
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
                .codeSnippet("""
// Custom Production Auto-Configuration:
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(name = "custom.datasource.enabled", havingValue = "true", matchIfMissing = true)
public class CustomDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class) // Only creates bean if user has NOT defined one!
    public DataSource defaultDataSource() {
        return new HikariDataSource();
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Spring Boot Auto-Configuration operates on an **Opinionated Condition Evaluation Pipeline**:
1. At startup, `AutoConfigurationImportSelector` reads `org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
2. It parses candidate auto-configuration classes and evaluates `@Conditional` annotations:
   - `@ConditionalOnClass`: Checks if a specific driver/library is present on the classpath.
   - `@ConditionalOnMissingBean`: Verifies whether the developer has already declared a custom `@Bean`.
If conditions pass, Spring automatically wires the bean. The moment a developer defines their own bean, Spring backing off immediately without configuration collision."
""")
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
                .codeSnippet("""
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In Spring Security 6 (Spring Boot 3), the legacy `WebSecurityConfigurerAdapter` was completely retired. Security is configured declaratively via `@Bean SecurityFilterChain`.
Requests enter `DelegatingFilterProxy` in the Servlet container, which passes requests to `FilterChainProxy`. Filter execution order is strictly deterministic:
1. `SecurityContextHolderFilter`: Loads existing authentication from session/store.
2. Custom filters (e.g. `JwtAuthenticationFilter`): Validates token and calls `SecurityContextHolder.getContext().setAuthentication(auth)`.
3. `ExceptionTranslationFilter`: Catches `AccessDeniedException` and sends HTTP 401/403.
4. `AuthorizationFilter`: Evaluates endpoint permissions using `AuthorizationManager`."
""")
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
                .codeSnippet("""
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Solution 1: JPQL JOIN FETCH
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
    List<Order> findAllWithItems(@Param("status") OrderStatus status);

    // Solution 2: Declarative @EntityGraph
    @EntityGraph(attributePaths = {"items", "customer"})
    List<Order> findByCustomerId(Long customerId);

    // Solution 3: DTO Projection (Fastest, zero entity tracking overhead!)
    @Query("SELECT new com.app.dto.OrderSummary(o.id, o.total, c.name) FROM Order o JOIN o.customer c")
    List<OrderSummary> findSummaries();
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"The **N+1 Problem** occurs when a parent entity has a `@OneToMany` or `@ManyToOne` relationship with `FetchType.LAZY`. Loading 100 orders executes 1 initial SQL query to get the orders, followed by 100 separate queries as each order's children are traversed, destroying database performance.
The 4 solutions in order of preference:
1. **DTO Projection**: Best for read-heavy APIs; fetches only selected columns in 1 single JOIN query.
2. **`@EntityGraph`**: Declarative, dynamic join fetching without hardcoding JPQL.
3. **`JOIN FETCH`**: Forces an SQL inner/left join in JPQL.
4. **`@BatchSize(size = 50)`**: Converts 100 individual queries into 2 queries using `WHERE id IN (?, ?, ...)`."
""")
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
                .codeSnippet("""
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.myapp.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", "ERR_404_NOT_FOUND");
        return problem; // Returns standard RFC 7807 JSON
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In Spring Boot 3 / Spring 6, error handling was standardized around **RFC 7807 Problem Details for HTTP APIs**.
Instead of returning ad-hoc error maps or custom DTOs, `@RestControllerAdvice` returns `ProblemDetail`. This guarantees client microservices receive a standardized, machine-readable schema:
`{ "type": "...", "title": "Resource Not Found", "status": 404, "detail": "User id 42 does not exist", "instance": "/api/users/42" }`."
""")
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
                .codeSnippet("""
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // Automatic 30m TTL
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }
}

// In Service layer:
@Service
public class ProductService {
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Product getProduct(Long id) {
        return repository.findById(id).orElse(null);
    }

    @CacheEvict(value = "products", key = "#product.id")
    public void updateProduct(Product product) {
        repository.save(product);
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Spring Cache abstraction provides a vendor-neutral facade using Spring AOP:
- `@Cacheable`: Intercepts calls. If cache key exists, returns cached value immediately. Otherwise executes method and populates cache. Set `sync = true` to prevent cache stampede.
- `@CachePut`: Always executes method and forces a cache write (used for updates).
- `@CacheEvict`: Removes entries upon mutations (`allEntries = true` clears entire namespace).
In production Redis configurations, always configure **JSON serialization** (via `GenericJackson2JsonRedisSerializer`) instead of default Java serialization to ensure human readability and cross-language microservice compatibility."
""")
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
                .codeSnippet("""
// Modern Spring 3.2 Declarative HTTP Interface:
public interface UserApiClient {
    @GetExchange("/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @PostExchange("/users")
    UserDto createUser(@RequestBody CreateUserRequest request);
}

// Registering the Client Proxy:
@Configuration
public class ClientConfig {
    @Bean
    public UserApiClient userApiClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl("https://api.internal.service").build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(UserApiClient.class);
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **RestTemplate**: Legacy synchronous client. 1 request blocks 1 OS thread. In maintenance mode since Spring 5.
- **WebClient**: Reactive, asynchronous, non-blocking client built on Netty event loops. Ideal for high concurrency with minimal threads.
- **RestClient (Spring 6.1 / Boot 3.2)**: The modern, synchronous replacement for `RestTemplate`. Features fluent builder syntax and works seamlessly with Virtual Threads!
- **HTTP Interfaces (`@HttpExchange`)**: Spring's native declarative alternative to OpenFeign, generating HTTP client implementations at runtime backed by either `RestClient` or `WebClient`."
""")
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
                .codeSnippet("""
// MDC Log Pattern showing Trace and Span IDs automatically:
// application.yml:
// logging.pattern.level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"

// In code, recording custom business metrics via Micrometer:
@Service
public class OrderService {
    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("business.orders.placed")
            .tag("tier", "enterprise")
            .description("Total successful orders placed")
            .register(registry);
    }

    public void placeOrder() {
        orderCounter.increment();
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Observability rests on 3 pillars: **Metrics**, **Logs**, and **Traces**.
In Spring Boot 3:
- **Micrometer Metrics**: Exposes endpoints (`/actuator/prometheus`) consumed by Prometheus and Grafana.
- **Micrometer Tracing**: Generates a 64/128-bit `TraceId` per user journey and `SpanId` per network hop. It injects these IDs into HTTP headers using the standard **W3C TraceContext (`traceparent`)** specification and into SLF4J MDC, enabling instant log correlation in Grafana Loki or Datadog."
""")
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
                .codeSnippet("""
-- Flyway migration in src/main/resources/db/migration/V1__init_user_schema.sql
CREATE TABLE users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);

-- application.yml:
-- spring.jpa.hibernate.ddl-auto: validate  <-- NEVER update in prod!
-- spring.flyway.enabled: true
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"`hibernate.ddl-auto=update` in production is a catastrophe waiting to happen:
1. It cannot rename columns (it simply drops the data and creates a new empty column).
2. It cannot run backfill scripts or data transformations.
3. During rolling zero-downtime deployments with 10 pods, multiple pods attempt to alter tables concurrently, causing deadlocks.
**Flyway** solves this with versioned SQL scripts (`V1__...sql`, `V2__...sql`). It acquires an atomic database lock on `flyway_schema_history`, verifies MD5 checksums, and executes migrations sequentially before Spring starts accepting traffic."
""")
                .build()
        );
    }

    // ==========================================
    // 3. DATABASE & SQL INTERVIEW QUESTIONS (10 Items)
    // ==========================================
    public static List<InterviewQuestion> getDatabaseQuestions() {
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
                .codeSnippet("""
-- Composite index leftmost prefix rule:
CREATE INDEX idx_order_customer_date ON orders (customer_id, order_date);

-- OPTIMAL: Uses composite index for filtering AND sorting:
EXPLAIN SELECT * FROM orders WHERE customer_id = 42 AND order_date >= '2026-01-01';

-- INDEX SCAN FAILED: Violates leftmost prefix; full table scan required!
EXPLAIN SELECT * FROM orders WHERE order_date >= '2026-01-01';
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Relational databases (MySQL InnoDB, PostgreSQL) standardize on **B+ Trees** because disk I/O is the ultimate system bottleneck.
- Compared to **B-Trees**: B+ Trees store data records ONLY at leaf nodes. Because internal directory nodes store only keys and child page pointers, a single 16KB disk page can hold over 1,000 keys (fanout > 1000). A 3-level B+ Tree can index 1 billion rows in just 3 disk seeks!
- Compared to **Hash Indexes**: Hash indexes provide O(1) point lookups but fail completely for range queries (`BETWEEN`, `>`, `<`) and sorting. In a B+ Tree, all leaf nodes form a **doubly linked list**, allowing blazing-fast sequential disk scans."
""")
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
                .codeSnippet("""
-- Setting transaction isolation level in PostgreSQL/MySQL:
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

BEGIN;
SELECT balance FROM accounts WHERE user_id = 101; -- Snapshot created!
-- Even if another transaction updates & commits user 101's balance,
-- repeated reads inside this transaction return the EXACT same value!
SELECT balance FROM accounts WHERE user_id = 101; 
COMMIT;
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"The ANSI SQL standard defines 4 isolation levels to combat 3 distinct read anomalies:
1. **Dirty Read**: Reading uncommitted dirty data. Prevented in `READ COMMITTED` and above.
2. **Non-Repeatable Read**: Reading different values for the same row in two queries. Prevented in `REPEATABLE READ`.
3. **Phantom Read**: Re-running a range query returning newly committed inserted/deleted rows.
Under the hood, modern databases implement these using **MVCC (Multi-Version Concurrency Control)**. In MySQL InnoDB Repeatable Read, the engine creates a transaction-level Read View and uses **Next-Key Locking (Record Lock + Gap Lock)** to prevent phantom insertions into index gaps."
""")
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
                .codeSnippet("""
// 1. Optimistic Locking in JPA:
@Entity
public class Inventory {
    @Id private Long id;
    private int stock;
    @Version private Long version; // Managed automatically by Hibernate
}

// 2. Pessimistic Locking in JPA:
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // Generates SELECT ... FOR UPDATE
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long id);
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Optimistic Locking**: Assumes conflicts are rare. Reads without locking. On write, executes `UPDATE ... SET version = version + 1 WHERE id = ? AND version = ?`. If rows affected is 0, throws `OptimisticLockException`. Zero lock contention, great throughput for read-heavy systems.
- **Pessimistic Locking**: Assumes conflicts are frequent. Acquires an exclusive database row lock upfront via `SELECT ... FOR UPDATE`. All other transactions wait. Mandatory for high-contention bank balance transfers or flash sales where retry loops would overwhelm the CPU."
""")
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
                .codeSnippet("""
-- Covering Index optimization:
CREATE INDEX idx_user_email_name ON users (email, full_name);

-- OPTIMAL (Covering Index):
-- MySQL reads directly from secondary index tree without touching primary table!
EXPLAIN SELECT full_name FROM users WHERE email = 'test@google.com';
-- Output: "Using index"
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Clustered Index**: The table ITSELF is the index. Leaf nodes contain the full row data. A table can have only ONE clustered index (InnoDB defaults to the Primary Key).
- **Secondary Index**: An auxiliary B+ tree. Leaf nodes store only the indexed columns + the primary key value.
- **The Double Seek Overhead**: Querying via secondary index searches the secondary tree to retrieve the Primary Key, then performs a second seek on the clustered index to fetch the full row.
- **How to avoid it**: Use a **Covering Index** (`CREATE INDEX idx ON tbl(colA, colB)`). If the `SELECT` and `WHERE` clauses only reference `colA` and `colB`, the database answers the query directly from the secondary index leaf without touching table data pages!"
""")
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
                .codeSnippet("""
-- Intentional Denormalization in high-traffic e-commerce:
ALTER TABLE orders ADD COLUMN cached_customer_name VARCHAR(100);
-- Avoids multi-table JOIN orders -> customers on order history feed (10,000 QPS)
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Normalization eliminates data redundancy and update anomalies:
- **1NF**: Atomic values, no array columns.
- **2NF**: No partial dependencies on composite keys.
- **3NF**: No transitive dependencies (`A -> B -> C`).
- **BCNF**: Every determinant must be a candidate key.
**When to Denormalize**: In large-scale distributed architectures where read-to-write ratios exceed 100:1. Executing 5-table JOINs across sharded databases is impossible or cripplingly slow. We denormalize pre-aggregated counts or replicated names into the parent table, using CDC (Change Data Capture / Debezium) or eventual consistency to keep copies in sync."
""")
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
                .codeSnippet("""
// Consistent Hashing Router Concept:
public class ShardRouter {
    private final TreeMap<Integer, String> ring = new TreeMap<>();

    public void addNode(String nodeIp, int virtualNodes) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = MurmurHash3.hash32(nodeIp + "#" + i);
            ring.put(hash, nodeIp);
        }
    }

    public String routeKey(String shardKey) {
        int hash = MurmurHash3.hash32(shardKey);
        var entry = ring.ceilingEntry(hash);
        return (entry != null) ? entry.getValue() : ring.firstEntry().getValue();
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Partitioning**: Logical split of a table on ONE database server.
- **Sharding**: Physical split of data across MULTIPLE independent database servers (Shared-Nothing).
- **Consistent Hashing**: Traditional `hash(key) % N` causes an operational disaster: adding 1 new server re-hashes almost 100% of all keys, forcing massive data migration. Consistent Hashing places nodes and keys on a virtual ring (0 to 2^32-1). When adding a new shard node, only `K/N` keys are remapped, allowing smooth live cluster scaling without downtime."
""")
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
                .codeSnippet("""
-- Analyzing query plan in PostgreSQL:
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM orders WHERE customer_id = 42 ORDER BY created_at DESC LIMIT 10;

-- What to watch for in the output:
-- 1. "Index Scan Backward using idx_orders_customer_created" -> Excellent!
-- 2. "Buffers: shared hit=4" -> Data read directly from RAM buffer cache.
-- 3. "Seq Scan on orders" -> BAD! Missing index causing full table scan!
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"When profiling slow queries, never rely solely on `EXPLAIN`: use `EXPLAIN ANALYZE` because it executes the SQL and reports real runtime numbers.
Key checklist:
1. **Access Type**: Look for `const`, `eq_ref`, `ref`, or `range`. Eliminate `ALL` (full table scan) on large tables.
2. **Buffer Hits**: Check `shared hit` vs `shared read`. If read is high, queries are stalling on physical disk reads.
3. **Cardianality Discrepancy**: If `estimated rows` is 10 but `actual rows` is 100,000, table statistics are stale; execute `ANALYZE table_name;` immediately to refresh optimizer statistics."
""")
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
                .codeSnippet("""
// Cassandra tunable consistency achieving strong consistency (R + W > N):
// Replication Factor N = 3
// Write Consistency = QUORUM (2)
// Read Consistency  = QUORUM (2)
// Since 2 + 2 > 3, guaranteed to read the latest write!
Statement stmt = SimpleStatement.newInstance("SELECT * FROM users WHERE id = ?")
    .setConsistencyLevel(DefaultConsistencyLevel.QUORUM);
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"In distributed systems, network partitions (P) are unavoidable physical realities (cut cables, switch failures). Therefore, the choice is never CA; it is strictly between **CP (Consistency)** or **AP (Availability)** during a partition.
The **PACELC Theorem** extends CAP by describing behavior during normal non-partitioned times:
- **If Partition (P)**: Trade off Availability (A) vs Consistency (C).
- **Else (E)**: Trade off Latency (L) vs Consistency (C).
*Examples*:
- **Cassandra**: `PA/EL` (Available during partitions; chooses Low Latency over Consistency normally).
- **Google Spanner**: `PC/EC` (Strict Consistency during partitions; achieves low latency through TrueTime atomic clocks)."
""")
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
                .codeSnippet("""
-- MySQL InnoDB Redo Log Buffer tuning:
-- innodb_flush_log_at_trx_commit = 1 (Full ACID durability on every commit)
-- innodb_flush_log_at_trx_commit = 2 (Flushes to OS buffer, fsync once per second - 10x throughput!)
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Random disk writes are slow because they require disk head seeks or SSD block read-modify-write cycles.
**Write-Ahead Logging (WAL)** achieves high write throughput and ACID Durability:
1. When a transaction commits, modifications are applied in-memory to the **Buffer Pool** (marking pages as dirty).
2. The delta change is sequentially appended to the **Write-Ahead Log (Redo Log)** on disk via an ultra-fast sequential `fsync()`.
3. The transaction returns success to the client immediately!
4. Background threads asynchronously **checkpoint** (flush) dirty buffer pool pages to random table disk locations. If the server crashes, the database replays WAL records from the last checkpoint to recover all committed data."
""")
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
                .codeSnippet("""
// Application-Level Deadlock Prevention & Recovery:
// 1. Enforce strict lock ordering:
long firstId = Math.min(fromAccountId, toAccountId);
long secondId = Math.max(fromAccountId, toAccountId);

// Always lock lower ID first across all threads!
accountRepo.lockForUpdate(firstId);
accountRepo.lockForUpdate(secondId);

// 2. Automated Spring Retry on transient deadlock:
@Retryable(
    retryFor = { CannotAcquireLockException.class, DeadlockLoserDataAccessException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2.0)
)
@Transactional
public void transferMoney(Long from, Long to, BigDecimal amount) { /* ... */ }
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"A deadlock is a cycle in the database lock Wait-For Graph: Transaction 1 holds Lock A and waits for Lock B; Transaction 2 holds Lock B and waits for Lock A.
Database engines run background deadlock detectors. When a cycle is found, the engine terminates the **deadlock victim** (the transaction with fewest undo logs) with an error.
**The 3 Golden Rules to Prevent Deadlocks**:
1. **Deterministic Lock Ordering**: If updating two rows, always sort IDs in ascending order before acquiring locks.
2. **Keep Transactions Short**: Never perform external HTTP calls or heavy computations inside a database transaction.
3. **Automated Exponential Backoff Retry**: Application code must treat deadlocks as transient concurrency events and retry automatically."
""")
                .build()
        );
    }

    // ==========================================
    // 4. CS SUBJECTS INTERVIEW QUESTIONS (10 Items)
    // ==========================================
    public static List<InterviewQuestion> getCsQuestions() {
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
                .codeSnippet("""
// C demonstration of Process (fork) vs Thread (pthread):
// 1. Process fork: creates separate copy-on-write memory address space
pid_t pid = fork();
if (pid == 0) {
    // Child process: modifying globalVar does NOT affect parent!
    globalVar = 100;
}

// 2. Thread creation: shares exact same memory address space
pthread_t thread;
pthread_create(&thread, NULL, worker_function, NULL);
// Modifying heap/global variables in worker_function IS immediately visible to main!
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Process**: An instance of a program in execution with an isolated virtual address space (Text, Data, BSS, Heap, Stack). Processes communicate via IPC (Pipes, Sockets, Shared Memory). Context switching is slow because the OS must flush the CPU's **Translation Lookaside Buffer (TLB)**.
- **Thread**: The unit of execution within a process. All threads in a process share the same Virtual Address Space, Heap, Global Variables, and File Descriptors.
- **Private to each thread**: Stack (local variables, function calls), Program Counter (PC), and CPU Registers."
""")
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
                .codeSnippet("""
// Breaking Circular Wait via Hierarchical Lock Ordering in Java:
public void transfer(Account from, Account to, double amount) {
    Account firstLock = from.getId() < to.getId() ? from : to;
    Account secondLock = from.getId() < to.getId() ? to : from;

    synchronized (firstLock) {
        synchronized (secondLock) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"A deadlock occurs if and only if all **4 Coffman Conditions** hold:
1. **Mutual Exclusion**: Exclusive access to a resource.
2. **Hold and Wait**: Holding a resource while waiting for another.
3. **No Preemption**: Resources cannot be confiscated.
4. **Circular Wait**: `P0 -> P1 -> ... -> Pn -> P0`.
In real systems, breaking **Circular Wait** is the gold standard: impose a strict global numbering order on all resources. If every thread must acquire Lock X before Lock Y whenever `X < Y`, a circular dependency graph is mathematically impossible!"
""")
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
                .codeSnippet("""
# Linux command to inspect page faults and memory layout:
# Minor faults (page in memory but not mapped) vs Major faults (disk I/O required):
ps -o min_flt,maj_flt,cmd -p <PID>

# Inspect huge page configuration (used in DBs to reduce TLB misses):
cat /proc/meminfo | grep -i hugepages
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"Virtual Memory gives each process the illusion of a vast, contiguous address space while providing hardware-enforced memory isolation.
1. Memory is chunked into 4KB **Pages**.
2. When the CPU executes an instruction, the **MMU** translates the virtual address using the **TLB (Translation Lookaside Buffer)** cache.
3. If TLB hits: address is resolved in 1 nanosecond!
4. If TLB misses: MMU walks the multi-level page table.
5. If the valid bit is 0, a **Page Fault** hardware interrupt fires. The kernel pauses the process, fetches the 4KB page from disk swap into RAM, updates the page table, and resumes execution."
""")
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
                .codeSnippet("""
// Java Semaphore regulating access to a connection pool:
public class DatabaseConnectionPool {
    private final Semaphore poolPermits = new Semaphore(10); // Max 10 concurrent users

    public void executeQuery() throws InterruptedException {
        poolPermits.acquire(); // Blocks if 10 threads currently executing
        try {
            runDatabaseQuery();
        } finally {
            poolPermits.release(); // Releases permit back to pool
        }
    }
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Mutex**: An ownership lock. The thread that locked it MUST be the thread that unlocks it. Ideal for critical section mutual exclusion. Supports recursive locking and priority inheritance.
- **Semaphore**: A signaling mechanism. It maintains a permit counter. Does NOT have ownership: Thread 1 can call `acquire()` and Thread 2 can call `release()`. Used for producer-consumer signaling and throttling access to finite pools of resources."
""")
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
                .codeSnippet("""
# Check number of active TCP connections in TIME_WAIT state:
ss -s
# or:
netstat -nat | grep TIME_WAIT | wc -l

# Linux TCP kernel tuning for high-throughput servers:
# sysctl -w net.ipv4.tcp_tw_reuse=1
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"TCP guarantees reliable, ordered byte streams:
- **3-Way Handshake**: Client sends `SYN(Seq=X)`. Server responds `SYN(Seq=Y), ACK(X+1)`. Client responds `ACK(Y+1)`. Both sides establish initial sequence numbers.
- **4-Way Teardown**: TCP is full-duplex. Client sends `FIN`; Server responds `ACK`. Once server finishes its remaining transmissions, Server sends `FIN`; Client responds `ACK` and enters **TIME_WAIT**.
- **Why TIME_WAIT lasts 2MSL (~60s)**:
  1. If the final ACK is dropped by the network, the server retransmits `FIN`. TIME_WAIT ensures the client can re-send the ACK instead of sending an `RST` error.
  2. Lingering duplicate packets from the connection drain out of routers so they cannot corrupt a future socket using the same 4-tuple `(src_ip, src_port, dst_ip, dst_port)`."
""")
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
                .codeSnippet("""
# Testing HTTP/2 and HTTP/3 ALPN negotiation with curl:
curl -I --http2 https://google.com
curl -I --http3 https://cloudflare.com

# HTTP/3 QUIC Stack:
# [ Application: HTTP/3 ]
# [ Security & Transport: QUIC (Built-in TLS 1.3 encryption) ]
# [ Network: UDP ]
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **HTTP/1.1**: Plain text. Suffered from application-layer Head-of-Line blocking (1 request per TCP connection at a time, requiring browser domain-sharding hacks).
- **HTTP/2**: Binary protocol with **Multiplexing** (hundreds of parallel streams interleaved over a single TCP connection) + **HPACK** header compression. However, packet drop at the TCP layer causes OS-level HoL blocking across all streams!
- **HTTP/3 (QUIC)**: Replaces TCP with **QUIC on top of UDP**. Solves transport HoL blocking: dropped packets on Stream 1 do NOT block Stream 2. Integrates TLS 1.3 handshake into transport setup for **0-RTT connection resumption**, and supports **Connection Migration** when switching from WiFi to Cellular without dropping sockets!"
""")
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
                .codeSnippet("""
# Tracing recursive DNS hierarchy resolution step-by-step:
dig +trace google.com

# Inspecting TLS 1.3 handshake certificates:
openssl s_client -connect google.com:443 -tls1_3
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"This journey spans 5 layers:
1. **Browser**: Parses URL, checks HSTS cache, checks browser/OS DNS cache.
2. **DNS Resolution**: Local Resolver queries Root Nameserver (`.`), TLD Nameserver (`.com`), and Authoritative Nameserver (`ns1.google.com`), returning the IP.
3. **Transport & Security**: TCP 3-Way Handshake (SYN -> SYN-ACK -> ACK) followed by TLS 1.3 ECDHE key exchange to negotiate symmetric AES-256 session keys in 1 RTT.
4. **Backend Network**: Request enters Anycast Edge CDN / Load Balancer, gets forwarded to microservice pods.
5. **Browser Rendering**: Browser parses HTML into DOM, CSS into CSSOM, combines them into the **Render Tree**, computes **Layout** coordinates, and paints pixels on screen."
""")
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
                .codeSnippet("""
// Demonstrating AES-256-GCM Symmetric Encryption in Java:
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
SecretKey key = new SecretKeySpec(aesKeyBytes, "AES");
GCMParameterSpec spec = new GCMParameterSpec(128, iv);

cipher.init(Cipher.ENCRYPT_MODE, key, spec);
byte[] cipherText = cipher.doFinal(plainTextBytes);
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Asymmetric Encryption (RSA, ECC)**: Public key encrypts, private key decrypts. Computationally heavy. Used ONLY during the initial TLS handshake to authenticate identity and establish a shared secret.
- **Symmetric Encryption (AES-GCM, ChaCha20)**: Same key encrypts and decrypts. Hardware accelerated on modern CPUs (AES-NI instructions). Used for 100% of all bulk HTTP data payload transfer.
- **TLS 1.3 vs TLS 1.2**: TLS 1.3 eliminated legacy static RSA key exchange in favor of **ECDHE (Elliptic Curve Diffie-Hellman Ephemeral)**. This guarantees **Perfect Forward Secrecy (PFS)**: even if an attacker steals the server's private master key 5 years from now, they cannot decrypt recorded historical traffic!"
""")
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
                .codeSnippet("""
# Shell demonstration of Inodes and Links:
touch original.txt
ln original.txt hardlink.txt     # Hard link -> shares same Inode!
ln -s original.txt symlink.txt   # Soft link -> new Inode pointing to path string

ls -li original.txt hardlink.txt symlink.txt
# Output:
# 142857 -rw-r--r-- 2 user user 0 original.txt
# 142857 -rw-r--r-- 2 user user 0 hardlink.txt (same inode 142857, link count = 2)
# 998811 lrwxrwxrwx 1 user user 12 symlink.txt -> original.txt (new inode 998811)
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"An **Inode** is the fundamental data structure of POSIX file systems. It stores file size, permissions, ownership, timestamps, and direct/indirect disk block pointers. Noticeably, it does NOT store the filename! A directory is simply a file containing a lookup map of `(filename -> Inode number)`.
- **Hard Link (`ln target link`)**: Creates a second directory entry pointing to the **exact same Inode**. The inode's reference count increments. Deleting one name does not delete the data until the link count reaches 0. Cannot span different disks/partitions.
- **Soft Link (`ln -s target link`)**: A standalone file with its **own Inode** whose content is simply the string path to the target file. If the target file is removed, the symlink becomes a dangling pointer."
""")
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
                .codeSnippet("""
// Spring Boot SSE Emitter for LLM Token Streaming:
@GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamAiResponse(@RequestParam String prompt) {
    SseEmitter emitter = new SseEmitter(60_000L);
    aiService.streamTokens(prompt, token -> {
        try {
            emitter.send(SseEmitter.event().data(token));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }, emitter::complete);
    return emitter;
}
""")
                .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (60-Second Answer)
"- **Long Polling**: Emulates push via repeated client HTTP requests. High TCP connection churn and header overhead. Use only as legacy fallback.
- **Server-Sent Events (SSE)**: Unidirectional streaming from Server to Client over standard HTTP (`text/event-stream`). Built-in automatic browser reconnection, event IDs, works across standard HTTP/2 proxies without WebSocket upgrade issues. The modern industry standard for **LLM token streaming (ChatGPT)** and live financial tickers.
- **WebSockets**: Full-duplex, bidirectional TCP connection initiated via HTTP `101 Switching Protocols`. Framed with tiny 2-byte overhead. Essential for **real-time bidirectional applications** like multiplayer games, collaborative documents (Google Docs), and bidirectional chat."
""")
                .build()
        );
    }
}
