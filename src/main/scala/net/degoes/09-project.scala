/**
 * GRADUATION PROJECT
 *
 * In this section, you will tie together everything you have learned in order to significantly
 * optimize the performance of JVM-based code.
 */
package net.degoes.project

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import zio.Chunk
import scala.util.Random

object dataset1 {
  sealed trait Value
  object Value {
    final case class Text(value: String)    extends Value
    final case class Integer(value: Long)   extends Value
    final case class Decimal(value: Double) extends Value
    case object NA                          extends Value
  }

  final case class Field(name: String)

  final case class Row(map: Map[String, Value]) {
    def apply(field: Field): Value = map(field.name)
  }

  final case class Dataset(rows: Chunk[Row]) { self =>
    def apply(field: Field): Dataset =
      Dataset(
        rows.map(row =>
          if (row.map.contains(field.name)) Row(Map(field.name -> row(field)))
          else Row(Map())
        )
      )

    def *(that: Dataset): Dataset =
      self.binary(that, "*") {
        case (Value.Integer(left), Value.Integer(right)) => Value.Integer(left * right)
        case (Value.Integer(left), Value.Decimal(right)) => Value.Decimal(left * right)
        case (Value.Decimal(left), Value.Decimal(right)) => Value.Decimal(left * right)
        case (Value.Decimal(left), Value.Integer(right)) => Value.Decimal(left * right)
      }

    def +(that: Dataset): Dataset =
      self.binary(that, "+") {
        case (Value.Integer(left), Value.Integer(right)) => Value.Integer(left + right)
        case (Value.Integer(left), Value.Decimal(right)) => Value.Decimal(left + right)
        case (Value.Decimal(left), Value.Decimal(right)) => Value.Decimal(left + right)
        case (Value.Decimal(left), Value.Integer(right)) => Value.Decimal(left + right)
      }

    def -(that: Dataset): Dataset =
      self.binary(that, "-") {
        case (Value.Integer(left), Value.Integer(right)) => Value.Integer(left - right)
        case (Value.Integer(left), Value.Decimal(right)) => Value.Decimal(left - right)
        case (Value.Decimal(left), Value.Decimal(right)) => Value.Decimal(left - right)
        case (Value.Decimal(left), Value.Integer(right)) => Value.Decimal(left - right)
      }

    def /(that: Dataset): Dataset =
      self.binary(that, "/") {
        case (Value.Integer(left), Value.Integer(right)) => Value.Integer(left / right)
        case (Value.Integer(left), Value.Decimal(right)) => Value.Decimal(left / right)
        case (Value.Decimal(left), Value.Decimal(right)) => Value.Decimal(left / right)
        case (Value.Decimal(left), Value.Integer(right)) => Value.Decimal(left / right)
      }

    private def binary(that: Dataset, symbol: String)(
      f: PartialFunction[(Value, Value), Value]
    ): Dataset =
      Dataset(self.rows.zip(that.rows).map { tuple =>
        val (leftRow, rightRow) = tuple

        val map =
          for {
            left                   <- leftRow.map
            (leftName, leftValue)   = left
            right                  <- rightRow.map
            (rightName, rightValue) = right
          } yield s"(leftName ${symbol} rightName)" ->
            ((leftValue, rightValue) match {
              case (left, right) if f.isDefinedAt((left, right)) => f((left, right))
              case (_, _)                                        => Value.NA
            })

        Row(map)
      })
  }
}

/**
 * GRADUATION PROJECT
 *
 * Develop a version of `Dataset` that has a similar API, but which is at least 10x as fast. See how
 * far you can push it (can you get to 100x?).
 *
 * You may assume data is completely homogeneous and that no values are null. However, if ambitious,
 * you may solve the same problem under the assumption of undefined values and heterogeneous data.
 */
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
class ProjectBenchmark {
  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  object benchmark1 {
    import dataset1._

    var dataset: Dataset = _

    val start: Field  = Field("start")
    val end: Field    = Field("end")
    val netPay: Field = Field("netPay")
  }

  @Setup
  def setupSlow(): Unit = {
    import benchmark1._
    import dataset1._

    val rng: Random = new Random(0L)

    dataset = Dataset(Chunk.fill(size) {
      val start  = rng.between(0, 360)
      val end    = rng.between(start, 360)
      val netPay = rng.between(20000, 60000)

      Row(
        Map(
          "start"  -> Value.Integer(start),
          "end"    -> Value.Integer(end),
          "netPay" -> Value.Integer(netPay)
        )
      )
    })
  }

  @Benchmark
  def baseline(blackhole: Blackhole): Unit = {
    import benchmark1._
    import dataset1._

    val result = (dataset(start) + dataset(end)) / dataset(netPay)

    blackhole.consume(result)
  }

}
