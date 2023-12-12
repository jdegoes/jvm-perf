package net.degoes.tuning;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import io.vavr.collection.List;
import io.vavr.Tuple2;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class TuningBenchmark4 {
  @Param({"2", "4", "8"})
  int n = 0;

  @Benchmark
  public void nqueens(Blackhole blackhole) {
    queens(n);
  }

  boolean isAttacked(Tuple2<Integer, Integer> q1, Tuple2<Integer, Integer> q2) {
    return q1._1 == q2._1 ||
      q1._2 == q2._2 ||
      Math.abs(q2._1 - q1._1) == Math.abs(q2._2 - q1._2);
  }
    
  boolean isSafe(Tuple2<Integer, Integer> queen, List<Tuple2<Integer, Integer>> others) {
    return others.forAll(xy -> !isAttacked(queen, xy));
  }
    
  List<List<Tuple2<Integer, Integer>>> placeQueens(int k) {
    if (k == 0) return List.of(List.empty());
    else
      return placeQueens(k - 1).flatMap(queens -> 
        List.range(1, n + 1).filter(column -> 
          isSafe(new Tuple2(k, column), queens)
        ).map(column -> {
          List<Tuple2<Integer, Integer>> result = queens.prepend(new Tuple2(k, column));
          return result;
        })
      );

  }

  List<List<Tuple2<Integer, Integer>>> queens(int n) {
    return placeQueens(n);
  }
}
