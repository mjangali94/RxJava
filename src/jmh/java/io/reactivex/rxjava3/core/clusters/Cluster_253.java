package io.reactivex.rxjava3.core.clusters;

public class Cluster_253 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_253() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerErrorDelayError2.evaluate();
            this._Benchmark_benchmark_1.payloads.with4Others.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperThrowsDelayError.evaluate();
            this._Benchmark_benchmark_1.payloads.withMainError.evaluate();
            this._Benchmark_benchmark_0.payloads.asyncFusedDelayError.evaluate();
            this._Benchmark_benchmark_5.payloads.cancelWhileSubscribing.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.concatMapEmptyDelayError);
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.concatMapJustSourceDelayError);
        }

   }

}