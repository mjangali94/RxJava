package io.reactivex.rxjava3.core.clusters;

public class Cluster_601 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_601() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.switchTriggerUnsubscribe.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.doubleOnSubscribe);
            this._Benchmark_benchmark_2.payloads.concatMapDelayError.evaluate();
        }

   }

}