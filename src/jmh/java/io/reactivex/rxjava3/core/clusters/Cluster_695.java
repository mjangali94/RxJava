package io.reactivex.rxjava3.core.clusters;

public class Cluster_695 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFromStreamTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.ObservableFromStreamTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchIfEmptyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_695() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_1.payloads.emptyConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.valuesAndThenError.evaluate();
            this._Benchmark_benchmark_3.payloads.concatMapErrorEmptySource.evaluate();
            this._Benchmark_benchmark_4.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable.evaluate();
            this._Benchmark_benchmark_0.payloads.onErrorAvailableUntilReset.evaluate();
            this._Benchmark_benchmark_6.payloads.errorDelayed2.evaluate();
            this._Benchmark_benchmark_7.payloads.emptySource.evaluate();
            this._Benchmark_benchmark_7.payloads.emptyOther.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyConditional.evaluate();
            this._Benchmark_benchmark_10.payloads.empty.evaluate();
            this._Benchmark_benchmark_11.payloads.fromArrayEmpty.evaluate();
            this._Benchmark_benchmark_12.payloads.normalEmptyConditional.evaluate();
            this._Benchmark_benchmark_13.payloads.workerNotDisposedPrematurelyAsyncInNormalOut.evaluate();
            this._Benchmark_benchmark_14.payloads.switchTriggerUnsubscribe.evaluate();
        }

   }

}