/**
 * ALLOCATION
 *
 * In theory, the JVM allocates by merely incrementing a pointer to the next free memory location,
 * making allocation extremely cheap. While mostly correct, this model of allocation is misleadingly
 * incomplete.
 *
 * Whatever must be allocated, must also be unallocated. In the JVM, this is the job of the garbage
 * collector, which must run to reclaim memory that is no longer in use. The process of garbage
 * collection is not free, but rather imposes significant cost on low-latency and high-performance
 * applications.
 *
 * In this section, you will explore the cost of allocation.
 *
 * EXERCISE 1
 *
 * Design a 'noAlloc' benchmark that attempts to follow the exact same process as the 'alloc'
 * benchmark, but without the allocation.
 *
 * HINT: Think about pre-allocation.
 *
 * See allocation/AllocBenchmark.java
 *
 * EXERCISE 2
 *
 * Design another 'noAlloc' benchmark that attempts to follow the exact same process as the 'alloc'
 * benchmark, but without the allocation. How many times faster is the no allocation benchmark?
 *
 * See allocation/CopyAllocBenchmark.java
 *
 * GRADUATION PROJECT
 *
 * In order to better understand the process of garbage collection, in this exercise, you will
 * implement a toy mark/sweep garbage collector. It is only a toy because (a) it only considers on
 * -heap objects, and (b) it does not try to encode any information about the object graph into the
 * linear raw memory, but rather, uses high-level data structures that are easy to work with.
 *
 * Implement the mark/sweep algorithm in the `markSweep` benchmark by iterating over all objects in
 * the heap twice. In the first iteration, mark all objects that are reachable from the root object.
 * In the second iteration, sweep all objects that are not marked.
 *
 * See allocation/MarkSweepBenchmark.java
 */