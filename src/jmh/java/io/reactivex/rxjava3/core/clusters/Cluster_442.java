package io.reactivex.rxjava3.core.clusters;

public class Cluster_442 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSingleTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatEagerTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatEagerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_442() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatArrayEagerTest.evaluate();
            this._Benchmark_benchmark_0.payloads.concatEagerPublisherTest.evaluate();
            this._Benchmark_benchmark_2.payloads.error.evaluate();
            this._Benchmark_benchmark_3.payloads.errorLastOrErrorFlowable.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.iterableDelayError);
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.iterableDelayErrorMaxConcurrency);
        }

   }

}