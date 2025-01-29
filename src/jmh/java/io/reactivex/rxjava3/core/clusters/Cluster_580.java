package io.reactivex.rxjava3.core.clusters;

public class Cluster_580 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_580() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.disposed.evaluate();
            this._Benchmark_benchmark_1.payloads.take2.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelOnDrain.evaluate();
            this._Benchmark_benchmark_3.payloads.syncFusedBoundaryConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.take.evaluate();
            this._Benchmark_benchmark_5.payloads.maxSize.evaluate();
            this._Benchmark_benchmark_6.payloads.backpressureNoneConditional.evaluate();
            this._Benchmark_benchmark_7.payloads.selectorDisconnectsIndependentSource.evaluate();
            this._Benchmark_benchmark_3.payloads.syncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.conditionalRequestOneByOne.evaluate();
            this._Benchmark_benchmark_0.payloads.fused.evaluate();
            this._Benchmark_benchmark_11.payloads.syncFused.evaluate();
            this._Benchmark_benchmark_12.payloads.range.evaluate();
            this._Benchmark_benchmark_13.payloads.unsubscribesFromUpstream.evaluate();
            this._Benchmark_benchmark_6.payloads.allPresentConditional.evaluate();
            this._Benchmark_benchmark_6.payloads.mixedConditional.evaluate();
            this._Benchmark_benchmark_16.payloads.concatMapJustRange.evaluate();
        }

   }

}