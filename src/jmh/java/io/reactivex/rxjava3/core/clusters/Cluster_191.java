package io.reactivex.rxjava3.core.clusters;

public class Cluster_191 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableGroupByTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableGroupByTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_191() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.keySelectorThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.mainErrorsInnerCancelled.evaluate();
            this._Benchmark_benchmark_1.payloads.innerErrorsMainCancelled.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarReentrant2.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarReentrant.evaluate();
            this._Benchmark_benchmark_5.payloads.toFutureList.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.justObservableJust);
            this._Benchmark_benchmark_1.payloads.scalarQueueNoOverflow.evaluate();
        }

   }

}