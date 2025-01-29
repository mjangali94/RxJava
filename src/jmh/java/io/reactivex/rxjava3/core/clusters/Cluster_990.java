package io.reactivex.rxjava3.core.clusters;

public class Cluster_990 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnTerminateTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoFinallyTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapNotificationTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark _Benchmark_benchmark_18;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_20;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnTerminateTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeContainsTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.single.SingleFromMaybeTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoFinallyTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapNotificationTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_990() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_1.payloads.doOnTerminateComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.empty.evaluate();
            this._Benchmark_benchmark_3.payloads.empty.evaluate();
            this._Benchmark_benchmark_4.payloads.emptyToMaybe.evaluate();
            this._Benchmark_benchmark_4.payloads.empty.evaluate();
            this._Benchmark_benchmark_6.payloads.emptyWithJust.evaluate();
            this._Benchmark_benchmark_7.payloads.emptyOtherToo.evaluate();
            this._Benchmark_benchmark_7.payloads.defaultIfEmptyEmpty.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyEager.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyNonEager.evaluate();
            this._Benchmark_benchmark_11.payloads.onErrorCompleteEmpty.evaluate();
            this._Benchmark_benchmark_11.payloads.onErrorReturnEmpty.evaluate();
            this._Benchmark_benchmark_13.payloads.emptyConditional.evaluate();
            this._Benchmark_benchmark_14.payloads.emptyDefault.evaluate();
            this._Benchmark_benchmark_15.payloads.normalEmptyConditional.evaluate();
            this._Benchmark_benchmark_9.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_17.payloads.onCompleteNull.evaluate();
            this._Benchmark_benchmark_18.payloads.flatMapSingleEmpty.evaluate();
            this._Benchmark_benchmark_14.payloads.empty.evaluate();
            this._Benchmark_benchmark_20.payloads.fused.evaluate();
        }

   }

}