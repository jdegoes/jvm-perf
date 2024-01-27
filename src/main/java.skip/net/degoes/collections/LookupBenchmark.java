package net.degoes.collections;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import io.vavr.collection.List;
import java.util.Collections;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class LookupBenchmark {
  int Size       = 1000;
  int IdToLookup = Size - 1;

  class Person {
    int id;
    int age;
    String name;

    Person(int id, int age, String name) {
      this.id = id;
      this.age = age;
      this.name = name;
    }
  }
  
  List<Person> peopleList = List.range(0, Size).map(i -> new Person(i, i, "Person "+i));

  @Setup(Level.Trial)
  public void setup() {
  }

  @Benchmark
  public void list(Blackhole blackhole) {
    blackhole.consume(peopleList.find(x -> x.id == IdToLookup).get());
  }
}
