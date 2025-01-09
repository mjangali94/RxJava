package io.reactivex.rxjava3.core.clusters;

public class Cluster_1115 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1115() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.fusedToParallel2.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_2.payloads.boundaryFusedNone.evaluate();
            this._Benchmark_benchmark_3.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedNone.evaluate();
            this._Benchmark_benchmark_2.payloads.boundaryFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.boundaryFusedMixed.evaluate();
            this._Benchmark_benchmark_2.payloads.boundaryFusedAll.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedAll.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedMixed.evaluate();
            this._Benchmark_benchmark_10.payloads.asyncUpstreamFusionBoundary.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedNoneConditional.evaluate();
        }

   }

}