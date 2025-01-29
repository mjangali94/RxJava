package io.reactivex.rxjava3.core.clusters;

public class Cluster_9 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_9() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.firstErrorOtherSucceed.evaluate();
            this._Benchmark_benchmark_0.payloads.firstSucceedOtherError.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperReturnsNullObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperThrowsObservable.evaluate();
        }

   }

}