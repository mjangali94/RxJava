package io.reactivex.rxjava3.core.clusters;

public class Cluster_180 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableMostRecentTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableMostRecentTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_180() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mostRecent.evaluate();
            this._Benchmark_benchmark_1.payloads.lastCompletableManualCompleteExceptionallyCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.firstCompletableManualCompleteExceptionallyCancels.evaluate();
        }

   }

}