package io.reactivex.rxjava3.core.clusters;

public class Cluster_856 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRefCountTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRefCountTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_856() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerSingleError.evaluate();
            this._Benchmark_benchmark_1.payloads.nullIterableElement.evaluate();
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerMaybeObserverError.evaluate();
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerObserverError.evaluate();
            this._Benchmark_benchmark_4.payloads.badSourceDispose.evaluate();
        }

   }

}