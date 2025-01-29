package io.reactivex.rxjava3.core.clusters;

public class Cluster_441 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatEagerTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatArrayEagerDelayErrorTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatEagerTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatArrayEagerDelayErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_441() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.asyncFusion.evaluate();
            this._Benchmark_benchmark_0.payloads.success.evaluate();
            this._Benchmark_benchmark_0.payloads.syncFusionRejected.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.publisherNormalMaxConcurrency);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.publisherNormal);
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.justSingleJust);
            this._Benchmark_benchmark_6.payloads.toSortedListComparatorCapacityFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.toSortedListCapacityFlowable.evaluate();
            this._Benchmark_benchmark_8.runBenchmark(this._Benchmark_benchmark_8.payloads.normal);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.publisherDelayErrorMaxConcurrency);
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.publisherDelayError);
        }

   }

}