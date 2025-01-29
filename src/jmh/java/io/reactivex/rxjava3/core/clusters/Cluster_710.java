package io.reactivex.rxjava3.core.clusters;

public class Cluster_710 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMaterializeTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromMaybeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMaterializeTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_710() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.asyncFusedUnboundedIn.evaluate();
            this._Benchmark_benchmark_1.payloads.badSource.evaluate();
            this._Benchmark_benchmark_2.payloads.backpressureViaRequest.evaluate();
            this._Benchmark_benchmark_3.payloads.errorReceived.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
            this._Benchmark_benchmark_5.payloads.fromMaybeError.evaluate();
            this._Benchmark_benchmark_6.payloads.concatEagerIterableTest.evaluate();
            this._Benchmark_benchmark_7.payloads.fromCompletableError.evaluate();
        }

   }

}