package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import scala.util.control.NoStackTrace;
import io.vavr.collection.List;
import java.util.Random;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class UseArraysBenchmark {
  private Random rng = new Random(0L);

  @Param({"10000", "100000"})
  int size = 0;

  List<Float> list = null;

  java.util.List<Float> builder = new java.util.ArrayList();
  
  @Setup
  public void setup() {
    list = List.ofAll(Stream.generate(() -> rng.nextFloat()).limit(size));
    builder.clear();
  }

  @Benchmark
  public void list(Blackhole blackhole) {
    int i                         = 0;
    List<Float> current           = list;
    float x1                      = current.head();
    float x2                      = current.head();
    builder.clear();
    
    while (i < size) {
      float x3 = current.head();

      builder.add((x1 + x2 + x3) / 3);

      current = current.tail();
      i = i + 1;
      x1 = x2;
      x2 = x3;
    }
    
    blackhole.consume(List.ofAll(builder));
  }

  @Benchmark
  public void array(Blackhole blackhole) {
  }
}
