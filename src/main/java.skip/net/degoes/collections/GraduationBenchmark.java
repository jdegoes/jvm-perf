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
public class GraduationBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  @Benchmark
  public void concat(Blackhole blackhole) {
    int i = 0;
    Chain<Integer> c = Chain.make(1);
    
    while (i < size) {
      c = c.concat(c);
      i = i + 1;
    }
    blackhole.consume(c);
  }

  public static class Chain<A> {
    public Chain<A> concat(Chain<A> that) {
      return Chain.empty(); // TODO
    }
    
    public static <A> Chain<A> empty() {
      return new Chain<>();
    }

    public static <A> Chain<A> make(A... as) {
      return new Chain<>(); // TODO
    }
  }
}