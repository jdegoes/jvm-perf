package net.degoes.tricks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import scala.util.control.NoStackTrace;
import scala.util.Random;
import java.util.stream.Collectors;
import zio.Chunk;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgsAppend = {})
@Threads(16)
public class MapToArrayBenchmark {

  @Param({"10000", "100000"})
  int size = 0;

  Random rng = new Random(0L);

  static Component Email = new Component("email");
  static Component Name  = new Component("name");
  static Component Phone = new Component("phone");
  static Component Age   = new Component("age");
  static Component Zip   = new Component("zip");
  static Component City  = new Component("city");
  static Component State = new Component("state");
  static Component Country = new Component("country");
  
  Operation Identity = new Operation() {};

  Chunk<Data> allData = null;
  Transformation transformation = new Transformation(
    HashMap.of(
      Email, Identity,
      Name, Identity,
      Phone, Identity,
      Age, Identity,
      Zip, Identity,
      City, Identity,
      State, Identity,
      Country, Identity
    )
  );

  @Setup 
  public void setup() {
    allData = Chunk.fill(size, () -> new Data(
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10),
      rng.nextString(10)
    ));
  }

  @Benchmark
  public void map(Blackhole blackhole) {
    var i = 0;
    while (i < size) {
      Data data = allData.apply(i);
      transformData(data, transformation);
      i = i + 1;
    }
  }

  void transformData(Data data, Transformation transformation) {
    transformation.map.forEach((component, operation) -> {
      if (component.equals(Email)) data.email = operation.apply(data.email);
      else if (component.equals(Name)) data.name = operation.apply(data.name);
      else if (component.equals(Phone)) data.phone = operation.apply(data.phone);
      else if (component.equals(Age)) data.age = operation.apply(data.age);
      else if (component.equals(Zip)) data.zip = operation.apply(data.zip);
      else if (component.equals(City)) data.city = operation.apply(data.city);
      else if (component.equals(State)) data.state = operation.apply(data.state);
      else if (component.equals(Country)) data.country = operation.apply(data.country);
    });
  }
  
  class Data {
    String email;
    String name;
    String phone;
    String age;
    String zip;
    String city;
    String state;
    String country;

    Data(
      String email,
      String name,
      String phone,
      String age,
      String zip,
      String city,
      String state,
      String country
    ) {
      this.email = email;
      this.name = name;
      this.phone = phone;
      this.age = age;
      this.zip = zip;
      this.city = city;
      this.state = state;
      this.country = country;
    }
  }
  class Transformation {
    Map<Component, Operation> map;
    Transformation(Map<Component, Operation> map) {
      this.map = map;
    }
  }

  static class Component {
    String name;
    Component(String name) {
      this.name = name;
    }

    private static Component[] componentArray = {Email, Name, Phone, Age, Zip, City, State, Country};

    static Chunk<Component> All = Chunk.fromArray(componentArray);
  }
  
  abstract class Operation {
    String apply(String value) {
      if (this.equals(Identity)) {
        return value;
      } else if (this instanceof Anonymize) {
        Anonymize anonymize = (Anonymize) this;
        if (anonymize.full) {
          return "*****";
        } else {
          return value.substring(0, 3) + "*****";
        }
      } else if (this instanceof Encrypt) {
        Encrypt encrypt = (Encrypt) this;

        return value.chars().map(c -> (char) (c ^ encrypt.key.hashCode())).mapToObj(String::valueOf).collect(Collectors.joining());
      } else if (this.equals(Uppercase)) {
        return value.toUpperCase();
      } else if (this instanceof Composite) {
        Composite composite = (Composite) this;
        return composite.right.apply(composite.left.apply(value));
      } else return value;
    }
  }
  
  class Anonymize extends Operation {
    boolean full;
    Anonymize(boolean full) {
      this.full = full;
    }
  }
  
  class Encrypt extends Operation {
    String key;
    Encrypt(String key) {
      this.key = key;
    }
  }

  Operation Uppercase = new Operation() {};
  
  class Composite extends Operation {
    Operation left;
    Operation right;

    Composite(Operation left, Operation right) {
      this.left = left;
      this.right = right;
    }
  }
}

