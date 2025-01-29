package io.reactivex.rxjava3.core.clusters;

public class Cluster_1 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeStartWithTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark _Benchmark_benchmark_17;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeStartWithTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatWithFlowableOfFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.concatCovariance.evaluate();
            this._Benchmark_benchmark_0.payloads.concatCovariance2.evaluate();
            this._Benchmark_benchmark_0.payloads.concatWithIterableOfFlowable.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.maybeEmptyNormal);
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.emptyCompletableComplete);
            this._Benchmark_benchmark_8.payloads.noCancelPrevious.evaluate();
            this._Benchmark_benchmark_9.payloads.fusionWithConcatMap.evaluate();
            this._Benchmark_benchmark_10.payloads.untilFalseEmpty.evaluate();
            this._Benchmark_benchmark_10.payloads.untilTrueEmpty.evaluate();
            this._Benchmark_benchmark_12.payloads.concatDelayErrorFlowable.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.justFlowableJust);
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.justMaybeJust);
            this._Benchmark_benchmark_15.runBenchmark(this._Benchmark_benchmark_15.payloads.justMaybeJust);
            this._Benchmark_benchmark_16.runBenchmark(this._Benchmark_benchmark_16.payloads.justMaybeEmpty);
            this._Benchmark_benchmark_17.payloads.empty.evaluate();
        }

   }

}