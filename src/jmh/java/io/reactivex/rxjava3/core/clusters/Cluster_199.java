package io.reactivex.rxjava3.core.clusters;

public class Cluster_199 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_199() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mergeScalar2.evaluate();
            this._Benchmark_benchmark_0.payloads.someConcurrencyInnerScalarCancel.evaluate();
            this._Benchmark_benchmark_2.payloads.keySelectorThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.allConcurrencyBackpressured.evaluate();
            this._Benchmark_benchmark_4.payloads.mergeDelayErrorIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeScalarError.evaluate();
        }

   }

}