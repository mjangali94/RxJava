package io.reactivex.rxjava3.core.clusters;

public class Cluster_321 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeToFutureTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeToFutureTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_321() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.justSecondsScheduler);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justSecondsScheduler);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.justScheduler);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justScheduler);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.justSeconds);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justSeconds);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.just);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.just);
        }

   }

}