package io.reactivex.rxjava3.core.clusters;

public class Cluster_524 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_524() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatMapDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.concatArrayDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.concatObservableDelayError.evaluate();
            this._Benchmark_benchmark_3.payloads.switchMapDelayErrorJustSource.evaluate();
            this._Benchmark_benchmark_3.payloads.switchMapJustSource.evaluate();
            this._Benchmark_benchmark_5.payloads.flatMapCombinerCombinerReturnsNull.evaluate();
            this._Benchmark_benchmark_6.payloads.toSortedListComparatorCapacity.evaluate();
            this._Benchmark_benchmark_0.payloads.concat3.evaluate();
            this._Benchmark_benchmark_0.payloads.concat4.evaluate();
            this._Benchmark_benchmark_9.payloads.disposed.evaluate();
            this._Benchmark_benchmark_6.payloads.toSortedListCapacity.evaluate();
            this._Benchmark_benchmark_0.payloads.noCancelPreviousArray.evaluate();
        }

   }

}