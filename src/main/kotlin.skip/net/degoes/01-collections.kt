/**
 * COLLECTIONS
 *
 * Thanks to powerful abstractions on the JVM, including java.util Collections, or standard library
 * collections in Scala, Kotlin, and other JVM-based languages, it is easy to write code that
 * processes data in bulk.
 *
 * With this ease comes a danger: it is easy to write code that is not performant. This performance
 * cost comes about because of several factors:
 *
 *   1. Wrong collection type. Different collection types have different overhead on different kinds
 *      of operations. For example, doubly-linked linked lists are good at prepending and appending
 *      single elements, but are terrible at random access.
 *
 * 2. Boxing of primitives. On the JVM, primitives are not objects, and so they must be boxed into
 * objects in order to be stored in collections. This boxing and unboxing can be expensive.
 *
 * 3. Cache locality. Modern CPUs are very fast, but they are also very complex. One of the ways
 * that CPUs achieve their speed is by caching data in memory. Most collection types do not store
 * their elements contiguously in memory, even if they are primitives, and so cannot take advantage
 * of the CPU cache, resulting in slower performance.
 *
 * In this section, you will use the JMH benchmarking tool in order to explore collection
 * performance across a range of collections, and then you will discover not only how to use the
 * fastest collection type but how to increase its applicability to a wider range of use cases.
 */
package net.degoes.collections

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import io.vavr.collection.List

/**
 * EXERCISE 1
 *
 * This benchmark is currently configured with a Vavr List, which is a singly-linked-list data type. Add two
 * other collection types to this benchmark, and make sure to at least try Array.
 *
 * EXERCISE 2
 *
 * Identify which collection is the fastest for prepending a single element, and explain why.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class ElementPrependBenchmark {
  val PrependsPerIteration = 100

  @Param("1000", "10000", "100000")
  var size: Int = 0

  var startList: List<String> = List.of()

  @Setup(Level.Trial)
  fun setup(): Unit {
    startList = List.range(0, size).map { _ -> "a" }
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit =
    blackhole.consume(startList.prepend("a"))
}

/**
 * EXERCISE 3
 *
 * Create a benchmark for concatenation across lists, vectors (or another standard collection
 * type), and arrays.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class ConcatBenchmark {
  @Setup(Level.Trial)
  fun setup(): Unit {
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
  }
}

/**
 * EXERCISE 4
 *
 * Create a benchmark for random access across lists, vectors (or another standard collection
 * type), and arrays.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class RandomAccessBenchmark {
  @Setup(Level.Trial)
  fun setup(): Unit {
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
  }
}

/**
 * EXERCISE 5
 *
 * Create a benchmark for iteration, which sums all the elements in a collection, across lists,
 * vectors (or another standard collection type), and arrays.
 *
 * NOTE: Arrays of primitives are specialized on the JVM. Which means they do not incur overhead of
 * "boxing", a topic we will return to later. For now, just make sure to store java.lang.Integer
 * values in the Array in order to ensure the benchmark is fair.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class IterationBenchmark {
  @Setup(Level.Trial)
  fun setup(): Unit {
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
  }
}

/**
 * EXERCISE 6
 *
 * Create a benchmark for lookup of an element by a property of the element, across lists, arrays,
 * and maps.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class LookupBenchmark {
  val Size       = 1000
  val IdToLookup = Size - 1

  data class Person(val id: Int, val age: Int, val name: String)

  val peopleList: List<Person> = List.range(0, Size).map { i -> Person(i, i, "Person ${i}") }

  @Setup(Level.Trial)
  fun setup(): Unit {
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
    blackhole.consume(peopleList.find { it.id == IdToLookup }!!)
  }
}

/**
 * GRADUATION PROJECT
 *
 * Develop a new immutable collection type (`Chain`) that has O(1) for concatenation. Compare its
 * performance to at least two other collection types. Then augment this collection type with
 * iteration, so you can benchmark iteration against the other collection types.
 *
 * Think carefully about whether or not it is possible to have a single collection type that has
 * best-in-class performance across all operations. Why or why not?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class GraduationBenchmark {
  @Param("100", "1000", "10000")
  var size: Int = 0

  @Benchmark
  fun concat(blackhole: Blackhole): Unit {
    var i = 0
    var c = Chain.make(1)
    while (i < size) {
      c = c.append(c)
      i = i + 1
    }
    blackhole.consume(c)
  }

  class Chain<out A>() {

    companion object {
      fun <A> make(vararg values: A): Chain<A> = Chain.empty() // TODO
      fun empty(): Chain<Nothing> = Chain()

    }

    fun <A> append(that: Chain<A>): Chain<A> = Chain.empty() // TODO
  }
}
