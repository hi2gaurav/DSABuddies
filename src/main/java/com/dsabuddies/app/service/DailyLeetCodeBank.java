package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.DailyContentDto.LeetCodeProblem;

import java.util.List;

public class DailyLeetCodeBank {

    public static List<LeetCodeProblem> getAllProblems() {
        return List.of(
            getLruCache(),
            getTrappingRainWater(),
            getCourseSchedule(),
            getMinimumWindowSubstring(),
            getLongestIncreasingSubsequence(),
            getFindMedianDataStream(),
            getNumberOfIslands()
        );
    }

    private static LeetCodeProblem getLruCache() {
        return LeetCodeProblem.builder()
            .id("LC-146")
            .title("LRU Cache")
            .difficulty("MEDIUM")
            .topic("Hash Table & Doubly Linked List")
            .url("https://leetcode.com/problems/lru-cache/")
            .companies(List.of("Google", "Amazon", "Microsoft", "Apple", "Bloomberg", "Meta"))
            .problemSummary("Design a data structure that follows the constraints of a Least Recently Used (LRU) cache with O(1) time complexity for both get and put operations.")
            .optimalApproach("Combine a HashMap<Integer, Node> for O(1) key lookups with a Doubly Linked List with dummy head and tail. On get/update, splice the node and move it directly to head. On eviction, remove node right before dummy tail.")
            .timeComplexity("O(1) strictly for both get() and put()")
            .spaceComplexity("O(capacity) to store at most capacity nodes in HashMap and Doubly Linked List")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"To achieve O(1) time for both lookup (`get`) and insertion/eviction (`put`), we need two properties:
1. Instant key lookup — which demands a Hash Map.
2. Fast element reordering and tail eviction in O(1) without shifting elements — which demands a Doubly Linked List.
A singly linked list cannot delete a node in O(1) because deleting requires updating the preceding node's pointer. With a Doubly Linked List and sentinel dummy `head` and `tail` nodes, we eliminate edge cases around empty lists and single-element updates."

---

### 💡 2. Intuition & Transition from Brute Force
- **Naive Array/Queue**: Shifting elements on access takes O(N) time.
- **Java `LinkedHashMap`**: Under the hood, `LinkedHashMap(capacity, 0.75f, true)` provides this behavior, but Google interviewers specifically require implementing the data structures from scratch to test pointer manipulation, memory locality, and concurrency awareness.

---

### 💻 3. Full Production-Grade Java Implementation
```java
import java.util.HashMap;
import java.util.Map;

public class LRUCache {

    // Doubly linked list node
    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head; // Dummy head (most recently used sentinel)
    private final Node tail; // Dummy tail (least recently used sentinel)

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.cache = new HashMap<>(capacity);

        // Sentinel nodes to avoid null checks on list boundaries
        this.head = new Node(-1, -1);
        this.tail = new Node(-1, -1);
        this.head.next = this.tail;
        this.tail.prev = this.head;
    }

    public int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1;
        }
        // Mark accessed node as Most Recently Used
        moveToHead(node);
        return node.value;
    }

    public void put(int key, int value) {
        Node existingNode = cache.get(key);

        if (existingNode != null) {
            // Key exists: update value and move to MRU position
            existingNode.value = value;
            moveToHead(existingNode);
            return;
        }

        // Check if eviction is required before adding new node
        if (cache.size() >= capacity) {
            Node lruNode = removeTail();
            cache.remove(lruNode.key);
        }

        Node newNode = new Node(key, value);
        cache.put(key, newNode);
        addToHead(newNode);
    }

    // --- Internal Doubly Linked List Pointer Operations (All O(1)) ---

    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
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

    private Node removeTail() {
        Node lru = tail.prev;
        removeNode(lru);
        return lru;
    }
}
```

---

### 📐 4. Step-by-Step Dry Run Trace
Suppose `capacity = 2`:
1. `put(1, 10)`: List: `[Head] <-> [1:10] <-> [Tail]`. Map: `{1: Node(1)}`.
2. `put(2, 20)`: List: `[Head] <-> [2:20] <-> [1:10] <-> [Tail]`. Map: `{1, 2}`.
3. `get(1)`: Returns `10`. Splices `[1:10]` to head. List: `[Head] <-> [1:10] <-> [2:20] <-> [Tail]`.
4. `put(3, 30)`: Capacity full (2). Evicts tail node `[2:20]`. Inserts `[3:30]`. List: `[Head] <-> [3:30] <-> [1:10] <-> [Tail]`. Map: `{1, 3}`.
5. `get(2)`: Returns `-1` (successfully evicted).

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(1)** for both `get(key)` and `put(key, value)`. `HashMap` lookups, node splices, and pointer updates are constant time.
- **Space Complexity**: **O(C)** where `C = capacity`. Maximum `C` entries in `HashMap` and `C + 2` nodes in Doubly Linked List.

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: How would you make this thread-safe under high concurrent read/write traffic?**
  *Candidate Answer*: A single `ReentrantLock` or `synchronized` block serializes all requests, creating a bottleneck. Instead, use a `ReentrantReadWriteLock` for reads, or strip locks across shards (like Java's `ConcurrentHashMap`). Even better, Caffeine Cache uses an asynchronous ring-buffer (actor model) where reads record access events into a ring buffer and a single background thread batches node promotions.
- **Q: What if the key size or value size is variable and we evict based on byte size rather than count?**
  *Candidate Answer*: Track current `usedBytes` and compare against `maxBytes`. When adding an element, evict LRU nodes in a loop until `usedBytes + newBytes <= maxBytes`.
""")
            .build();
    }

    private static LeetCodeProblem getTrappingRainWater() {
        return LeetCodeProblem.builder()
            .id("LC-42")
            .title("Trapping Rain Water")
            .difficulty("HARD")
            .topic("Two Pointers / Monotonic Stack")
            .url("https://leetcode.com/problems/trapping-rain-water/")
            .companies(List.of("Google", "Amazon", "Meta", "Microsoft", "Goldman Sachs", "Uber"))
            .problemSummary("Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.")
            .optimalApproach("Use Two Pointers (left = 0, right = n-1) tracking leftMax and rightMax. Water trapped at any index is bounded by min(leftMax, rightMax) - height[i]. Move the pointer with the smaller max bar inwards.")
            .timeComplexity("O(N) single pass")
            .spaceComplexity("O(1) auxiliary space")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"Water trapped above any bar `i` is determined entirely by the formula:
`water[i] = max(0, min(max_left_height, max_right_height) - height[i])`.
Rather than precomputing prefix and suffix max arrays in O(N) space, we can eliminate all auxiliary memory using **Two Pointers**. Since the trapped water is strictly bounded by the *shorter* of the two bounding walls, we maintain `leftMax` and `rightMax` while moving the smaller pointer inward. The smaller wall guarantees that even if a taller wall exists further inside, the current boundary is the true bottleneck."

---

### 💡 2. Intuition & Transition from Brute Force
- **Brute Force (O(N^2) time, O(1) space)**: For each bar, scan left to find `maxLeft` and right to find `maxRight`.
- **Dynamic Programming (O(N) time, O(N) space)**: Precompute `leftMax[i]` array and `rightMax[i]` array.
- **Optimal Two Pointers (O(N) time, O(1) space)**: As long as `leftMax < rightMax`, the water level above bar `left` depends only on `leftMax`, regardless of whatever lies between `left` and `right`!

---

### 💻 3. Full Production-Grade Java Implementation
```java
public class TrappingRainWater {

    public int trap(int[] height) {
        if (height == null || height.length < 3) {
            return 0; // Cannot trap water with less than 3 bars
        }

        int left = 0;
        int right = height.length - 1;
        int leftMax = 0;
        int rightMax = 0;
        int totalTrappedWater = 0;

        while (left < right) {
            if (height[left] <= height[right]) {
                // The left wall is the bottleneck
                if (height[left] >= leftMax) {
                    leftMax = height[left]; // New boundary reached
                } else {
                    totalTrappedWater += (leftMax - height[left]);
                }
                left++;
            } else {
                // The right wall is the bottleneck
                if (height[right] >= rightMax) {
                    rightMax = height[right]; // New boundary reached
                } else {
                    totalTrappedWater += (rightMax - height[right]);
                }
                right--;
            }
        }

        return totalTrappedWater;
    }
}
```

---

### 📐 4. Step-by-Step Dry Run Trace
Input: `height = [0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]`
- `left = 0, right = 11`: `h[0]=0 <= h[11]=1` -> `leftMax=0` -> `left=1`
- `left = 1, right = 11`: `h[1]=1 <= h[11]=1` -> `leftMax=1` -> `left=2`
- `left = 2, right = 11`: `h[2]=0 <= h[11]=1` -> `water += 1 - 0 = 1` -> `left=3`
- `left = 3, right = 11`: `h[3]=2 > h[11]=1`  -> `rightMax=1` -> `right=10`
- `left = 3, right = 10`: `h[10]=2 <= h[3]=2` -> `rightMax=2` -> `right=9`
- Continuing inwards yields: Total Trapped Water = `6`.

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(N)**. Every bar is visited exactly once as `left` and `right` converge.
- **Space Complexity**: **O(1)**. Only 5 scalar primitive variables (`left`, `right`, `leftMax`, `rightMax`, `totalTrappedWater`).

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: Can you solve this using a Monotonic Stack instead?**
  *Candidate Answer*: Yes. Maintain a monotonic decreasing stack of bar indices. When encountering a bar taller than stack top, pop the top (the bottom of the basin). The new stack top is the left boundary, and the current bar is the right boundary. Trapped water is `(min(h[left], h[right]) - h[bottom]) * (right - left - 1)`. Stack is O(N) space, which is why Two Pointers is strictly superior in interviews.
- **Q: What if this is Trapping Rain Water II (3D Grid on m x n matrix)?**
  *Candidate Answer*: In 3D, water spills out of the lowest boundary cell. We push all perimeter cells into a Min-Heap (Priority Queue) and use a BFS traversal. We pop the lowest height cell, examine its 4 neighbors, accumulate water if neighbor is shorter, update neighbor height to `max(neighbor, current)`, and push neighbor into heap. Runs in `O(M * N log(M * N))`.
""")
            .build();
    }

