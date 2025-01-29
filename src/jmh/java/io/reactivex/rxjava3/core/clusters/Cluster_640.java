package io.reactivex.rxjava3.core.clusters;

public class Cluster_640 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableScanTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeWhileTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDistinctUntilChangedTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableScanTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeWhileTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_640() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.customComparatorThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.scanWithRequestOne.evaluate();
            this._Benchmark_benchmark_2.payloads.errorCauseIncludesLastValue.evaluate();
            this._Benchmark_benchmark_3.payloads.scanFunctionReturnsNull.evaluate();
        }

   }

}