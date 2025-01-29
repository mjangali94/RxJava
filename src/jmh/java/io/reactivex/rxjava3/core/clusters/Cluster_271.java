package io.reactivex.rxjava3.core.clusters;

public class Cluster_271 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_271() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.mixed.evaluate();
            this._Benchmark_benchmark_0.payloads.basicSyncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_0.payloads.cancel.evaluate();
            this._Benchmark_benchmark_0.payloads.innerError.evaluate();
            this._Benchmark_benchmark_0.payloads.limit.evaluate();
            this._Benchmark_benchmark_0.payloads.basicFusionRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.basicNonFused.evaluate();
            this._Benchmark_benchmark_9.payloads.noSubsequentSubscriptionDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedPollCrash.evaluate();
        }

   }

}