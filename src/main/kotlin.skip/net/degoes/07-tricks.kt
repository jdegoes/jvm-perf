/**
 * TRICKS
 *
 * Until now, you have discovered many sources of overhead in developing software for the JVM.
 * Although you have some idea of how to avoid these sources of overhead, there has been no
 * systematic treatment of different techniques that can be applied to each type of overhead.
 *
 * In this section, you will learn some of the essential "tricks of the trade". In the process, you
 * will become proficient at writing fast code when the occassion requires.
 */
package net.degoes.tricks

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.util.control.NoStackTrace
import zio.Chunk
import io.vavr.collection.HashMap
import io.vavr.collection.List

/**
 * EXERCISE 1
 *
 * Because the JVM supports null values, you can use the null value as an extra "sentinal" value,
 * rather than using a wrapper data structure to propagate the same information. This can reduce
 * allocation and indirection and improve performance.
 *
 * In this exercise, create a version of the benchmark that uses null values instead of the
 * `Optional` data type. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis"))
@Threads(16)
open class UseNullBenchmark {
  @Param("10000", "1000000")
  var size: Int = 0

  open class Optional<out A>()

  fun <B> Optional<B>.orElse(that: () -> Optional<B>): Optional<B> =
    if (this is None) that() else this

  data class Some<A>(val value: A) : Optional<A>()
  object None                      : Optional<Nothing>()

  @Benchmark
  fun optionals(blackhole: Blackhole): Unit {
    var i                         = 0
    var current: Optional<String> = Some("a")
    val cutoff                    = size - 10
    while (i < size) {
      if (i > cutoff) current = None
      else current = current.orElse { Some("a") }
      i = i + 1
    }
    blackhole.consume(current)
  }

  fun <B> otherwise(opt: B?, other: () -> B?): B? = if (opt == null) opt else other()

  @Benchmark
  fun nulls(blackhole: Blackhole): Unit {
  }
}

/**
 * EXERCISE 2
 *
 * Arrays exploit CPU caches and primitive specialization, which means they can be tremendously
 * faster for certain tasks.
 *
 * In this exercise, create a version of the benchmark that uses arrays instead of lists. Ensure it
 * follows the same structure and flow of the existing benchmark in order to make a fair comparison.
 *
 * See if you can create an Array-based version that is 10x faster than the List-based version.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class UseArraysBenchmark {
  private val rng = scala.util.Random(0L)

  @Param("10000", "100000")
  var size: Int = 0

  var list: List<Float> = List.empty()

  @Setup
  fun setup(): Unit {
    list = List.fill(size) { rng.nextFloat() }
  }

  class ListBuilder<A>() {
    private var list: List<A> = List.empty()
    
    fun append(value: A): Unit {
      list = list.prepend(value)
    }

    fun result(): List<A> = list
  }

  @Benchmark
  fun list(blackhole: Blackhole): Unit {
    var i           = 0
    var current     = list
    val listBuilder = ListBuilder<Float>()
    var x1          = current.head()
    var x2          = current.head()
    while (i < size) {
      val x3 = current.head()

      listBuilder.append((x1 + x2 + x3) / 3)

      current = current.tail()
      i = i + 1
      x1 = x2
      x2 = x3
    }
    blackhole.consume(listBuilder.result())
  }

  @Benchmark
  fun array(blackhole: Blackhole): Unit {}
}

/**
 * EXERCISE 3
 *
 * Although the JVM can optimize away some allocations in some cases, it's safer and more reliable
 * to simply avoid them in performance-sensitive code. This means using mutable structures, and
 * sometimes re-using structures (using pools, pre-allocation, and other techniques).
 *
 * In this exercise, create a version of the benchmark that uses a mutable data structure instead of
 * an immutable data structure. Ensure it follows the same structure and flow of the existing
 * benchmark in order to make a fair comparison.
 *
 * BONUS: Try to solve the problem using zero allocations in the benchmark. You will have to use
 * pre-allocation in order to achieve this goal.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class NoAllocationBenchmark {
  private val rng = scala.util.Random(0L)

  val users: Array<String> = Array(1000) { rng.nextString(10) }

  @Param("1000", "10000")
  var size: Int = 0

  var events: Array<Event> = emptyArray()

  @Setup
  fun setup(): Unit {
    events = Array(size) {
      val userIdx = rng.nextInt(users.size)
      val userId  = users[userIdx]

      when(rng.between(0, 3)) {
        0 -> AdView(userId)
        1 -> AdClick(userId)
        else -> AdConversion(userId)
      }
    }
  }

  @Benchmark
  fun immutable(blackhole: Blackhole): Unit {
    var i       = 0
    var current = MetricsMap(HashMap.empty<String, Metrics>())
    while (i < size) {
      val event = events[i]
      current = current.aggregate(MetricsMap(event))
      i = i + 1
    }
    blackhole.consume(current)
  }

  @Benchmark
  fun mutable(blackhole: Blackhole): Unit {}

  data class Metrics(val adViews: Int, val adClicks: Int, val adConversions: Int) {
    fun aggregate(that: Metrics): Metrics =
      Metrics(
        this.adViews + that.adViews,
        this.adClicks + that.adClicks,
        this.adConversions + that.adConversions
      )
  }

  data class MetricsMap(val map: HashMap<String, Metrics>) {
    fun add(event: Event): MetricsMap = aggregate(MetricsMap(event))

    fun aggregate(that: MetricsMap): MetricsMap =
      MetricsMap(combineWith(this.map, that.map, { left: Metrics, right: Metrics -> left.aggregate(right) }))
    
    companion object {
      fun <K, V> combineWith(left: HashMap<K, V>, right: HashMap<K, V>, f: (V, V) -> V): HashMap<K, V> {
        return left.keySet().fold(right) { acc: HashMap<K, V>, k: K ->
          acc.put(k, (acc.get(k).map { f(it, left.get(k).get()) }).getOrElse { left.get(k).get() })
        }
      }
      
      operator fun invoke(event: Event): MetricsMap =
        when(event) {
          is AdView ->
            MetricsMap(HashMap.of(event.userId, Metrics(1, 0, 0)))
          is AdClick ->
            MetricsMap(HashMap.of(event.userId, Metrics(0, 1, 0)))
          is AdConversion ->
            MetricsMap(HashMap.of(event.userId, Metrics(0, 0, 1)))
          else ->
            throw IllegalArgumentException()
        }
    }
  }

  open class Event()

  data class AdView(val userId: String)       : Event()
  data class AdClick(val userId: String)      : Event()
  data class AdConversion(val userId: String) : Event()
}

/**
 * EXERCISE 4
 *
 * The JVM implements generics with type erasure, which means that generic data types must box all
 * primitives. In cases where you are using generic data types for primitives, it can make sense to
 * manually specialize the generic data types to your specific primitives. Although this creates
 * much more boilerplate, it allows you to improve performance.
 *
 * In this exercise, create a version of the benchmark that uses a specialized data type instead of
 * a generic data type. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class SpecializeBenchmark {
  @Param("10000", "100000")
  var size: Int = 0

  var genericTree: GenericTree<Int> = Leaf<Int>(0)

  @Setup
  fun setupGenericTree(): Unit {
    val count: Int = Math.sqrt(size.toDouble()).toInt()

    var current: GenericTree<Int> =
      Branch<Int>(Array(count) { Leaf(0) })

    var i = 0
    while (i < count) {
      current = Branch(arrayOf(current))
      i = i + 1
    }

    genericTree = current
  }

  @Benchmark
  fun genericTree(blackhole: Blackhole): Unit {
    fun loop(tree: GenericTree<Int>): Int {
      return if (tree is Leaf<Int>) {
        tree.value
      } else if (tree is Branch<Int>) {
        val children = tree.children

        var sum = 0
        var i   = 0
        while (i < children.size) {
          sum = sum + loop(children[i])
          i = i + 1
        }
        sum
      } else throw IllegalAccessError()
    }

    blackhole.consume(loop(genericTree))
  }

  @Benchmark
  fun intTree(blackhole: Blackhole): Unit {}

  interface GenericTree<A>
  data class Leaf<A>(val value: A)                        : GenericTree<A>
  data class Branch<A>(val children: Array<GenericTree<A>>) : GenericTree<A>
}

/**
 * EXERCISE 6
 *
 * In some cases, you can eliminate heap allocation in function return values by packing multiple
 * values into a single primitive value. For example, a 64 bit long can actually hold multiple
 * separate channels of information, and will not require heap allocation.
 *
 * In this exercise, create a version of the benchmark that uses a packed return value instead of
 * the provided data class. Ensure it follows the same structure and flow of the existing benchmark
 * in order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-Inline", "-XX:-DoEscapeAnalysis"))
@Threads(16)
open class PrimitivizeReturnBenchmark {
  data class Geolocation(val precise: Boolean, val lat: Int, val long: Int)

  @Benchmark
  fun unpacked(blackhole: Blackhole): Unit =
    blackhole.consume(Geolocation(true, 1, 2))

  @Benchmark
  fun packed(blackhole: Blackhole): Unit {}
}

/**
 * EXERCISE 7
 *
 * If you are processing data in bulk, and the fields of your data type are all primitives, then you
 * can reduce heap allocation by using arrays of the individual fields, rather than arrays of the
 * data type. This reduces allocation and indirection and improves cache hits.
 *
 * In this exercise, create a version of the benchmark that uses arrays of primitives instead of
 * arrays of the provided data type. Ensure it follows the same structure and flow of the existing
 * benchmark in order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis"))
@Threads(16)
open class FlattenProductsBenchmark {
  val rng = scala.util.Random(0L)

  data class Billing(val startDay: Int, val endDay: Int, val dailyRate: Double)

  @Param("10000", "100000")
  var size: Int = 0

  var billings: Array<Billing> = emptyArray()

  @Setup
  fun setup(): Unit {
    billings = Array(size) { Billing(0, 0, 0.0) }
  }

  @Benchmark
  fun unflattened(blackhole: Blackhole): Unit {
    var i     = 0
    while (i < size) {
      val billing = Billing(0, 30, 300.0)
      billings[i] = billing
      blackhole.consume(billing)
      i = i + 1
    }
    i = 0
    var total = 0.0
    while (i < size) {
      val billing = billings[i]
      total = total + (billing.endDay - billing.startDay) * billing.dailyRate
      i = i + 1
    }
    blackhole.consume(total)
  }

  @Benchmark
  fun flattened(blackhole: Blackhole): Unit {}
}

/**
 * EXERCISE 8
 *
 * Virtual dispatch imposes overhead in any case where the JVM cannot devirtualize. If the number of
 * subtypes sharing the same (virtual) interface is fixed, then you can manually devirtualize by
 * using an integer tag to indicate which subtype should be used. Then in any case where you would
 * call a virtual method, you instead match on the tag, and call the concrete (non-virtual) method
 * corresponding to that tag.
 *
 * In this exercise, create a version of the benchmark that uses manual devirtualization instead of
 * virtual dispatch. Ensure it follows the same structure and flow of the existing benchmark in
 * order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis"))
@Threads(16)
open class DevirtualizeBenchmark {
  val rng = scala.util.Random(0L)

  @Param("10000", "100000")
  var size: Int = 0

  var virtual_ops: Array<Op> = emptyArray()

  @Setup
  fun setup(): Unit {
    val rng = scala.util.Random(0L)

    virtual_ops = Array(size) {
      when(rng.between(0, 6)) {
        0 -> Inc
        1 -> Dec
        2 -> Mul2
        3 -> Div2
        4 -> Neg
        else -> Abs
      }
    }
  }

  @Benchmark
  fun virtualized(blackhole: Blackhole): Unit {
    var current = 0
    var i       = 0
    while (i < size) {
      val op = virtual_ops[i]
      current = op(current)
      i = i + 1
    }
  }

  @Benchmark
  fun devirtualized(blackhole: Blackhole): Unit {}

  interface Op  {
    operator fun invoke(x: Int): Int
  }
  object Inc  : Op {
    override operator fun invoke(x: Int): Int = x + 1
  }
  object Dec  : Op {
    override operator fun invoke(x: Int): Int = x - 1
  }
  object Mul2 : Op {
    override operator fun invoke(x: Int): Int = x * 2
  }
  object Div2 : Op {
    override operator fun invoke(x: Int): Int = x / 2
  }
  object Neg  : Op {
    override operator fun invoke(x: Int): Int = -x
  }
  object Abs  : Op {
    override operator fun invoke(x: Int): Int = Math.abs(x)
  }
}

/**
 * EXERCISE 10
 *
 * Exceptions can impose overhead on performance-sensitive code. This overhead comes primarily from
 * stack traces. If you can avoid stack traces, then you can avoid most of the overhead of
 * exceptions.
 *
 * In this exercise, you will try two separate tricks to avoid poorly-performing exception- bound
 * code:
 *
 *   1. Use a return value instead of throwing an exception. 2. Throw a special exception type that
 *      does not generate a stack trace.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis"))
@Threads(16)
open class NoExceptionsBenchmark {
  val rng = scala.util.Random(0L)

  @Param("10000", "100000")
  var size: Int = 0

  data class Exception1(val message2: String) : Exception(message2)

  fun maybeException1(): Int {
    if (rng.nextBoolean()) throw Exception1("message")
    return 42
  }

  @Benchmark
  fun throwException(blackhole: Blackhole): Unit {
    var i = 0
    while (i < size) {
      try {
        maybeException1()
      } catch (ex: Exception) {
        blackhole.consume(ex.message)
      }
      i = i + 1
    }
  }

}

/**
  * EXERCISE 11
  * 
  * Hash maps can offer quite high performance (O(1)), but never as high performance as array 
  * lookups (lower constant factor). To accelerate some code, you can switch from using non-integer
  * sparse keys to using dense integer keys, which lets you replace the map with an array.
  *
  * In this exercise, create an equivalent implementation to the provided one that uses arrays
  * instead of maps and observe the effects on performance.
  */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = emptyArray())
