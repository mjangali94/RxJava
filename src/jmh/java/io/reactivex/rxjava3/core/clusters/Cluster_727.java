package io.reactivex.rxjava3.core.clusters;

public class Cluster_727 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_727() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.success.evaluate();
            this._Benchmark_benchmark_0.payloads.unsubscribeIdempotence.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperReturnsNullObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperThrowsObservable.evaluate();
            this._Benchmark_benchmark_4.payloads.bufferOpenCloseCloseReturnsNull.evaluate();
            this._Benchmark_benchmark_5.payloads.mainError.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel.evaluate();
        }

   }

}