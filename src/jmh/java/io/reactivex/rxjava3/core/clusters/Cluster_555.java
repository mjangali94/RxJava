package io.reactivex.rxjava3.core.clusters;

public class Cluster_555 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.mixed.MaybeFlatMapPublisherTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.single.SingleMergeTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.mixed.MaybeFlatMapPublisherTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_555() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fused.evaluate();
            this._Benchmark_benchmark_1.payloads.empty.evaluate();
            this._Benchmark_benchmark_2.payloads.singleOrErrorOneElement.evaluate();
            this._Benchmark_benchmark_3.payloads.emptySource.evaluate();
            this._Benchmark_benchmark_4.payloads.elementAtOrErrorInvalidIndex.evaluate();
            this._Benchmark_benchmark_5.payloads.consumerThrowsConditional2.evaluate();
            this._Benchmark_benchmark_6.payloads.backpressureIfOneRequestedOneShouldBeDelivered.evaluate();
            this._Benchmark_benchmark_7.payloads.unboundedIn.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.untilFirstPublisher.evaluate();
            this._Benchmark_benchmark_10.payloads.merge2.evaluate();
            this._Benchmark_benchmark_11.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_12.payloads.backpressureHonoredFlowable.evaluate();
            this._Benchmark_benchmark_10.payloads.merge3.evaluate();
        }

   }

}