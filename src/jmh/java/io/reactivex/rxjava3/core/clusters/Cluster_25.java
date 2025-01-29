package io.reactivex.rxjava3.core.clusters;

public class Cluster_25 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_25() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mapperThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.ambIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.ambIterable2.evaluate();
            this._Benchmark_benchmark_3.payloads.mainSuccessSingle.evaluate();
            this._Benchmark_benchmark_4.payloads.fallbackError.evaluate();
            this._Benchmark_benchmark_5.payloads.normalEmitLast.evaluate();
            this._Benchmark_benchmark_6.payloads.normalMissing.evaluate();
            this._Benchmark_benchmark_7.payloads.cancelAfterAbandonmentSize.evaluate();
            this._Benchmark_benchmark_3.payloads.untilPublisherOtherOnNext.evaluate();
            this._Benchmark_benchmark_7.payloads.cancelAfterAbandonmentSkip.evaluate();
        }

   }

}