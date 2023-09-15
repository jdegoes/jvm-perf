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

import org.openjdk.jmh.annotations._
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
 * because the JVM knows the concrete type of the object, when it invokes the apply method, it knows
 * exactly where the code for that function is, and does not need to perform a preliminary lookup.
 * This should result in faster performance.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class PolyBenchmark {
  @Param(Array("1000", "10000", "100000"))
  var size: Int = _

  var poly_operators: Chunk[Operator] = _
  // var mono_operators: Chunk[Operator.DividedBy.type] = _

  @Setup
  def setupPoly(): Unit =
    poly_operators = Operator.randomN(size)

  @Benchmark
  def poly(blackhole: Blackhole): Unit = {
    var i      = 0
    var result = 0
    while (i < size) {
      val operator = poly_operators(i)

      result = operator(result, i + 1)

      i = i + 1
    }
    blackhole.consume(result)
  }

  trait Operator  {
    def apply(l: Int, r: Int): Int
  }
  object Operator {
    // Deterministic RNG:
    private val rng = new scala.util.Random(0L)

    val All: IndexedSeq[Operator] =
      Array(Plus, Times, DividedBy, Max, Min)

    case object Plus      extends Operator {
      def apply(l: Int, r: Int): Int = l + r
    }
    case object Times     extends Operator {
      def apply(l: Int, r: Int): Int = l + r
    }
    case object DividedBy extends Operator {
      def apply(l: Int, r: Int): Int = l + r
    }
    case object Max       extends Operator {
      def apply(l: Int, r: Int): Int = l + r
    }
    case object Min       extends Operator {
      def apply(l: Int, r: Int): Int = l + r
    }

    def random(): Operator = All(rng.nextInt(All.length))

    def randomN(n: Int): Chunk[Operator] = Chunk.fromIterable(Iterable.fill(n)(random()))

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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class PolySimBenchmark {
  val obj: JVMObject             =
    JVMObject(1, JVMClassMetadata("Dog", Map(JVMMethod("Dog", "bark") -> Address(0))))
  val is: Bytecode.InvokeStatic  = Bytecode.InvokeStatic(Address(0))
  val iv: Bytecode.InvokeVirtual = Bytecode.InvokeVirtual(JVMMethod("Dog", "bark"))

  @Benchmark
  def invokeStatic(blackhole: Blackhole): Unit =
    blackhole.consume(is.address.value)

  case class JVMObject(data: Any, meta: JVMClassMetadata)
  case class JVMClassMetadata(clazz: String, vtable: Map[JVMMethod, Address])
  case class JVMMethod(clazz: String, name: String)
  case class Address(value: Int)
  sealed trait Bytecode
  object Bytecode {
    case object Mul                             extends Bytecode
    case class InvokeStatic(address: Address)   extends Bytecode
    case class InvokeVirtual(method: JVMMethod) extends Bytecode
  }
}
