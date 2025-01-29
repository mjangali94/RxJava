package io.reactivex.rxjava3.core.clusters;

public class Cluster_740 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableTimeoutTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.completable.CompletableTimeoutTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_740() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.slowPathRebatch.evaluate();
            this._Benchmark_benchmark_1.payloads.workerNotDisposedPrematurelySyncInNormalOut.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedCancelAfterPoll.evaluate();
            this._Benchmark_benchmark_1.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_4.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedCancelAfterPollConditional.evaluate();
            this._Benchmark_benchmark_6.payloads.manyBackpressured2.evaluate();
            this._Benchmark_benchmark_7.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_8.payloads.initializeIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.workerNotDisposedPrematurelySyncInNormalOutConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
        }

   }

}