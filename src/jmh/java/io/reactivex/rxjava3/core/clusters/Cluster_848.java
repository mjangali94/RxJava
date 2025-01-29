package io.reactivex.rxjava3.core.clusters;

public class Cluster_848 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryWithPredicateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeAmbTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapSingleTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeOfTypeTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryWithPredicateTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeAmbTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeOfTypeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_848() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.unsubscribeFromRetry.evaluate();
            this._Benchmark_benchmark_1.payloads.untilPublisherOtherOnNext.evaluate();
            this._Benchmark_benchmark_2.payloads.mutableWithSelector.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelAsFlowable.evaluate();
            this._Benchmark_benchmark_4.payloads.noSubscriptionIfOtherErrors.evaluate();
            this._Benchmark_benchmark_5.payloads.mainCompletes.evaluate();
            this._Benchmark_benchmark_6.payloads.disposeOnArrival2.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_8.payloads.onErrorNextDispose.evaluate();
            this._Benchmark_benchmark_9.payloads.mainComplete.evaluate();
            this._Benchmark_benchmark_10.payloads.untilPublisherMainSuccess.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_12.payloads.dispose.evaluate();
            this._Benchmark_benchmark_13.payloads.dispose.evaluate();
            this._Benchmark_benchmark_14.payloads.unsubscribeFromRetry.evaluate();
            this._Benchmark_benchmark_15.payloads.disposed.evaluate();
            this._Benchmark_benchmark_16.payloads.isDisposed.evaluate();
        }

   }

}