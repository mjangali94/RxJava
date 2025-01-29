package io.reactivex.rxjava3.core.clusters;

public class Cluster_822 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipWhileTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCacheTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipWhileTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableThrottleLatestTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_822() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.crossCancelOnSuccess.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.fallbackError.evaluate();
            this._Benchmark_benchmark_4.payloads.disposed.evaluate();
            this._Benchmark_benchmark_5.payloads.mapperReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_7.payloads.otherCompletes.evaluate();
            this._Benchmark_benchmark_8.payloads.normal.evaluate();
        }

   }

}