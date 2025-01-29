package io.reactivex.rxjava3.core.clusters;

public class Cluster_284 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_21;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReduceWithSingleTest._Benchmark _Benchmark_benchmark_22;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_21 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_22 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReduceWithSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_21.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_284() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.lastOrErrorMultipleElements.evaluate();
            this._Benchmark_benchmark_1.payloads.withNonEmptyFlowable.evaluate();
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justFlowableJust);
            this._Benchmark_benchmark_3.payloads.elementAtOrDefault.evaluate();
            this._Benchmark_benchmark_3.payloads.elementAtOrDefaultWithIndexOutOfBounds.evaluate();
            this._Benchmark_benchmark_5.payloads.concatSimple.evaluate();
            this._Benchmark_benchmark_6.payloads.withPublisherDispose.evaluate();
            this._Benchmark_benchmark_7.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_8.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_5.payloads.concatCovariance3.evaluate();
            this._Benchmark_benchmark_7.payloads.just.evaluate();
            this._Benchmark_benchmark_11.payloads.collectInto.evaluate();
            this._Benchmark_benchmark_5.payloads.concatCovariance4.evaluate();
            this._Benchmark_benchmark_13.payloads.firstJust.evaluate();
            this._Benchmark_benchmark_14.payloads.firstJust.evaluate();
            this._Benchmark_benchmark_13.payloads.lastJust.evaluate();
            this._Benchmark_benchmark_14.payloads.lastJust.evaluate();
            this._Benchmark_benchmark_17.payloads.reduceSeedFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.lastOrErrorOneElement.evaluate();
            this._Benchmark_benchmark_14.payloads.singleJust.evaluate();
            this._Benchmark_benchmark_13.payloads.singleJust.evaluate();
            this._Benchmark_benchmark_21.payloads.singleOrErrorMultipleElements.evaluate();
            this._Benchmark_benchmark_22.payloads.normal.evaluate();
        }

   }

}