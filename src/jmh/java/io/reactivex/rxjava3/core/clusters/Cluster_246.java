package io.reactivex.rxjava3.core.clusters;

public class Cluster_246 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_246() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.conditionalSlowPathCancel.evaluate();
            this._Benchmark_benchmark_0.payloads.conditionalFastPatchCancelBeforeComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedRejectedConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.slowPathTakeExact.evaluate();
            this._Benchmark_benchmark_0.payloads.conditionalSlowPathTakeExact.evaluate();
            this._Benchmark_benchmark_5.payloads.fusedCrashDelayError.evaluate();
            this._Benchmark_benchmark_5.payloads.fusedCrash.evaluate();
            this._Benchmark_benchmark_7.payloads.ifFunctionThrowsThatNoMoreEventsAreProcessed.evaluate();
            this._Benchmark_benchmark_8.payloads.normalEmptyConditional.evaluate();
            this._Benchmark_benchmark_8.payloads.normalTakeConditional.evaluate();
        }

   }

}