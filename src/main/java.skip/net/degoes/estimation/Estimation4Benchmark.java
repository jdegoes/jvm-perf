package net.degoes.estimation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.function.IntUnaryOperator;


@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis", "-XX:-Inline"})
@Threads(16)
public class Estimation4Benchmark {
  @Param({"1000", "10000"})
  int size = 0;

  IntUnaryOperator[] Adders = {i -> i + 1, i -> i + 2, i -> i + 3, i -> i + 4, i -> i + 5};

  IntUnaryOperator[] operations1        = null;
  ElementChanger<Integer>[] operations2 = null;
  IntegerChanger[] operations3          = null;

  @Setup
  public void setup() {
    operations1 = IntStream.range(0, size).mapToObj(index -> Adders[index % Adders.length]).toArray(IntUnaryOperator[]::new);
    operations2 = new ElementChanger[size];
    Arrays.fill(operations2, Adder);
    operations3 = new IntegerChanger[size];
    Arrays.fill(operations3, Adder2);
  }

  @Benchmark
  public void ops1(Blackhole blackhole) {
    int i      = 0;
    int result = 0;
    while (i < size) {
      IntUnaryOperator op = operations1[i];
      result = op.applyAsInt(result);
      i = i + 1;
    }
    blackhole.consume(result);
  }

  @Benchmark
  public void ops2(Blackhole blackhole) {
    int i      = 0;
    int result = 0;
    while (i < size) {
      var op = operations2[i];
      result = op.change(result);
      i = i + 1;
    }
    blackhole.consume(result);
  }

  @Benchmark
  public void ops3(Blackhole blackhole) {
    int i      = 0;
    int result = 0;
    while (i < size) {
      var op = operations3[i];
      result = op.change(result);
      i = i + 1;
    }
    blackhole.consume(result);
  }

  @FunctionalInterface
  interface ElementChanger<T> {
    abstract T change(T t);
  }
  ElementChanger<Integer> Adder = i -> i + 1;

  abstract class IntegerChanger {
    abstract int change(int t);
  }
  
  IntegerChanger Adder2 = new IntegerChanger() {
    int change(int t) {
      return t + 1;
    }
  };
}