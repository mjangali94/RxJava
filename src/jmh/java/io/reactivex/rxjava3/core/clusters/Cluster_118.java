package io.reactivex.rxjava3.core.clusters;

public class Cluster_118 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatEagerTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToFutureTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromCompletionStageTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatEagerTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToFutureTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCacheTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_118() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.iterableError);
            this._Benchmark_benchmark_1.payloads.withTimeoutNoTimeout.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.iterableErrorMaxConcurrency);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.iterableDelayError);
            this._Benchmark_benchmark_4.payloads.syncSuccess.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.iterableDelayErrorMaxConcurrency);
            this._Benchmark_benchmark_4.payloads.cancel.evaluate();
            this._Benchmark_benchmark_7.payloads.cancelledUpFrontConnectAnyway.evaluate();
        }

   }

}