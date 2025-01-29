package io.reactivex.rxjava3.core.clusters;

public class Cluster_801 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.disposables.ListCompositeDisposableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.disposables.ListCompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_801() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.noParentIsDisposed.evaluate();
            this._Benchmark_benchmark_1.payloads.nextThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.hasNextThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.toStringStates.evaluate();
            this._Benchmark_benchmark_4.payloads.neverSource.evaluate();
            this._Benchmark_benchmark_5.payloads.request1Conditional.evaluate();
            this._Benchmark_benchmark_6.payloads.constructorIterable.evaluate();
            this._Benchmark_benchmark_5.payloads.synchronousRebatching.evaluate();
            this._Benchmark_benchmark_8.payloads.multiTake.evaluate();
            this._Benchmark_benchmark_9.payloads.concatMapEmptyDelayError.evaluate();
        }

   }

}