package io.reactivex.rxjava3.core.clusters;

public class Cluster_723 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_723() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.zipArrayOneIsNull.evaluate();
            this._Benchmark_benchmark_1.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.reduceWithSeedReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.collectInitialSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.toListSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.normal);
            this._Benchmark_benchmark_1.payloads.successSuccess.evaluate();
            this._Benchmark_benchmark_7.payloads.skipLastWithNegativeCount.evaluate();
        }

   }

}