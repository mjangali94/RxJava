package io.reactivex.rxjava3.core.clusters;

public class Cluster_117 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark _Benchmark_benchmark_22;
       private io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark _Benchmark_benchmark_25;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_29;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_33;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark();
            _Benchmark_benchmark_22 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark();
            _Benchmark_benchmark_25 = new io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark();
            _Benchmark_benchmark_29 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_33 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_25.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_29.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_33.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_117() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.lastWithElements.evaluate();
            this._Benchmark_benchmark_0.payloads.lastMultiSubscribe.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToStringFlowable.evaluate();
            this._Benchmark_benchmark_3.payloads.asyncFusion.evaluate();
            this._Benchmark_benchmark_4.payloads.followingFirstFlowable.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToListFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.blockingLastNormal.evaluate();
            this._Benchmark_benchmark_7.payloads.reduceIntsFlowable.evaluate();
            this._Benchmark_benchmark_7.payloads.reduceInts.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtWithIndexOutOfBoundsFlowable.evaluate();
            this._Benchmark_benchmark_10.payloads.withFollowingFirstFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.blockingFirstNormal.evaluate();
            this._Benchmark_benchmark_12.payloads.issue1527Flowable.evaluate();
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.onStartRequestsAreAdditiveAndOverflowBecomesMaxValue);
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.onStartRequestsAreAdditive);
            this._Benchmark_benchmark_12.payloads.issue1527.evaluate();
            this._Benchmark_benchmark_16.payloads.withOtherPublisherDispose.evaluate();
            this._Benchmark_benchmark_17.payloads.justTwoEmissionsObservableThrowsError.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtFlowable.evaluate();
            this._Benchmark_benchmark_4.payloads.followingFirst.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtOrDefaultWithIndexOutOfBoundsFlowable.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToString.evaluate();
            this._Benchmark_benchmark_22.runBenchmark(this._Benchmark_benchmark_22.payloads.getAfterCancel);
            this._Benchmark_benchmark_22.runBenchmark(this._Benchmark_benchmark_22.payloads.getWithTimeoutAfterCancel);
            this._Benchmark_benchmark_9.payloads.elementAtOrDefaultFlowable.evaluate();
            this._Benchmark_benchmark_25.payloads.syncArrayHidden.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToList.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtWithIndexOutOfBounds.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAt.evaluate();
            this._Benchmark_benchmark_29.payloads.disposeOnNextAfterFirst.evaluate();
            this._Benchmark_benchmark_10.payloads.withFollowingFirst.evaluate();
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.onStartCalledOnceViaSubscribe);
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.onStartCalledOnceViaUnsafeSubscribe);
            this._Benchmark_benchmark_33.payloads.just10Arguments.evaluate();
        }

   }

}