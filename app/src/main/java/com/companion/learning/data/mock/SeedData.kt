package com.companion.learning.data.mock

import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.MilestoneEntity
import com.companion.learning.data.local.entity.RoadmapEntity

/**
 * Rich demo seed data that simulates what Gemini AI generates for a
 * "30-Day DSA Interview Roadmap" goal. This is shown on first launch
 * so the app has content to showcase immediately.
 */
object SeedData {

    val mockRoadmap = RoadmapEntity(
        id = "demo-dsa-roadmap",
        title = "30-Day DSA Interview Prep",
        goal = "Master Data Structures & Algorithms for technical interviews at top companies.",
        duration = "30 days",
        experienceLevel = "Intermediate",
        hoursPerDay = 3,
        llmProviderId = "GEMINI",
        schemaVersion = 1,
        status = "ACTIVE",
        startedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L), // started 7 days ago
        createdAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
    )

    val mockMilestones = listOf(
        MilestoneEntity(
            id = "ms-dsa-1",
            roadmapId = "demo-dsa-roadmap",
            weekNumber = 1,
            title = "Arrays, Hashing & Two Pointers",
            summary = "Build a solid foundation with the most common interview patterns — prefix sums, hash maps, and the two-pointer technique.",
            expansionStatus = "EXPANDED"
        ),
        MilestoneEntity(
            id = "ms-dsa-2",
            roadmapId = "demo-dsa-roadmap",
            weekNumber = 2,
            title = "Sliding Window, Sorting & Binary Search",
            summary = "Learn to reduce O(n²) solutions to O(n) with sliding windows, master interval problems, and implement binary search patterns.",
            expansionStatus = "EXPANDED"
        ),
        MilestoneEntity(
            id = "ms-dsa-3",
            roadmapId = "demo-dsa-roadmap",
            weekNumber = 3,
            title = "Linked Lists, Stacks, Queues & Trees",
            summary = "Work through pointer manipulation, monotonic stacks, and the full suite of binary tree traversal patterns.",
            expansionStatus = "EXPANDED"
        ),
        MilestoneEntity(
            id = "ms-dsa-4",
            roadmapId = "demo-dsa-roadmap",
            weekNumber = 4,
            title = "Graphs, Dynamic Programming & Mock Interviews",
            summary = "Tackle graph traversal (BFS/DFS), shortest path, and the key DP patterns. Finish with timed mock interview sessions.",
            expansionStatus = "PENDING"
        )
    )

    val mockCurriculumItems = listOf(
        // ── WEEK 1 ───────────────────────────────────────────────────────────────
        CurriculumItemEntity(
            id = "ci-dsa-1", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 1, topic = "Time & Space Complexity + Arrays",
            description = "Big-O notation, prefix sum, Kadane's algorithm. Solve: Maximum Subarray, Running Sum.",
            estimatedTime = 120, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-2", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 2, topic = "Hashing — HashMap & HashSet",
            description = "Two Sum, Group Anagrams, Contains Duplicate, Longest Consecutive Sequence.",
            estimatedTime = 120, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-3", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 3, topic = "Two Pointers",
            description = "Valid Palindrome, Two Sum II (sorted), 3Sum, Container With Most Water.",
            estimatedTime = 120, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-4", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 4, topic = "Sliding Window",
            description = "Best Time to Buy Stock, Longest Substring Without Repeating Characters, Minimum Window Substring.",
            estimatedTime = 120, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-5", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 5, topic = "Sorting & Intervals",
            description = "Merge Intervals, Insert Interval, Non-Overlapping Intervals, Meeting Rooms II.",
            estimatedTime = 120, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-6", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 6, topic = "Week 1 Mixed Practice",
            description = "Timed set: 8 problems covering arrays, hashing, two pointers, and sliding window. Focus on speed.",
            estimatedTime = 180, status = "COMPLETED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-7", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-1",
            dayNumber = 7, topic = "Revision + LeetCode Weekly Contest",
            description = "Re-solve 3 hardest problems from this week from memory. Join this week's LeetCode contest.",
            estimatedTime = 90, status = "COMPLETED"
        ),
        // ── WEEK 2 ───────────────────────────────────────────────────────────────
        CurriculumItemEntity(
            id = "ci-dsa-8", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 8, topic = "Binary Search — Fundamentals",
            description = "Classic binary search, First Bad Version, Search Insert Position, Search in Rotated Array.",
            estimatedTime = 120, status = "IN_PROGRESS"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-9", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 9, topic = "Advanced Binary Search",
            description = "Binary search on answer space: Koko Eating Bananas, Capacity to Ship Packages, Split Array.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-10", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 10, topic = "Linked List Fundamentals",
            description = "Reverse Linked List, Merge Two Sorted Lists, Linked List Cycle, Find Duplicate Number.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-11", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 11, topic = "Fast & Slow Pointer",
            description = "Middle of Linked List, Reorder List, LRU Cache (bonus).",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-12", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 12, topic = "Stack — Fundamentals & Patterns",
            description = "Valid Parentheses, Min Stack, Evaluate Reverse Polish Notation, Daily Temperatures.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-13", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 13, topic = "Queue & Monotonic Queue",
            description = "Implement Queue with Stacks, Sliding Window Maximum, Largest Rectangle in Histogram.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-14", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-2",
            dayNumber = 14, topic = "Week 2 Revision + Contest",
            description = "Re-solve 5 problems under 45-min constraint. Participate in LeetCode Biweekly.",
            estimatedTime = 90, status = "NOT_STARTED"
        ),
        // ── WEEK 3 ───────────────────────────────────────────────────────────────
        CurriculumItemEntity(
            id = "ci-dsa-15", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 15, topic = "Binary Trees — Traversals",
            description = "Invert Binary Tree, Maximum Depth, Diameter of Binary Tree, Level Order Traversal.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-16", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 16, topic = "Binary Search Tree (BST)",
            description = "Validate BST, Kth Smallest Element in BST, Lowest Common Ancestor, BST Iterator.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-17", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 17, topic = "Heaps / Priority Queue",
            description = "Kth Largest Element, K Closest Points, Task Scheduler, Find Median from Data Stream.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-18", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 18, topic = "Backtracking",
            description = "Subsets, Combinations, Permutations, Letter Combinations of Phone Number.",
            estimatedTime = 150, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-19", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 19, topic = "Trie (Prefix Tree)",
            description = "Implement Trie, Word Search II, Design Add and Search Words Data Structure.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-20", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 20, topic = "Week 3 Mixed Practice",
            description = "8 timed problems across trees, heaps, and backtracking. Simulate 90-min interview.",
            estimatedTime = 180, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-21", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-3",
            dayNumber = 21, topic = "Week 3 Revision + Contest",
            description = "Target: solve 3 medium problems in under 60 min. Review mistakes after.",
            estimatedTime = 90, status = "NOT_STARTED"
        ),
        // ── WEEK 4 ───────────────────────────────────────────────────────────────
        CurriculumItemEntity(
            id = "ci-dsa-22", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 22, topic = "Graphs — BFS & DFS",
            description = "Number of Islands, Clone Graph, Max Area of Island, Rotting Oranges.",
            estimatedTime = 150, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-23", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 23, topic = "Graphs — Topological Sort & Union Find",
            description = "Course Schedule I & II, Number of Connected Components, Redundant Connection.",
            estimatedTime = 150, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-24", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 24, topic = "Shortest Path (Dijkstra / BFS on weighted graph)",
            description = "Network Delay Time, Cheapest Flights Within K Stops, Path with Minimum Effort.",
            estimatedTime = 150, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-25", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 25, topic = "Dynamic Programming — 1D",
            description = "Climbing Stairs, House Robber I & II, Coin Change, Longest Increasing Subsequence.",
            estimatedTime = 150, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-26", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 26, topic = "Dynamic Programming — 2D",
            description = "Unique Paths, Longest Common Subsequence, Edit Distance, Interleaving String.",
            estimatedTime = 180, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-27", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 27, topic = "Greedy Algorithms",
            description = "Jump Game I & II, Gas Station, Hand of Straights, Partition Labels.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-28", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 28, topic = "Bit Manipulation",
            description = "Number of 1 Bits, Counting Bits, Reverse Bits, Missing Number, Sum of Two Integers.",
            estimatedTime = 90, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-29", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 29, topic = "Mock Interview Simulation #1",
            description = "2 hours, 4 problems: 1 easy + 2 medium + 1 hard. Simulate real interview conditions.",
            estimatedTime = 120, status = "NOT_STARTED"
        ),
        CurriculumItemEntity(
            id = "ci-dsa-30", roadmapId = "demo-dsa-roadmap", milestoneId = "ms-dsa-4",
            dayNumber = 30, topic = "Mock Interview Simulation #2 + Final Revision",
            description = "Final mock, then review every weak topic. You're ready — go land that offer! 🚀",
            estimatedTime = 120, status = "NOT_STARTED"
        )
    )
}
