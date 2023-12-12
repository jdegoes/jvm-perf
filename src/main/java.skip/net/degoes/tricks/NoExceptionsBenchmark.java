package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import scala.util.control.NoStackTrace;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis"})
@Threads(16)
public class NoExceptionsBenchmark {
  Random rng = new Random(0L);

  @Param({"10000", "100000"})
  int size = 0;

  class Exception1 extends Exception {
    String message;
    public Exception1(String message) {
      this.message = message;
    }
    
    public String getMessage() {
      return message;
    }
  }

  int maybeException1() throws Exception1 {
    if (rng.nextBoolean()) {
      throw new Exception1("message");
    }
    return 42;
  }

  @Benchmark
  public void throwException(Blackhole blackhole) {
    int i = 0;
    while (i < size) {
      try {
        maybeException1();
      } catch (Exception1 ex) {
        blackhole.consume(ex.message);
      }
      i = i + 1;
    }
  }
}