package net.degoes.tools;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

@org.openjdk.jmh.annotations.State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class PrintInliningBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  Size makeSize(int i) {
    return new Size(i);
  }

  @Benchmark
  public void benchmark(Blackhole blackhole) {
    int i   = 0;
    int sum = 0;
    while (i < size) {
      Size size = makeSize(i);
      sum = sum + size.value;
      i = i + 1;
    }
    blackhole.consume(sum);
  }

  class Size {
    int value;
    Size(int value) {
      this.value = value;
    }
  }
}
