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

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class TuningBenchmark1 {
  @Param("10000", "1000000")
  var size: Int = 0

  @Param("100000")
  var numberOfObjects: Int = 0

  @Benchmark
  fun burstHeap(blackhole: Blackhole): Unit {
    var iter = 0
    while (iter < 4) {
      var junk = java.util.ArrayList<ByteArray>(numberOfObjects)
      var j    = 0
      while (j < numberOfObjects) {
        junk.add(ByteArray(size))
        j = j + 1
      }
      blackhole.consume(junk)
      iter = iter + 1
    }
  }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class TuningBenchmark2 {
  @Param("8000000")
  var size: Int = 0

  @Benchmark
  fun constantHeap(blackhole: Blackhole): Unit {
    blackhole.consume(ByteArray(size))
  }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class TuningBenchmark3 {
  @Param("4000")
  var size: Int = 0

  @Benchmark
  fun heapBandwidth(blackhole: Blackhole): Unit {
    blackhole.consume(ByteArray(size))
  }
}

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class TuningBenchmark4 {
  @Param("2", "4", "8")
  var n: Int = 0

  @Benchmark
  fun nqueens(blackhole: Blackhole): Unit {
    fun queens(n: Int): List<List<Pair<Int, Int>>> {
      fun isAttacked(q1: Pair<Int, Int>, q2: Pair<Int, Int>): Boolean {
        return q1.first == q2.first ||
          q1.second == q2.second ||
          Math.abs(q2.first - q1.first) == Math.abs(q2.second - q1.second)
      }

      fun isSafe(queen: Pair<Int, Int>, others: List<Pair<Int, Int>>) =
        others.all { q -> !isAttacked(queen, q) }

      fun placeQueens(k: Int): List<List<Pair<Int, Int>>> {
        return if (k == 0) listOf(emptyList<Pair<Int, Int>>()) else
          placeQueens(k - 1).flatMap { queens ->
            (1..n).toList().filter { column ->
              isSafe(k to column, queens)
            }.map { column -> listOf(k to column) + queens }
          }
      }

      return placeQueens(n)
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
