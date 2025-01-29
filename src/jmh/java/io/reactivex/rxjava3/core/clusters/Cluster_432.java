package io.reactivex.rxjava3.core.clusters;

public class Cluster_432 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableNextTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReplayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableNextTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimestampTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeIntervalTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_432() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.onErrorOtherOverflow.evaluate();
            this._Benchmark_benchmark_0.payloads.onNextSlowPath.evaluate();
            this._Benchmark_benchmark_0.payloads.onNextSlowPathCreateQueue.evaluate();
            this._Benchmark_benchmark_3.payloads.replaySelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.resetWhileActiveIsNoOp.evaluate();
            this._Benchmark_benchmark_5.payloads.synchronousNext.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.timeInfo);
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.timeInfo);
        }

   }

}