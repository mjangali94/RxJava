package io.reactivex.rxjava3.core.clusters;

public class Cluster_225 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_225() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedSourceCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_0.payloads.upstreamFusionRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorHasNextThrowsImmediatelyJust.evaluate();
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorNextThrowsAndUnsubscribes.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextIsNotCalledAfterChildUnsubscribedOnNext.evaluate();
            this._Benchmark_benchmark_7.payloads.lastOfEmptyReturnsEmpty.evaluate();
            this._Benchmark_benchmark_8.payloads.empty.evaluate();
        }

   }

}