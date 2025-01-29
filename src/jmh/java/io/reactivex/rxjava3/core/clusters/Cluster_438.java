package io.reactivex.rxjava3.core.clusters;

public class Cluster_438 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDematerializeTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDematerializeTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
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
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_438() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.just.evaluate();
            this._Benchmark_benchmark_1.payloads.lastWithNoElements.evaluate();
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.safeSubscriberAlreadySafe);
            this._Benchmark_benchmark_3.payloads.withPublisherDispose.evaluate();
            this._Benchmark_benchmark_4.payloads.reduceWithObjectsFlowable.evaluate();
            this._Benchmark_benchmark_5.payloads.take.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_7.payloads.reduceFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_8.payloads.consumerThrows.evaluate();
            this._Benchmark_benchmark_9.payloads.cancelThenRequest.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_11.payloads.selectorNull.evaluate();
            this._Benchmark_benchmark_12.payloads.conditionalFiltered.evaluate();
        }

   }

}