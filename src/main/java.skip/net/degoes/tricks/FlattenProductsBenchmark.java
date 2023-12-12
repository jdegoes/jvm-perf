package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import java.util.Random;


@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis"})
@Threads(16)
public class FlattenProductsBenchmark {
  Random rng = new Random(0L);

  class Billing {
    int startDay;
    int endDay;
    double dailyRate;

    public Billing(int startDay, int endDay, double dailyRate) {
      this.startDay = startDay;
      this.endDay = endDay;
      this.dailyRate = dailyRate;
    }
  }

  @Param({"10000", "100000"})
  int size = 0;

  Billing[] billings = null;

  @Setup
  public void setup() {
    Random rng = new Random(0L);

    billings = new Billing[size];
  }

  @Benchmark
  public void unflattened(Blackhole blackhole) {
    int i     = 0;
    while (i < size) {
      Billing billing = new Billing(0, 30, 300);
      billings[i] = billing;
      blackhole.consume(billing);
      i = i + 1;
    }
    i = 0;
    double total = 0.0;
    while (i < size) {
      Billing billing = billings[i];
      total = total + (billing.endDay - billing.startDay) * billing.dailyRate;
      i = i + 1;
    }
    blackhole.consume(total);
  }

  @Benchmark
  public void flattened(Blackhole blackhole) {
   }
}
