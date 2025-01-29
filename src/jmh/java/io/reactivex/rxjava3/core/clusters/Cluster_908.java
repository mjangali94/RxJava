package io.reactivex.rxjava3.core.clusters;

public class Cluster_908 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_908() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelAfterAbandonmentOverlap.evaluate();
            this._Benchmark_benchmark_1.payloads.takeOneNoCancel.evaluate();
            this._Benchmark_benchmark_1.payloads.takeNoCancel.evaluate();
            this._Benchmark_benchmark_3.payloads.noHeadRetentionSize.evaluate();
            this._Benchmark_benchmark_1.payloads.errorInline.evaluate();
            this._Benchmark_benchmark_5.payloads.fromFlowableDisposeComposesThrough.evaluate();
            this._Benchmark_benchmark_6.payloads.noHeadRetentionSize.evaluate();
            this._Benchmark_benchmark_7.payloads.disposedUpfront.evaluate();
            this._Benchmark_benchmark_8.payloads.disposeNoNeedForReset.evaluate();
            this._Benchmark_benchmark_9.payloads.crossCancelOnComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.disposeNoNeedForReset.evaluate();
            this._Benchmark_benchmark_9.payloads.disposeResets.evaluate();
            this._Benchmark_benchmark_7.payloads.disposedUpfrontFallback.evaluate();
            this._Benchmark_benchmark_9.payloads.crossCancelOnError.evaluate();
        }

   }

}