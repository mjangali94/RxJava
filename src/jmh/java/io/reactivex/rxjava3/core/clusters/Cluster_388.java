package io.reactivex.rxjava3.core.clusters;

public class Cluster_388 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCountTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_19;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCountTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_388() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.flatMapPublisher.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapPublisherMapperThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapPublisherCancelDuringFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapPublisherCancelDuringSingle.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.justFlowableJust);
            this._Benchmark_benchmark_0.payloads.flatMapPublisherSingleError.evaluate();
            this._Benchmark_benchmark_6.payloads.singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne.evaluate();
            this._Benchmark_benchmark_7.payloads.toSortedListComparatorCapacity.evaluate();
            this._Benchmark_benchmark_7.payloads.withFollowingFirstFlowable.evaluate();
            this._Benchmark_benchmark_7.payloads.toSortedListCapacity.evaluate();
            this._Benchmark_benchmark_10.runBenchmark(this._Benchmark_benchmark_10.payloads.requestThroughTakeThatReducesRequest);
            this._Benchmark_benchmark_10.runBenchmark(this._Benchmark_benchmark_10.payloads.requestThroughTakeWhereRequestIsSmallerThanTake);
            this._Benchmark_benchmark_12.payloads.withBackpressure.evaluate();
            this._Benchmark_benchmark_13.payloads.dispose.evaluate();
            this._Benchmark_benchmark_14.payloads.elementAtOrErrorMultipleElements.evaluate();
            this._Benchmark_benchmark_15.payloads.backpressureHonoredFlowable.evaluate();
            this._Benchmark_benchmark_16.runBenchmark(this._Benchmark_benchmark_16.payloads.justSingleJust);
            this._Benchmark_benchmark_17.payloads.fromPublisher.evaluate();
            this._Benchmark_benchmark_18.payloads.dispose.evaluate();
            this._Benchmark_benchmark_19.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_14.payloads.elementAtWithDefaultConstrainsUpstreamRequests.evaluate();
            this._Benchmark_benchmark_13.payloads.collectIntoFlowable.evaluate();
            this._Benchmark_benchmark_14.payloads.elementAtOrErrorOneElement.evaluate();
        }

   }

}