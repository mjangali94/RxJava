package io.reactivex.rxjava3.core.clusters;

public class Cluster_644 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRetryTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_644() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.singleOrError.evaluate();
            this._Benchmark_benchmark_1.payloads.mapperCrashScalar.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarEmptySource.evaluate();
            this._Benchmark_benchmark_3.payloads.unsubscription.evaluate();
            this._Benchmark_benchmark_4.payloads.switchMapSingleFunctionDoesntReturnSingle.evaluate();
            this._Benchmark_benchmark_5.payloads.innerEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.retryPredicate.evaluate();
            this._Benchmark_benchmark_7.payloads.errorSingle.evaluate();
        }

   }

}