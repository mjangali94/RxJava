package io.reactivex.rxjava3.core.clusters;

public class Cluster_746 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromCallableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.disposables.DisposableHelperTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromRunnableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.observers.BlockingFirstObserverTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromSupplierTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableUnsafeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableResourceWrapperTest._Benchmark _Benchmark_benchmark_20;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark _Benchmark_benchmark_21;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromCallableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.disposables.DisposableHelperTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromRunnableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.observers.BlockingFirstObserverTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableUnsafeTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.observable.ObservableResourceWrapperTest._Benchmark();
            _Benchmark_benchmark_21 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
            this._Benchmark_benchmark_21.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_746() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fromCallableThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.trySet.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeWhileRunningComplete.evaluate();
            this._Benchmark_benchmark_3.payloads.disposeWhileRunningComplete.evaluate();
            this._Benchmark_benchmark_4.payloads.firstValueOnly.evaluate();
            this._Benchmark_benchmark_5.payloads.fromSupplier.evaluate();
            this._Benchmark_benchmark_6.payloads.wrapCustomCompletable.evaluate();
            this._Benchmark_benchmark_5.payloads.fromSupplierInvokesLazy.evaluate();
            this._Benchmark_benchmark_5.payloads.fromSupplierTwice.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableDisposed.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionErrorsDisposed.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableErrorsDisposed.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionDisposed.evaluate();
            this._Benchmark_benchmark_2.payloads.fromAction.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnable.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableInvokesLazy.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionInvokesLazy.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionTwice.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableTwice.evaluate();
            this._Benchmark_benchmark_19.payloads.andThenFirstCancels.evaluate();
            this._Benchmark_benchmark_20.payloads.disposed.evaluate();
            this._Benchmark_benchmark_21.payloads.rebatchRequestsArgumentCheck.evaluate();
        }

   }

}