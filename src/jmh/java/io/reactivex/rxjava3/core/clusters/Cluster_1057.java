package io.reactivex.rxjava3.core.clusters;

public class Cluster_1057 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1057() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.otherError.evaluate();
            this._Benchmark_benchmark_1.payloads.error.evaluate();
            this._Benchmark_benchmark_2.payloads.noSubsequentSubscription.evaluate();
            this._Benchmark_benchmark_3.payloads.noSubsequentSubscription.evaluate();
            this._Benchmark_benchmark_4.payloads.concatMapReturnsNull.evaluate();
            this._Benchmark_benchmark_5.payloads.nonFatalExceptionThrownByCombinatorForSingleSourceIsNotReportedByUpstreamOperator.evaluate();
            this._Benchmark_benchmark_3.payloads.requestBeforeComplete.evaluate();
        }

   }

}