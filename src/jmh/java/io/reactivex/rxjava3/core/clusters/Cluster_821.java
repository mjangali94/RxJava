package io.reactivex.rxjava3.core.clusters;

public class Cluster_821 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCastTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorCompleteTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableHideTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDetachTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.single.SingleDetachTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDetachTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIgnoreElementTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark _Benchmark_benchmark_17;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCastTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableHideTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDetachTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.single.SingleDetachTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDetachTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIgnoreElementTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark();
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
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_821() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.onSubscribeThrowsCancelsUpstream.evaluate();
            this._Benchmark_benchmark_1.payloads.castCrashUnsubscribes.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.cancel.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.isDisposed);
            this._Benchmark_benchmark_5.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_7.payloads.emptyWithOnNext.evaluate();
            this._Benchmark_benchmark_8.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_11.payloads.unsubscriptionPropagatesBeforeSubscribe.evaluate();
            this._Benchmark_benchmark_12.payloads.onSubscribeThrowsCancelsUpstream.evaluate();
            this._Benchmark_benchmark_13.payloads.normal.evaluate();
            this._Benchmark_benchmark_14.payloads.dispose.evaluate();
            this._Benchmark_benchmark_15.payloads.dispose.evaluate();
            this._Benchmark_benchmark_16.payloads.dispose.evaluate();
            this._Benchmark_benchmark_17.payloads.functionCrashUnsubscribes.evaluate();
            this._Benchmark_benchmark_16.payloads.dispose2.evaluate();
        }

   }

}