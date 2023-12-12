package net.degoes.tools;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import scala.util.Random;
import zio.Chunk;
import net.degoes.project.dataset1.*;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;

class ProfilerExample {
  public static void main(String[] args) {

    int Size = 10_000;

    Random rng = new Random(0L);

    Field start  = new net.degoes.project.dataset1.Field("start");
    Field end    = new Field("end");
    Field netPay = new Field("netPay");

    Dataset dataset = new Dataset(Chunk.fill(Size, () -> {
      int dstart  = rng.between(0, 360);
      int dend    = rng.between(dstart, 360);
      int dnetPay = rng.between(20000, 60000);

      return new Row(
        HashMap.of(
          "start" , new Value.Integer(dstart),
          "end"   , new Value.Integer(dend),
          "netPay", new Value.Integer(dnetPay)
        )
      );
    }));

    long i = 0L;
    while (i < 1_000_000) {
      //(dataset.apply(start) + dataset.apply(end)) / dataset.apply(netPay);
      i = i + 1L;
    }
  }
}