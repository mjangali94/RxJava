package io.reactivex.rxjava3.core.clusters;

public class Cluster_12 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableBlockingSubscribeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptySingleTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeToObservableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapCompletableTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeToCompletableTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromPublisherTest._Benchmark _Benchmark_benchmark_20;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark _Benchmark_benchmark_21;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark _Benchmark_benchmark_22;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark _Benchmark_benchmark_23;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromSingleTest._Benchmark _Benchmark_benchmark_24;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipIterableTest._Benchmark _Benchmark_benchmark_26;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark _Benchmark_benchmark_27;
       private io.reactivex.rxjava3.core.TransformerTest._Benchmark _Benchmark_benchmark_29;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeJustTest._Benchmark _Benchmark_benchmark_30;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptySingleTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeToObservableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableReduceTests._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.single.SingleDelayTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeToCompletableTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromPublisherTest._Benchmark();
            _Benchmark_benchmark_21 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark();
            _Benchmark_benchmark_22 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark();
            _Benchmark_benchmark_23 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark();
            _Benchmark_benchmark_24 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromSingleTest._Benchmark();
            _Benchmark_benchmark_26 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipIterableTest._Benchmark();
            _Benchmark_benchmark_27 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark();
            _Benchmark_benchmark_29 = new io.reactivex.rxjava3.core.TransformerTest._Benchmark();
            _Benchmark_benchmark_30 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeJustTest._Benchmark();
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
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
            this._Benchmark_benchmark_21.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
            this._Benchmark_benchmark_23.makePayloads();
            this._Benchmark_benchmark_24.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_26.makePayloads();
            this._Benchmark_benchmark_27.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_29.makePayloads();
            this._Benchmark_benchmark_30.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_12() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.withCompletable.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.noArgComplete);
            this._Benchmark_benchmark_2.payloads.withEmpty.evaluate();
            this._Benchmark_benchmark_3.payloads.withEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.withNonEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.complete.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.repeatLongPredicateInvalid);
            this._Benchmark_benchmark_7.payloads.invalidPrefetch.evaluate();
            this._Benchmark_benchmark_8.payloads.source.evaluate();
            this._Benchmark_benchmark_9.payloads.source.evaluate();
            this._Benchmark_benchmark_10.payloads.reduceWithCovariantObjects.evaluate();
            this._Benchmark_benchmark_10.payloads.reduceCovariance.evaluate();
            this._Benchmark_benchmark_3.payloads.withNonEmpty.evaluate();
            this._Benchmark_benchmark_13.payloads.withCompletableDispose.evaluate();
            this._Benchmark_benchmark_14.payloads.dispose.evaluate();
            this._Benchmark_benchmark_15.payloads.dispose.evaluate();
            this._Benchmark_benchmark_16.payloads.dispose.evaluate();
            this._Benchmark_benchmark_17.payloads.withCompletableDispose.evaluate();
            this._Benchmark_benchmark_18.payloads.source.evaluate();
            this._Benchmark_benchmark_10.payloads.reduceWithCovariantObjectsFlowable.evaluate();
            this._Benchmark_benchmark_20.payloads.dispose.evaluate();
            this._Benchmark_benchmark_21.payloads.source.evaluate();
            this._Benchmark_benchmark_22.payloads.invalidPrefetch.evaluate();
            this._Benchmark_benchmark_23.payloads.fromCompletable.evaluate();
            this._Benchmark_benchmark_24.payloads.fromSingle.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.observerComplete);
            this._Benchmark_benchmark_26.payloads.zipIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_27.payloads.andThenMaybeCompleteValue.evaluate();
            this._Benchmark_benchmark_10.payloads.reduceWithObjects.evaluate();
            this._Benchmark_benchmark_29.payloads.flowableTransformerThrows.evaluate();
            this._Benchmark_benchmark_30.payloads.scalarSupplier.evaluate();
        }

   }

}