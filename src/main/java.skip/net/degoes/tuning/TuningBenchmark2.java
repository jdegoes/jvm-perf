package net.degoes.tuning;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class TuningBenchmark2 {
  @Param({"8000000"})
  int size = 0;

  @Benchmark
  public void constantHeap(Blackhole blackhole) {
    blackhole.consume(new byte[size]);
  }
}

