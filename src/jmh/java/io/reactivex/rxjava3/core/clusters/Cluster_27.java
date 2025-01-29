package io.reactivex.rxjava3.core.clusters;

public class Cluster_27 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_27() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.errorLatest.evaluate();
            this._Benchmark_benchmark_1.payloads.reentrantOnNextCancel.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeNoNeedForReset.evaluate();
            this._Benchmark_benchmark_0.payloads.deferredRequest.evaluate();
            this._Benchmark_benchmark_4.payloads.reentrantOnNextCancel.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribedNoCancelBuffer.evaluate();
            this._Benchmark_benchmark_0.payloads.requestExact.evaluate();
            this._Benchmark_benchmark_1.payloads.noHeadRetentionCompleteSize.evaluate();
        }

   }

}