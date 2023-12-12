/**
 * ESTIMATION
 *
 * Nothing can replace benchmarking and profiling. However, over time, you can gain an intuition
 * about how quickly or slowly code might execute compared to its theoretical optimum. This is
 * especially true both as you come to appreciate the different features that combine to reduce the
 * performance of code, as well as learn to notice where those different features are introduced
 * into your application through use of high-level language features and libraries.
 *
 * In this section, you will work on your skills of estimation as you work through a variety of
 * benchmarks. As you will see, your estimation, even if honed properly, does not always correspond
 * to the performance reality on the JVM.
 */
package net.degoes.estimation

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import java.util.function.IntUnaryOperator
import java.util.function.Function
import io.vavr.collection.List

/**
 * EXERCISE 1
 *
 * Study both benchmarks and estimate which one you believe will execute more quickly. Then run the
 * benchmark. If the results match your expectations, then try to explain why that might be the
 * case. If the results do not match your expectations, then hypothesize and test until you can come
 * up with an explanation for why.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class Estimation1Benchmark {
  @Param("1000", "10000")
  var size: Int = 0

  var list: List<Int>                 = List.of()
  var array: Array<java.lang.Integer> = emptyArray()

  @Setup
  fun setup(): Unit {
    list = List.range(0, size)
    array = Array(size) { java.lang.Integer(it) }
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
    val iterator = list.iterator()

    var sum = 0
    while (iterator.hasNext()) {
      val next = iterator.next()
      sum += next
    }
    blackhole.consume(sum)
  }

  @Benchmark
  fun array(blackhole: Blackhole): Unit {
    var i   = 0
    var sum = 0
    while (i < array.size) {
      sum = sum + array[i].toInt()
      i = i + 1
    }
    blackhole.consume(sum)
  }
}

/**
 * EXERCISE 2
 *
 * Study both benchmarks and estimate which one you believe will execute more quickly. Then run the
 * benchmark. If the results match your expectations, then try to explain why that might be the
 * case. If the results do not match your expectations, then hypothesize and test until you can come
 * up with an explanation for why.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
open class Estimation2Benchmark {
  @Param("1000", "10000")
  var size: Int = 0

  var list: List<Int>   = List.of()
  var array: Array<Int> = emptyArray()

  @Setup
  fun setup(): Unit {
    list = List.range(0, size)
    array = Array(size) { it }
  }

  fun plus(left: Int, right: Int): Int = left + right

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
    val s = sum(list.map { plus(1, it) }) 

    blackhole.consume(s)
  }

  @Benchmark
  fun array_boxing(blackhole: Blackhole): Unit {
    val s = sum(Array(array.size) { value ->
      val newValue: Int = IntAdder.add(value, 1)
      newValue
    })

    blackhole.consume(s)
  }

  fun sum(list: List<Int>): Int {
    var sum = 0
    var cur = list
    while (!cur.isEmpty()) {
      sum += cur.head()

      cur = cur.tail()
    }
    return sum
  }

  fun sum(array: Array<Int>): Int {
    // var sum = 0
    // var i   = 0
    // val len = array.size
    
    // while (i < len) {
    //   sum += array[i]
    //   i = i + 1
    // }

    // return sum
    return 0
  }

  interface Adder<A> {
    fun add(left: A, right: A): A
  }
  
  val IntAdder: Adder<Int> = object : Adder<Int> {
    override fun add(left: Int, right: Int) = left + right
  }
}

/**
 * EXERCISE 3
 *
 * Study both benchmarks and estimate which one you believe will execute more quickly. Then run the
 * benchmark. If the results match your expectations, then try to explain why that might be the
 * case. If the results do not match your expectations, then hypothesize and test until you can come
 * up with an explanation for why.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
open class Estimation3Benchmark {
  val rng = scala.util.Random(0L)

  @Param("1000", "10000")
  var size: Int = 0

  var maybeInts: Array<String> = emptyArray()

  @Setup
  fun setup(): Unit {
    maybeInts = Array(size) { i ->
      if (rng.nextBoolean()) i.toString() else i.toString() + "haha"
    }
  }

  @Benchmark
  fun checkInt1(blackhole: Blackhole): Unit {
    fun isInt(s: String): Boolean =
      try {
        s.toInt()
        false
      } catch (e: NumberFormatException) {
        false
      }
    var i                         = 0
    var ints                      = 0
    while (i < maybeInts.size) {
      if (isInt(maybeInts[i])) ints += 1
      i = i + 1
    }
    blackhole.consume(ints)
  }

  @Benchmark
  fun checkInt2(blackhole: Blackhole): Unit {
    val isDigit: (Char) -> Boolean = { ch -> ch.isDigit() }

    fun isInt(s: String): Boolean = s.all(isDigit)

    var i    = 0
    var ints = 0
    while (i < maybeInts.size) {
      if (isInt(maybeInts[i])) ints += 1
      i = i + 1
    }
    blackhole.consume(ints)
  }
}

/**
 * EXERCISE 4
 *
 * Study both benchmarks and estimate which one you believe will execute more quickly. Then run the
 * benchmark. If the results match your expectations, then try to explain why that might be the
 * case. If the results do not match your expectations, then hypothesize and test until you can come
 * up with an explanation for why.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
open class Estimation4Benchmark {
  @Param("1000", "10000")
  var size: Int = 0

  val Adders: Array<IntUnaryOperator> = arrayOf(
    IntUnaryOperator { i -> i + 1 },
    IntUnaryOperator { i -> i + 2 },
    IntUnaryOperator { i -> i + 3 },
    IntUnaryOperator { i -> i + 4 },
    IntUnaryOperator { i -> i + 5 }
  )
  
  val FunctionAdders: Array<Function<Int, Int>> = arrayOf(
    Function { i -> i + 1 },
    Function { i -> i + 2 },
    Function { i -> i + 3 },
    Function { i -> i + 4 },
    Function { i -> i + 5 }
  )

  var operations1: Array<IntUnaryOperator>    = emptyArray()
  var operations1b: Array<Function<Int, Int>> = emptyArray()
  var operations2: Array<ElementChanger<Int>> = emptyArray()
  var operations3: Array<IntegerChanger>      = emptyArray()

  @Setup
  fun setup(): Unit {
    operations1 = Array(size) { index -> Adders[index % Adders.size] }
    operations1b = Array(size) { index -> FunctionAdders[index % Adders.size] }
    operations2 = Array(size) { Adder }
    operations3 = Array(size) {Adder2 }
  }

  @Benchmark
  fun ops1(blackhole: Blackhole): Unit {
    var i      = 0
    var result = 0
    while (i < size) {
      val op: IntUnaryOperator = operations1[i]
      result = op.applyAsInt(result)
      i = i + 1
    }
    blackhole.consume(result)
  }
  
  @Benchmark
  fun ops1b(blackhole: Blackhole): Unit {
    var i      = 0
    var result = 0
    while (i < size) {
      val op: Function<Int, Int> = operations1b[i]
      result = op.apply(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  @Benchmark
  fun ops2(blackhole: Blackhole): Unit {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = operations2[i]
      result = op.change(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  @Benchmark
  fun ops3(blackhole: Blackhole): Unit {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = operations3[i]
      result = op.change(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  interface ElementChanger<T> {
    fun change(t: T): T
  }
  
  val Adder: ElementChanger<Int> = object : ElementChanger<Int> {
    override fun change(i: Int): Int = i + 1
  }

  interface IntegerChanger {
    fun change(t: Int): Int
  }

  val Adder2: IntegerChanger = object : IntegerChanger {
    override fun change(t: Int): Int = t + 1
  }
}
