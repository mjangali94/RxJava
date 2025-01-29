package io.reactivex.rxjava3.core.clusters;

public class Cluster_892 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithObservableTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithObservableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_892() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath.evaluate();
            this._Benchmark_benchmark_1.payloads.listWithBlockingFirst.evaluate();
            this._Benchmark_benchmark_0.payloads.subscribeMultipleTimes.evaluate();
            this._Benchmark_benchmark_0.payloads.noBackpressure.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.disposeAfterHasNext.evaluate();
            this._Benchmark_benchmark_0.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure.evaluate();
            this._Benchmark_benchmark_0.payloads.fusionRejected.evaluate();
            this._Benchmark_benchmark_8.payloads.concatIterableDelayError.evaluate();
            this._Benchmark_benchmark_9.payloads.flatMapIterableCombinerReturnsNull.evaluate();
            this._Benchmark_benchmark_10.payloads.innerBadSource.evaluate();
        }

   }

}