package io.reactivex.rxjava3.core.clusters;

public class Cluster_262 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableZipTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableZipTests._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_262() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.nonBlockingObservable.evaluate();
            this._Benchmark_benchmark_1.payloads.blockingLastEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.fromPublisherDispose.evaluate();
            this._Benchmark_benchmark_1.payloads.blockingFirstEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.fusedAPICalls.evaluate();
        }

   }

}