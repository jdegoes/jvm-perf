package net.degoes.project;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.Random;
import net.degoes.project.dataset1.*;
import io.vavr.collection.HashMap;

import zio.Chunk;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class ProjectBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  class benchmark1 {

    static Dataset dataset = null;

    static Field start  = new Field("start");
    static Field end    = new Field("end");
    static Field netPay = new Field("netPay");
  }

  @Setup
  public void setupSlow() {
    Random rng = new Random(0L);

    benchmark1.dataset = new Dataset(Chunk.fill(size, () -> {
      int start  = rng.between(0, 360);
      int end    = rng.between(start, 360);
      int netPay = rng.between(20000, 60000);

      return new Row(
        HashMap.of(
          "start", new Value.Integer(start),
          "end",   new Value.Integer(end),
          "netPay", new Value.Integer(netPay)
	)
      );
    }));
  }

  @Benchmark
  public void baseline(Blackhole blackhole) {
    var result = (benchmark1.dataset.apply(benchmark1.start).plus(benchmark1.dataset.apply(benchmark1.end))).divide(benchmark1.dataset.apply(benchmark1.netPay));
    blackhole.consume(result);
  }
}