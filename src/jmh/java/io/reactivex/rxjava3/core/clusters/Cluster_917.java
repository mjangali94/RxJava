package io.reactivex.rxjava3.core.clusters;

public class Cluster_917 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.exceptions.OnErrorNotImplementedExceptionTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.exceptions.OnErrorNotImplementedExceptionTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_917() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.observableSubscribe0.evaluate();
            this._Benchmark_benchmark_0.payloads.observableSubscribe1.evaluate();
            this._Benchmark_benchmark_2.payloads.generateFunctionStateNullAllowed.evaluate();
            this._Benchmark_benchmark_3.payloads.disposed.evaluate();
            this._Benchmark_benchmark_2.payloads.generateConsumerStateNullAllowed.evaluate();
        }

   }

}