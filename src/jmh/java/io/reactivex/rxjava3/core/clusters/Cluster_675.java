package io.reactivex.rxjava3.core.clusters;

public class Cluster_675 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDelaySubscriptionOtherTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_675() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.scanSeedFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_1.payloads.noPrematureSubscription.evaluate();
            this._Benchmark_benchmark_1.payloads.noMultipleSubscriptions.evaluate();
            this._Benchmark_benchmark_3.payloads.subscribeAfterDisconnectThenConnect.evaluate();
            this._Benchmark_benchmark_3.payloads.noSubscriberRetentionOnCompleted.evaluate();
            this._Benchmark_benchmark_0.payloads.distinctFunctionReturnsNull.evaluate();
            this._Benchmark_benchmark_6.payloads.takeLastWithNegativeCount.evaluate();
            this._Benchmark_benchmark_7.payloads.elementAtWithMinusIndex.evaluate();
            this._Benchmark_benchmark_7.payloads.elementAtOrDefaultWithMinusIndex.evaluate();
        }

   }

}