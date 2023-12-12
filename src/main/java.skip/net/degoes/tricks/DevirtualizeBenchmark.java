package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import java.util.Random;
import java.util.stream.Stream;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis"})
@Threads(16)
public class DevirtualizeBenchmark {
  Random rng = new Random(0L);

  @Param({"10000", "100000"})
  int size = 0;

  Op[] virtual_ops = null;

  @Setup
  public void setup() {
    Random rng = new Random(0L);
    virtual_ops = Stream.generate(() -> {
      Op result = null;
      switch (rng.nextInt(6)) {
        case 0:
          result = Inc;
          break;
        case 1:
          result = Dec;
          break;
        case 2:
          result = Mul2;
          break;
        case 3:
          result = Div2;
          break;
        case 4:
          result = Neg;
          break;
        case 5:
          result = Abs;
          break;
      }
      return result;
    }).limit(size).toArray(Op[]::new);
  }

  @Benchmark
  public void virtualized(Blackhole blackhole) {
    int current = 0;
    int i       = 0;
    while (i < size) {
      Op op = virtual_ops[i];
      current = op.apply(current);
      i = i + 1;
    }
  }

  @Benchmark
  public void devirtualized(Blackhole blackhole) {
  }

  abstract class Op {
    abstract int apply(int x);
  }
  
  Op Inc = new Op() {
    int apply(int x) { return x + 1; }
  };
  Op Dec = new Op() {
    int apply(int x) { return x - 1; }
  };
  Op Mul2 = new Op() {
    int apply(int x) { return x * 2; }
  };
  Op Div2 = new Op() {
    int apply(int x) { return x / 2; }
  };
  Op Neg = new Op() {
    int apply(int x) { return -x; }
  };
  Op Abs = new Op() {
    int apply(int x) { return Math.abs(x); }
  };
}
