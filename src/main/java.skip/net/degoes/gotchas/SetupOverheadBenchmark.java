package net.degoes.gotchas;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class SetupOverheadBenchmark {
  @Param({"100", "1000"})
  int maxFib = 0;

  @Param({"10", "100"})
  int fib = 0;

  int fibAcc(int n, int a, int b) {
    if (n == 0) return a;
    else return fibAcc(n - 1, b, a + b);
  }

  int fib(int n) {
    return fibAcc(n, 0, 1);
  }

  @Benchmark
  public void precomputedFib(Blackhole blackhole) {
    int[] precomputedFib = IntStream.rangeClosed(0, maxFib).map(n -> fib(n)).toArray();

    blackhole.consume(precomputedFib[fib]);
  }

  @Benchmark
  public void dynamicFib(Blackhole blackhole) {
    blackhole.consume(fib(fib));
  }
}