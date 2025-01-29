package io.reactivex.rxjava3.core.clusters;

public class Cluster_19 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableReplayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleTimestampTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleTimeIntervalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_19() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.unsafeChildOnNextThrows.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.timeInfo);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.timeInfo);
            this._Benchmark_benchmark_3.payloads.onSubscribe.evaluate();
        }

   }

}