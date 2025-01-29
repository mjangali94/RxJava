package io.reactivex.rxjava3.core.clusters;

public class Cluster_819 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_19;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_819() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatArrayDelayError);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concat3);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concat4);
            this._Benchmark_benchmark_3.payloads.justTake.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.emptyAndScalarBackpressured);
            this._Benchmark_benchmark_5.payloads.nearMaxValueWithoutBackpressure.evaluate();
            this._Benchmark_benchmark_6.payloads.just.evaluate();
            this._Benchmark_benchmark_5.payloads.nearMaxValueWithBackpressure.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.scalarAndEmptyBackpressured);
            this._Benchmark_benchmark_9.payloads.requestOneByOneConditional.evaluate();
            this._Benchmark_benchmark_10.payloads.debounceFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_11.payloads.multipleOnComplete.evaluate();
            this._Benchmark_benchmark_9.payloads.just.evaluate();
            this._Benchmark_benchmark_11.payloads.onNextAfterOnComplete.evaluate();
            this._Benchmark_benchmark_5.payloads.fusedReject.evaluate();
            this._Benchmark_benchmark_5.payloads.fastPathCancel.evaluate();
            this._Benchmark_benchmark_9.payloads.many.evaluate();
            this._Benchmark_benchmark_5.payloads.fastPathCancelExact.evaluate();
            this._Benchmark_benchmark_5.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero.evaluate();
            this._Benchmark_benchmark_19.payloads.scanWithSeedWhenScanSeedProviderThrows.evaluate();
            this._Benchmark_benchmark_9.payloads.closeCalledAfterItems.evaluate();
            this._Benchmark_benchmark_5.payloads.slowPathCancel.evaluate();
        }

   }

}