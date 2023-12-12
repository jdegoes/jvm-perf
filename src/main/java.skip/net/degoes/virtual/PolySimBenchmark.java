package net.degoes.virtual;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import io.vavr.collection.HashMap;

import zio.Chunk;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(16)
public class PolySimBenchmark {
  JVMObject obj =
    new JVMObject(1, new JVMClassMetadata("Dog", HashMap.of(new JVMMethod("Dog", "bark"), new Address(0))));
  Bytecode.InvokeStatic is  = new Bytecode.InvokeStatic(new Address(0));
  Bytecode.InvokeVirtual iv = new Bytecode.InvokeVirtual(new JVMMethod("Dog", "bark"));

  @Benchmark
  public void invokeStatic(Blackhole blackhole) {
    blackhole.consume(is.address.value);
  }

  static class JVMObject {
    Object data;
    JVMClassMetadata meta;
    
    public JVMObject(Object data, JVMClassMetadata meta) {
      this.data = data;
      this.meta = meta;
    }
  }

  public static class JVMClassMetadata {
    String clazz;
    HashMap<JVMMethod, Address> vtable;

    public JVMClassMetadata(String clazz, HashMap<JVMMethod, Address> vtable) {
      this.clazz = clazz;
      this.vtable = vtable;
    }
  }

  public static class JVMMethod {
    String clazz;
    String name;

    public JVMMethod(String clazz, String name) {
      this.clazz = clazz;
      this.name = name;
    }
  }

  public static class Address {
    int value;
    
    public Address(int value) {
      this.value = value;
    }
  }

  public static class Bytecode {
    static class InvokeStatic extends Bytecode {
      Address address;

      public InvokeStatic(Address address) {
        this.address = address;
      }
    }
    
    static class InvokeVirtual extends Bytecode {
      JVMMethod method;

      public InvokeVirtual(JVMMethod method) {
        this.method = method;
      }
    }
  }
}