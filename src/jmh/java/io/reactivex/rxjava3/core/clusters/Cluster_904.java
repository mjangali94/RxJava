package io.reactivex.rxjava3.core.clusters;

public class Cluster_904 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableForEachTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureErrorTest._Benchmark _Benchmark_benchmark_18;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableForEachTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_904() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.lastCompletableFutureCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.singleCompletableFutureCancels.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.unsubscribeComposes.evaluate();
            this._Benchmark_benchmark_4.payloads.isDisposedToMaybe.evaluate();
            this._Benchmark_benchmark_5.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_0.payloads.firstCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.lastCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.singleCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.lastCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.firstCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.singleCompletableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_4.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_13.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_14.payloads.dispose.evaluate();
            this._Benchmark_benchmark_15.payloads.fasterSource.evaluate();
            this._Benchmark_benchmark_16.payloads.dispose.evaluate();
            this._Benchmark_benchmark_17.payloads.unsubscriptionPropagatesAfterSubscribe.evaluate();
            this._Benchmark_benchmark_18.payloads.overflowCancels.evaluate();
        }

   }

}