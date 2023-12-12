package net.degoes.boxing;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class BoxedBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  Boxed<Integer>[] boxed = null;

  @Setup
  public void setup() {
    boxed = new Boxed[size];
    int i = 0;
    while (i < size) {
      boxed[i] = new Boxed<Integer>(0);
      i = i + 1;
    }
  }

  @Benchmark
  public void boxed(Blackhole blackhole) {
    int i   = 0;
    int sum = 0;
    while (i < size) {
      int newValue = boxed[i].value + 1;
      boxed[i] = new Boxed(newValue);
      sum = sum + newValue;
      i = i + 1;
    }
    blackhole.consume(sum);
  }

  @Benchmark
  public void unboxed(Blackhole blackhole) {
  }

  public static class Boxed<T> {
    T value;
    
    public Boxed(T value) {
      this.value = value;
    }
  }
}