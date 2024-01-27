package net.degoes.tools;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.function.Function;
import io.vavr.collection.List;
import io.vavr.Tuple2;

@org.openjdk.jmh.annotations.State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis", "-XX:-Inline"})
@Threads(16)
public class JavapBenchmark {

  @Param({"1000", "10000", "100000"})
  int size = 0;

  State<Integer, Integer> program = null;

  @Setup(Level.Trial)
  public void setup() {
    program = List.range(0, size).foldLeft(State.succeed(0), ((acc, x) -> {
      return acc.flatMap(a -> {
        State<Integer, Integer> state = State.getState();
        return state.flatMap(i -> {
          return State.setState(i + 1).map(j -> i + 1);
        });
      });
    }));
  }

  @Benchmark
  public void benchmark(Blackhole blackhole) {
    program.execute(0);
  }
}

class State<S, A> {

  <B> State<S, B> flatMap(Function<A, State<S, B>> f) {
    return new FlatMap(this, f);
  }

  <B> State<S, B> map(Function<A, B> f) {
    return flatMap(v -> succeed(f.apply(v)));
  }

  static <S, A> State<S, A> succeed(A a) {
    return new Succeed(a);
  }

  static <S> State<S, S> getState() {
    return new GetState();
  }

  static <S> State<S, Unit> setState(S s) {
    return new SetState(s);
  }

  Tuple2<S, A> execute(S state0) {
    return loop(state0);
  }

  <A> State<S, Object> continueWith(
    Object value,
    AtomicReference<List<Function<Object, State<S, Object>>>> stack,
    AtomicReference<A> result
  ) {
    if (stack.get().isEmpty()) {
      result.set((A) value);
      return null;
    } else {
      return stack.getAndUpdate(list -> list.tail()).head().apply(value);
    }
  }

  <A, B> Function<A, State<S, Object>> eraseK(Function<A, B> f) {
    return (x -> (State<S, Object>) f.apply(x));
  }

  <A> Tuple2<S, A> loop(S state0) {
    AtomicReference<State<S, Object>> next = new AtomicReference((State<S, Object>) this);
    AtomicReference<S> state = new AtomicReference(state0);
    AtomicReference<A> result = new AtomicReference(null);
    AtomicReference<List<Function<Object, State<S, Object>>>> stack = new AtomicReference(List.of());

    while (next.get() != null) {
      Object current = next.get();
      if (current instanceof GetState<?>) {
        next.set(continueWith(state.get(), stack, result));
      } else if (current instanceof SetState<?> nextState) {
        state.set((S) nextState.s);
        next.set(continueWith(Unit.getInstance(), stack, result));
      } else if (current instanceof FlatMap<?, ?, ?> nextState) {
        stack.updateAndGet(list -> list.prepend((Function<Object, State<S, Object>>) eraseK(nextState.f)));
        next.set((State<S, Object>) nextState.state);
      } else if (current instanceof Succeed<?, ?> nextState) {
        next.set(continueWith(nextState.a, stack, result));
      } else throw new IllegalArgumentException();
    }
    
    return new Tuple2(state.get(), result.get());
  }
}

class GetState<S> extends State<S, S> {
}

class SetState<S> extends State<S, Unit> {
  S s;
  SetState(S s) {
    this.s = s;
  }
}

class Succeed<S, A> extends State<S, A> {
  A a;
  Succeed(A a) {
    this.a = a;
  }
}
class FlatMap<S, A, B> extends State<S, B> {
  State<S, A> state;
  Function<A, State<S, B>> f;
  FlatMap(State<S, A> state, Function<A, State<S, B>> f) {
    this.state = state;
    this.f = f;
  }
}

class Unit {
  private static Unit instance;
  static Unit getInstance() {
    if (instance == null) instance = new Unit();
    return instance;
  }
}
