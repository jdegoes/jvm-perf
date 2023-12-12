package net.degoes.allocation;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Arrays;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class MarkSweepBenchmark {
  Random rng = new Random(0L);

  int ObjSize = 10;

  @Param({"1000", "10000", "100000"})
  int size = 0;

  Heap heap         = null;
  Obj[] rootObjects = null;

  // @Setup
  // public void setup() {
  //   Obj[] objects = new Obj[size];
  //   int i = 0;
  //   while (i < size) {
  //     Data.Integer[] data = new Data.Integer[ObjSize];
  //     int j = 0;
      
  //     while (j < ObjSize) {
  //       data[j] = new Data.Integer(0);
  //       j = j + 1;
  //     }

  //     objects[i] = new Obj(false, data);
  //     i = i + 1;
  //   }

  //   heap = new Heap(objects);

  //   while (i < size) {
  //     Obj obj = heap.objects[i];
  //     int j = 0;
  //     while (j < ObjSize) {
  //       if (rng.nextBoolean()) {
  //         int pointerObjIndex = rng.nextInt(size);
  //         obj.data[j] = new Data.Pointer(heap.objects[pointerObjIndex]);
  //       }
  //       j = j + 1;
  //     }

  //     i = i + 1;
  //   }

  //   rootObjects = Arrays.copyOfRange(objects, 0, 10);
  // }

  @Benchmark
  public void markSweep(Blackhole blackhole) {
  }

  public static class Data {
    public static class Integer extends Data {
      int value;
      public Integer(int value) {
        this.value = value;
      }
    }

    public static class Pointer extends Data {
      Obj value;
      public Pointer(Obj value) {
        this.value = value;
      }
    }
  }

  static class Obj {
    boolean marked;
    Data[] data;

    public Obj(boolean marked, Data[] data) {
      this.marked = marked;
      this.data = data;
    }
  }

  static class Heap {
    Obj[] objects;
    public Heap(Obj[] objects) {
      this.objects = objects;
    }
  }
}