@Threads(16)
open class MapToArrayBenchmark {

  @Param("10000", "100000")
  var size: Int = 0

  val rng = scala.util.Random(0L)

  var allData: Chunk<Data> = Chunk.fromArray(emptyArray<Data>())
  
  val Email = Component("email")
  val Name  = Component("name")
  val Phone = Component("phone")
  val Age   = Component("age")
  val Zip   = Component("zip")
  val City  = Component("city")
  val State = Component("state")
  val Country = Component("country")
  
  val All: Chunk<Component> = Chunk.fromArray(arrayOf(
    Email,
    Name,
    Phone,
    Age,
    Zip,
    City,
    State,
    Country
  ))
  
  val transformation = 
    Transformation(
      mapOf(
        Email to Identity,
        Name to Identity,
        Phone to Identity,
        Age to Identity,
        Zip to Identity,
        City to Identity,
        State to Identity,
        Country to Identity
      )
    )

  @Setup 
  fun setup(): Unit {
    allData = Chunk.fill(size) {
      Data(
        email = rng.nextString(10),
        name = rng.nextString(10),
        phone = rng.nextString(10),
        age = rng.nextString(10),
        zip = rng.nextString(10),
        city = rng.nextString(10),
        state = rng.nextString(10),
        country = rng.nextString(10)
      )
    }
  }

