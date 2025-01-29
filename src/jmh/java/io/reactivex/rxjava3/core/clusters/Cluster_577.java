package io.reactivex.rxjava3.core.clusters;

public class Cluster_577 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithStartEndObservableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithStartEndObservableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_577() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.bothError.evaluate();
            this._Benchmark_benchmark_1.payloads.unsubscribeAll.evaluate();
            this._Benchmark_benchmark_2.payloads.ambArraySingleElement.evaluate();
            this._Benchmark_benchmark_3.payloads.callable.evaluate();
            this._Benchmark_benchmark_4.payloads.lastWindowIsFixedInTime.evaluate();
            this._Benchmark_benchmark_5.payloads.switchMapErrorEmptySource.evaluate();
            this._Benchmark_benchmark_5.payloads.switchMapDelayErrorEmptySource.evaluate();
        }

   }

}