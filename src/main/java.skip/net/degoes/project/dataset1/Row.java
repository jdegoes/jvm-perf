package net.degoes.project.dataset1;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.concurrent.atomic.AtomicReference;
import zio.Chunk;
import scala.util.Random;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;

public class Row {
  Map<String, Value> map;
  
  public Row(Map<String, Value> map) {
    this.map = map;
  }

  Value apply(Field field) {
    return map.apply(field.name);
  }
}
