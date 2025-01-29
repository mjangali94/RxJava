package io.reactivex.rxjava3.core.clusters;

public class Cluster_448 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.MaybeToCompletionStageTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFromCompletionStageTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMapTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromFutureTest._Benchmark _Benchmark_benchmark_13;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.MaybeToCompletionStageTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.MaybeFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMapTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromFutureTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_448() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.completableFutureCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.completableManualCompleteCancels.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_0.payloads.completableManualCompleteExceptionallyCancels.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.dispose);
            this._Benchmark_benchmark_6.payloads.consumerDisposed.evaluate();
            this._Benchmark_benchmark_7.payloads.disposeUpfront.evaluate();
            this._Benchmark_benchmark_8.payloads.scalarMapToCrashingCallable.evaluate();
            this._Benchmark_benchmark_9.payloads.firstCancels.evaluate();
            this._Benchmark_benchmark_10.payloads.firstCancels.evaluate();
            this._Benchmark_benchmark_11.payloads.disposeOnArrival2.evaluate();
            this._Benchmark_benchmark_12.payloads.refCountSynchronousTake.evaluate();
            this._Benchmark_benchmark_13.payloads.cancelImmediately.evaluate();
        }

   }

}