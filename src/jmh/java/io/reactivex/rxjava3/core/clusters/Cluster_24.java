package io.reactivex.rxjava3.core.clusters;

public class Cluster_24 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryWithPredicateTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryWithPredicateTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_24() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.singleRequestNotForgottenWhenNoData.evaluate();
            this._Benchmark_benchmark_1.payloads.synchronousDrop.evaluate();
            this._Benchmark_benchmark_2.payloads.justWithOnNext.evaluate();
            this._Benchmark_benchmark_3.payloads.unsubscribeFromRetry.evaluate();
            this._Benchmark_benchmark_4.payloads.untilFires.evaluate();
            this._Benchmark_benchmark_5.payloads.fallbackComplete.evaluate();
            this._Benchmark_benchmark_4.payloads.untilPublisherMainSuccess.evaluate();
            this._Benchmark_benchmark_4.payloads.mainCompletes.evaluate();
            this._Benchmark_benchmark_8.payloads.fallbackComplete.evaluate();
            this._Benchmark_benchmark_9.payloads.ambCancelsOthers.evaluate();
            this._Benchmark_benchmark_10.payloads.firstCompletableManualCompleteExceptionallyCancels.evaluate();
        }

   }

}