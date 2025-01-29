package io.reactivex.rxjava3.core.clusters;

public class Cluster_89 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_89() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedFlatMapNullHidden.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedFlatMapNullHidden.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.mergeDelayErrorIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.lastRange.evaluate();
            this._Benchmark_benchmark_5.payloads.lastRange.evaluate();
            this._Benchmark_benchmark_3.payloads.mergeIterableOneIsNull.evaluate();
        }

   }

}