package io.reactivex.rxjava3.core.clusters;

public class Cluster_400 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableIntervalRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIntervalRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_400() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.error);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.error);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.error);
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.error);
        }

   }

}