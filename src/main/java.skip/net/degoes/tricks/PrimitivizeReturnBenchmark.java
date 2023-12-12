package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;


@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-Inline", "-XX:-DoEscapeAnalysis"})
@Threads(16)
public class PrimitivizeReturnBenchmark {
  class Geolocation {
    boolean precise;
    int lat;
    int lng;
    Geolocation(boolean precise, int lat, int lng) {
      this.precise = precise;
      this.lat = lat;
      this.lng = lng;
    }
  }

  @Benchmark
  public void unpacked(Blackhole blackhole) {
    blackhole.consume(new Geolocation(true, 1, 2));
  }

  @Benchmark
  public void packed(Blackhole blackhole) {
  }
}
