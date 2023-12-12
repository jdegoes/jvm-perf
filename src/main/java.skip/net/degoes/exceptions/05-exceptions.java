/**
 * EXCEPTIONS
 *
 * Exceptions can be a source of overhead in any case where they cease to be "exceptional" (i.e.
 * when they occur frequently and are expected to occur as part of the business logic).
 *
 * In this section, you will explore and isolate the overhead of exceptions.
 */

/**
 * EXERCISE 1
 *
 * Develop a benchmark to measure the overhead of throwing and catching `MyException` with a fixed
 * message. Compare this with the overhead of constructing a new `MyException` without throwing (or
 * catching) it. What can you conclude from this benchmark?
 */

// See exceptions/ThrowExceptionBenchmark.java

/**
 * EXERCISE 2
 *
 * Develop a benchmark to measure the overhead of throwing and catching the same exception. Compare
 * this with the overhead of throwing and catching new exceptions. What can you conclude from this
 * comparison, together with the previous exercise?
 */

// See exceptions/ThrowSameExceptionBenchmark.java

/**
 * EXERCISE 3
 *
 * Develop a benchmark to measure the overhead of calling Exception#fillInStackTrace. What can you
 * conclude from this benchmark?
 */

// See exceptions/FillInStackTraceBenchmark.java
