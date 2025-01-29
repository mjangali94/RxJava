package io.reactivex.rxjava3.core.clusters;

public class Cluster_431 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptySingleTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_431() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.bufferSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_1.payloads.errorOther.evaluate();
            this._Benchmark_benchmark_2.payloads.errorOther.evaluate();
            this._Benchmark_benchmark_0.payloads.scanSeedSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.observerDelayError.evaluate();
        }

   }

}