/**
 * VIRTUAL DISPATCH
 *
 * Surprisingly, not all methods are equal: calling some methods can be quite fast, and calling
 * other methods can be dangerously slow, even if their implementations are *exactly* the same.
 *
 * This surprising fact is due to the way that object-oriented languages implement polymorphism.
 * Polymorphism allows us to write code that is generic over a type. For example, we might have some
 * business logic that can work with any key/value store, whether backed by a database, an in-memory
 * hash map, or a cloud API.
 *
 * In object-oriented programming languages, we achieve this type of polymorphism with inheritance,
 * and then implementing or overriding methods in a subtype.
 *
 * In this section, you will learn more about how this works, its impact on performance, and
 * potential workarounds for performance sensitive code.
 */
package net.degoes.virtual

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import zio.Chunk

/**
 * EXERCISE 1
 *
 * Every method invocation potentially goes through virtual dispatch, which is a process involving
 * looking up which concrete non-final method invocation is potentially a virtual dispatch.
 *
 * In this exercise, you will explore the cost of virtual dispatch. The current benchmark creates a
 * chunk of operators, each one of which is a random operator chosen from among the provided set. At
 * runtime, the JVM does not know which element of the chunk has which concrete type, so it must
 * lookup the correct method to invoke on an object-by-object basis. This results in lower
 * performance.
 *
 * Augment this benchmark with another benchmark, which uses another chunk, where every element of
 * the chunk uses the same concrete operator (e.g. Operator.DividedBy.type). In your new benchmark,
 * because the JVM knows the concrete type of the object, when it invokes the invoke method, it knows
 * exactly where the code for that function is, and does not need to perform a preliminary lookup.
 * This should result in faster performance.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class PolyBenchmark {
  @Param("1000", "10000", "100000")
  var size: Int = 0

  var poly_operators: Chunk<Operator>? = null
  // var mono_operators: Chunk<Operator.DividedBy.type>? = null

  @Setup
  fun setupPoly(): Unit {
    poly_operators = Operator.randomN(size)
  }

  @Benchmark
  fun poly(blackhole: Blackhole): Unit {
    var i      = 0
    var result = 0
    while (i < size) {
      val op = poly_operators!!.apply(i)

      result = op(result, i + 1)

      i = i + 1
    }
    blackhole.consume(result)
  }

  interface Operator  {
    operator fun invoke(l: Int, r: Int): Int

    companion object {
      // Deterministic RNG:
      private val rng = scala.util.Random(0L)

      val All: Array<Operator> = arrayOf(Plus, Times, DividedBy, Max, Min)

      object Plus      : Operator {
        override operator fun invoke(l: Int, r: Int): Int = l + r
      }
      object Times     : Operator {
        override operator fun invoke(l: Int, r: Int): Int = l + r
      }
      object DividedBy : Operator {
        override operator fun invoke(l: Int, r: Int): Int = l + r
      }
      object Max       : Operator {
        override operator fun invoke(l: Int, r: Int): Int = l + r
      }
      object Min       : Operator {
        override operator fun invoke(l: Int, r: Int): Int = l + r
      }

      fun random(): Operator = All[rng.nextInt(All.size)]

      fun randomN(n: Int): Chunk<Operator> = Chunk.fromArray(Array(n) { random() })

    }
  }
}

/**
 * EXERCISE 2
 *
 * In this exercise, you will simulate the cost of a virtual dispatch by creating a benchark that
 * must lookup the correct method based on the virtual method table stored together with the data
 * for an object.
 *
 * Create an invokeVirtual benchmark that uses `obj.meta` to find the address of the method to be
 * invoked. Compare the performance of this benchmark to the invokeStatic benchmark.
 *
 * Note that this benchmark is not that realistic. There is no hash map lookup with invoke dynamic.
 * Nonetheless, getting a feel for the extra work the JVM must do to perform a virtual dispatch is
 * useful.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class PolySimBenchmark {
  val obj: JVMObject =
    JVMObject(1, JVMClassMetadata("Dog", mapOf(JVMMethod("Dog", "bark") to Address(0))))
  
  val invs: InvokeStatic  = InvokeStatic(Address(0))
  val invv: InvokeVirtual = InvokeVirtual(JVMMethod("Dog", "bark"))

  @Benchmark
  fun invokeStatic(blackhole: Blackhole): Unit =
    blackhole.consume(invs.address.value)

  data class JVMObject(val dat: Any, val meta: JVMClassMetadata)
  data class JVMClassMetadata(val clazz: String, val vtable: Map<JVMMethod, Address>)
  data class JVMMethod(val clazz: String, val name: String)
  data class Address(val value: Int)
  
  interface Bytecode
  
  object Mul : Bytecode
  data class InvokeStatic(val address: Address) : Bytecode
  data class InvokeVirtual(val method: JVMMethod) : Bytecode
}
