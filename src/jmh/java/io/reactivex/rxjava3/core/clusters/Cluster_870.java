package io.reactivex.rxjava3.core.clusters;

public class Cluster_870 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithSingleTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithCompletableTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_15;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithSingleTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithCompletableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_870() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.scalarInnerQueueEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_2.payloads.disposedFlowable.evaluate();
            this._Benchmark_benchmark_3.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_4.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_5.payloads.backpressureViaRequest.evaluate();
            this._Benchmark_benchmark_5.payloads.withBackpressureRequestWayMore.evaluate();
            this._Benchmark_benchmark_5.payloads.slowPathCancelBeforeComplete.evaluate();
            this._Benchmark_benchmark_8.payloads.errorSingle.evaluate();
            this._Benchmark_benchmark_9.payloads.takeMain.evaluate();
            this._Benchmark_benchmark_5.payloads.slowPathCancel.evaluate();
            this._Benchmark_benchmark_11.payloads.normal.evaluate();
            this._Benchmark_benchmark_12.payloads.disposed.evaluate();
            this._Benchmark_benchmark_13.payloads.normalBackpressured.evaluate();
            this._Benchmark_benchmark_14.payloads.takeMain.evaluate();
            this._Benchmark_benchmark_15.payloads.normalTake.evaluate();
        }

   }

}