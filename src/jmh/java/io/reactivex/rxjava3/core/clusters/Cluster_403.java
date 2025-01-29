package io.reactivex.rxjava3.core.clusters;

public class Cluster_403 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.util.HalfSerializerObserverTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.SingleFromCompletionStageTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.util.HalfSerializerObserverTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.SingleFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_403() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.sourceObservableRetry0.evaluate();
            this._Benchmark_benchmark_1.payloads.onSuccessAfterDispose.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantOnNextOnNext.evaluate();
            this._Benchmark_benchmark_0.payloads.sourceObservableRetry1.evaluate();
            this._Benchmark_benchmark_4.payloads.syncNull.evaluate();
            this._Benchmark_benchmark_5.payloads.delayOnError.evaluate();
        }

   }

}