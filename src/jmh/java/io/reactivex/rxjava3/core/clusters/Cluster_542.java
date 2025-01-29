package io.reactivex.rxjava3.core.clusters;

public class Cluster_542 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapCompletableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromPubisherTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapCompletableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_542() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.windowUnsubscribeOverlapping.evaluate();
            this._Benchmark_benchmark_1.payloads.nearMaxValueWithBackpressure.evaluate();
            this._Benchmark_benchmark_2.payloads.range.evaluate();
            this._Benchmark_benchmark_0.payloads.cancellingWindowCancelsUpstreamSkip.evaluate();
            this._Benchmark_benchmark_4.payloads.mapperCancels.evaluate();
            this._Benchmark_benchmark_5.payloads.clearIsEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.takeMain.evaluate();
        }

   }

}