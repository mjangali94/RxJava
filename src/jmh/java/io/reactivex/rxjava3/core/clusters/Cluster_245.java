package io.reactivex.rxjava3.core.clusters;

public class Cluster_245 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.observers.CallbackCompletableObserverTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.observers.CallbackCompletableObserverTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_245() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.backpressureBufferZeroCapacity.evaluate();
            this._Benchmark_benchmark_1.payloads.conditionalNone.evaluate();
            this._Benchmark_benchmark_2.payloads.conditionalNormal.evaluate();
            this._Benchmark_benchmark_2.payloads.conditionalNormalSlowpath.evaluate();
            this._Benchmark_benchmark_1.payloads.filterThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.conditional.evaluate();
            this._Benchmark_benchmark_2.payloads.conditionalRequestOneByOne2.evaluate();
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.fusedCrashDelayError);
            this._Benchmark_benchmark_8.payloads.rangeConditional.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyActionShouldReportNoCustomOnError.evaluate();
            this._Benchmark_benchmark_1.payloads.syncNoneFused.evaluate();
        }

   }

}