  @Benchmark
  fun map(blackhole: Blackhole): Unit {
    var i = 0
    while (i < size) {
      val data = allData.apply(i)
      transformData(data, transformation)
      i = i + 1
    }
  }

  fun transformData(data: Data, transformation: Transformation): Unit {
    transformation.map.forEach { (component, operation) ->
      when(component) {
        Email   -> data.email   = operation(data.email)
        Name    -> data.name    = operation(data.name)
        Phone   -> data.phone   = operation(data.phone)
        Age     -> data.age     = operation(data.age)
        Zip     -> data.zip     = operation(data.zip)
        City    -> data.city    = operation(data.city)
        State   -> data.state   = operation(data.state)
        Country -> data.country = operation(data.country)
      }
    }
  }
  data class Data(
    var email: String,
    var name: String,
    var phone: String,
    var age: String,
    var zip: String,
    var city: String,
    var state: String,
    var country: String
  )
  
  data class Transformation(val map: Map<Component, Operation>)
  data class Component(val name: String) {
  }
  
  interface Operation {
    operator fun invoke(value: String): String = 
      when(this) {
        Identity -> 
          value 
        is Anonymize -> 
          if (this.full) "*****" else value.take(3) + "*****"
        is Encrypt -> 
          value.map { c -> (c.toInt().xor(this.key.hashCode()).toChar()) }.joinToString()
        Uppercase -> 
          value.toUpperCase()
        is Composite -> 
          this.right(this.left(value))
        else ->
          throw IllegalArgumentException()
      }
  }
  
