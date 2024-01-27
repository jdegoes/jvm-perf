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

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.util.control.NoStackTrace

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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis"))
@Threads(16)
class UseNullBenchmark {
  @Param(Array("10000", "1000000"))
  var size: Int = _

  sealed trait Optional[+A] {
    final def orElse[B >: A](that: => Optional[B]): Optional[B] =
      if (this == Optional.None) that else this
  }
  object Optional           {
    case class Some[A](value: A) extends Optional[A]
    case object None             extends Optional[Nothing]
  }

  @Benchmark
  def optionals(blackhole: Blackhole): Unit = {
    var i                         = 0
    var current: Optional[String] = Optional.Some("a")
    val cutoff                    = size - 10
    while (i < size) {
      if (i > cutoff) current = Optional.None
      else current = current.orElse(Optional.Some("a"))
      i = i + 1
    }
    blackhole.consume(current)
  }

  @Benchmark
  def nulls(blackhole: Blackhole): Unit = ()
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class UseArraysBenchmark {
  private val rng = new scala.util.Random(0L)

  @Param(Array("10000", "100000"))
  var size: Int = _

  var list: List[Float] = _

  @Setup
  def setup(): Unit =
    list = List.fill(size)(rng.nextFloat)

  @Benchmark
  def list(blackhole: Blackhole): Unit = {
    var i           = 0
    var current     = list
    val listBuilder = List.newBuilder[Float]
    var x1          = current.head
    var x2          = current.head
    while (i < size) {
      val x3 = current.head

      listBuilder += ((x1 + x2 + x3) / 3)

      current = current.tail
      i = i + 1
      x1 = x2
      x2 = x3
    }
    blackhole.consume(listBuilder.result())
  }

  @Benchmark
  def array(blackhole: Blackhole): Unit = ()
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class NoAllocationBenchmark {
  private val rng = new scala.util.Random(0L)

  val users: Array[String] = Array.fill(1000)(rng.nextString(10))

  @Param(Array("1000", "10000"))
  var size: Int = _

  var events: Array[Event] = _

  @Setup
  def setup(): Unit =
    events = Array.fill(size) {
      val userIdx = rng.nextInt(users.length)
      val userId  = users(userIdx)

      rng.between(0, 3) match {
        case 0 => Event.AdView(userId)
        case 1 => Event.AdClick(userId)
        case 2 => Event.AdConversion(userId)
      }
    }

  @Benchmark
  def immutable(blackhole: Blackhole): Unit = {
    var i       = 0
    var current = MetricsMap(Map.empty[UserId, Metrics])
    while (i < size) {
      val event = events(i)
      current = current.aggregate(MetricsMap(event))
      i = i + 1
    }
    blackhole.consume(current)
  }

  @Benchmark
  def mutable(blackhole: Blackhole): Unit = ()

  type UserId = String

  case class Metrics(adViews: Int, adClicks: Int, adConversions: Int) {
    def aggregate(that: Metrics): Metrics =
      Metrics(
        adViews = this.adViews + that.adViews,
        adClicks = this.adClicks + that.adClicks,
        adConversions = this.adConversions + that.adConversions
      )
  }

  case class MetricsMap(map: Map[UserId, Metrics]) { self =>
    def add(event: Event): MetricsMap =
      self.aggregate(MetricsMap(event))

    def aggregate(that: MetricsMap): MetricsMap =
      MetricsMap(
        map = combineWith(self.map, that.map)(_ aggregate _)
      )
  }
  object MetricsMap                                {
    def apply(event: Event): MetricsMap =
      event match {
        case Event.AdView(userId)       =>
          MetricsMap(Map(userId -> Metrics(1, 0, 0)))
        case Event.AdClick(userId)      =>
          MetricsMap(Map(userId -> Metrics(0, 1, 0)))
        case Event.AdConversion(userId) =>
          MetricsMap(Map(userId -> Metrics(0, 0, 1)))
      }
  }

  def combineWith[K, V](left: Map[K, V], right: Map[K, V])(f: (V, V) => V): Map[K, V] =
    left.foldLeft(right) { case (acc, (k, v)) =>
      acc.updated(k, acc.get(k).fold(v)(f(_, v)))
    }

  sealed trait Event {
    def userId: UserId
  }
  object Event       {
    case class AdView(userId: UserId)       extends Event
    case class AdClick(userId: UserId)      extends Event
    case class AdConversion(userId: UserId) extends Event
  }
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class SpecializeBenchmark {
  @Param(Array("10000", "100000"))
  var size: Int = _

  var genericTree: GenericTree[Int] = _

  @Setup
  def setupGenericTree(): Unit = {
    val count = Math.sqrt(size).toInt

    var current: GenericTree[Int] =
      GenericTree.Branch[Int](Array.fill(count)(GenericTree.Leaf(0)))

    var i = 0
    while (i < count) {
      current = GenericTree.Branch(Array(current))
      i = i + 1
    }

    genericTree = current
  }

  @Benchmark
  def genericTree(blackhole: Blackhole): Unit = {
    def loop(tree: GenericTree[Int]): Int =
      if (tree.isInstanceOf[GenericTree.Leaf[_]]) {
        tree.asInstanceOf[GenericTree.Leaf[Int]].value
      } else {
        val children = tree.asInstanceOf[GenericTree.Branch[Int]].children

        var sum = 0
        var i   = 0
        while (i < children.length) {
          sum = sum + loop(children(i))
          i = i + 1
        }
        sum
      }

    blackhole.consume(loop(genericTree))
  }

  @Benchmark
  def intTree(blackhole: Blackhole): Unit = ()

  sealed trait GenericTree[A]
  object GenericTree {
    case class Leaf[A](value: A)                        extends GenericTree[A]
    case class Branch[A](children: Seq[GenericTree[A]]) extends GenericTree[A]
  }
}

/**
 * EXERCISE 5
 *
 * NOTE: This exercise is Scala-specific (and, even specific to Scala platform and version). Feel
 * free to skip this if solving exercises in another JVM-based programming language.
 *
 * In Scala, pattern matching can impose overhead. To improve performance, you can replace pattern
 * matching by various techniques (for example, isInstanceOf).
 *
 * In this exercise, create a version of the benchmark that uses isInstanceOf instead of pattern
 * matching. Ensure it follows the same structure and flow of the existing benchmark in order to
 * make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class NoPatternMatchingBenchmark {
  val rng = new scala.util.Random(0L)

  @Param(Array("10000", "100000"))
  var size: Int = _

  var eithers: Array[Either[Int, Int]] = _

  @Setup
  def setup(): Unit = {
    val rng = new scala.util.Random(0L)

    eithers = Array.fill(size)(rng.nextInt(100)).map { n =>
      if (n % 2 == 0) Left(n) else Right(n)
    }
  }

  @Benchmark
  def patternMatching(blackhole: Blackhole): Unit = {
    var i      = 0
    var lefts  = 0
    var rights = 0
    while (i < size) {
      eithers(i) match {
        case Left(n)  => lefts = lefts + n
        case Right(n) => rights = rights + n
      }
      i = i + 1
    }
    blackhole.consume(lefts + rights)
  }

  @Benchmark
  def isInstanceOf(blackhole: Blackhole): Unit = ()
}

/**
 * EXERCISE 6
 *
 * In some cases, you can eliminate heap allocation in function return values by packing multiple
 * values into a single primitive value. For example, a 64 bit long can actually hold multiple
 * separate channels of information, and will not require heap allocation.
 *
 * In this exercise, create a version of the benchmark that uses a packed return value instead of
 * the provided case class. Ensure it follows the same structure and flow of the existing benchmark
 * in order to make a fair comparison.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-Inline", "-XX:-DoEscapeAnalysis"))
@Threads(16)
class PrimitivizeReturnBenchmark {
  case class Geolocation(precise: Boolean, lat: Int, long: Int)

  @Benchmark
  def unpacked(blackhole: Blackhole): Unit =
    blackhole.consume(Geolocation(true, 1, 2))

  @Benchmark
  def packed(blackhole: Blackhole): Unit = ()
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis"))
@Threads(16)
class FlattenProductsBenchmark {
  val rng = new scala.util.Random(0L)

  case class Billing(startDay: Int, endDay: Int, dailyRate: Double)

  @Param(Array("10000", "100000"))
  var size: Int = _

  var billings: Array[Billing] = _

  @Setup
  def setup(): Unit = {
    val rng = new scala.util.Random(0L)

    billings = Array.ofDim(size)
  }

  @Benchmark
  def unflattened(blackhole: Blackhole): Unit = {
    var i     = 0
    while (i < size) {
      val billing = Billing(0, 30, 300)
      billings(i) = billing
      blackhole.consume(billing)
      i = i + 1
    }
    i = 0
    var total = 0.0
    while (i < size) {
      val billing = billings(i)
      total = total + (billing.endDay - billing.startDay) * billing.dailyRate
      i = i + 1
    }
    blackhole.consume(total)
  }

  @Benchmark
  def flattened(blackhole: Blackhole): Unit = ()
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis"))
@Threads(16)
class DevirtualizeBenchmark {
  val rng = new scala.util.Random(0L)

  @Param(Array("10000", "100000"))
  var size: Int = _

  var virtual_ops: Array[Op] = _

  @Setup
  def setup(): Unit = {
    val rng = new scala.util.Random(0L)

    virtual_ops = Array.fill(size)(rng.between(0, 6) match {
      case 0 => Op.Inc
      case 1 => Op.Dec
      case 2 => Op.Mul2
      case 3 => Op.Div2
      case 4 => Op.Neg
      case 5 => Op.Abs
    })
  }

  @Benchmark
  def virtualized(blackhole: Blackhole): Unit = {
    var current = 0
    var i       = 0
    while (i < size) {
      val op = virtual_ops(i)
      current = op.apply(current)
      i = i + 1
    }
  }

  @Benchmark
  def devirtualized(blackhole: Blackhole): Unit = ()

  trait Op  {
    def apply(x: Int): Int
  }
  object Op {
    case object Inc  extends Op {
      def apply(x: Int): Int = x + 1
    }
    case object Dec  extends Op {
      def apply(x: Int): Int = x - 1
    }
    case object Mul2 extends Op {
      def apply(x: Int): Int = x * 2
    }
    case object Div2 extends Op {
      def apply(x: Int): Int = x / 2
    }
    case object Neg  extends Op {
      def apply(x: Int): Int = -x
    }
    case object Abs  extends Op {
      def apply(x: Int): Int = Math.abs(x)
    }
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis"))
@Threads(16)
class NoExceptionsBenchmark {
  val rng = new scala.util.Random(0L)

  @Param(Array("10000", "100000"))
  var size: Int = _

  case class Exception1(message: String) extends Exception {
    override def getMessage(): String = message
  }

  def maybeException1(): Int = {
    if (rng.nextBoolean()) throw Exception1("message")
    42
  }

  @Benchmark
  def throwException(blackhole: Blackhole): Unit = {
    var i = 0
    while (i < size) {
      try
        maybeException1()
      catch {
        case Exception1(message) => blackhole.consume(message)
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array())
@Threads(16)
class MapToArrayBenchmark {
  import zio.Chunk 

  @Param(Array("10000", "100000"))
  var size: Int = _ 

  val rng = new scala.util.Random(0L)

  var allData: Chunk[Data] = _ 
  val transformation = 
    Transformation(
      Map(
        Component.Email -> Operation.Identity,
        Component.Name -> Operation.Identity,
        Component.Phone -> Operation.Identity,
        Component.Age -> Operation.Identity,
        Component.Zip -> Operation.Identity,
        Component.City -> Operation.Identity,
        Component.State -> Operation.Identity,
        Component.Country -> Operation.Identity
      )
    )

  @Setup 
  def setup(): Unit = {
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
  def map(blackhole: Blackhole): Unit = {
    var i = 0
    while (i < size) {
      val data = allData(i)
      transformData(data, transformation)
      i = i + 1
    }
  }

  def transformData(data: Data, transformation: Transformation): Unit = {
    transformation.map.foreach { case (component, operation) =>
      component match {
        case Component.Email => 
          data.email = operation(data.email)
        case Component.Name =>
          data.name = operation(data.name)
        case Component.Phone =>
          data.phone = operation(data.phone)
        case Component.Age =>
          data.age = operation(data.age)
        case Component.Zip =>
          data.zip = operation(data.zip)
        case Component.City =>
          data.city = operation(data.city)
        case Component.State =>
          data.state = operation(data.state)
        case Component.Country =>
          data.country = operation(data.country)
        case _ => () 
      }
    }
  }
  case class Data(
    var email: String,
    var name: String,
    var phone: String,
    var age: String,
    var zip: String,
    var city: String,
    var state: String,
    var country: String
  )
  case class Transformation(map: Map[Component, Operation])
  case class Component(name: String)
  object Component {
    val All: Chunk[Component] = Chunk(
      Component.Email,
      Component.Name,
      Component.Phone,
      Component.Age,
      Component.Zip,
      Component.City,
      Component.State,
      Component.Country
    )

    val Email = Component("email")
    val Name  = Component("name")
    val Phone = Component("phone")
    val Age   = Component("age")
    val Zip   = Component("zip")
    val City  = Component("city")
    val State = Component("state")
    val Country = Component("country")
  }
  sealed trait Operation {
    def apply(value: String): String = 
      this match {
        case Operation.Identity => 
          value 
        case Operation.Anonymize(full) => 
          if (full) "*****"
          else value.take(3) + "*****"
        case Operation.Encrypt(key) => 
          value.map(c => (c ^ key.hashCode).toChar)
        case Operation.Uppercase => 
          value.toUpperCase
        case Operation.Composite(left, right) => 
          right(left(value))
      }
  }
  object Operation {
    case object Identity extends Operation
    case class Anonymize(full: Boolean) extends Operation 
    case class Encrypt(key: String) extends Operation 
    case object Uppercase extends Operation
    case class Composite(left: Operation, right: Operation) extends Operation
  }
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
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = Array("-XX:-DoEscapeAnalysis"))
@Threads(16)
class StackInterpreterBenchmark {
  import RouteParser._

  // Parses: /users/{username}/posts/{post-id}
  val parser: RouteParser[(String, Int)] =
    Slash *> Literal("users") *> Slash *> StringVar <* Slash <* Literal("posts") <* Slash <*> IntVar

  @Benchmark
  def classic(blackhole: Blackhole): Unit =
    blackhole.consume(parser.parse("/users/jdegoes/posts/123"))

  @Benchmark
  def interpreted(blackhole: Blackhole): Unit = ()

  sealed trait RouteParser[+A] {
    def parse(path: String): Option[(A, String)]

    def <*[B](that: RouteParser[B]): RouteParser[A] =
      this.zipLeft(that)

    def *>[B](that: RouteParser[B]): RouteParser[B] =
      this.zipRight(that)

    def <*>[B](that: RouteParser[B]): RouteParser[(A, B)] =
      this.zip(that)

    def combineWith[B, C](that: RouteParser[B])(f: (A, B) => C): RouteParser[C] =
      RouteParser.Combine(this, that, f)

    def map[B](f: A => B): RouteParser[B] =
      RouteParser.Map(this, f)

    def zip[B](that: RouteParser[B]): RouteParser[(A, B)] =
      this.combineWith(that)((_, _))

    def zipLeft[B](that: RouteParser[B]): RouteParser[A] =
      this.combineWith(that)((a, _) => a)

    def zipRight[B](that: RouteParser[B]): RouteParser[B] =
      this.combineWith(that)((_, b) => b)
  }
  object RouteParser           {
    case class Literal(value: String) extends RouteParser[Unit] {
      def parse(path: String): Option[(Unit, String)] =
        if (path.startsWith(value)) Some(((), path.substring(value.length)))
        else None
    }

    case object Slash extends RouteParser[Unit] {
      def parse(path: String): Option[(Unit, String)] =
        if (path.startsWith("/")) Some(((), path.substring(1)))
        else None
    }

    case object StringVar extends RouteParser[String] {
      def parse(path: String): Option[(String, String)] = {
        val idx = path.indexOf('/')
        if (idx == -1) Some((path, ""))
        else Some((path.substring(0, idx), path.substring(idx)))
      }
    }

    case object IntVar extends RouteParser[Int] {
      def parse(path: String): Option[(Int, String)] = {
        val idx = path.indexOf('/')
        if (idx == -1) {
          path.toIntOption.map(int => (int, ""))
        } else {
          val seg = path.substring(0, idx)

          seg.toIntOption.map(int => (int, path.substring(idx)))
        }
      }
    }

    case class Map[A, B](parser: RouteParser[A], f: A => B) extends RouteParser[B] {
      def parse(path: String): Option[(B, String)] =
        parser.parse(path).map { case (a, rest) => (f(a), rest) }
    }

    case class Combine[A, B, C](left: RouteParser[A], right: RouteParser[B], f: (A, B) => C)
        extends RouteParser[C] {
      def parse(path: String): Option[(C, String)] =
        left.parse(path).flatMap { case (a, path) =>
          right.parse(path).map { case (b, path) => (f(a, b), path) }
        }
    }
  }
}
