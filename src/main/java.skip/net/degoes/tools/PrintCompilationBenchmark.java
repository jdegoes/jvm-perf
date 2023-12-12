package net.degoes.tools;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

@org.openjdk.jmh.annotations.State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class PrintCompilationBenchmark {
  @Param({"10", "20"})
  int depth = 0;

  int fib(int n) {
    if (n <= 1) return 1;
    else return fib(n - 1) + fib(n - 2);
  }

  @Benchmark
  public void fib(Blackhole blackhole) {
    blackhole.consume(fib(depth));
  }
}
