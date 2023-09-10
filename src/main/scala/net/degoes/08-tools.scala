/**
 * TOOLS
 *
 * JMH is an incredibly useful tool for benchmarking and optimizing code. However, although JMH is
 * qute useful, it cannot tell you why your code is slow or tell you which parts of your code you
 * should benchmark.
 *
 * In this section, you will explore several tools that you can use both to help identify
 * performance bottlenecks, as well as to understand why an identified section of code is slow.
 */
package net.degoes.tools

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * EXERCISE 1
 *
 * Use the flag "-XX:+PrintCompilation" to print out the JIT compilation of the benchmark. Is the
 * `fib` method compiled to native code by HotSpot?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(1)
class PrintCompilationBenchmark {
  @Param(Array("10", "20"))
  var depth: Int = _

  def fib(n: Int): Int =
    if (n <= 1) 1 else fib(n - 1) + fib(n - 2)

  @Benchmark
  def fib(blackhole: Blackhole): Unit =
    blackhole.consume(fib(depth))
}

/**
 * EXERCISE 2
 *
 * Use the flag "-XX:+PrintInlining" (together with "-XX:+UnlockDiagnosticVMOptions") to print out
 * the inlining of the benchmark.
 *
 * Is the `makeSize` method inlined by HotSpot?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(1)
class PrintInliningBenchmark {
  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  def makeSize(i: Int): Size = Size(i)

  @Benchmark
  def benchmark(blackhole: Blackhole): Unit = {
    var i   = 0
    var sum = 0
    while (i < size) {
      val size = makeSize(i)
      sum = sum + size.value
      i = i + 1
    }
    blackhole.consume(sum)
  }

  case class Size(value: Int)
}

/**
 * EXERCISE 3
 *
 * Profilers can be incredibly useful for identifying performance bottlenecks. Even though it is
 * hard to optimize against a profiling, a profiler can help you identify the most expensive
 * sections of code, which you can then benchmark and optimize.
 *
 * In this exercise, you will take your benchmark tool of choice to identify performance bottlenecks
 * in the provided code. You can use this information in the next module.
 */
object ProfilerExample {
  def main(args: Array[String]): Unit = {
    import zio.Chunk
    import net.degoes.project.dataset1._

    val Size = 10_000

    val rng = new scala.util.Random(0L)

    val start: Field  = Field("start")
    val end: Field    = Field("end")
    val netPay: Field = Field("netPay")

    val dataset = Dataset(Chunk.fill(Size) {
      val start  = rng.between(0, 360)
      val end    = rng.between(start, 360)
      val netPay = rng.between(20000, 60000)

      Row(
        Map(
          "start"  -> Value.Integer(start),
          "end"    -> Value.Integer(end),
          "netPay" -> Value.Integer(netPay)
        )
      )
    })

    var i = 0L
    while (i < 1_000_000) {
      (dataset(start) + dataset(end)) / dataset(netPay)
      i = i + 1L
    }
  }
}
