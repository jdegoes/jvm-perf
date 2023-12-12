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
 */
package net.degoes.gotchas

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
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
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class MisleadingBenchmark {
  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  def getBoxedAge(i: Int): Age = Age(i)

  @Benchmark
  def boxed(blackhole: Blackhole): Unit = {
    var i   = 0
    var sum = 0
    while (i < size) {
      val age = getBoxedAge(i)
      sum = sum + age.value
      i = i + 1
    }
    blackhole.consume(sum)
  }

  case class Age(value: Int)
}

/**
 * EXERCISE 3
 *
 * This benchmark purports to show that precomputing fibonacci numbers is slower than just computing
 * them dynamically. However, the benchmark is flawed. Fix the benchmark so that it shows the
 * expected result.
 *
 * NOTE: In general, mistakes involving setup overhead will NOT be this easy to identify and fix.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class SetupOverheadBenchmark {
  @Param(Array("100", "1000"))
  var maxFib: Int = _

  @Param(Array("10", "100"))
  var fib: Int = _

  def fib(n: Int): Int = {
    @annotation.tailrec
    def fibAcc(n: Int, a: Int, b: Int): Int =
      if (n == 0) a
      else fibAcc(n - 1, b, a + b)

    fibAcc(n, 0, 1)
  }

  @Benchmark
  def precomputedFib(blackhole: Blackhole): Unit = {
    var precomputedFib: Array[Int] = Array.from(0 to maxFib).map(fib(_))

    blackhole.consume(precomputedFib(fib))
  }

  @Benchmark
  def dynamicFib(blackhole: Blackhole): Unit =
    blackhole.consume(fib(fib))
}