    private static LeetCodeProblem getCourseSchedule() {
        return LeetCodeProblem.builder()
            .id("LC-207")
            .title("Course Schedule")
            .difficulty("MEDIUM")
            .topic("Graph / Topological Sort / Kahn's Algorithm")
            .url("https://leetcode.com/problems/course-schedule/")
            .companies(List.of("Google", "Amazon", "Meta", "Microsoft", "Twitter", "Uber"))
            .problemSummary("There are numCourses you have to take, labeled from 0 to numCourses - 1. Given prerequisites array, determine if it is possible to finish all courses.")
            .optimalApproach("Model as a Directed Graph where courses are nodes. Detect cycles using Kahn's Algorithm (BFS with in-degree array) or DFS with 3 states (UNVISITED, VISITING, VISITED). If cycle exists, return false.")
            .timeComplexity("O(V + E) where V = numCourses, E = prerequisites.length")
            .spaceComplexity("O(V + E) for adjacency list and BFS queue/in-degree array")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"This is a textbook **Cycle Detection in a Directed Graph** problem. If there is a cycle (e.g., Course A requires B, and B requires A), completing all courses is impossible.
The two standard ways to solve this are:
1. **Kahn's Algorithm (BFS with In-Degree array)**: Start with nodes having 0 prerequisites (in-degree 0), remove them from the graph, and decrement their neighbors' in-degree.
2. **DFS with 3 Coloring**: 0 = Unvisited, 1 = Visiting (in current recursion stack), 2 = Visited.
In production, Kahn's algorithm is preferred because it avoids deep recursion stack overflow for large graphs and naturally produces the valid course order."

---

### 💡 2. Intuition & Transition from Brute Force
- Any course with `inDegree == 0` has no prerequisites and can be immediately taken.
- When course `u` is taken, we decrement `inDegree[v]` for all dependent courses `v`.
- If a course's in-degree drops to 0, it is ready to be taken and gets added to the queue.
- If total courses taken equals `numCourses`, no cycles exist; otherwise, deadlocked cycles remain.

---

### 💻 3. Full Production-Grade Java Implementation
```java
import java.util.*;

public class CourseSchedule {

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        if (numCourses <= 1 || prerequisites == null || prerequisites.length == 0) {
            return true;
        }

        // 1. Build Adjacency List and In-Degree Array
        List<List<Integer>> adj = new ArrayList<>(numCourses);
        for (int i = 0; i < numCourses; i++) {
            adj.add(new ArrayList<>());
        }

        int[] inDegree = new int[numCourses];
        for (int[] edge : prerequisites) {
            int course = edge[0];
            int prereq = edge[1];
            adj.get(prereq).add(course); // prereq -> course
            inDegree[course]++;
        }

        // 2. Enqueue all courses with zero prerequisites
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        // 3. Process courses via BFS
        int processedCourses = 0;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            processedCourses++;

            for (int neighbor : adj.get(current)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 4. If processed count matches total courses, topological sort succeeded
        return processedCourses == numCourses;
    }
}
```

---

### 📐 4. Step-by-Step Dry Run Trace
Suppose `numCourses = 4`, `prereqs = [[1,0], [2,0], [3,1], [3,2]]`:
- Graph: `0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3`
- `inDegree`: `[0:0, 1:1, 2:1, 3:2]`
- Initial Queue: `[0]`
- Poll `0`: `processed=1`. Neighbors 1 & 2 in-degree becomes 0. Queue: `[1, 2]`.
- Poll `1`: `processed=2`. Neighbor 3 in-degree becomes 1. Queue: `[2]`.
- Poll `2`: `processed=3`. Neighbor 3 in-degree becomes 0. Queue: `[3]`.
- Poll `3`: `processed=4`. Queue empty.
- Result: `processed (4) == numCourses (4)` -> `true`!

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(V + E)** where `V = numCourses` and `E = prerequisites.length`. Building the graph takes O(E), and each vertex and edge is traversed exactly once.
- **Space Complexity**: **O(V + E)** for the adjacency list `adj` and BFS queue.

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: How would you return the actual course ordering (Course Schedule II)?**
  *Candidate Answer*: Collect the polled elements in an array `int[] order = new int[numCourses]`. If `processedCourses == numCourses`, return `order`; else return `new int[0]`.
- **Q: How would you parallelize this if courses can be taken concurrently across semesters?**
  *Candidate Answer*: Process courses in BFS "levels" (layers). Each queue level represents courses that can be taken in parallel in the same semester. Minimum semesters required equals BFS depth.
""")
            .build();
    }

    private static LeetCodeProblem getMinimumWindowSubstring() {
        return LeetCodeProblem.builder()
            .id("LC-76")
            .title("Minimum Window Substring")
            .difficulty("HARD")
            .topic("Sliding Window / Hash Map")
            .url("https://leetcode.com/problems/minimum-window-substring/")
            .companies(List.of("Google", "Meta", "Amazon", "Microsoft", "LinkedIn", "Airbnb"))
            .problemSummary("Given two strings s and t, return the minimum window substring of s such that every character in t (including duplicates) is included in the window.")
            .optimalApproach("Maintain a frequency map of string t and a sliding window [left, right] in s. Expand right until all characters in t are satisfied (matchedCount == required). Then contract left to find the minimum valid window, updating smallest length recorded.")
            .timeComplexity("O(M + N) where M = s.length(), N = t.length()")
            .spaceComplexity("O(1) auxiliary space (fixed ASCII array of size 128)")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"This is an optimal **Variable-Size Sliding Window** problem.
We maintain two pointers `[left, right]`. We expand `right` until our current window contains all required characters with their required frequencies from `t`. Once the window is valid, we contract `left` to strip unnecessary characters from the left, updating our global minimum window length. We repeat until `right` reaches the end of `s`."

---

### 💡 2. Intuition & Transition from Brute Force
- **Naive Check (O(M^2 * N))**: Checking all O(M^2) substrings and counting character frequencies takes cubic time.
- **Sliding Window with Frequency Array (O(M + N))**: Using fixed-size ASCII arrays `int[128]` eliminates HashMap overhead and gives cache-friendly O(1) character updates. We track `matchedChars` so we never scan the frequency map in O(26) during each step.

---

### 💻 3. Full Production-Grade Java Implementation
```java
public class MinimumWindowSubstring {

    public String minWindow(String s, String t) {
        if (s == null || t == null || s.length() < t.length()) {
            return "";
        }

        // 1. Frequency map of target string t
        int[] targetCount = new int[128];
        int requiredDistinct = 0;

        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (targetCount[c] == 0) {
                requiredDistinct++;
            }
            targetCount[c]++;
        }

        // 2. Sliding window state
        int[] windowCount = new int[128];
        int matchedDistinct = 0;
        int left = 0;
        int minLen = Integer.MAX_VALUE;
        int minStart = 0;

        // 3. Expand right pointer
        for (int right = 0; right < s.length(); right++) {
            char rChar = s.charAt(right);
            windowCount[rChar]++;

            if (targetCount[rChar] > 0 && windowCount[rChar] == targetCount[rChar]) {
                matchedDistinct++;
            }

            // 4. Contract left pointer while window satisfies all requirements
            while (matchedDistinct == requiredDistinct) {
                int currentWindowLen = right - left + 1;
                if (currentWindowLen < minLen) {
                    minLen = currentWindowLen;
                    minStart = left;
                }

                char lChar = s.charAt(left);
                windowCount[lChar]--;
                if (targetCount[lChar] > 0 && windowCount[lChar] < targetCount[lChar]) {
                    matchedDistinct--; // Window is no longer valid
                }
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? "" : s.substring(minStart, minStart + minLen);
    }
}
```

---

### 📐 4. Step-by-Step Dry Run Trace
Suppose `s = "ADOBECODEBANC"`, `t = "ABC"`:
- Required: `A:1, B:1, C:1` (`requiredDistinct = 3`).
- Expand `right` to index 5 (`"ADOBEC"`): contains A, B, C! Valid window length = 6.
- Contract `left`: remove `A`, now invalid.
- Expand `right` to index 10 (`"CODEBA"`): valid window length = 6.
- Expand `right` to index 12 (`"BANC"`): contains B, A, C! Window length = 4.
- Return `"BANC"`.

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(M + N)** where `M = s.length()` and `N = t.length()`. Target map build takes O(N). Each character in `s` is visited at most twice (once by `right`, once by `left`).
- **Space Complexity**: **O(1)**. Fixed-size arrays of length 128 (ASCII set) take constant memory.

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: What if the characters include arbitrary Unicode or Chinese characters (UTF-16)?**
  *Candidate Answer*: Fixed `int[128]` would overflow. Replace with `HashMap<Character, Integer>` or `Int2IntOpenHashMap` (fastutil) for primitive efficiency.
- **Q: What if string `s` is a 100GB streaming log file that cannot fit in memory?**
  *Candidate Answer*: Maintain a sliding window buffer over the stream. As new bytes arrive, advance `right`. If buffer size exceeds maximum window size, prune `left`. We only need to keep the buffer between `left` and `right`.
""")
            .build();
    }

    private static LeetCodeProblem getLongestIncreasingSubsequence() {
        return LeetCodeProblem.builder()
            .id("LC-300")
            .title("Longest Increasing Subsequence")
            .difficulty("MEDIUM")
            .topic("Binary Search / Patience Sorting")
            .url("https://leetcode.com/problems/longest-increasing-subsequence/")
            .companies(List.of("Google", "Amazon", "Microsoft", "Meta", "Bloomberg"))
            .problemSummary("Given an integer array nums, return the length of the longest strictly increasing subsequence.")
            .optimalApproach("Use Patience Sorting: maintain a tails array where tails[i] stores the smallest tail of all increasing subsequences of length i+1. For each num, binary search for its insertion slot in tails. If it's larger than all, append it; else replace.")
            .timeComplexity("O(N log N) strictly")
            .spaceComplexity("O(N) to store tails array")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"While the dynamic programming approach `dp[i] = max(dp[j] + 1)` solves this in O(N^2) time, Google expects the optimal **O(N log N)** algorithm using **Patience Sorting**.
We maintain a dynamic array `tails`, where `tails[k]` holds the smallest tail element of all valid increasing subsequences of length `k + 1`.
Because `tails` is strictly sorted at all times, for every new number `x`, we can use Binary Search in O(log N) to find its position. We either extend the longest subsequence or replace an existing element with a smaller candidate, making it easier for future numbers to extend the sequence."

---

### 💻 3. Full Production-Grade Java Implementation
```java
public class LongestIncreasingSubsequence {

    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // tails[i] stores the smallest tail of an increasing subsequence of length i + 1
        int[] tails = new int[nums.length];
        int size = 0; // Length of current longest increasing subsequence

        for (int x : nums) {
            // Binary search to find the insertion index of x in tails[0 ... size-1]
            int left = 0;
            int right = size;

            while (left < right) {
                int mid = left + (right - left) / 2;
                if (tails[mid] < x) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }

            tails[left] = x;
            if (left == size) {
                size++; // x is strictly greater than all elements, extending the LIS
            }
        }

        return size;
    }
}
```

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(N log N)**. We process `N` numbers, performing a binary search of cost `O(log N)` for each.
- **Space Complexity**: **O(N)** to hold the `tails` array.

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: How would you reconstruct the actual LIS elements rather than just its length?**
  *Candidate Answer*: Maintain an index array and a predecessor pointer array `parent[N]`. When updating `tails[left] = x`, set `parent[x_idx] = tails_indices[left - 1]`. At the end, backtrack from the last element to reconstruct the exact sequence in O(LIS) time.
""")
            .build();
    }

    private static LeetCodeProblem getFindMedianDataStream() {
        return LeetCodeProblem.builder()
            .id("LC-295")
            .title("Find Median from Data Stream")
            .difficulty("HARD")
            .topic("Heap / Priority Queue")
            .url("https://leetcode.com/problems/find-median-from-data-stream/")
            .companies(List.of("Google", "Amazon", "Meta", "Microsoft", "Apple", "Goldman Sachs"))
            .problemSummary("Design a data structure that supports adding integers from a stream and finding the median of all elements seen so far in O(1) time.")
            .optimalApproach("Maintain two Heaps: a Max-Heap for the lower half of numbers, and a Min-Heap for the upper half. Keep heaps balanced such that maxHeap.size() == minHeap.size() or maxHeap.size() == minHeap.size() + 1. Median is either maxHeap.peek() or the average of both roots.")
            .timeComplexity("O(log N) for addNum(), O(1) for findMedian()")
            .spaceComplexity("O(N) to store stream elements in heaps")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"To find the median in O(1) time while continuously accepting streaming elements, we partition the dataset into two halves using **Two Heaps**:
1. A **Max-Heap (`lowerHalf`)** storing the smaller 50% of numbers.
2. A **Min-Heap (`upperHalf`)** storing the larger 50% of numbers.
By maintaining the invariant that every element in `lowerHalf <= upperHalf`, the median is simply the root of `lowerHalf` (for odd count) or the average of both roots (for even count)."

---

### 💻 3. Full Production-Grade Java Implementation
```java
import java.util.Collections;
import java.util.PriorityQueue;

public class MedianFinder {

    // maxHeap stores the smaller half of numbers (root is largest of smaller half)
    private final PriorityQueue<Integer> maxHeap;
    // minHeap stores the larger half of numbers (root is smallest of larger half)
    private final PriorityQueue<Integer> minHeap;

    public MedianFinder() {
        this.maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        this.minHeap = new PriorityQueue<>();
    }

    public void addNum(int num) {
        // 1. Add to maxHeap first
        maxHeap.offer(num);

        // 2. Ensure every element in maxHeap is <= minHeap
        minHeap.offer(maxHeap.poll());

        // 3. Balance sizes: maxHeap can have at most 1 more element than minHeap
        if (minHeap.size() > maxHeap.size()) {
            maxHeap.offer(minHeap.poll());
        }
    }

    public double findMedian() {
        if (maxHeap.isEmpty()) {
            throw new IllegalStateException("No elements in stream");
        }

        if (maxHeap.size() > minHeap.size()) {
            return maxHeap.peek();
        } else {
            return ((double) maxHeap.peek() + minHeap.peek()) / 2.0;
        }
    }
}
```

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(log N)** for `addNum()` due to heap insertions; **O(1)** for `findMedian()`.
- **Space Complexity**: **O(N)** total storage across both heaps.

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: What if 99% of all numbers are in the range [0, 100]?**
  *Candidate Answer*: Maintain an integer bucket array `int[101]` and a running count. Median is found by scanning the buckets until reaching `count / 2`. Gives O(1) addition and O(1) median without heap overhead.
""")
            .build();
    }

    private static LeetCodeProblem getNumberOfIslands() {
        return LeetCodeProblem.builder()
            .id("LC-200")
            .title("Number of Islands")
            .difficulty("MEDIUM")
            .topic("DFS / BFS / Disjoint Set Union")
            .url("https://leetcode.com/problems/number-of-islands/")
            .companies(List.of("Google", "Amazon", "Meta", "Microsoft", "Bloomberg", "Uber"))
            .problemSummary("Given an m x n 2D binary grid grid which represents a map of '1's (land) and '0's (water), return the number of islands.")
            .optimalApproach("Iterate through every cell. When a '1' is encountered, increment island counter and initiate a DFS/BFS traversal to sink all connected land cells by marking them as '0' or visited.")
            .timeComplexity("O(M * N) where M = rows, N = cols")
            .spaceComplexity("O(M * N) call stack in worst case (grid filled with land)")
            .detailedSolution("""
### 🎯 1. The Google Candidate Pitch (First 2 Minutes)
"This is an undirected **Connected Components** problem on a 2D grid.
We iterate through every cell `(r, c)`. When we hit `'1'`, we increment the island count and immediately launch a DFS or BFS traversal to **'sink' the island in-place** by changing all connected `'1'`s into `'0'`s. In-place modification eliminates the need for an auxiliary `visited[][]` matrix."

---

### 💻 3. Full Production-Grade Java Implementation
```java
public class NumberOfIslands {

    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        int islandCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '1') {
                    islandCount++;
                    dfsSink(grid, r, c, rows, cols);
                }
            }
        }

        return islandCount;
    }

    private void dfsSink(char[][] grid, int r, int c, int rows, int cols) {
        // Boundary checks and water check
        if (r < 0 || r >= rows || c < 0 || c >= cols || grid[r][c] != '1') {
            return;
        }

        // Sink the current land cell in-place
        grid[r][c] = '0';

        // Recurse on 4 cardinal neighbors
        dfsSink(grid, r + 1, c, rows, cols); // Down
        dfsSink(grid, r - 1, c, rows, cols); // Up
        dfsSink(grid, r, c + 1, rows, cols); // Right
        dfsSink(grid, r, c - 1, rows, cols); // Left
    }
}
```

---

### ⚖️ 5. Complexity & Memory Profile
- **Time Complexity**: **O(M * N)**. Every cell is visited at most 5 times (once by outer loop, 4 times by neighbor checks).
- **Space Complexity**: **O(M * N)** recursion call stack in the worst case (e.g. grid completely filled with land).

---

### 🚀 6. Google Interviewer Follow-Ups & Hard Variants
- **Q: What if the grid is too large to fit in memory (distributed map)?**
  *Candidate Answer*: Divide grid into horizontal chunks across worker machines. Each worker runs connected components on its slice. Use **Disjoint Set Union (Union-Find)** to merge components that touch the chunk boundary seams.
""")
            .build();
    }
}
