/**
 * BOXING
 *
 * The JVM draws a sharp distinction between primitive types (such as integers, floats, and bytes)
 * and reference types (such as String and user-defined classes).
 *
 * Primitive types may be stored on the stack, and when they are stored on the heap (for example, as
 * part of a user-defined class), they are stored in a very compact form. Finally, arrays are
 * specialized for primitive types, which enable very compact and performant access to their
 * elements.
 *
 * In this section, you will explore the nature and overhead of boxing.
 */
package net.degoes.boxing

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * EXERCISE 1
 *
 * Design a benchmark to measure the overhead of boxing. In order to be fair to the boxing
 * benchmark, you should design it to have a similar structure and process. The difference is that
 * it will not box the individual integers in an array.
 *
 * Discuss the overhead of boxing and how it compared with your initial expectations.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class BoxedBenchmark {
  @Param("100", "1000", "10000")
  var size: Int = 0

  var boxed: Array<Boxed<Int>> = emptyArray()

  @Setup
  fun setup(): Unit {
    boxed = Array(size) { Boxed<Int>(0) }
  }

  @Benchmark
  fun boxed(blackhole: Blackhole): Unit {
    var i   = 0
    var sum = 0
    while (i < size) {
      val newValue = boxed[i].value + 1
      boxed[i] = Boxed(newValue)
      sum = sum + newValue
      i = i + 1
    }
    blackhole.consume(sum)
  }

  @Benchmark
  fun unboxed(blackhole: Blackhole): Unit {
  }

  data class Boxed<T>(val value: T)
}

/**
 * EXERCISE 2
 *
 * Boxing is not just something that occurs with generic data structures, such as lists, sets, and
 * maps. It occurs also with interfaces that provide generic functionality.
 *
 * In this exercise, you will explore the cost of boxing with the Compare interface. The
 * Compare interface is a generic interface that allows you to compare two values of the same
 * type. Create a specialized version to see the overhead of boxing in this example.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class BoxedComparatorBenchmark {
  @Param("100", "1000", "10000")
  var size: Int = 0

  var ints: Array<Int> = emptyArray()

  @Setup
  fun setup(): Unit {
    ints = Array(size) { _ -> 0 }
  }

  @Benchmark
  fun boxed(blackhole: Blackhole): Unit {
    blackhole.consume(comparison(ints, 0, IntGenericComparator))
  }

  fun <T> comparison(array: Array<T>, n: T, cmp: Compare<T>) {
    var i = 0
    var sum = 0
    while (i < size) {
      sum += cmp.compare(array[i], n)
      i = i + 1
    }
    sum
  }

  interface Compare<T> {
    abstract fun compare(l: T, r: T): Int
  }
  
  val IntGenericComparator: Compare<Int> = object : Compare<Int> {
    override fun compare(l: Int, r: Int): Int = l - r
  }
}
