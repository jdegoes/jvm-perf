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
public class ElementPrependBenchmark {
  int PrependsPerIteration = 100;

  @Param({"1000", "10000", "100000"})
  int size = 0;

  List<String> startList = null;

  @Setup(Level.Trial)
  public void setup() {
    startList = List.ofAll(Collections.nCopies(size, "a"));
  }

  @Benchmark
  public void list(Blackhole blackhole) {
    blackhole.consume(startList.prepend("a"));
  }
}
