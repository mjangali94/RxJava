package io.reactivex.rxjava3.core.clusters;

public class Cluster_349 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.flowable.FlowableDoOnTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScalarXMapTest._Benchmark _Benchmark_benchmark_20;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_23;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_24;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_25;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableDoOnTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScalarXMapTest._Benchmark();
            _Benchmark_benchmark_23 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_24 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_25 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_23.makePayloads();
            this._Benchmark_benchmark_24.makePayloads();
            this._Benchmark_benchmark_25.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_349() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.takeLastZeroProcessesAllItemsButIgnoresThem.evaluate();
            this._Benchmark_benchmark_1.payloads.lastRange.evaluate();
            this._Benchmark_benchmark_2.payloads.lastRange.evaluate();
            this._Benchmark_benchmark_3.payloads.issue1522.evaluate();
            this._Benchmark_benchmark_4.payloads.upstreamIsProcessedButIgnoredFlowable.evaluate();
            this._Benchmark_benchmark_5.payloads.seedFactoryFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.withFlowable.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_8.payloads.basic.evaluate();
            this._Benchmark_benchmark_3.payloads.requestOverflow.evaluate();
            this._Benchmark_benchmark_10.payloads.doOnEach.evaluate();
            this._Benchmark_benchmark_10.payloads.doOnCompleted.evaluate();
            this._Benchmark_benchmark_4.payloads.completedOkFlowable.evaluate();
            this._Benchmark_benchmark_10.payloads.doOnTerminateComplete.evaluate();
            this._Benchmark_benchmark_14.payloads.switchWhenNotEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.completedOk.evaluate();
            this._Benchmark_benchmark_16.payloads.range.evaluate();
            this._Benchmark_benchmark_17.payloads.syncRangeHidden.evaluate();
            this._Benchmark_benchmark_18.payloads.noUnsubscribeDownstream.evaluate();
            this._Benchmark_benchmark_4.payloads.upstreamIsProcessedButIgnored.evaluate();
            this._Benchmark_benchmark_20.payloads.mapToNonScalar.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_5.payloads.backpressureWithoutInitialValue.evaluate();
            this._Benchmark_benchmark_23.payloads.blockingSingleEmpty.evaluate();
            this._Benchmark_benchmark_24.payloads.disposed.evaluate();
            this._Benchmark_benchmark_25.payloads.singleDoesNotRequestMoreThanItNeedsToEmitItem.evaluate();
            this._Benchmark_benchmark_0.payloads.lastOfManyReturnsLast.evaluate();
        }

   }

}