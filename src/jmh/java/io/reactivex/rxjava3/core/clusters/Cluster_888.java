package io.reactivex.rxjava3.core.clusters;

public class Cluster_888 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_888() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_1.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_3.payloads.normalEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.noRequestEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.exactEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.singleError.evaluate();
            this._Benchmark_benchmark_7.payloads.switchMapFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_8.payloads.noDisconnectSomeoneElse.evaluate();
            this._Benchmark_benchmark_8.payloads.empty.evaluate();
            this._Benchmark_benchmark_10.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_11.payloads.backpressureOnEmptyStream.evaluate();
        }

   }

}