  object Identity : Operation
  data class Anonymize(val full: Boolean) : Operation 
  data class Encrypt(val key: String) : Operation 
  object Uppercase : Operation
  data class Composite(val left: Operation, val right: Operation) : Operation
}


/**
 * GRADUATION PROJECT
 *
 * Sometimes you can transform a process that is built using interfaces and classes (a so-called
 * "executable encoding") into something more primitive and fundamental that can be interpreted by a
 * stack machine. This has potential to significantly improve performance.
 *
 * In this graduation project, you will work with the instructor to implement this optimization for
 * a route parser, and see what sort of speedup you can achieve without changing the expressiveness
 * of the process.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = arrayOf("-XX:-DoEscapeAnalysis"))
@Threads(16)
open class StackInterpreterBenchmark {

  // Parses: /users/{username}/posts/{post-id}
  val parser: RouteParser<Pair<String, Int>> =
    Slash.zipRight(Literal("users")).zipRight(Slash).zipRight(StringVar).zipLeft(Slash).zipLeft(Literal("posts")).zipLeft(Slash).zip(IntVar)

  @Benchmark
  fun classic(blackhole: Blackhole): Unit =
    blackhole.consume(parser.parse("/users/jdegoes/posts/123"))

  @Benchmark
  fun interpreted(blackhole: Blackhole): Unit {}

  interface RouteParser<out A> {
    fun parse(path: String): Pair<A, String>?

    fun <B, C> combineWith(that: RouteParser<B>, f: (A, B) -> C): RouteParser<C> {
      return Combine(this, that, f)
    }

    fun <B> map(f: (A) -> B): RouteParser<B> =
      Map(this, f)

    fun <B> zip(that: RouteParser<B>): RouteParser<Pair<A, B>> =
      this.combineWith(that) { left, right -> left to right }

    fun <B> zipLeft(that: RouteParser<B>): RouteParser<A> =
      this.combineWith(that) { a, _ -> a }

    fun <B> zipRight(that: RouteParser<B>): RouteParser<B> =
      this.combineWith(that) { _, b -> b }
  }

  data class Literal(val value: String) : RouteParser<Unit> {
    override fun parse(path: String): Pair<Unit, String>? =
      if (path.startsWith(value)) (Unit to path.substring(value.length))
      else null
  }

  object Slash : RouteParser<Unit> {
    override fun parse(path: String): Pair<Unit, String>? =
      if (path.startsWith("/")) (Unit to path.substring(1))
      else null
  }

  object StringVar : RouteParser<String> {
    override fun parse(path: String): Pair<String, String>? {
      val idx = path.indexOf('/')
      return if (idx == -1) (path to "") else (path.substring(0, idx) to path.substring(idx))
    }
  }

  object IntVar : RouteParser<Int> {
    override fun parse(path: String): Pair<Int, String>? {
      val idx = path.indexOf('/')
      return if (idx == -1) path.toIntOrNull()?.let { int -> int to "" } else {
        val seg = path.substring(0, idx)
        seg.toIntOrNull()?.let { int -> int to path.substring(idx) }
      }
    }
  }

  data class Map<A, B>(val parser: RouteParser<A>, val f: (A) -> B) : RouteParser<B> {
    override fun parse(path: String): Pair<B, String>? {
      return parser.parse(path)?.let { (a, rest) -> f(a) to rest }
    }
  }

  data class Combine<A, B, C>(val left: RouteParser<A>, val right: RouteParser<B>, val f: (A, B) -> C) : RouteParser<C> {
    override fun parse(path: String): Pair<C, String>? =
      left.parse(path)?.let { (a, path) ->
        right.parse(path)?.let { (b, path) -> f(a, b) to path }
      }
  }
}
