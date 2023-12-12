/**
 * ALLOCATION
 *
 * In theory, the JVM allocates by merely incrementing a pointer to the next free memory location,
 * making allocation extremely cheap. While mostly correct, this model of allocation is misleadingly
 * incomplete.
 *
 * Whatever must be allocated, must also be unallocated. In the JVM, this is the job of the garbage
 * collector, which must run to reclaim memory that is no longer in use. The process of garbage
 * collection is not free, but rather imposes significant cost on low-latency and high-performance
 * applications.
 *
 * In this section, you will explore the cost of allocation.
 */
package net.degoes.allocation

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import zio.Chunk

/**
 * EXERCISE 1
 *
 * Design a 'noAlloc' benchmark that attempts to follow the exact same process as the 'alloc'
 * benchmark, but without the allocation.
 *
 * HINT: Think about pre-allocation.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class AllocBenchmark {
  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  @Setup
  def setup(): Unit = {}

  @Benchmark
  def alloc(blackhole: Blackhole): Unit = {
    var sum = 0
    var i   = 0
    while (i < size) {
      sum = sum + (new {}.hashCode())
      i = i + 1
    }
    blackhole.consume(sum)
  }

  @Benchmark
  def noAlloc(blackhole: Blackhole): Unit = ()
}

/**
 * EXERCISE 2
 *
 * Design another 'noAlloc' benchmark that attempts to follow the exact same process as the 'alloc'
 * benchmark, but without the allocation. How many times faster is the no allocation benchmark?
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class CopyAllocBenchmark {
  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  var people: Chunk[Person] = _

  @Setup
  def setup(): Unit =
    people = Chunk.fromIterable(0 until size).map(Person(_))

  @Benchmark
  def alloc(): Unit =
    people.map(p => p.copy(age = p.age + 1))

  case class Person(var age: Int)
}

/**
 * GRADUATION PROJECT
 *
 * In order to better understand the process of garbage collection, in this exercise, you will
 * implement a toy mark/sweep garbage collector. It is only a toy because (a) it only considers on
 * -heap objects, and (b) it does not try to encode any information about the object graph into the
 * linear raw memory, but rather, uses high-level data structures that are easy to work with.
 *
 * Implement the mark/sweep algorithm in the `markSweep` benchmark by iterating over all objects in
 * the heap twice. In the first iteration, mark all objects that are reachable from the root object.
 * In the second iteration, sweep all objects that are not marked.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class MarkSweepBenchmark {
  val rng = new scala.util.Random(0L)

  val ObjSize = 10

  @Param(Array("1000", "10000", "100000"))
  var size: Int = _

  var heap: Heap              = _
  var rootObjects: Array[Obj] = _

  @Setup
  def setup(): Unit = {
    val objects = Array.fill(size)(Obj(false, Array.fill(ObjSize)(Data.Integer(0))))

    heap = Heap(objects)

    var i = 0
    while (i < size) {
      val obj = heap.objects(i)
      var j   = 0
      while (j < ObjSize) {
        if (rng.nextBoolean()) {
          val pointerObjIndex = rng.between(0, size)
          obj.data(j) = Data.Pointer(heap.objects(pointerObjIndex))
        }
        j = j + 1
      }

      i = i + 1
    }

    rootObjects = objects.take(10)
  }

  @Benchmark
  def markSweep(blackhole: Blackhole): Unit = ()

  sealed trait Data
  object Data {
    case class Integer(value: Int) extends Data
    case class Pointer(value: Obj) extends Data
  }
  case class Obj(var marked: Boolean, data: Array[Data])
  case class Heap(objects: Array[Obj])
}
