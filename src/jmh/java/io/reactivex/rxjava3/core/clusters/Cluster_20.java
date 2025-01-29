package io.reactivex.rxjava3.core.clusters;

public class Cluster_20 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_20() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.blockingLastNormal.evaluate();
            this._Benchmark_benchmark_0.payloads.blockingFirstNormal.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToStringObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.collectToListObservable.evaluate();
            this._Benchmark_benchmark_4.payloads.followingFirstObservable.evaluate();
            this._Benchmark_benchmark_5.payloads.exceptionWithMoreThanOneElement.evaluate();
            this._Benchmark_benchmark_5.payloads.toFuture.evaluate();
            this._Benchmark_benchmark_7.payloads.withFollowingFirstObservable.evaluate();
            this._Benchmark_benchmark_0.payloads.blockingLastEmpty.evaluate();
            this._Benchmark_benchmark_9.payloads.fromArrayOneIsNull.evaluate();
            this._Benchmark_benchmark_10.payloads.stackOverflowErrorIsThrown.evaluate();
        }

   }

}