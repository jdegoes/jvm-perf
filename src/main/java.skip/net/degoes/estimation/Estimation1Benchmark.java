package net.degoes.estimation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.stream.IntStream;
import io.vavr.collection.List;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class Estimation1Benchmark {
  @Param({"1000", "10000"})
  int size = 0;

  List<Integer> list = null;
  Integer[] array = null;

  @Setup
  public void setup() {
    list = List.range(0, size);
    array = IntStream.range(0, size).boxed().toArray(Integer[]::new);
  }

  @Benchmark
  public void list(Blackhole blackhole) {
    Iterator<Integer> iterator = list.iterator();
    int sum = 0;
    
    while (iterator.hasNext()) {
      var next = iterator.next();
      sum = sum + next;
    }
    blackhole.consume(sum);
  }

  @Benchmark
  public void array(Blackhole blackhole) {
    int i   = 0;
    int sum = 0;
    while (i < array.length) {
      sum += array[i];
      i = i + 1;
    }
    blackhole.consume(sum);
  }
}