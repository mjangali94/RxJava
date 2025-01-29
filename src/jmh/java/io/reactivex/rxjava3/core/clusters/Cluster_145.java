package io.reactivex.rxjava3.core.clusters;

public class Cluster_145 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_145() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.multipleOnComplete.evaluate();
            this._Benchmark_benchmark_0.payloads.onNextAfterOnComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.iteratorThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.nullError.evaluate();
        }

   }

}