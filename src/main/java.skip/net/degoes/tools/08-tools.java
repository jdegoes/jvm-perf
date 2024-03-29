/**
 * TOOLS
 *
 * JMH is an incredibly useful tool for benchmarking and optimizing code. However, although JMH is
 * qute useful, it cannot tell you why your code is slow or tell you which parts of your code you
 * should benchmark.
 *
 * In this section, you will explore several tools that you can use both to help identify
 * performance bottlenecks, as well as to understand why an identified section of code is slow.
 *
 * EXERCISE 1
 *
 * Use the flag "-XX:+PrintCompilation" to print out the JIT compilation of the benchmark. Is the
 * `fib` method compiled to native code by HotSpot?
 *
 * See tools/PrintCompilationBenchmark.java
 *
 * EXERCISE 2
 *
 * Use the flag "-XX:+PrintInlining" (together with "-XX:+UnlockDiagnosticVMOptions") to print out
 * the inlining of the benchmark.
 *
 * Is the `makeSize` method inlined by HotSpot?
 *
 * See tools/PrintInliningBenchmark.java
 *
 * EXERCISE 3
 *
 * Profilers can be incredibly useful for identifying performance bottlenecks. Even though it is
 * hard to optimize against a profiling, a profiler can help you identify the most expensive
 * sections of code (in terms of CPU or memory), which you can then benchmark and optimize.
 *
 * In this exercise, you will take your benchmark tool of choice to identify performance bottlenecks
 * in the provided code. You can use this information in the next module.
 *
 * See tools/ProfilerExample.java
 *
 * GRADUATION PROJECT
 *
 * Sometimes, you need to see something closer to the raw bytecode that your compiler generates.
 * This is especially true when you are using higher-level languages like Kotlin, Scala, and
 * Clojure, because these languages have features that do not map directly to JVM bytecode.
 *
 * In order to do this, you can use the `javap` method with the following flags:
 *
 *   - `-c` prints out the bytecode
 *   - `-l` prints out line numbers
 *   - `-p` prints out private methods
 *   - `-s` prints out internal type signatures
 *   - `-v` prints out verbose information
 *
 * In this exercise, you will use `javap` to see the bytecode generated by the Scala compiler for
 * the provided benchmark. Walk through the reverse-engineered code and try to understand any
 * sources of inefficiency that you see. Revise the inefficient code until `javap` shows you cleanly
 * generated code that you would expect to be fast.
 *
 * See tools/JavapBenchmark.java
 */