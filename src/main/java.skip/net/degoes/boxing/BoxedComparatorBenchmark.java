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
public class BoxedComparatorBenchmark {
  @Param({"10", "100", "1000", "10000", "100000"})
  int size = 0;

  int[] ints = null;

  @Setup
  public void setup() {
    ints = new int[size];
    int i = 0;
    while (i < size) {
      ints[i] = 0;
      i = i + 1;
    }
  }

  @Benchmark
  public void boxed(Blackhole blackhole) {
    int i   = 0;
    int sum = 0;
    while (i < size) {
      sum = sum + IntGenericComparator.compare(ints[i], 0);
      i = i + 1;
    }
    blackhole.consume(sum);
  }

  abstract class Comparator<T> {
    abstract int compare(T l, T r);
  }
  
  Comparator<Integer> IntGenericComparator = new Comparator<Integer>() {
    int compare(Integer l , Integer r) {
      return l - r;
    }
  };
}