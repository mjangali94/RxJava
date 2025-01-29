package io.reactivex.rxjava3.core.clusters;

public class Cluster_317 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapSingleTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_317() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.disposeOnNextAfterFirst.evaluate();
            this._Benchmark_benchmark_0.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.drainReentrant.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_4.payloads.switchMapSingleDelayErrorJustSource.evaluate();
            this._Benchmark_benchmark_0.payloads.disposeBeforeSwitchInOnNext.evaluate();
            this._Benchmark_benchmark_4.payloads.switchMapSingleJustSource.evaluate();
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.justObservableEmpty);
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.justFlowableEmpty);
            this._Benchmark_benchmark_9.payloads.toSortedListComparatorCapacityObservable.evaluate();
            this._Benchmark_benchmark_9.payloads.withFollowingFirst.evaluate();
            this._Benchmark_benchmark_9.payloads.toSortedListCapacityObservable.evaluate();
        }

   }

}