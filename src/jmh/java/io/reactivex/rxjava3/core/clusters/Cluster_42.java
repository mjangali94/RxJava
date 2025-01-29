package io.reactivex.rxjava3.core.clusters;

public class Cluster_42 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_15;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_42() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.backpressureWithInitialValue.evaluate();
            this._Benchmark_benchmark_0.payloads.backpressureWithInitialValueObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.followingFirst.evaluate();
            this._Benchmark_benchmark_3.payloads.collectToString.evaluate();
            this._Benchmark_benchmark_4.payloads.withFollowingFirst.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtOrDefaultWithIndexOutOfBounds.evaluate();
            this._Benchmark_benchmark_3.payloads.collectToList.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtOrDefault.evaluate();
            this._Benchmark_benchmark_0.payloads.backpressureWithNoInitialValue.evaluate();
            this._Benchmark_benchmark_9.payloads.lastWithElements.evaluate();
            this._Benchmark_benchmark_9.payloads.lastMultiSubscribe.evaluate();
            this._Benchmark_benchmark_11.payloads.issue1527.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtWithIndexOutOfBounds.evaluate();
            this._Benchmark_benchmark_0.payloads.reduceMaybeCheckDisposed.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtWithIndexOutOfBoundsObservable.evaluate();
            this._Benchmark_benchmark_15.payloads.withNonEmptyObservable.evaluate();
        }

   }

}