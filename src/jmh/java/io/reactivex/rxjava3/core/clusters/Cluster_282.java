package io.reactivex.rxjava3.core.clusters;

public class Cluster_282 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_282() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.justConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_0.payloads.closeCalledOnCancel.evaluate();
            this._Benchmark_benchmark_0.payloads.manyConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.manyConditionalSkip.evaluate();
            this._Benchmark_benchmark_5.payloads.conditionalRequestOneByOne.evaluate();
            this._Benchmark_benchmark_6.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable.evaluate();
            this._Benchmark_benchmark_7.payloads.doubleOnError.evaluate();
        }

   }

}