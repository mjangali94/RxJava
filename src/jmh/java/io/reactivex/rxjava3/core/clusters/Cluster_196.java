package io.reactivex.rxjava3.core.clusters;

public class Cluster_196 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_196() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.untilPublisherOtherOnNext.evaluate();
            this._Benchmark_benchmark_1.payloads.reentrantTake.evaluate();
            this._Benchmark_benchmark_2.payloads.emptySource.evaluate();
            this._Benchmark_benchmark_3.payloads.mainSuccessAfterOtherSignal.evaluate();
            this._Benchmark_benchmark_4.payloads.unsubscribeFromRetry.evaluate();
            this._Benchmark_benchmark_5.payloads.mainSuccessPublisher.evaluate();
            this._Benchmark_benchmark_6.payloads.requestInlineLatest.evaluate();
            this._Benchmark_benchmark_2.payloads.emptyOther.evaluate();
            this._Benchmark_benchmark_8.payloads.downstreamUnsubscribes.evaluate();
            this._Benchmark_benchmark_2.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_10.payloads.noBackpressure.evaluate();
            this._Benchmark_benchmark_11.payloads.reentrantComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.unsubscribeInlineLatest.evaluate();
            this._Benchmark_benchmark_6.payloads.unsubscribeInlineExactLatest.evaluate();
        }

   }

}