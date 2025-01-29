package io.reactivex.rxjava3.core.clusters;

public class Cluster_173 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_173() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerEscapeCompleted.evaluate();
            this._Benchmark_benchmark_0.payloads.delayErrorSimpleComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.groupByCompose.evaluate();
            this._Benchmark_benchmark_3.payloads.badRequest.evaluate();
        }

   }

}