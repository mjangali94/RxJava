package io.reactivex.rxjava3.core.clusters;

public class Cluster_47 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_47() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.ambIterableOrder.evaluate();
            this._Benchmark_benchmark_0.payloads.ambFirstDone.evaluate();
            this._Benchmark_benchmark_0.payloads.completableSourcesInIterable.evaluate();
            this._Benchmark_benchmark_3.payloads.badSource.evaluate();
        }

   }

}