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

public class Dataset {
  Chunk<Row> rows;
  
  public Dataset(Chunk<Row> rows) {
    this.rows = rows;
  }

  public Dataset apply(Field field) {
    return new Dataset(
      rows.map(row -> {
        if (row.map.containsKey(field.name))
          return new Row(HashMap.of(field.name, row.apply(field)));
        else return new Row(HashMap.empty());
      })
    );
  }

  public Dataset times(Dataset that) {
    return binary(that, "*", (left, right) -> {
      if (left instanceof Value.Integer && right instanceof Value.Integer)
        return new Value.Integer(((Value.Integer) left).value * ((Value.Integer) right).value);

      if (left instanceof Value.Integer && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Integer) left).value * ((Value.Decimal) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Integer)
        return new Value.Decimal(((Value.Decimal) left).value * ((Value.Integer) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Decimal) left).value * ((Value.Decimal) right).value);

      throw new UnsupportedOperationException();
    });
  }

  public Dataset plus(Dataset that) {
    return binary(that, "+", (left, right) -> {
      if (left instanceof Value.Integer && right instanceof Value.Integer)
        return new Value.Integer(((Value.Integer) left).value + ((Value.Integer) right).value);

      if (left instanceof Value.Integer && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Integer) left).value + ((Value.Decimal) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Integer)
        return new Value.Decimal(((Value.Decimal) left).value + ((Value.Integer) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Decimal) left).value + ((Value.Decimal) right).value);

      throw new UnsupportedOperationException();
    });
  }

  public Dataset minus(Dataset that) {
    return binary(that, "-", (left, right) -> {
      if (left instanceof Value.Integer && right instanceof Value.Integer)
        return new Value.Integer(((Value.Integer) left).value - ((Value.Integer) right).value);

      if (left instanceof Value.Integer && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Integer) left).value - ((Value.Decimal) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Integer)
        return new Value.Decimal(((Value.Decimal) left).value - ((Value.Integer) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Decimal) left).value - ((Value.Decimal) right).value);

      throw new UnsupportedOperationException();
    });
  }

  public Dataset divide(Dataset that) {
    return binary(that, "/", (left, right) -> {
      if (left instanceof Value.Integer && right instanceof Value.Integer)
        return new Value.Integer(((Value.Integer) left).value / ((Value.Integer) right).value);

      if (left instanceof Value.Integer && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Integer) left).value / ((Value.Decimal) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Integer)
        return new Value.Decimal(((Value.Decimal) left).value / ((Value.Integer) right).value);

      if (left instanceof Value.Decimal && right instanceof Value.Decimal)
        return new Value.Decimal(((Value.Decimal) left).value / ((Value.Decimal) right).value);

      throw new UnsupportedOperationException();
    });
  }

  private Dataset binary(Dataset that, String symbol, BinaryOperator<Value> f) {
  
    Chunk<scala.Tuple2<Row, Row>> zipped = (Chunk<scala.Tuple2<Row, Row>>) rows.zip(that.rows);

    return new Dataset(zipped.map(tuple -> {
      Row leftRow = tuple._1;
      Row rightRow = tuple._2;

      AtomicReference<Map<String, Value>> newMap = new AtomicReference(HashMap.empty());
      
      leftRow.map.forEach((leftName, leftValue) -> {
        rightRow.map.forEach((rightName, rightValue) -> {
          String name = "(leftName "+symbol+" rightName)";
          try {
            newMap.getAndUpdate(map -> map.put(name, f.apply(leftValue, rightValue)));
          } catch (Exception ex) {
            newMap.getAndUpdate(map -> map.put(name, Value.NA));
          }
        });
      });

      return new Row(newMap.get());
    }));
  }
}