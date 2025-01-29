package io.reactivex.rxjava3.core.clusters;

public class Cluster_844 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFromCompletionStageTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSwitchTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromCallableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.ObservableFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithMaybeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_844() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.disposedOnCall.evaluate();
            this._Benchmark_benchmark_2.payloads.disposedOnCall.evaluate();
            this._Benchmark_benchmark_3.payloads.normalMaybe.evaluate();
            this._Benchmark_benchmark_4.payloads.cancel.evaluate();
            this._Benchmark_benchmark_5.payloads.interrupt.evaluate();
            this._Benchmark_benchmark_6.payloads.otherError.evaluate();
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
        }

   }

}