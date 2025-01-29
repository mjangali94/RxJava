package io.reactivex.rxjava3.core.clusters;

public class Cluster_345 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMapTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_345() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.scalarMapToEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.mergeIterableDelayErrorWithErrorMaxConcurrency.evaluate();
            this._Benchmark_benchmark_2.payloads.concatSimple.evaluate();
            this._Benchmark_benchmark_2.payloads.concatCovariance3.evaluate();
            this._Benchmark_benchmark_4.payloads.failingFusedInnerCancelsSource.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.emptyCompletableComplete);
        }

   }

}