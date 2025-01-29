package io.reactivex.rxjava3.core.clusters;

public class Cluster_90 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.ObservableBlockingStreamTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithCompletableTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_19;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_22;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.ObservableBlockingStreamTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCacheTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithCompletableTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark();
            _Benchmark_benchmark_19 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithCompletableTest._Benchmark();
            _Benchmark_benchmark_22 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_19.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_22.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_90() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.takeMain.evaluate();
            this._Benchmark_benchmark_1.payloads.range.evaluate();
            this._Benchmark_benchmark_2.payloads.normalDelayErrors.evaluate();
            this._Benchmark_benchmark_3.payloads.allEmptyConditional.evaluate();
            this._Benchmark_benchmark_3.payloads.allEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.requestWrongFusion.evaluate();
            this._Benchmark_benchmark_6.payloads.rangeBackpressured.evaluate();
            this._Benchmark_benchmark_7.payloads.fusedReject.evaluate();
            this._Benchmark_benchmark_2.payloads.normalDelayErrorsTillTheEnd.evaluate();
            this._Benchmark_benchmark_9.payloads.disposeOnArrival.evaluate();
            this._Benchmark_benchmark_10.payloads.basicToObservable.evaluate();
            this._Benchmark_benchmark_11.runBenchmark(this._Benchmark_benchmark_11.payloads.concatMapDelayErrorJustRange);
            this._Benchmark_benchmark_12.payloads.normal.evaluate();
            this._Benchmark_benchmark_13.payloads.syncFusedBoundary.evaluate();
            this._Benchmark_benchmark_14.payloads.range.evaluate();
            this._Benchmark_benchmark_3.payloads.allPresent.evaluate();
            this._Benchmark_benchmark_3.payloads.mixed.evaluate();
            this._Benchmark_benchmark_17.payloads.takeLastTake.evaluate();
            this._Benchmark_benchmark_14.payloads.rangeHidden.evaluate();
            this._Benchmark_benchmark_19.payloads.takeMain.evaluate();
            this._Benchmark_benchmark_3.payloads.mixedConditional.evaluate();
            this._Benchmark_benchmark_3.payloads.allPresentConditional.evaluate();
            this._Benchmark_benchmark_22.payloads.takeMain.evaluate();
            this._Benchmark_benchmark_11.runBenchmark(this._Benchmark_benchmark_11.payloads.innerWithScalar);
        }

   }

}