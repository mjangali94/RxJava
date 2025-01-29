package io.reactivex.rxjava3.core.clusters;

public class Cluster_502 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFirstTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableHideTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenObservableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFirstTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableHideTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenObservableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_502() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.justHidden.evaluate();
            this._Benchmark_benchmark_2.payloads.justHidden.evaluate();
            this._Benchmark_benchmark_3.payloads.firstOrErrorMultipleElements.evaluate();
            this._Benchmark_benchmark_4.payloads.disposed.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtOrErrorInvalidIndex.evaluate();
            this._Benchmark_benchmark_6.payloads.singleOrErrorMultipleElements.evaluate();
            this._Benchmark_benchmark_7.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_8.payloads.collectInto.evaluate();
        }

   }

}