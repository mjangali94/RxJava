package io.reactivex.rxjava3.core.clusters;

public class Cluster_234 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoOnEachTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoOnEachTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_234() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.middleError.evaluate();
            this._Benchmark_benchmark_1.payloads.issue1451Case1.evaluate();
            this._Benchmark_benchmark_2.payloads.onNextThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.issue1451Case2.evaluate();
        }

   }

}