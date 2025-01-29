package io.reactivex.rxjava3.core.clusters;

public class Cluster_647 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableForEachTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableForEachTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_647() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarInnerOuterOverflowSlowPath.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarInnerOuterOverflow.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextThrowsSecondTimeSlowpath.evaluate();
            this._Benchmark_benchmark_0.payloads.nextThrowsSlowpath.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
        }

   }

}