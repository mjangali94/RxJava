package io.reactivex.rxjava3.core.clusters;

public class Cluster_877 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_877() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.drainMoreWork.evaluate();
            this._Benchmark_benchmark_1.payloads.frontOverflow.evaluate();
            this._Benchmark_benchmark_2.payloads.ignoreBackpressure.evaluate();
            this._Benchmark_benchmark_2.payloads.mainErrorInnerNextIgnoreCancel.evaluate();
            this._Benchmark_benchmark_1.payloads.directCompletesUnsafe.evaluate();
            this._Benchmark_benchmark_5.payloads.asyncUpstreamFusionBoundary.evaluate();
        }

   }

}