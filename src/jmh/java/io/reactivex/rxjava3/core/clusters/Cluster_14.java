package io.reactivex.rxjava3.core.clusters;

public class Cluster_14 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableUnsafeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDoOnTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCreateTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMaterializeTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableUnsafeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDoOnTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCreateTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMaterializeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark();
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
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_14() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.scalarMapperCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.hasSource.evaluate();
            this._Benchmark_benchmark_2.payloads.upstream.evaluate();
            this._Benchmark_benchmark_3.payloads.unsafeCreateThrowsNPE.evaluate();
            this._Benchmark_benchmark_4.payloads.doOnDisposeCalled.evaluate();
            this._Benchmark_benchmark_5.payloads.takeNegative.evaluate();
            this._Benchmark_benchmark_6.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_7.payloads.unsafeWithFlowable.evaluate();
            this._Benchmark_benchmark_8.payloads.error.evaluate();
            this._Benchmark_benchmark_9.payloads.emitterHasToString.evaluate();
        }

   }

}