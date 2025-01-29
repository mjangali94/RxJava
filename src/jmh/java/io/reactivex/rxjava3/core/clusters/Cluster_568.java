package io.reactivex.rxjava3.core.clusters;

public class Cluster_568 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromObservableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDetachTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryWithPredicateTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_17;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenObservableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCompletableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromObservableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDetachTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryWithPredicateTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_568() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelMain.evaluate();
            this._Benchmark_benchmark_1.payloads.disposed.evaluate();
            this._Benchmark_benchmark_0.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_3.payloads.fromObservableEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.repeatUntilFalse.evaluate();
            this._Benchmark_benchmark_5.payloads.andThenCompletableCompleteComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.andThenMaybeCompleteEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.andThenCompletableCompleteNever.evaluate();
            this._Benchmark_benchmark_8.payloads.normalResumeNext.evaluate();
            this._Benchmark_benchmark_5.payloads.andThenDisposed.evaluate();
            this._Benchmark_benchmark_0.payloads.errorMain.evaluate();
            this._Benchmark_benchmark_11.payloads.lastViaFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.errorOther.evaluate();
            this._Benchmark_benchmark_13.payloads.disposeOnNextAfterFirst.evaluate();
            this._Benchmark_benchmark_14.payloads.dispose.evaluate();
            this._Benchmark_benchmark_15.payloads.fusionClear.evaluate();
            this._Benchmark_benchmark_16.payloads.issue3008RetryWithPredicate.evaluate();
            this._Benchmark_benchmark_17.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_16.payloads.issue3008RetryInfinite.evaluate();
        }

   }

}