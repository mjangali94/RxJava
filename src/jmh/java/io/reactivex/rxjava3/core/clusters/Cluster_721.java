package io.reactivex.rxjava3.core.clusters;

public class Cluster_721 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.exceptions.OnErrorNotImplementedExceptionTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilPredicateTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.exceptions.OnErrorNotImplementedExceptionTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilPredicateTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_721() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.unorderedFastPathRequest1.evaluate();
            this._Benchmark_benchmark_0.payloads.orderedFastPathReject.evaluate();
            this._Benchmark_benchmark_0.payloads.orderedFastPathRequest1.evaluate();
            this._Benchmark_benchmark_3.payloads.flowableForEachWhile.evaluate();
            this._Benchmark_benchmark_4.payloads.createBufferFactoryCrashOnSubscribe.evaluate();
            this._Benchmark_benchmark_5.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_6.payloads.disposedUpfront.evaluate();
        }

   }

}