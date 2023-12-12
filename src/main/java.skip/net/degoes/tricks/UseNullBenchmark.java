package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import java.util.function.Supplier;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis"})
@Threads(16)
public class UseNullBenchmark {
  @Param({"10000", "1000000"})
  int size = 0;

  abstract class Optional<A> {
    Optional<A> orElse(Supplier<Optional<A>> that) {
      if (this.equals(None)) {
        return that.get();
      } else {
        return this;
      }
    }
  }
  
  class Some<A> extends Optional<A> {
    A value;
    public Some(A value) {
      this.value = value;
    }
  }

  Optional None = new Optional() {

  };

  @Benchmark
  public void optionals(Blackhole blackhole) {
    int i                    = 0;
    Optional<String> current = new Some("a");
    int cutoff               = size - 10;
    
    while (i < size) {
      if (i > cutoff) {
        current = None;
      } else {
        current = current.orElse(() -> new Some("a"));
      }
      i = i + 1;
    }

    blackhole.consume(current);
  }

  @Benchmark
  public void nulls(Blackhole blackhole) {
  }
}