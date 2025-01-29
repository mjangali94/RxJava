package io.reactivex.rxjava3.core.clusters;

public class Cluster_67 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.flowable.FlowableMergeTests._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.observers.FutureSingleObserverTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.SingleToCompletionStageTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.core.ConverterTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableFlowableTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark _Benchmark_benchmark_20;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark _Benchmark_benchmark_23;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark _Benchmark_benchmark_24;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark _Benchmark_benchmark_29;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark _Benchmark_benchmark_30;
       private io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark _Benchmark_benchmark_31;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.flowable.FlowableMergeTests._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.observers.FutureSingleObserverTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.SingleToCompletionStageTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.core.ConverterTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableFlowableTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_23 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_24 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark();
            _Benchmark_benchmark_29 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_30 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark();
            _Benchmark_benchmark_31 = new io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_23.makePayloads();
            this._Benchmark_benchmark_24.makePayloads();
            this._Benchmark_benchmark_24.makePayloads();
            this._Benchmark_benchmark_23.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_29.makePayloads();
            this._Benchmark_benchmark_30.makePayloads();
            this._Benchmark_benchmark_31.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_67() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.withSingle.evaluate();
            this._Benchmark_benchmark_1.payloads.covarianceOfMerge.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.just.evaluate();
            this._Benchmark_benchmark_2.payloads.normalGetWitHTimeout.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.noArgSuccess);
            this._Benchmark_benchmark_6.payloads.composeWithDeltaLogic.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_8.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.fusedEmptyCheck.evaluate();
            this._Benchmark_benchmark_11.payloads.normal.evaluate();
            this._Benchmark_benchmark_12.payloads.rangeWithOverflow3.evaluate();
            this._Benchmark_benchmark_13.payloads.parallelFlowableGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_14.payloads.fusedEmptyCheck.evaluate();
            this._Benchmark_benchmark_15.payloads.flatMapEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.covarianceOfCompose3.evaluate();
            this._Benchmark_benchmark_6.payloads.covarianceOfCompose4.evaluate();
            this._Benchmark_benchmark_18.payloads.zipIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_19.payloads.dispose.evaluate();
            this._Benchmark_benchmark_20.payloads.dispose.evaluate();
            this._Benchmark_benchmark_8.payloads.flatMapObservable.evaluate();
            this._Benchmark_benchmark_18.payloads.zipIterableTwoIsNull.evaluate();
            this._Benchmark_benchmark_23.payloads.fusedFlatMapExecution.evaluate();
            this._Benchmark_benchmark_24.payloads.fusedFlatMapExecution.evaluate();
            this._Benchmark_benchmark_24.payloads.fusedFlatMapNull.evaluate();
            this._Benchmark_benchmark_23.payloads.fusedFlatMapNull.evaluate();
            this._Benchmark_benchmark_13.payloads.flowableGenericsSignatureTest.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyIterable.evaluate();
            this._Benchmark_benchmark_29.payloads.normal.evaluate();
            this._Benchmark_benchmark_30.payloads.doOnSuccess.evaluate();
            this._Benchmark_benchmark_31.payloads.withSingleDispose.evaluate();
        }

   }

}