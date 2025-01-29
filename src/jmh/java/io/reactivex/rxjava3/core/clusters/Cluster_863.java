package io.reactivex.rxjava3.core.clusters;

public class Cluster_863 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithStartEndObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableUnsubscribeOnTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithStartEndObservableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.observers.DeferredScalarObserverTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableUnsubscribeOnTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_863() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.error.evaluate();
            this._Benchmark_benchmark_2.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_3.payloads.unsafe.evaluate();
            this._Benchmark_benchmark_3.payloads.wrap.evaluate();
        }

   }

}