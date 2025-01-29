package io.reactivex.rxjava3.core.clusters;

public class Cluster_785 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnLifecycleTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnLifecycleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_785() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapErrorEmptySource);
            this._Benchmark_benchmark_1.payloads.fromObservableJust.evaluate();
            this._Benchmark_benchmark_2.payloads.combineLatestDelayErrorIterableFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_2.payloads.combineLatestIterableFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.fusionRejectedDelayErrorr);
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.scalarInnerJustDisposeDelayError);
            this._Benchmark_benchmark_6.payloads.actionThrows.evaluate();
            this._Benchmark_benchmark_7.payloads.isDisposed.evaluate();
        }

   }

}