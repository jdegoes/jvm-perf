/**
 * GOTCHAS
 *
 * The JVM is a highly dynamic environment. You may believe that a benchmark shows you one thing,
 * when in fact the opposite may be the case in your application code.
 *
 * It is for this reason that everyone should treat the result of benchmarks and profiling data,
 * which can feed into a hypothesis, which can then be tested and either rejected or tenatively
 * accepted.
 *
 * In this section, you will see for yourself reasons to be cautious.
 *
 * EXERCISE 1
 *
 * Create an unboxed version of this benchmark, which follows the structure and flow of the boxed
 * version (for fairness). What do you expect to happen? What actually happens?
 *
 * Note that the results you see in this benchmark are NOT generally applicable to your application.
 * It would be a gross error to generalize them.
 *
 * EXERCISE 2
 *
 * Add the JVM options "-XX:-DoEscapeAnalysis", "-XX:-Inline" and re-run the benchmark. Now guess why
 * you see the behavior you are seeing, and come up with a modification to the benchmark that will
 * enable you to see the expected behavior (a modification that would accurately reflect some
 * application code you might write).
 *
 * See gotchas/MisleadingBenchmark.java
 *
 * EXERCISE 3
 *
 * This benchmark purports to show that precomputing fibonacci numbers is slower than just computing
 * them dynamically. However, the benchmark is flawed. Fix the benchmark so that it shows the
 * expected result.
 *
 * NOTE: In general, mistakes involving setup overhead will NOT be this easy to identify and fix.
 *
 * See gotchas/SetupOverheadBenchmark.java
 */