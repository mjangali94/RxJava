package io.reactivex.rxjava3.core.clusters;

public class Cluster_862 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromSupplierTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.disposables.ListCompositeDisposableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromRunnableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.util.NotificationLiteTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.disposables.ArrayCompositeDisposableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRefCountTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableBlockingSubscribeTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark _Benchmark_benchmark_20;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.disposables.ListCompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromRunnableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.util.NotificationLiteTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.disposables.ArrayCompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRefCountTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.completable.CompletableBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_862() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.andThenCanceled.evaluate();
            this._Benchmark_benchmark_1.payloads.fromSupplierThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.afterDispose.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableThrows.evaluate();
            this._Benchmark_benchmark_4.payloads.combineLatestDelayErrorEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.acceptFullObserver.evaluate();
            this._Benchmark_benchmark_6.payloads.booleanSupplierPredicateReverse.evaluate();
            this._Benchmark_benchmark_7.payloads.normal.evaluate();
            this._Benchmark_benchmark_8.payloads.arrayFirstCancels.evaluate();
            this._Benchmark_benchmark_8.payloads.iterableFirstCancels.evaluate();
            this._Benchmark_benchmark_2.payloads.remove.evaluate();
            this._Benchmark_benchmark_11.payloads.timeoutResetsSource.evaluate();
            this._Benchmark_benchmark_12.runBenchmark(this._Benchmark_benchmark_12.payloads.observerError);
            this._Benchmark_benchmark_13.payloads.timedLateEmit.evaluate();
            this._Benchmark_benchmark_4.payloads.combineLatestArrayEmpty.evaluate();
            this._Benchmark_benchmark_15.payloads.timeoutResetsSource.evaluate();
            this._Benchmark_benchmark_16.payloads.disposedOnArrival.evaluate();
            this._Benchmark_benchmark_17.payloads.onErrorReturnDispose.evaluate();
            this._Benchmark_benchmark_16.payloads.disposedOnCall.evaluate();
            this._Benchmark_benchmark_19.payloads.combineLatestDelayErrorEmpty.evaluate();
            this._Benchmark_benchmark_20.payloads.timestepError.evaluate();
        }

   }

}