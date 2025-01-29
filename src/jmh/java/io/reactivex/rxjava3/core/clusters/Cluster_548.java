package io.reactivex.rxjava3.core.clusters;

public class Cluster_548 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTimedTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTimedTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithTimeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTimedTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTimedTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithTimeTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_548() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_1.payloads.takeLastTime.evaluate();
            this._Benchmark_benchmark_1.payloads.takeLastTimeAndSize.evaluate();
            this._Benchmark_benchmark_3.payloads.bufferTimedSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
            this._Benchmark_benchmark_5.payloads.timedUnboundedCancelUpfront.evaluate();
            this._Benchmark_benchmark_6.payloads.noHeadRetentionCompleteTime.evaluate();
            this._Benchmark_benchmark_7.payloads.noHeadRetentionErrorTime.evaluate();
        }

   }

}