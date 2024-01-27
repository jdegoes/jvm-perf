/**
 * TUNING
 *
 * The JVM exposes several knobs that you can use to tweak and tune performance for your
 * applications.
 *
 * In this section, you will explore these knobs, with a special emphasis on garbage collection.
 *
 * Garbage collection is all about tradeoffs. Broadly speaking, the main tradeoffs are as follows:
 *
 * Throughput versus latency. Throughput is the amount of work that can be done in a given amount of
 * time. Latency is the amount of time it takes to complete a single unit of work. Garbage
 * collection can be tuned to maximize throughput, at the expense of latency, or to maximize
 * latency, at the expense of throughput.
 *
 * Memory usage versus throughput. Garbage collection can be tuned to use less memory, at the
 * expense of throughput. Alternately, throughput can be maximized, at the expense of memory usage.
 * Running JVM applications on memory-constrained environments will require tuning for memory usage.
 *
 * EXERCISE 1
 *
 * Execute the benchmarks using the default garbage collector.
 *
 * See tuning/TuningBenchmark1.java
 *
 * EXERCISE 2
 *
 * Execute the benchmarks using the parallel garbage collector by using the JVM flag
 * -XX:+UseParallelGC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:ParallelGCThreads                  (default: # of CPU cores)
 * -XX:MaxGCPauseMillis                   (default: 100)
 * -XX:GCTimeRatio                        (default: 99)
 * -XX:YoungGenerationSizeIncrement       (default: 20)
 * -XX:TenuredGenerationSizeIncrement     (default: 20)
 * -XX:AdaptiveSizeDecrementScaleFactor   (default: 4)
 * -XX:UseGCOverheadLimit                 (default: true)
 *
 * See tuning/TuningBenchmark2.java
 *
 * EXERCISE 3
 *
 * Execute the benchmarks using the concurrent mark sweep garbage collector by using the JVM flag
 * -XX:+UseConcMarkSweepGC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:CMSInitiatingOccupancyFraction   (default: 68)
 * -XX:UseCMSInitiatingOccupancyOnly    (default: false)
 * -XX:CMSInitiatingOccupancyFraction   (default: 68)
 * -XX:CMSScavengeBeforeRemark          (default: false)
 * -XX:ScavengeBeforeFullGC             (default: false)
 * -XX:CMSParallelRemarkEnabled         (default: true)
 * -XX:UseGCOverheadLimit               (default: true)
 *
 * See tuning/TuningBenchmark3.java
 *
 * EXERCISE 4
 *
 * Execute the benchmarks using the G1 garbage collector by using the JVM flag -XX:+UseG1GC.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:InitiatingHeapOccupancyPercent   (default: 45)
 * -XX:G1UseAdaptiveIHOP                (default: true)
 * -XX:G1HeapWastePercent               (default: 5)
 * -XX:G1PeriodicGCSystemLoadThreshold  (default: 120)
 * -XX:MinHeapFreeRatio                 (default: 40)
 * -XX:MaxHeapFreeRatio                 (default: 70)
 * -XX:G1NewSizePercent                 (default: 5)
 * -XX:G1MaxNewSizePercent              (default: 60)
 * -XX:NewSize                          (default: 1/2 of the heap)
 * -XX:MaxNewSize                       (default: 1/2 of the heap)
 * -XX:+AlwaysPreTouch                  (default: false)
 *
 * See tuning/TuningBenchmark4.java
 *
 * EXERCISE 5
 *
 * Execute the benchmarks using the Z garbage collector by using the JVM flag -XX:+UseZGC,
 * and -XX:+UnlockExperimentalVMOptions depending on the JVM version you are using.
 *
 * Experiment with the following settings to see the effect on performance:
 *
 * -XX:ConcGCThreads                    (default: # of CPU cores)
 *
 */
