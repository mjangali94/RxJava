package io.reactivex.rxjava3.core.clusters;

public class Cluster_114 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_114() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.debounceWithEmpty.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.concatMapJustRange);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.noCancelPrevious);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.concatMapJustSource);
            this._Benchmark_benchmark_4.payloads.workerNotDisposedPrematurelySyncInNormalOut.evaluate();
            this._Benchmark_benchmark_5.payloads.concatWithNonCompliantSourceDoubleOnComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.fused.evaluate();
            this._Benchmark_benchmark_5.payloads.concatObservableDelayErrorTillEnd.evaluate();
            this._Benchmark_benchmark_5.payloads.concatMapDelayErrorWithError.evaluate();
        }

   }

}