/**
 * EXCEPTIONS
 *
 * Exceptions can be a source of overhead in any case where they cease to be "exceptional" (i.e.
 * when they occur frequently and are expected to occur as part of the business logic).
 *
 * In this section, you will explore and isolate the overhead of exceptions.
 */
package net.degoes.exceptions

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * EXERCISE 1
 *
 * Develop a benchmark to measure the overhead of throwing and catching `MyException` with a fixed
 * message. Compare this with the overhead of constructing a new `MyException` without throwing (or
 * catching) it. What can you conclude from this benchmark?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class ThrowExceptionBenchmark {
  case class MyException(message: String) extends Exception(message)

  @Benchmark
  def throwCatchException(): Unit = ???

  @Benchmark
  def constructException(): Unit = ???
}

/**
 * EXERCISE 2
 *
 * Develop a benchmark to measure the overhead of throwing and catching the same exception. Compare
 * this with the overhead of throwing and catching new exceptions. What can you conclude from this
 * comparison, together with the previous exercise?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class ThrowSameExceptionBenchmark {
  case class MyException(message: String) extends Exception(message)

  val exception = MyException("Hello")

  @Benchmark
  def throwCatchNewException(): Unit = try
    throw MyException("Hello")
  catch { case _: Throwable => () }

  @Benchmark
  def throwCatchSameException(): Unit = ??? // TODO
}

/**
 * EXERCISE 3
 *
 * Develop a benchmark to measure the overhead of calling Exception#fillInStackTrace. What can you
 * conclude from this benchmark?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class FillInStackTraceBenchmark {
  case class MyException(message: String) extends Exception(message)

  val exception = MyException("Hello")

  @Benchmark
  def fillInStackTrace(): Unit = ??? // TODO

  @Benchmark
  def throwCatchNewException(): Unit = try
    throw MyException("Hello")
  catch { case _: Throwable => () }
}
