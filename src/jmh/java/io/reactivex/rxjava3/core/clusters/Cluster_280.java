package io.reactivex.rxjava3.core.clusters;

public class Cluster_280 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSortedListTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_280() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusionRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.justHidden.evaluate();
            this._Benchmark_benchmark_0.payloads.mixedInnerSource.evaluate();
            this._Benchmark_benchmark_0.payloads.mixedInnerSource2.evaluate();
            this._Benchmark_benchmark_0.payloads.normal0.evaluate();
            this._Benchmark_benchmark_5.payloads.sortedComparator.evaluate();
            this._Benchmark_benchmark_5.payloads.sorted.evaluate();
            this._Benchmark_benchmark_7.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorHasNextThrowsSecondCall.evaluate();
            this._Benchmark_benchmark_10.payloads.syncIterableHidden.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorNextThrows.evaluate();
            this._Benchmark_benchmark_12.payloads.listWithBlockingFirstFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.smallPrefetch2.evaluate();
        }

   }

}