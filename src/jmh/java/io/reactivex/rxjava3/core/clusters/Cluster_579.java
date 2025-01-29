package io.reactivex.rxjava3.core.clusters;

public class Cluster_579 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_579() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedReject.evaluate();
            this._Benchmark_benchmark_0.payloads.requestWrongFusion.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeOnArrival.evaluate();
            this._Benchmark_benchmark_4.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.noBackpressure.evaluate();
            this._Benchmark_benchmark_6.payloads.syncFusedBoundary.evaluate();
            this._Benchmark_benchmark_7.payloads.basicToFlowable.evaluate();
            this._Benchmark_benchmark_8.payloads.rangeSource.evaluate();
            this._Benchmark_benchmark_0.payloads.fastPathCancelExact.evaluate();
            this._Benchmark_benchmark_0.payloads.fastPathCancel.evaluate();
            this._Benchmark_benchmark_11.payloads.normalNonEmpty.evaluate();
            this._Benchmark_benchmark_12.payloads.asyncFusedRejected.evaluate();
        }

   }

}