package io.reactivex.rxjava3.core.clusters;

public class Cluster_275 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_275() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerErrorAfterMainError.evaluate();
            this._Benchmark_benchmark_1.payloads.concatIterableBackpressured.evaluate();
            this._Benchmark_benchmark_1.payloads.concat2Backpressured.evaluate();
            this._Benchmark_benchmark_1.payloads.concat3Backpressured.evaluate();
            this._Benchmark_benchmark_4.payloads.onCompleteAvailableUntilReset.evaluate();
            this._Benchmark_benchmark_1.payloads.emptyConcatWithError.evaluate();
        }

   }

}