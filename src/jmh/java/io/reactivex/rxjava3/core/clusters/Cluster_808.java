package io.reactivex.rxjava3.core.clusters;

public class Cluster_808 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_808() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.nextCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCrash2.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.error.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCrash.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
        }

   }

}