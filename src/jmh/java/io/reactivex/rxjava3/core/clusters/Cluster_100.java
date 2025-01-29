package io.reactivex.rxjava3.core.clusters;

public class Cluster_100 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.jdk8.SingleMapOptionalTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_100() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.error.evaluate();
            this._Benchmark_benchmark_1.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.successJust.evaluate();
            this._Benchmark_benchmark_1.payloads.successMany.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedMany.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedJust.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedManyRejected.evaluate();
            this._Benchmark_benchmark_1.payloads.successManyTake.evaluate();
            this._Benchmark_benchmark_8.payloads.doOnDisposeSuccess.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtIndex1WithDefaultOnEmptySource.evaluate();
            this._Benchmark_benchmark_9.payloads.elementAtIndex0WithDefaultOnEmptySource.evaluate();
            this._Benchmark_benchmark_11.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_12.payloads.lastOrErrorNoElement.evaluate();
            this._Benchmark_benchmark_13.payloads.success.evaluate();
            this._Benchmark_benchmark_14.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_15.payloads.empty.evaluate();
            this._Benchmark_benchmark_16.payloads.lastOfEmptyReturnsEmpty.evaluate();
        }

   }

}