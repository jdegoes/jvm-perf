package net.degoes.project.dataset1;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.concurrent.atomic.AtomicReference;
import zio.Chunk;
import scala.util.Random;
import io.vavr.collection.Map;
import net.degoes.project.dataset1.Field;
import io.vavr.collection.HashMap;

public abstract class Value {
  static Value NA = new Value() {};

  public static class Text extends Value {
    String value;
    public Text(String value) {
      this.value = value;
    }
  }

  public static class Integer extends Value {
    long value;
    public Integer(long value) {
      this.value = value;
    }
  }

  public static class Decimal extends Value {
    double value;
    public Decimal(double value) {
      this.value = value;
    }
  }

}