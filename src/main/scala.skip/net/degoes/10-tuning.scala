/**
 * TUNING
 *
 * The JVM exposes several knobs that you can use to tweak and tune performance for your
 * applications.
 *
 * In this section, you will explore these knobs, with a special emphasis on garbage collection.
 *
 * Garbage collection is all about tradeoffs. Broadly speaking, the main tradeoffs are as follows:
 *
 * Throughput versus latency. Throughput is the amount of work that can be done in a given amount of
 * time. Latency is the amount of time it takes to complete a single unit of work. Garbage
 * collection can be tuned to maximize throughput, at the expense of latency, or to maximize
 * latency, at the expense of throughput.
 *
 * Memory usage versus throughput. Garbage collection can be tuned to use less memory, at the
 * expense of throughput. Alternately, throughput can be maximized, at the expense of memory usage.
 * Running JVM applications on memory-constrained environments will require tuning for memory usage.
 */
package net.degoes.tuning

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class TuningBenchmark1 {
  @Param(Array("10000", "1000000"))
  var size: Int = _

  @Param(Array("100000"))
  var numberOfObjects: Int = _

  @Benchmark
  def burstHeap(blackhole: Blackhole): Unit = {
    var iter = 0
    while (iter < 4) {
      var junk = new java.util.ArrayList[Array[Byte]](numberOfObjects)
      var j    = 0
      while (j < numberOfObjects) {
        junk.add(new Array[Byte](size))
        j = j + 1
      }
      blackhole.consume(junk)
      iter = iter + 1
    }
  }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class TuningBenchmark2 {
  @Param(Array("8000000"))
  var size: Int = _

  @Benchmark
  def constantHeap(blackhole: Blackhole): Unit =
    blackhole.consume(new Array[Byte](size))
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class TuningBenchmark3 {
  @Param(Array("4000"))
  var size: Int = _

  @Benchmark
  def heapBandwidth(blackhole: Blackhole): Unit =
    blackhole.consume(new Array[Byte](size))
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class TuningBenchmark4 {
  @Param(Array("2", "4", "8"))
  var n: Int = _

  @Benchmark
  def nqueens(blackhole: Blackhole): Unit = {
    def queens(n: Int): List[List[(Int, Int)]] = {
      def isAttacked(q1: (Int, Int), q2: (Int, Int)) =
        q1._1 == q2._1 ||
          q1._2 == q2._2 ||
          (q2._1 - q1._1).abs == (q2._2 - q1._2).abs

      def isSafe(queen: (Int, Int), others: List[(Int, Int)]) =
        others.forall(!isAttacked(queen, _))

      def placeQueens(k: Int): List[List[(Int, Int)]] =
        if (k == 0)
          List(List())
        else
          for {
            queens <- placeQueens(k - 1)
            column <- 1 to n
            queen   = (k, column)
            if isSafe(queen, queens)
          } yield queen :: queens
      placeQueens(n)
    }

    queens(n)
  }
}

/*
 * EXERCISE 1
 *
 * Execute the benchmarks using the default garbage collector.
 *
 * EXERCISE 2
 *
 * Execute the benchmarks using the parallel garbage collector by using the JVM flag
 * -XX:+UseParallelGC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:ParallelGCThreads                  (default: # of CPU cores)
 * -XX:MaxGCPauseMillis                   (default: 100)
 * -XX:GCTimeRatio                        (default: 99)
 * -XX:YoungGenerationSizeIncrement       (default: 20)
 * -XX:TenuredGenerationSizeIncrement     (default: 20)
 * -XX:AdaptiveSizeDecrementScaleFactor   (default: 4)
 * -XX:UseGCOverheadLimit                 (default: true)
 *
 * EXERCISE 3
 *
 * Execute the benchmarks using the concurrent mark sweep garbage collector by using the JVM flag
 * -XX:+UseConcMarkSweepGC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:CMSInitiatingOccupancyFraction   (default: 68)
 * -XX:UseCMSInitiatingOccupancyOnly    (default: false)
 * -XX:CMSInitiatingOccupancyFraction   (default: 68)
 * -XX:CMSScavengeBeforeRemark          (default: false)
 * -XX:ScavengeBeforeFullGC             (default: false)
 * -XX:CMSParallelRemarkEnabled         (default: true)
 * -XX:UseGCOverheadLimit               (default: true)
 *
 * EXERCISE 4
 *
 * Execute the benchmarks using the G1 garbage collector by using the JVM flag -XX:+UseG1GC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:InitiatingHeapOccupancyPercent   (default: 45)
 * -XX:G1UseAdaptiveIHOP                (default: true)
 * -XX:G1HeapWastePercent               (default: 5)
 * -XX:G1PeriodicGCSystemLoadThreshold  (default: 120)
 * -XX:MinHeapFreeRatio                 (default: 40)
 * -XX:MaxHeapFreeRatio                 (default: 70)
 * -XX:G1NewSizePercent                 (default: 5)
 * -XX:G1MaxNewSizePercent              (default: 60)
 * -XX:NewSize                          (default: 1/2 of the heap)
 * -XX:MaxNewSize                       (default: 1/2 of the heap)
 * -XX:+AlwaysPreTouch                  (default: false)
 *
 * EXERCISE 5
 *
 * Execute the benchmarks using the Z garbage collector by using the JVM flag -XX:+UseZGC,
 * and -XX:+UnlockExperimentalVMOptions depending on the JVM version you are using.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:ConcGCThreads                    (default: # of CPU cores)
 *
 */
