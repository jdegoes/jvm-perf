package net.degoes.allocation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import scala.collection.JavaConverters;

import zio.Chunk;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class CopyAllocBenchmark {
  @Param({"100", "1000", "10000"})
  int size = 0;

  Chunk<Person> people = null;

  @Setup
  public void setup() {
    people = Chunk.fromIterator(JavaConverters.asScalaIterator(IntStream.range(0, size).boxed().map(i -> new Person(i)).iterator()));
  }

  @Benchmark
  public void alloc() {
    people.map(p -> p.copy(p.age + 1));
  }

  public static class Person {
    int age;
    
    public Person(int age) {
      this.age = age;
    }

    public static Person copy(int age) {
      return new Person(age);
    }
  }
}
