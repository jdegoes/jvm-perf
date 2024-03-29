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

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import java.util.ArrayList

import net.degoes.project.dataset1
import net.degoes.project.dataset1.Field
import net.degoes.project.dataset1.Dataset
import net.degoes.project.dataset1.Row
import net.degoes.project.dataset1.Value

import io.vavr.collection.List

import zio.Chunk

/**
 * EXERCISE 1
 *
 * Use the flag "-XX:+PrintCompilation" to print out the JIT compilation of the benchmark. Is the
 * `fib` method compiled to native code by HotSpot?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class PrintCompilationBenchmark {
  @Param("10", "20")
  var depth: Int = 0

  fun fib(n: Int): Int = if (n <= 1) 1 else fib(n - 1) + fib(n - 2)

  @Benchmark
  fun fib(blackhole: Blackhole): Unit =
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
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class PrintInliningBenchmark {
  @Param("100", "1000", "10000")
  var size: Int = 0

  fun makeSize(i: Int): Size = Size(i)

  @Benchmark
  fun benchmark(blackhole: Blackhole): Unit {
    var i   = 0
    var sum = 0
    while (i < size) {
      val size = makeSize(i)
      sum = sum + size.value
      i = i + 1
    }
    blackhole.consume(sum)
  }

  data class Size(val value: Int)
}

/**
 * EXERCISE 3
 *
 * Profilers can be incredibly useful for identifying performance bottlenecks. Even though it is
 * hard to optimize against a profiling, a profiler can help you identify the most expensive
 * sections of code (in terms of CPU or memory), which you can then benchmark and optimize.
 *
 * In this exercise, you will take your benchmark tool of choice to identify performance bottlenecks
 * in the provided code. You can use this information in the next module.
 */
object ProfilerExample {
  fun main(args: Array<String>): Unit {

    val Size = 10_000

    val rng = scala.util.Random(0L)

    val start: Field  = Field("start")
    val end: Field    = Field("end")
    val netPay: Field = Field("netPay")

    val dataset = Dataset(Chunk.fill(Size) {
      val rndStart  = rng.between(0, 360)
      val rndEnd    = rng.between(rndStart, 360)
      val rndNetPay = rng.between(20000, 60000)

      Row(
        mapOf<String, Value>(
          "start"  to dataset1.Integer(rndStart.toLong()),
          "end"    to dataset1.Integer(rndEnd.toLong()),
          "netPay" to dataset1.Integer(rndNetPay.toLong())
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

/**
 * GRADUATION PROJECT
 *
 * Sometimes, you need to see something closer to the raw bytecode that your compiler generates.
 * This is especially true when you are using higher-level languages like Kotlin, Scala, and
 * Clojure, because these languages have features that do not map directly to JVM bytecode.
 *
 * In order to do this, you can use the `javap` method with the following flags:
 *
 *   - `-c` prints out the bytecode
 *   - `-l` prints out line numbers
 *   - `-p` prints out private methods
 *   - `-s` prints out internal type signatures
 *   - `-v` prints out verbose information
 *
 * In this exercise, you will use `javap` to see the bytecode generated by the Scala compiler for
 * the provided benchmark. Walk through the reverse-engineered code and try to understand any
 * sources of inefficiency that you see. Revise the inefficient code until `javap` shows you cleanly
 * generated code that you would expect to be fast.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis", "-XX:-Inline"))
@Threads(16)
open class JavapBenchmark {
  val rng = scala.util.Random(0L)

  @Param("1000", "10000", "100000")
  var size: Int = 0

  var program: State<Int, Int> = succeed<Int, Int>(0)

  @Setup(Level.Trial)
  fun setup(): Unit {
    program = (0 until size).fold(succeed<Int, Int>(0)) { acc, _ ->
      acc.flatMap { _ ->
        val state: State<Int, Int> = getState()
        state.flatMap { i ->
          setState(i + 1).map { _ -> i + 1 }
        }
      }
    }
  }

  @Benchmark
  fun benchmark(blackhole: Blackhole): Unit {
    program.execute(0)
  }

  open class State<S, out A>() {
    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> = FlatMap(this, f)

    fun <B> map(f: (A) -> B): State<S, B> = flatMap { x -> Succeed<S, B>(f(x)) }

    fun execute(state0: S): Pair<S, A> {
      fun erase(state: State<S, A>): State<S, Any> = state as State<S, Any>

      fun <A, B> eraseK(f: (A) -> State<S, B>): (A) -> State<S, Any> =
        f as ((A) -> State<S, Any>)

      fun <A> loop(): Pair<S, A> {
        var next: State<S, Any>?                    = erase(this)
        var state: S                                = state0
        var result: A?                              = null
        var stack: List<(Any) -> State<S, Any>>     = List.of()
        
        fun continueWith(value: Any): State<S, Any>? =
          if (stack.isEmpty()) {
            result = value as A
            null
          } else {
            val head = stack.first()
            stack = stack.drop(1)
            head(value)
          }

        while (next != null) {
          val safeNext: State<S, Any> = next!!
          next = when(safeNext) {
            is GetState<*> -> {
              continueWith(state as Any)
            }
            is SetState<*> -> {
              state = (safeNext as SetState<S>).s
              continueWith(Unit)
            }
            is FlatMap<*, *, *> -> {
              val prefix: (Any) -> State<S, Any> =
                eraseK<Any, Any> { (safeNext as FlatMap<S, Any, Any>).f(it) }

              stack = stack.prepend(prefix)
              (safeNext as FlatMap<S, Any, Any>).state
            }
            is Succeed<*, *> -> {
              continueWith((safeNext as Succeed<S, Any>).a)
            }
            else ->
              throw IllegalArgumentException()
          }
        }

        return (state to result!!)
      }

      return loop()
    }
  }
  
  class GetState<S>()                                                            : State<S, S>()
  data class SetState<S>(val s: S)                                               : State<S, Unit>()
  data class FlatMap<S, A, B>(val state: State<S, A>, val f: (A) -> State<S, B>) : State<S, B>()
  data class Succeed<S, A>(val a: A)                                             : State<S, A>()

  fun <S, A> succeed(a: A): State<S, A> = Succeed(a)
  fun <S> getState(): State<S, S> = GetState<S>()
  fun <S> setState(s: S): State<S, Unit> = SetState(s)
}
