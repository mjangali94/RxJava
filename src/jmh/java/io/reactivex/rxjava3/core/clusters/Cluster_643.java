package io.reactivex.rxjava3.core.clusters;

public class Cluster_643 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFromSupplierTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_643() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerError.evaluate();
            this._Benchmark_benchmark_1.payloads.toObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.toObservableAndBack.evaluate();
            this._Benchmark_benchmark_2.payloads.toObservableTake.evaluate();
            this._Benchmark_benchmark_4.payloads.cancelComposes.evaluate();
            this._Benchmark_benchmark_5.payloads.fusedEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_7.payloads.disposeToObservable.evaluate();
            this._Benchmark_benchmark_7.payloads.disposeBeforeEnd.evaluate();
            this._Benchmark_benchmark_6.payloads.emptyLastOrErrorObservable.evaluate();
            this._Benchmark_benchmark_4.payloads.error.evaluate();
            this._Benchmark_benchmark_0.payloads.simple.evaluate();
        }

   }

}