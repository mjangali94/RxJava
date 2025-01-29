package io.reactivex.rxjava3.core.clusters;

public class Cluster_1056 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryWithPredicateTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark _Benchmark_benchmark_13;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryWithPredicateTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithMaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1056() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.singleRequestsExactlyWhatItNeedsIf1RequestedFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable.evaluate();
            this._Benchmark_benchmark_3.payloads.elementAtIndex1OnEmptySource.evaluate();
            this._Benchmark_benchmark_3.payloads.elementAtIndex0OnEmptySource.evaluate();
            this._Benchmark_benchmark_5.payloads.eventOrdering2.evaluate();
            this._Benchmark_benchmark_7.payloads.justAndRetry.evaluate();
            this._Benchmark_benchmark_8.payloads.blockingForEachThrows.evaluate();
            this._Benchmark_benchmark_9.payloads.toMapValueSelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_9.payloads.toMultiMapValueSelectorReturnsNullAllowed.evaluate();
            this._Benchmark_benchmark_11.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_12.runBenchmark(this._Benchmark_benchmark_12.payloads.concatOuterBackpressure);
            this._Benchmark_benchmark_13.payloads.normalEmpty.evaluate();
        }

   }

}