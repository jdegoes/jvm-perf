package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import java.util.Collections;
import java.util.Arrays;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class SpecializeBenchmark {
  @Param({"10000", "100000"})
  int size = 0;

  GenericTree<Integer> genericTree = null;

  @Setup
  public void setupGenericTree() {
    int count = (int) Math.sqrt(size);

    GenericTree<Integer>[] leaves = (GenericTree<Integer>[]) new GenericTree[count];
    Arrays.fill(leaves, new Leaf(0));
    GenericTree<Integer> current = new Branch(leaves);

    int i = 0;
    while (i < count) {
      GenericTree<Integer>[] newArray = new GenericTree[1];
      newArray[0] = current;
      current = new Branch(newArray);
      i = i + 1;
    }

    genericTree = current;
  }

  int loop(GenericTree<Integer> tree) {
    if (tree instanceof Leaf<?>) {
      return ((Leaf<Integer>) tree).value;
    } else {
      GenericTree<Integer>[] children = ((Branch<Integer>) tree).children;

      int sum = 0;
      int i   = 0;
      while (i < children.length) {
        sum = sum + loop(children[i]);
        i = i + 1;
      }
      return sum;
    }
  }
  
  @Benchmark
  public void genericTree(Blackhole blackhole) {
    blackhole.consume(loop(genericTree));
  }

  @Benchmark
  public void intTree(Blackhole blackhole) {
  }

  abstract class GenericTree<A> {
  }

  class Leaf<A> extends GenericTree<A> {
    A value;
    Leaf(A value) {
      this.value = value;
    }
  }

  class Branch<A> extends GenericTree<A> {
    GenericTree<A>[] children;
    Branch(GenericTree<A>[] children) {
      this.children = children;
    }
  }
}
