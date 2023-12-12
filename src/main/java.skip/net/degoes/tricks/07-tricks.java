/**
 * TRICKS
 *
 * Until now, you have discovered many sources of overhead in developing software for the JVM.
 * Although you have some idea of how to avoid these sources of overhead, there has been no
 * systematic treatment of different techniques that can be applied to each type of overhead.
 *
 * In this section, you will learn some of the essential "tricks of the trade". In the process, you
 * will become proficient at writing fast code when the occassion requires.
 *
 * EXERCISE 1
 *
 * Because the JVM supports null values, you can use the null value as an extra "sentinal" value,
 * rather than using a wrapper data structure to propagate the same information. This can reduce
 * allocation and indirection and improve performance.
 *
 * In this exercise, create a version of the benchmark that uses null values instead of the
 * `Optional` data type. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 *
 * See tricks/UseNullBenchmark.java
 *
 * EXERCISE 2
 *
 * Arrays exploit CPU caches and primitive specialization, which means they can be tremendously
 * faster for certain tasks.
 *
 * In this exercise, create a version of the benchmark that uses arrays instead of lists. Ensure it
 * follows the same structure and flow of the existing benchmark in order to make a fair comparison.
 *
 * See if you can create an Array-based version that is 10x faster than the List-based version.
 *
 * See tricks/UseArraysBenchmark.java
 *
 * EXERCISE 3
 *
 * Although the JVM can optimize away some allocations in some cases, it's safer and more reliable
 * to simply avoid them in performance-sensitive code. This means using mutable structures, and
 * sometimes re-using structures (using pools, pre-allocation, and other techniques).
 *
 * In this exercise, create a version of the benchmark that uses a mutable data structure instead of
 * an immutable data structure. Ensure it follows the same structure and flow of the existing
 * benchmark in order to make a fair comparison.
 *
 * BONUS: Try to solve the problem using zero allocations in the benchmark. You will have to use
 * pre-allocation in order to achieve this goal.
 *
 * See tricks/NoAllocationBenchmark.java
 *
 * EXERCISE 4
 *
 * The JVM implements generics with type erasure, which means that generic data types must box all
 * primitives. In cases where you are using generic data types for primitives, it can make sense to
 * manually specialize the generic data types to your specific primitives. Although this creates
 * much more boilerplate, it allows you to improve performance.
 *
 * In this exercise, create a version of the benchmark that uses a specialized data type instead of
 * a generic data type. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 *
 * See tricks/SpecializeBenchmark.java
 *
 * EXERCISE 6
 *
 * In some cases, you can eliminate heap allocation in function return values by packing multiple
 * values into a single primitive value. For example, a 64 bit long can actually hold multiple
 * separate channels of information, and will not require heap allocation.
 *
 * In this exercise, create a version of the benchmark that uses a packed return value instead of
 * the provided case class. Ensure it follows the same structure and flow of the existing benchmark
 * in order to make a fair comparison.
 *
 * See tricks/PrimitivizeReturnBenchmark.java
 *
 * EXERCISE 7
 *
 * If you are processing data in bulk, and the fields of your data type are all primitives, then you
 * can reduce heap allocation by using arrays of the individual fields, rather than arrays of the
 * data type. This reduces allocation and indirection and improves cache hits.
 *
 * In this exercise, create a version of the benchmark that uses arrays of primitives instead of
 * arrays of the provided data type. Ensure it follows the same structure and flow of the existing
 * benchmark in order to make a fair comparison.
 *
 * See tricks/FlattenProductsBenchmark.java
 *
 * EXERCISE 8
 *
 * Virtual dispatch imposes overhead in any case where the JVM cannot devirtualize. If the number of
 * subtypes sharing the same (virtual) interface is fixed, then you can manually devirtualize by
 * using an integer tag to indicate which subtype should be used. Then in any case where you would
 * call a virtual method, you instead match on the tag, and call the concrete (non-virtual) method
 * corresponding to that tag.
 *
 * In this exercise, create a version of the benchmark that uses manual devirtualization instead of
 * virtual dispatch. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 *
 * See tricks/DevirtualizeBenchmark.java
 *
 * EXERCISE 10
 *
 * Exceptions can impose overhead on performance-sensitive code. This overhead comes primarily from
 * stack traces. If you can avoid stack traces, then you can avoid most of the overhead of
 * exceptions.
 *
 * In this exercise, you will try two separate tricks to avoid poorly-performing exception- bound
 * code:
 *
 *   1. Use a return value instead of throwing an exception. 2. Throw a special exception type that
 *      does not generate a stack trace.
 *
 * See tricks/NoExceptionsBenchmark.java
 *
 * EXERCISE 11
 * 
 * Hash maps can offer quite high performance (O(1)), but never as high performance as array 
 * lookups (lower constant factor). To accelerate some code, you can switch from using non-integer
 * sparse keys to using dense integer keys, which lets you replace the map with an array.
 *
 * In this exercise, create an equivalent implementation to the provided one that uses arrays
 * instead of maps and observe the effects on performance.
 *
 * See tricks/MapToArrayBenchmark.java
 *
 * GRADUATION PROJECT
 *
 * Sometimes you can transform a process that is built using interfaces and classes (a so-called
 * "executable encoding") into something more primitive and fundamental that can be interpreted by a
 * stack machine. This has potential to significantly improve performance.
 *
 * In this graduation project, you will work with the instructor to implement this optimization for
 * a route parser, and see what sort of speedup you can achieve without changing the expressiveness
 * of the process.
 *
 * See tricks/StackInterpreterBenchmark.java
 */
