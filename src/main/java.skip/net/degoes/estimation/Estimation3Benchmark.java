package net.degoes.estimation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Arrays;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis", "-XX:-Inline"})
@Threads(16)
public class Estimation3Benchmark {
  Random rng = new Random(0L);

  @Param({"1000", "10000"})
  int size = 0;

  String[] maybeInts = null;

  @Setup
  public void setup() {
    maybeInts = IntStream.range(0, size)
        .mapToObj(i -> rng.nextBoolean()?Integer.toString(i):(Integer.toString(i) + "haha"))
        .toArray(String[]::new);
  }

  boolean isInt(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
    
  @Benchmark
  public void checkInt1(Blackhole blackhole) {
    int i                         = 0;
    int ints                      = 0;
    
    while (i < maybeInts.length) {
      if (isInt(maybeInts[i])) {
        ints += 1;
      }
      i = i + 1;
    }
    blackhole.consume(ints);
  }
  
  IntPredicate isDigit = ch -> Character.isDigit(ch);

  boolean isInt2(String s) {
    int i = 0;
    int len = s.length();
    while (i < len) {
      if (!isDigit.test(s.charAt(i))) return false;
      i += 1;
    }
    return true;
  }

  @Benchmark
  public void checkInt2(Blackhole blackhole) {

    int i    = 0;
    int ints = 0;
    
    while (i < maybeInts.length) {
      if (isInt2(maybeInts[i])) ints += 1;
      i = i + 1;
    }
    blackhole.consume(ints);
  }
}
