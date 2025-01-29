package io.reactivex.rxjava3.core.clusters;

public class Cluster_154 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableMergeTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableMergeTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.flowable.FlowableSubscriberTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_154() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mergeCovariance.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeCovariance3.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeCovariance2.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeCovariance4.evaluate();
            this._Benchmark_benchmark_4.payloads.flatMapBiMapperMaxConcurrency.evaluate();
            this._Benchmark_benchmark_4.payloads.allConcurrency.evaluate();
            this._Benchmark_benchmark_6.payloads.just.evaluate();
            this._Benchmark_benchmark_7.payloads.fusedFlatMapExecutionHidden.evaluate();
            this._Benchmark_benchmark_8.payloads.fusedFlatMapExecutionHidden.evaluate();
            this._Benchmark_benchmark_9.payloads.withResultSelectorMaxConcurrent.evaluate();
            this._Benchmark_benchmark_4.payloads.allConcurrencyScalarInnerEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.maxConcurrencySustained.evaluate();
            this._Benchmark_benchmark_4.payloads.allConcurrencyScalarInner.evaluate();
            this._Benchmark_benchmark_4.payloads.someConcurrencyScalarInnerCancel.evaluate();
            this._Benchmark_benchmark_14.runBenchmark(this._Benchmark_benchmark_14.payloads.onStartCalledOnceViaLift);
            this._Benchmark_benchmark_4.payloads.mergeScalarEmpty.evaluate();
            this._Benchmark_benchmark_16.payloads.skipOne.evaluate();
        }

   }

}