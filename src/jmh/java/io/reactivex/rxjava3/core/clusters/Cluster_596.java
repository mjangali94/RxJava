package io.reactivex.rxjava3.core.clusters;

public class Cluster_596 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_596() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.basicFusionRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.disposed.evaluate();
            this._Benchmark_benchmark_3.payloads.empty.evaluate();
            this._Benchmark_benchmark_4.payloads.innerError.evaluate();
            this._Benchmark_benchmark_0.payloads.mainBoundaryErrorInnerEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.checkUnboundedInnerQueue.evaluate();
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.justObservableError);
        }

   }

}