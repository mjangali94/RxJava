package io.reactivex.rxjava3.core.clusters;

public class Cluster_1040 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDisposeOnTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDoOnTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAndThenCompletableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableUsingTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDisposeOnTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDoOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1040() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.andThenMaybeCompleteError.evaluate();
            this._Benchmark_benchmark_1.payloads.andThenSecondCancels.evaluate();
            this._Benchmark_benchmark_2.payloads.collectionSupplierThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.emptyNonEager.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
            this._Benchmark_benchmark_5.payloads.errorReceived.evaluate();
            this._Benchmark_benchmark_1.payloads.andThenCompletableCompleteError.evaluate();
            this._Benchmark_benchmark_7.payloads.successAcceptThrows.evaluate();
        }

   }

}