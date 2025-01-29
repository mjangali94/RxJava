package io.reactivex.rxjava3.core.clusters;

public class Cluster_130 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToSortedListTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_130() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.asyncFusion.evaluate();
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.justObservableJust);
            this._Benchmark_benchmark_2.payloads.scalarQueueNoOverflowHidden.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.justSingleJust);
            this._Benchmark_benchmark_0.payloads.syncFusionRejected.evaluate();
            this._Benchmark_benchmark_5.payloads.withFollowingFirstObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.mergeIterableDelayErrorWithError.evaluate();
        }

   }

}