package net.degoes.tuning;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class TuningBenchmark1 {
  @Param({"10000", "1000000"})
  int size = 0;

  @Param({"100000"})
  int numberOfObjects = 0;

  @Benchmark
  public void burstHeap(Blackhole blackhole) {
    int iter = 0;
    while (iter < 4) {
      java.util.ArrayList<byte[]> junk = new java.util.ArrayList<byte[]>(numberOfObjects);
      int j    = 0;
      while (j < numberOfObjects) {
        junk.add(new byte[size]);
        j = j + 1;
      }
      blackhole.consume(junk);
      iter = iter + 1;
    }
  }
}

