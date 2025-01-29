package io.reactivex.rxjava3.core.clusters;

public class Cluster_916 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.util.HalfSerializerObserverTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableLatestTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableGenerateTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.util.HalfSerializerObserverTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_916() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.nextThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_4.payloads.reentrantOnNextOnComplete.evaluate();
            this._Benchmark_benchmark_5.payloads.errorFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_5.payloads.zipWithIterableIteratorNull.evaluate();
            this._Benchmark_benchmark_7.payloads.connectIsIdempotent.evaluate();
        }

   }

}