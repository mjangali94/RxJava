package io.reactivex.rxjava3.core.clusters;

public class Cluster_686 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.subscribers.BlockingSubscriberTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSingleTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnSubscribeTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark _Benchmark_benchmark_20;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.subscribers.BlockingSubscriberTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSingleTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnSubscribeTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_686() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelOnRequest.evaluate();
            this._Benchmark_benchmark_4.payloads.shouldUseUnsafeSubscribeInternallyNotSubscribe.evaluate();
            this._Benchmark_benchmark_5.payloads.justSingleItemObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.blockingFirstTimeout2.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelUpfront.evaluate();
            this._Benchmark_benchmark_9.payloads.countOne.evaluate();
            this._Benchmark_benchmark_10.payloads.normalJust.evaluate();
            this._Benchmark_benchmark_11.payloads.predicateThrowsExceptionAndValueInCauseMessage.evaluate();
            this._Benchmark_benchmark_0.payloads.backpressureIfOneRequestedOneShouldBeDeliveredFlowable.evaluate();
            this._Benchmark_benchmark_13.payloads.countOne.evaluate();
            this._Benchmark_benchmark_14.payloads.doOnSubscribe2.evaluate();
            this._Benchmark_benchmark_15.payloads.errorCauseIncludesLastValue.evaluate();
            this._Benchmark_benchmark_16.payloads.takeZero.evaluate();
            this._Benchmark_benchmark_4.payloads.neverObservable.evaluate();
            this._Benchmark_benchmark_18.payloads.just.evaluate();
            this._Benchmark_benchmark_19.payloads.justSource.evaluate();
            this._Benchmark_benchmark_20.payloads.dispose.evaluate();
        }

   }

}