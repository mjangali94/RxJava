package io.reactivex.rxjava3.core.clusters;

public class Cluster_460 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnTerminateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.MaybeMapOptionalTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoFinallyTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFromCompletionStageTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDematerializeTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeEqualTest._Benchmark _Benchmark_benchmark_18;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoOnTerminateTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.MaybeMapOptionalTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchIfEmptyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapBiSelectorTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoFinallyTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDoAfterSuccessTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.MaybeFromCompletionStageTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeZipArrayTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDematerializeTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_18 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeEqualTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_18.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_460() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doOnTerminateSuccess.evaluate();
            this._Benchmark_benchmark_1.payloads.normal.evaluate();
            this._Benchmark_benchmark_2.payloads.offlineComplete.evaluate();
            this._Benchmark_benchmark_3.payloads.successSuccess.evaluate();
            this._Benchmark_benchmark_4.payloads.nonEmpty.evaluate();
            this._Benchmark_benchmark_5.runBenchmark(this._Benchmark_benchmark_5.payloads.observerSuccess);
            this._Benchmark_benchmark_6.payloads.mapperCancels.evaluate();
            this._Benchmark_benchmark_7.payloads.normalJust.evaluate();
            this._Benchmark_benchmark_8.payloads.just.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose2.evaluate();
            this._Benchmark_benchmark_10.payloads.mapperThrows.evaluate();
            this._Benchmark_benchmark_11.payloads.mapperThrows.evaluate();
            this._Benchmark_benchmark_12.payloads.oneSourceOnly.evaluate();
            this._Benchmark_benchmark_13.payloads.empty.evaluate();
            this._Benchmark_benchmark_13.payloads.selectorDifferentType.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnTerminateSuccessCrash.evaluate();
            this._Benchmark_benchmark_4.payloads.empty.evaluate();
            this._Benchmark_benchmark_17.payloads.untilFirstMaybe.evaluate();
            this._Benchmark_benchmark_18.payloads.predicateThrows.evaluate();
        }

   }

}