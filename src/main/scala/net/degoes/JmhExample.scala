package net.degoes

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(16)
class JmhExample {

  @Benchmark
  def testMethod(blackHole: Blackhole): Double = {
    val list: List[Int] = List.range(1, Integer.MAX_VALUE / 100)
    val sum: Double     = list.sum
    blackHole.consume(sum)
    sum
  }
}
