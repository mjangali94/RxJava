package io.reactivex.rxjava3.core.clusters;

public class Cluster_185 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutTests._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_185() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.shouldTimeoutIfSecondOnNextNotWithinTimeout.evaluate();
            this._Benchmark_benchmark_1.payloads.reentrantOnNextCancel.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantOnNextCancel.evaluate();
            this._Benchmark_benchmark_3.payloads.disposedUpfrontFallback.evaluate();
            this._Benchmark_benchmark_1.payloads.reentrantOnNext.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantOnNext.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelOnArrival2.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.payloads.unboundedLeavesEarly.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeNoNeedForResetSizeBound.evaluate();
            this._Benchmark_benchmark_10.payloads.cancelOnArrival2.evaluate();
            this._Benchmark_benchmark_10.payloads.unboundedLeavesEarly.evaluate();
            this._Benchmark_benchmark_12.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.payloads.reentrantOnNextCancelBounded.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantOnNextCancelBounded.evaluate();
        }

   }

}