package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import scala.util.Random;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class NoAllocationBenchmark {
  private Random rng = new Random(0L);

  String[] users = Stream.generate(() -> rng.nextString(10)).limit(1000).toArray(String[]::new);

  @Param({"1000", "10000"})
  int size = 0;

  Event[] events = null;

  @Setup
  public void setup() {
    events = Stream.generate(() -> {
      int userIdx = rng.nextInt(users.length);
      String userId = users[userIdx];
      Event event = null;

      switch(rng.between(0, 3)) {
        case 0:
          event = new AdView(userId);
        case 1:
          event = new AdClick(userId);
        case 2:
          event = new AdConversion(userId);
      }
      return event;
    }).limit(size).toArray(Event[]::new);
  }

  @Benchmark
  public void immutable(Blackhole blackhole) {
    int i       = 0;
    MetricsMap current = new MetricsMap(HashMap.empty());
    while (i < size) {
      Event event = events[i];
      current = current.aggregate(MetricsMap.apply(event));
      i = i + 1;
    }
    blackhole.consume(current);
  }

  @Benchmark
  public void mutable(Blackhole blackhole) {
  }

  static class Metrics {
    int adViews;
    int adClicks;
    int adConversions;
    
    Metrics(int adViews, int adClicks, int adConversions) {
      this.adViews = adViews;
      this.adClicks = adClicks;
      this.adConversions = adConversions;
    }
    
    Metrics aggregate(Metrics that) {
      return new Metrics(
        this.adViews + that.adViews,
        this.adClicks + that.adClicks,
        this.adConversions + that.adConversions
      );
    }
  }

  static class MetricsMap {
    Map<String, Metrics> map;

    MetricsMap(Map<String, Metrics> map) {
      this.map = map;
    }
    
    MetricsMap add(Event event) {
      return this.aggregate(MetricsMap.apply(event));
    }

    MetricsMap aggregate(MetricsMap that) {
      return new MetricsMap(combineWith(this.map, that.map, (left, right) -> left.aggregate(right)));
    }
    
    static MetricsMap apply(Event event) {
      Map<String, Metrics> map = null;
      if (event instanceof AdView) map = HashMap.of(event.userId, new Metrics(1, 0, 0));
      else if (event instanceof AdClick) map = HashMap.of(event.userId, new Metrics(0, 1, 0));
      else map = HashMap.of(event.userId, new Metrics(0, 0, 1));
      
      return new MetricsMap(map);
    }
  }

  static <K, V> Map<K, V> combineWith(Map<K, V> left, Map<K, V> right, BinaryOperator<V> f) {
    return left.foldLeft(right, (acc, kv) -> acc.put(kv._1, acc.get(kv._1).fold(() -> kv._2, x -> f.apply(x, kv._2))));
  }

  abstract class Event {
    String userId;
  }
  
  class AdView extends Event {
    AdView(String userId) {
      this.userId = userId;
    }
  }
  class AdClick extends Event {
    AdClick(String userId) {
      this.userId = userId;
    }
  }
  class AdConversion extends Event {
    AdConversion(String userId) {
      this.userId = userId;
    }
  }
}
