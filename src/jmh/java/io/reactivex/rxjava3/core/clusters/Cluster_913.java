package io.reactivex.rxjava3.core.clusters;

public class Cluster_913 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_913() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.undeliverableUponCancellation.evaluate();
            this._Benchmark_benchmark_1.payloads.undeliverableUponCancellation.evaluate();
            this._Benchmark_benchmark_2.payloads.onSuccessSlowPathBackpressured.evaluate();
        }

   }

}