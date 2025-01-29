package io.reactivex.rxjava3.core.clusters;

public class Cluster_39 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithCompletableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.CompletableFromCompletionStageTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableToObservableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableResumeNextTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.CompletableToCompletionStageTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithCompletableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.CompletableFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableToObservableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableResumeNextTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_39() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.completableFutureCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.completableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_2.payloads.arrayCancelled.evaluate();
            this._Benchmark_benchmark_2.payloads.iterableCancelled.evaluate();
            this._Benchmark_benchmark_4.payloads.cancel.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
            this._Benchmark_benchmark_0.payloads.completableManualCompleteExceptionallyCancels.evaluate();
            this._Benchmark_benchmark_7.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_8.payloads.disposed.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
        }

   }

}