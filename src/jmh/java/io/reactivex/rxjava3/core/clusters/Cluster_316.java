package io.reactivex.rxjava3.core.clusters;

public class Cluster_316 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_316() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.secondJustError.evaluate();
            this._Benchmark_benchmark_1.payloads.tryTerminateConsumerSubscriberNoError.evaluate();
            this._Benchmark_benchmark_0.payloads.errorDelayed.evaluate();
            this._Benchmark_benchmark_3.payloads.generateConsumerStateNullAllowed.evaluate();
        }

   }

}