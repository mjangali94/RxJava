package io.reactivex.rxjava3.core.clusters;

public class Cluster_233 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDematerializeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDematerializeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_233() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_2.payloads.selectorCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.selectorNull.evaluate();
            this._Benchmark_benchmark_4.payloads.normalBackpressured.evaluate();
            this._Benchmark_benchmark_2.payloads.success.evaluate();
        }

   }

}