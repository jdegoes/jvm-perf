package net.degoes.estimation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import io.vavr.collection.List;
import java.util.stream.IntStream;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis", "-XX:-Inline"})
@Threads(16)
public class Estimation2Benchmark {
  @Param({"1000", "10000"})
  int size = 0;

  List<Integer> list = null;
  int[] array = null;

  @Setup
  public void setup() {
    list = List.range(0, size);
    array = IntStream.range(0, size).toArray();
  }

  int plus(int left, int right) {
    return left + right;
  }

  @Benchmark
  public void list(Blackhole blackhole) {
    int s = sum(list.map(i -> plus(1, i)));

    blackhole.consume(s);
  }

  @Benchmark
  public void array_boxing(Blackhole blackhole) {
    int s = sum(Arrays.stream(array).map(value -> {
      Integer newValue = IntAdder.add(value, 1);
      return newValue;
    }).toArray());

    blackhole.consume(s);
  }

  int sum(List<Integer> list) {
    int sum = 0;
    List<Integer> cur = list;
    while (!cur.isEmpty()) {
      sum += cur.head();

      cur = cur.tail();
    }
    return sum;
  }
  int sum(int[] array) {
    int sum = 0;
    int i   = 0;
    int len = array.length;
    while (i < len) {
      sum += array[i];
      i = i + 1;
    }
    return sum;
  }

  static abstract class Adder<A> {
    abstract A add(A left, A right);
  }
  
  Adder<Integer> IntAdder = new Adder<Integer>() {
    Integer add(Integer left, Integer right) {
      return left + right;
    }
  };
}
