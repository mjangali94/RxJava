package io.reactivex.rxjava3.core.clusters;

public class Cluster_554 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayEagerTruncateTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_554() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.replayMaxInt.evaluate();
            this._Benchmark_benchmark_1.payloads.replayMaxInt.evaluate();
            this._Benchmark_benchmark_2.payloads.collectorSupplierCrashToObservable.evaluate();
            this._Benchmark_benchmark_0.payloads.connectConsumerThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.connectConsumerThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
        }

   }

}