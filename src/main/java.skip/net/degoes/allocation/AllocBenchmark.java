package net.degoes.allocation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

import zio.Chunk;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class AllocBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  @Setup
  public void setup() {
  }

  @Benchmark
  public void alloc(Blackhole blackhole) {
    int sum = 0;
    int i   = 0;
    while (i < size) {
      sum = sum + (new Object().hashCode());
      i = i + 1;
    }
    blackhole.consume(sum);
  }

  @Benchmark
  public void noAlloc(Blackhole blackhole) {
  }
}