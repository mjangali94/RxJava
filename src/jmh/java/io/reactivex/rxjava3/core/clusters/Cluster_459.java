package io.reactivex.rxjava3.core.clusters;

public class Cluster_459 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipIterableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleElementTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapSingleTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.MaybeToCompletionStageTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlattenTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeToCompletableTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromMaybeTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFromOptionalTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeEqualTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.jdk8.MaybeMapOptionalTest._Benchmark _Benchmark_benchmark_21;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark _Benchmark_benchmark_22;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark _Benchmark_benchmark_25;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark _Benchmark_benchmark_28;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark _Benchmark_benchmark_31;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipIterableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleElementTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapSingleTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.MaybeToCompletionStageTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlattenTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeToCompletableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.jdk8.MaybeFromOptionalTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeEqualTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_21 = new io.reactivex.rxjava3.internal.jdk8.MaybeMapOptionalTest._Benchmark();
            _Benchmark_benchmark_22 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark();
            _Benchmark_benchmark_25 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_28 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_31 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_21.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_25.makePayloads();
            this._Benchmark_benchmark_25.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_28.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_28.makePayloads();
            this._Benchmark_benchmark_31.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_459() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.noArgSuccess);
            this._Benchmark_benchmark_1.payloads.zipIterableTwoIsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_4.payloads.just.evaluate();
            this._Benchmark_benchmark_5.payloads.lastWithNoElements.evaluate();
            this._Benchmark_benchmark_6.payloads.blockingSingleEmpty.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_8.payloads.offlineSuccess.evaluate();
            this._Benchmark_benchmark_9.payloads.errorPassesThruMap.evaluate();
            this._Benchmark_benchmark_10.payloads.zipArrayOneIsNull.evaluate();
            this._Benchmark_benchmark_11.payloads.dispose.evaluate();
            this._Benchmark_benchmark_12.payloads.dispose.evaluate();
            this._Benchmark_benchmark_13.payloads.successToComplete.evaluate();
            this._Benchmark_benchmark_14.payloads.fromMaybe.evaluate();
            this._Benchmark_benchmark_15.payloads.nonEmpty.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.noArgEmpty);
            this._Benchmark_benchmark_17.payloads.hasValue.evaluate();
            this._Benchmark_benchmark_18.payloads.dispose.evaluate();
            this._Benchmark_benchmark_19.payloads.switchWhenEmpty.evaluate();
            this._Benchmark_benchmark_13.payloads.dispose.evaluate();
            this._Benchmark_benchmark_21.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_22.payloads.one.evaluate();
            this._Benchmark_benchmark_2.payloads.flatMapSingleValueDifferentType.evaluate();
            this._Benchmark_benchmark_2.payloads.flatMapSingleValue.evaluate();
            this._Benchmark_benchmark_25.payloads.flatMapMaybeValueDifferentType.evaluate();
            this._Benchmark_benchmark_25.payloads.flatMapMaybeValue.evaluate();
            this._Benchmark_benchmark_3.payloads.flatMapSingleElementValue.evaluate();
            this._Benchmark_benchmark_28.payloads.concatMapMaybeValue.evaluate();
            this._Benchmark_benchmark_3.payloads.flatMapSingleElementValueDifferentType.evaluate();
            this._Benchmark_benchmark_28.payloads.concatMapMaybeValueDifferentType.evaluate();
            this._Benchmark_benchmark_31.payloads.normalToMaybe.evaluate();
        }

   }

}