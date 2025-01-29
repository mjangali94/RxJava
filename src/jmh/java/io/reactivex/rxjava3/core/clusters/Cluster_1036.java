package io.reactivex.rxjava3.core.clusters;

public class Cluster_1036 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapNotificationTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapNotificationTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1036() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.flatMapSingleValue.evaluate();
            this._Benchmark_benchmark_0.payloads.flatMapSingleValueDifferentType.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.successMany.evaluate();
            this._Benchmark_benchmark_2.payloads.successJust.evaluate();
            this._Benchmark_benchmark_5.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_6.payloads.doesntContain.evaluate();
            this._Benchmark_benchmark_6.payloads.doesContain.evaluate();
            this._Benchmark_benchmark_8.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.normal.evaluate();
            this._Benchmark_benchmark_10.payloads.success.evaluate();
            this._Benchmark_benchmark_11.payloads.defaultIfEmptyNonEmpty.evaluate();
            this._Benchmark_benchmark_12.payloads.normalWithEmpty.evaluate();
            this._Benchmark_benchmark_13.payloads.mapperReturnsNull.evaluate();
            this._Benchmark_benchmark_14.payloads.mapperReturnsNull.evaluate();
        }

   }

}