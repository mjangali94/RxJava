package io.reactivex.rxjava3.core.clusters;

public class Cluster_354 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDelayTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDelayTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_354() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.blockingFirst.evaluate();
            this._Benchmark_benchmark_1.payloads.disposed.evaluate();
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.empty);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.just);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.just);
        }

   }

}