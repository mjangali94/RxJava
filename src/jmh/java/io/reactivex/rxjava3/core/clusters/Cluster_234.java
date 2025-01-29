package io.reactivex.rxjava3.core.clusters;

public class Cluster_234 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelaySubscriptionTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableOnErrorXTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_234() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.withPublisherCallAfterTerminalEvent.evaluate();
            this._Benchmark_benchmark_1.payloads.onErrorReturnConst.evaluate();
            this._Benchmark_benchmark_2.payloads.valueConcatWithValue.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.concat2.evaluate();
            this._Benchmark_benchmark_2.payloads.concatIterable.evaluate();
            this._Benchmark_benchmark_2.payloads.concat3.evaluate();
            this._Benchmark_benchmark_2.payloads.concatIterableOne.evaluate();
            this._Benchmark_benchmark_2.payloads.concat4.evaluate();
            this._Benchmark_benchmark_2.payloads.emptyConcatWithValue.evaluate();
        }

   }

}