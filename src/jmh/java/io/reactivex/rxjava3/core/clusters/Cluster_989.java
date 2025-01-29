package io.reactivex.rxjava3.core.clusters;

public class Cluster_989 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFromOptionalTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlattenTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDetachTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.MaybeFromOptionalTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlattenTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDetachTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_989() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_1.payloads.empty.evaluate();
            this._Benchmark_benchmark_2.payloads.noSubsequentSubscriptionDelayError.evaluate();
            this._Benchmark_benchmark_3.payloads.mainEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.mainEmpty.evaluate();
            this._Benchmark_benchmark_5.payloads.onComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_6.payloads.undeliverableUponCancel.evaluate();
            this._Benchmark_benchmark_2.payloads.noSubsequentSubscription.evaluate();
            this._Benchmark_benchmark_9.payloads.empty.evaluate();
            this._Benchmark_benchmark_2.payloads.concatArrayDelayErrorWithError.evaluate();
        }

   }

}