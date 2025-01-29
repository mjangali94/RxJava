package io.reactivex.rxjava3.core.clusters;

public class Cluster_698 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatIterableTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeoutWithSelectorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_698() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.requestOneByOne.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedJust.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedMany.evaluate();
            this._Benchmark_benchmark_3.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedManyRejected.evaluate();
            this._Benchmark_benchmark_5.payloads.justWithOnComplete.evaluate();
            this._Benchmark_benchmark_6.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_6.payloads.hasNextThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.backpressureDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.successManyTake.evaluate();
            this._Benchmark_benchmark_10.payloads.take.evaluate();
            this._Benchmark_benchmark_3.payloads.cancel.evaluate();
            this._Benchmark_benchmark_6.payloads.take2.evaluate();
            this._Benchmark_benchmark_10.payloads.error.evaluate();
            this._Benchmark_benchmark_14.payloads.empty.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
        }

   }

}