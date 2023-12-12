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

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class Estimation1Benchmark {
  @Param(Array("1000", "10000"))
  var size: Int = _

  var list: List[Int]                 = _
  var array: Array[java.lang.Integer] = _

  @Setup
  def setup(): Unit = {
    list = List.from(0 until size)
    array = Array.from(0 until size).map(new java.lang.Integer(_))
  }

  @Benchmark
  def list(blackhole: Blackhole): Unit = {
    val iterator = list.iterator

    var sum = 0
    while (iterator.hasNext) {
      val next = iterator.next()
      sum += next
    }
    blackhole.consume(sum)
  }

  @Benchmark
  def array(blackhole: Blackhole): Unit = {
    var i   = 0
    var sum = 0
    while (i < array.length) {
      sum += array(i)
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
class Estimation2Benchmark {
  @Param(Array("1000", "10000"))
  var size: Int = _

  var list: List[Int]   = _
  var array: Array[Int] = _

  @Setup
  def setup(): Unit = {
    list = List.from(0 until size)
    array = Array.from[Int](0 until size)
  }

  def plus(left: Int, right: Int): Int = left + right

  @Benchmark
  def list(blackhole: Blackhole): Unit = {
    val s = sum(list.map(plus(1, _)))

    blackhole.consume(s)
  }

  @Benchmark
  def array_boxing(blackhole: Blackhole): Unit = {
    val s = sum(array.map { value =>
      val newValue = IntAdder.add(value, 1)

      newValue
    })

    blackhole.consume(s)
  }

  def sum(list: List[Int]): Int   = {
    var sum = 0
    var cur = list
    while (!cur.isEmpty) {
      sum += cur.head

      cur = cur.tail
    }
    sum
  }
  def sum(array: Array[Int]): Int = {
    var sum = 0
    var i   = 0
    val len = array.length
    while (i < len) {
      sum += array(i)
      i = i + 1
    }
    sum
  }

  trait Adder[A] {
    def add(left: A, right: A): A
  }
  val IntAdder: Adder[Int] =
    (left: Int, right: Int) => left + right
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
class Estimation3Benchmark {
  val rng = new scala.util.Random(0L)

  @Param(Array("1000", "10000"))
  var size: Int = _

  var maybeInts: Array[String] = _

  @Setup
  def setup(): Unit =
    maybeInts = Array.from((0 until size).map { int =>
      if (rng.nextBoolean()) int.toString()
      else int.toString() + "haha"
    })

  @Benchmark
  def checkInt1(blackhole: Blackhole): Unit = {
    def isInt(s: String): Boolean =
      try {
        s.toInt
        false
      } catch {
        case _: NumberFormatException => false
      }
    var i                         = 0
    var ints                      = 0
    while (i < maybeInts.length) {
      if (isInt(maybeInts(i))) ints += 1
      i = i + 1
    }
    blackhole.consume(ints)
  }

  @Benchmark
  def checkInt2(blackhole: Blackhole): Unit = {
    val isDigit: Char => Boolean = _.isDigit

    def isInt(s: String): Boolean = s.forall(isDigit)

    var i    = 0
    var ints = 0
    while (i < maybeInts.length) {
      if (isInt(maybeInts(i))) ints += 1
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
class Estimation4Benchmark {
  @Param(Array("1000", "10000"))
  var size: Int = _

  val Adders: Array[Int => Int] =
    Array(_ + 1, _ + 2, _ + 3, _ + 4, _ + 5)

  var operations1: Array[Int => Int]          = _
  var operations2: Array[ElementChanger[Int]] = _
  var operations3: Array[IntegerChanger]      = _

  @Setup
  def setup(): Unit = {
    operations1 = Array.from((0 until size).map(index => Adders(index % Adders.length)))
    operations2 = Array.fill(size)(Adder)
    operations3 = Array.fill(size)(Adder2)
  }

  @Benchmark
  def ops1(blackhole: Blackhole): Unit = {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = operations1(i)
      result = op(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  @Benchmark
  def ops2(blackhole: Blackhole): Unit = {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = operations2(i)
      result = op.change(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  @Benchmark
  def ops3(blackhole: Blackhole): Unit = {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = operations3(i)
      result = op.change(result)
      i = i + 1
    }
    blackhole.consume(result)
  }

  trait ElementChanger[T] {
    def change(t: T): T
  }
  val Adder: ElementChanger[Int] = (i: Int) => i + 1

  trait IntegerChanger {
    def change(t: Int): Int
  }
  val Adder2: IntegerChanger =
    new IntegerChanger {
      def change(t: Int): Int = t + 1
    }
}
