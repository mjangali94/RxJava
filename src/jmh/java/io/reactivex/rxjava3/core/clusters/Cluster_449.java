package io.reactivex.rxjava3.core.clusters;

public class Cluster_449 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableMostRecentTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithSizeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutTests._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableMostRecentTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservablePublishTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutWithSelectorTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimeoutTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_449() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.onSuccessAfterDispose.evaluate();
            this._Benchmark_benchmark_1.payloads.untilMaybeDispose.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelComposes.evaluate();
            this._Benchmark_benchmark_3.payloads.eagerDisposedOnComplete.evaluate();
            this._Benchmark_benchmark_4.payloads.singleSourceManyIterators.evaluate();
            this._Benchmark_benchmark_5.payloads.mainError.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelAfterAbandonmentSize.evaluate();
            this._Benchmark_benchmark_7.payloads.eagerDisposeResourceThenDisposeUpstream.evaluate();
            this._Benchmark_benchmark_7.payloads.nonEagerDisposeUpstreamThenDisposeResource.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_11.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelAfterAbandonmentSkip.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelAfterAbandonmentOverlap.evaluate();
        }

   }

}