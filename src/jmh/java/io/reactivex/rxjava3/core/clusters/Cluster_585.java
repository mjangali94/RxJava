package io.reactivex.rxjava3.core.clusters;

public class Cluster_585 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromCallableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.NotificationLiteTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.disposables.DisposableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.observers.BlockingMultiObserverTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCallbackObserverTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithTimeTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableSubscribeOnTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromCallableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.NotificationLiteTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.disposables.DisposableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.observers.BlockingMultiObserverTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCallbackObserverTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithTimeTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.completable.CompletableSubscribeOnTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_585() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fromCallable.evaluate();
            this._Benchmark_benchmark_0.payloads.fromCallableInvokesLazy.evaluate();
            this._Benchmark_benchmark_0.payloads.fromCallableTwice.evaluate();
            this._Benchmark_benchmark_3.payloads.onCompleteThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.onErrorThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.disposableNotification.evaluate();
            this._Benchmark_benchmark_6.payloads.empty.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_8.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.windowAbandonmentCancelsUpstreamExactTime.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_11.payloads.addAfterDisposed.evaluate();
        }

   }

}