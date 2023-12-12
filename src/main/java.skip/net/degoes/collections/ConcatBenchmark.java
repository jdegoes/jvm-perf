package net.degoes.collections;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import io.vavr.collection.List;
import java.util.Collections;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class ConcatBenchmark {
  @Setup(Level.Trial)
  public void setup() {
  }

  @Benchmark
  public void list(Blackhole blackhole) {

  }
}
