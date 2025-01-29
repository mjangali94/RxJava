package io.reactivex.rxjava3.core.clusters;

public class Cluster_71 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_71() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.lastCompletableFutureCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.lastCompletableFutureCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.firstCompletableFutureCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.firstCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.lastCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.lastCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.firstCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.singleCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.singleCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.firstCompletableManualCompleteExceptionallyCancels.evaluate();
        }

   }

}