package io.reactivex.rxjava3.core.clusters;

public class Cluster_526 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_526() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.innerNull);
            this._Benchmark_benchmark_1.payloads.isTerminated.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.innerThrows);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.callableCrashDelayError);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.scalarInnerEmptyDisposeDelayError);
        }

   }

}