package io.reactivex.rxjava3.core.clusters;

public class Cluster_203 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureErrorTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_203() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.errorDelayed2.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedNone.evaluate();
            this._Benchmark_benchmark_2.payloads.onSubscribe.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncUpstreamFused.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedAll.evaluate();
            this._Benchmark_benchmark_1.payloads.boundaryFusedAll.evaluate();
            this._Benchmark_benchmark_6.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.asyncFusedMixed.evaluate();
        }

   }

}