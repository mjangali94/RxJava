package io.reactivex.rxjava3.core.clusters;

public class Cluster_498 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDefaultIfEmptyTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDefaultIfEmptyTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_498() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelAfterOnComplete.evaluate();
            this._Benchmark_benchmark_0.payloads.cancelAfterOnError.evaluate();
            this._Benchmark_benchmark_0.payloads.normalOnNext.evaluate();
            this._Benchmark_benchmark_0.payloads.normalOnNextBackpressured.evaluate();
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_5.payloads.with2Others.evaluate();
            this._Benchmark_benchmark_5.payloads.with3Others.evaluate();
            this._Benchmark_benchmark_5.payloads.with4Others.evaluate();
            this._Benchmark_benchmark_8.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.untilPublisherOtherOnComplete.evaluate();
            this._Benchmark_benchmark_9.payloads.untilPublisherMainComplete.evaluate();
            this._Benchmark_benchmark_11.payloads.backpressureNonEmpty.evaluate();
            this._Benchmark_benchmark_9.payloads.untilPublisherOtherOnNext.evaluate();
            this._Benchmark_benchmark_13.payloads.disposeBeforeSwitchInOnNext.evaluate();
            this._Benchmark_benchmark_14.payloads.delayAndTakeUntilNeverSubscribeToSource.evaluate();
        }

   }

}