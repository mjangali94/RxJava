package io.reactivex.rxjava3.core.clusters;

public class Cluster_182 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutTests._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutTests._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_182() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mainCompletes.evaluate();
            this._Benchmark_benchmark_1.payloads.otherError.evaluate();
            this._Benchmark_benchmark_2.payloads.ambCancelsOthers.evaluate();
            this._Benchmark_benchmark_3.payloads.untilPublisherOtherOnComplete.evaluate();
            this._Benchmark_benchmark_0.payloads.untilPublisherMainSuccess.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.mainErrors.evaluate();
            this._Benchmark_benchmark_3.payloads.otherErrors.evaluate();
            this._Benchmark_benchmark_8.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose.evaluate();
            this._Benchmark_benchmark_9.payloads.mainError.evaluate();
        }

   }

}