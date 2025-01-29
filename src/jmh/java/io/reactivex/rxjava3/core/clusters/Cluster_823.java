package io.reactivex.rxjava3.core.clusters;

public class Cluster_823 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_823() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.normalDrop.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribeNoCancel.evaluate();
            this._Benchmark_benchmark_0.payloads.completeInline.evaluate();
            this._Benchmark_benchmark_3.payloads.selectorTake.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribedError.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribedDrop.evaluate();
            this._Benchmark_benchmark_6.payloads.otherError.evaluate();
            this._Benchmark_benchmark_7.payloads.untilPublisherDispose.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribedLatest.evaluate();
        }

   }

}