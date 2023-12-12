package net.degoes.gotchas;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class MisleadingBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  Age getBoxedAge(int i) {
    return new Age(i);
  }

  @Benchmark
  public void boxed(Blackhole blackhole) {
    int i   = 0;
    int sum = 0;
    
    while (i < size) {
      Age age = getBoxedAge(i);
      sum = sum + age.value;
      i = i + 1;
    }
    blackhole.consume(sum);
  }

  class Age {
    int value;
    Age(int value) {
      this.value = value;
    }
  }
}

