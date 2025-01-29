package io.reactivex.rxjava3.core.clusters;

public class Cluster_635 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.completable.CompletableRetryTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.completable.CompletableRetryTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_635() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.emptyCompletableComplete);
            this._Benchmark_benchmark_1.payloads.untilTrueEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.untilFalseEmpty.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.singleNormal);
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.justCompletableComplete);
            this._Benchmark_benchmark_1.payloads.untilFalseError.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.justCompletableError);
            this._Benchmark_benchmark_1.payloads.untilTrueError.evaluate();
        }

   }

}