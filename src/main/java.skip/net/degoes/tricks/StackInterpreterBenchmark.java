package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import java.util.function.BiFunction;
import java.util.function.Function;
import io.vavr.control.Option;
import io.vavr.Tuple2;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {"-XX:-DoEscapeAnalysis"})
@Threads(16)
public class StackInterpreterBenchmark {

  // Parses: /users/{username}/posts/{post-id}
  RouteParser<Tuple2<String, Integer>> parser() {
    return Slash
      .zipRight(new Literal("users"))
      .zipRight(Slash)
      .zipRight(StringVar)
      .zipLeft(Slash)
      .zipLeft(new Literal("posts"))
      .zipLeft(Slash)
       .zip(IntVar);
  }

  @Benchmark
  public void classic(Blackhole blackhole) {
    blackhole.consume(parser().parse("/users/jdegoes/posts/123"));
  }

  @Benchmark
  public void interpreted(Blackhole blackhole) {}

  class Literal extends RouteParser<Void> {
    String value;
    
    Literal(String value) {
      this.value = value;
    }

    Option<Tuple2<Void, String>> parse(String path) {
      if (path.startsWith(value)) return Option.of(new Tuple2(null, path.substring(value.length())));
      else return Option.none();
    }
  }

  RouteParser<Void> Slash = new RouteParser<Void>() {
    Option<Tuple2<Void, String>> parse(String path) {
      if (path.startsWith("/")) return Option.of(new Tuple2(null, path.substring(1)));
      else return Option.none();
    }
  };

  RouteParser<String> StringVar = new RouteParser<String>() {
    Option<Tuple2<String, String>> parse(String path) {
      int idx = path.indexOf('/');
      if (idx == -1) return Option.of(new Tuple2(path, ""));
      else return Option.of(new Tuple2(path.substring(0, idx), path.substring(idx)));
    }
  };

  RouteParser<Integer> IntVar = new RouteParser<Integer>() {
    Option<Tuple2<Integer, String>> parse(String path) {
      int idx = path.indexOf('/');
      Option<Integer> option;
      if (idx == -1) {
        try {
          option = Option.of(Integer.parseInt(path));
        } catch (NumberFormatException ex) {
          option = Option.none();
        }
        return option.map(i -> new Tuple2(i, ""));
      } else {
        String seg = path.substring(0, idx);
        try {
          option = Option.of(Integer.parseInt(seg));
        } catch (NumberFormatException ex) {
          option = Option.none();
        }
        return option.map(i -> new Tuple2(i, path.substring(idx)));
      }
    }
  };

  abstract class RouteParser<A> {
    abstract Option<Tuple2<A, String>> parse(String path);

    <B, C> RouteParser<C> combineWith(RouteParser<B> that, BiFunction<A, B, C> f) {
      return new RouteParser.Combine(this, that, f);
    }

    <B> RouteParser<B> map(Function<A, B> f) {
      return new RouteParser.Map(this, f);
    }

    <B> RouteParser<Tuple2<A, B>> zip(RouteParser<B> that) {
      return combineWith(that, (left, right) -> new Tuple2(left, right));
    }

    <B> RouteParser<A> zipLeft(RouteParser<B> that) {
      return combineWith(that, (a, b) -> a);
    }

    <B> RouteParser<B> zipRight(RouteParser<B> that) {
      return combineWith(that, (a, b) -> b);
    }
    
    class Map<A, B> extends RouteParser<B> {
      RouteParser<A> parser;
      Function<A, B> f;
      
      Map(RouteParser<A> parser, Function<A, B> f) {
        this.parser = parser;
        this.f = f;
      }
      
      Option<Tuple2<B, String>> parse(String path) {
        return parser.parse(path).map(tuple -> new Tuple2(f.apply(tuple._1), tuple._2));
      }
    }

    class Combine<A, B, C> extends RouteParser<C> {
      RouteParser<A> left;
      RouteParser<B> right;
      BiFunction<A, B, C> f;
      Combine(RouteParser<A> left, RouteParser<B> right, BiFunction<A, B, C> f) {
        this.left = left;
        this.right = right;
        this.f = f;
      }

      Option<Tuple2<C, String>> parse(String path) {
        Option<Tuple2<A, String>> parsed1 = left.parse(path);
        var flatMapped = parsed1.flatMap(tuple -> {
          Option<Tuple2<B, String>> parsed2 = right.parse(tuple._2);
          Option<Tuple2<C, String>> mapped = parsed2.map(tuple2 -> new Tuple2(f.apply(tuple._1, tuple2._1), tuple2._2));
          return mapped;
        });
        return flatMapped;
      }
    }
  }
}