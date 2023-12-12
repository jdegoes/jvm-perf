package net.degoes.algorithms;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class FindMostPopularFriendBenchmark {
  @Benchmark
  public void findMostPopularFriend(Blackhole blackHole) {
  }
}
