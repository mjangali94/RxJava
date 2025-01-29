package io.reactivex.rxjava3.core.clusters;

public class Cluster_31 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeToFutureTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRefCountTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeToFutureTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_31() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.customFusion.evaluate();
            this._Benchmark_benchmark_0.payloads.customFusionClear.evaluate();
            this._Benchmark_benchmark_2.payloads.noOpConnect.evaluate();
            this._Benchmark_benchmark_3.payloads.zipperReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.zipperThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.normalError.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel2.evaluate();
            this._Benchmark_benchmark_8.payloads.retryWhenFunctionReturnsNull.evaluate();
        }

   }

}