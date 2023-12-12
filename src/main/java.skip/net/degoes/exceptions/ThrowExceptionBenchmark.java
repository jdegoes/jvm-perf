package net.degoes.exceptions;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class ThrowExceptionBenchmark {
  class MyException extends Exception {
    public MyException(String message) {
      super(message);
    }
  }

  @Benchmark
  public void throwCatchException() {
  }

  @Benchmark
  public void constructException() {
  }
}