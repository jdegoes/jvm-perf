/**
 * GRADUATION PROJECT
 *
 * In this section, you will tie together everything you have learned in order to significantly
 * optimize the performance of JVM-based code.
 */
package net.degoes.project

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import zio.Chunk
import scala.util.Random

object dataset1 {
  interface Value
  final data class Text(val value: String)    : Value
  final data class Integer(val value: Long)   : Value
  final data class Decimal(val value: Double) : Value
  object NA                          : Value

  final data class Field(val name: String)

  final data class Row(val map: Map<String, Value>) {
    operator fun invoke(field: Field): Value = map[field.name]!!
  }

  final data class Dataset(val rows: Chunk<Row>) {
    operator fun invoke(field: Field): Dataset =
      Dataset(
        rows.map { row ->
          if (row.map.contains(field.name)) Row(mapOf(field.name to row(field)))
          else Row(emptyMap())
        }
      )

    operator fun times(that: Dataset): Dataset =
      binary(that, "*", { left, right ->
        when (left) {
          is Integer -> when (right) {
            is Integer -> Integer(left.value * right.value)
            is Decimal -> Decimal(left.value * right.value)
            else       -> NA
          }
          is Decimal -> when (right) {
            is Integer -> Decimal(left.value * right.value)
            is Decimal -> Decimal(left.value * right.value)
            else       -> NA
          }
          else       -> NA
        }
      })

    operator fun plus(that: Dataset): Dataset =
      binary(that, "+", { left, right ->
        when (left) {
          is Integer -> when (right) {
            is Integer -> Integer(left.value + right.value)
            is Decimal -> Decimal(left.value + right.value)
            else       -> NA
          }
          is Decimal -> when (right) {
            is Integer -> Decimal(left.value + right.value)
            is Decimal -> Decimal(left.value + right.value)
            else       -> NA
          }
          else       -> NA
        }
      })

    operator fun minus(that: Dataset): Dataset =
      binary(that, "-", { left, right ->
        when (left) {
          is Integer -> when (right) {
            is Integer -> Integer(left.value - right.value)
            is Decimal -> Decimal(left.value - right.value)
            else       -> NA
          }
          is Decimal -> when (right) {
            is Integer -> Decimal(left.value - right.value)
            is Decimal -> Decimal(left.value - right.value)
            else       -> NA
          }
          else       -> NA
        }
      })

    operator fun div(that: Dataset): Dataset =
      binary(that, "/", { left, right ->
        when (left) {
          is Integer -> when (right) {
            is Integer -> Integer(left.value / right.value)
            is Decimal -> Decimal(left.value / right.value)
            else       -> NA
          }
          is Decimal -> when (right) {
            is Integer -> Decimal(left.value / right.value)
            is Decimal -> Decimal(left.value / right.value)
            else       -> NA
          }
          else       -> NA
        }
      })

    fun binary(that: Dataset, symbol: String, f: (Value, Value) -> Value): Dataset {
      val chunk = (rows.zip(that.rows) as Chunk<scala.Tuple2<Row, Row>>).map { tuple -> Pair(tuple._1, tuple._2) }.map { (leftRow: Row, rightRow: Row) ->
        val newMap: HashMap<String, Value> = HashMap<String, Value>()
        leftRow.map.forEach { leftName: String, leftValue: Value ->
          rightRow.map.forEach { rightName: String, rightValue: Value ->
            val name = "(${leftName} ${symbol} ${rightName})"
            newMap.put(name, f(leftValue, rightValue))
          }
        }
        Row(newMap)
      }

      return Dataset(chunk)
    }
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
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
open class ProjectBenchmark {
  @Param("100", "1000", "10000")
  var size: Int = 0

  object benchmark1 {
    var dataset: dataset1.Dataset? = null

    val start: dataset1.Field  = dataset1.Field("start")
    val end: dataset1.Field    = dataset1.Field("end")
    val netPay: dataset1.Field = dataset1.Field("netPay")
  }

  @Setup
  fun setupSlow(): Unit {
    val rng: Random = Random(0L)

    benchmark1.dataset = dataset1.Dataset(Chunk.fill(size) {
      val start: Long  = rng.between(0, 360).toLong()
      val end: Long    = rng.between(start, 360).toLong()
      val netPay: Long = rng.between(20000, 60000).toLong()

      dataset1.Row(
        mapOf(
          "start"  to dataset1.Integer(start),
          "end"    to dataset1.Integer(end),
          "netPay" to dataset1.Integer(netPay)
        )
      )
    })
  }

  @Benchmark
  fun baseline(blackhole: Blackhole): Unit {
    val ds: dataset1.Dataset = benchmark1.dataset!!
    val result = (ds(benchmark1.start) + ds(benchmark1.end)) / ds(benchmark1.netPay)

    blackhole.consume(result)
  }

}
