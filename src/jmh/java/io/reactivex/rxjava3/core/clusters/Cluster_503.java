package io.reactivex.rxjava3.core.clusters;

public class Cluster_503 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapCompletableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_503() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.emptyHidden.evaluate();
            this._Benchmark_benchmark_0.payloads.streamNull.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextThrowsLater.evaluate();
            this._Benchmark_benchmark_3.payloads.normal.evaluate();
            this._Benchmark_benchmark_3.payloads.mapperReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.mapperThrows.evaluate();
            this._Benchmark_benchmark_6.payloads.normal.evaluate();
            this._Benchmark_benchmark_7.payloads.workerNotDisposedPrematurelyNormalInAsyncOut.evaluate();
        }

   }

}