package net.degoes.virtual;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.stream.Stream;
import scala.collection.JavaConverters;

import zio.Chunk;

@org.openjdk.jmh.annotations.State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class PolyBenchmark {
  @Param({"1000", "10000", "100000"})
  int size = 0;

  Chunk<Operator> poly_operators = null;
  // Chunk<Operator.DividedBy.type> mono_operators = null;

  @Setup
  public void setupPoly() {
    poly_operators = Operator.randomN(size);
  }

  @Benchmark
  public void poly(Blackhole blackhole) {
    int i      = 0;
    int result = 0;

    while (i < size) {
      Operator operator = poly_operators.apply(i);

      result = operator.apply(result, i + 1);

      i = i + 1;
    }
    blackhole.consume(result);
  }

  interface Operator  {
    int apply(int l, int r);

    // Deterministic RNG:
    static Random rng = new Random(0L);

    static Operator Plus = new Operator() {
      public int apply(int l, int r) {
        return l + r;
      }
    };

    static Operator Times = new Operator() {
      public int apply(int l, int r) {
        return l + r;
      }
    };

    static Operator DividedBy = new Operator() {
      public int apply(int l, int r) {
        return l + r;
      }
    };

    static Operator Max = new Operator() {
      public int apply(int l, int r) {
        return l + r;
      }
    };

    static Operator Min = new Operator() {
      public int apply(int l, int r) {
        return l + r;
      }
    };

    static Operator[] All = {Plus, Times, DividedBy, Max, Min};
    
    static Operator random() {
      return All[rng.nextInt(All.length)];
    }

    static Chunk<Operator> randomN(int n) {
      return Chunk.fromIterator(JavaConverters.asScalaIterator(Stream.generate(() -> random()).limit(n).iterator()));
    }
  }
}