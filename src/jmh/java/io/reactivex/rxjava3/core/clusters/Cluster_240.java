package io.reactivex.rxjava3.core.clusters;

public class Cluster_240 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_240() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.interruptWait.evaluate();
            this._Benchmark_benchmark_1.payloads.boundaryFusedMixed.evaluate();
            this._Benchmark_benchmark_1.payloads.boundaryFusedAll.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedAll.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedMixed.evaluate();
            this._Benchmark_benchmark_1.payloads.boundaryFusedMixedConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.boundaryFusedAllConditiona.evaluate();
            this._Benchmark_benchmark_8.payloads.asyncUpstreamFusionBoundary.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedAllConditional.evaluate();
        }

   }

}