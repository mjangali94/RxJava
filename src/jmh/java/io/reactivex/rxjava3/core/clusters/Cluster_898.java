package io.reactivex.rxjava3.core.clusters;

public class Cluster_898 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorCompleteTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_898() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapDelayErrorEmptySource);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.errorMatches);
            this._Benchmark_benchmark_2.payloads.dontSubscribeIfDone2.evaluate();
            this._Benchmark_benchmark_3.payloads.replaySelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.error);
        }

   }

}