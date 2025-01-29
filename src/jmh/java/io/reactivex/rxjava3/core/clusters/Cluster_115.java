package io.reactivex.rxjava3.core.clusters;

public class Cluster_115 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_115() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.debounceFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.withLatestFromCombinerReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.eagerness2.evaluate();
            this._Benchmark_benchmark_2.payloads.eagerness5.evaluate();
            this._Benchmark_benchmark_2.payloads.eagerness4.evaluate();
            this._Benchmark_benchmark_2.payloads.eagerness3.evaluate();
        }

   }

}