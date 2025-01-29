package io.reactivex.rxjava3.core.clusters;

public class Cluster_1016 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1016() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.undeliverableUponCancelDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeScalar.evaluate();
            this._Benchmark_benchmark_2.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.concatMapDelayErrorJustJust);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.concatMapJustJust);
        }

   }

}