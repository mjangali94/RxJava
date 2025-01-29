package io.reactivex.rxjava3.core.clusters;

public class Cluster_521 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipUntilTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFromCompletionStageTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipUntilTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.jdk8.ObservableFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
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
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_521() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedJust.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedMany.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedManyRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.successManyTake.evaluate();
            this._Benchmark_benchmark_4.payloads.scalarMapperCrash.evaluate();
            this._Benchmark_benchmark_5.payloads.concatIterableDelayErrorWithError.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelledUpFront.evaluate();
            this._Benchmark_benchmark_7.payloads.noDownstreamUnsubscribe.evaluate();
            this._Benchmark_benchmark_8.payloads.disposedUpfront.evaluate();
            this._Benchmark_benchmark_9.payloads.startWithIterableOneNull.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_11.payloads.emptyToObservable.evaluate();
            this._Benchmark_benchmark_12.payloads.syncSuccess.evaluate();
            this._Benchmark_benchmark_13.payloads.take.evaluate();
            this._Benchmark_benchmark_14.payloads.take.evaluate();
        }

   }

}