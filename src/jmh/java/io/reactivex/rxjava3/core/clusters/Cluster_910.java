package io.reactivex.rxjava3.core.clusters;

public class Cluster_910 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatEagerTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeOfTypeTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeHideTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeIsEmptyTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatEagerTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeOfTypeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_910() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelComposes.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_0.payloads.error.evaluate();
            this._Benchmark_benchmark_3.payloads.disposeToMaybe.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.justMaybeError);
            this._Benchmark_benchmark_5.payloads.untilTrueError.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.iterableNormal);
            this._Benchmark_benchmark_7.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.runBenchmark(this._Benchmark_benchmark_6.payloads.iterableNormalMaxConcurrency);
            this._Benchmark_benchmark_9.payloads.crossCancelOnComplete.evaluate();
            this._Benchmark_benchmark_5.payloads.retryTimesPredicateWithZeroRetries.evaluate();
        }

   }

}