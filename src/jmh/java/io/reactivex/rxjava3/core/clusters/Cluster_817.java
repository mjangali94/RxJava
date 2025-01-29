package io.reactivex.rxjava3.core.clusters;

public class Cluster_817 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.subscriptions.AsyncSubscriptionTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.subscriptions.AsyncSubscriptionTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_817() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.emptyInner.evaluate();
            this._Benchmark_benchmark_1.payloads.onCompleteThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeOnArrival.evaluate();
            this._Benchmark_benchmark_3.payloads.eagerDisposedOnError.evaluate();
            this._Benchmark_benchmark_4.payloads.disposed.evaluate();
        }

   }

}