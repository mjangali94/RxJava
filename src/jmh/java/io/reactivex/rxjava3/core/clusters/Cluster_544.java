package io.reactivex.rxjava3.core.clusters;

public class Cluster_544 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_544() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.syncFusedCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedNone.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedAll.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedMixed.evaluate();
            this._Benchmark_benchmark_0.payloads.prefetchFlowable.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_6.payloads.takeUntilWithPublishedStream.evaluate();
        }

   }

}