package io.reactivex.rxjava3.core.clusters;

public class Cluster_386 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_386() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.combineToNull2.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestDelayErrorArrayOfSources.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestArrayOfSources.evaluate();
            this._Benchmark_benchmark_1.payloads.combineLatestDelayErrorIterableOfSources.evaluate();
            this._Benchmark_benchmark_0.payloads.manyCombinerThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.observableSourcesInIterable.evaluate();
            this._Benchmark_benchmark_6.payloads.innerError.evaluate();
        }

   }

}