package io.reactivex.rxjava3.core.clusters;

public class Cluster_48 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_48() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.untilCompletableMainComplete.evaluate();
            this._Benchmark_benchmark_0.payloads.untilCompletableOtherOnComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.iterableCompleteLater.evaluate();
            this._Benchmark_benchmark_0.payloads.untilCompletableMainError.evaluate();
            this._Benchmark_benchmark_0.payloads.untilCompletableOtherError.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelAfterFirstDelayError.evaluate();
            this._Benchmark_benchmark_2.payloads.maxConcurrencyOneDelayMainErrors.evaluate();
        }

   }

}