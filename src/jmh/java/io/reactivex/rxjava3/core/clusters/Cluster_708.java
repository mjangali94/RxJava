package io.reactivex.rxjava3.core.clusters;

public class Cluster_708 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenPublisherTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenPublisherTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_708() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_1.payloads.noBackpressure.evaluate();
            this._Benchmark_benchmark_1.payloads.hasNextCancels.evaluate();
            this._Benchmark_benchmark_1.payloads.fusionRejected.evaluate();
            this._Benchmark_benchmark_4.payloads.fromIterableValueNull.evaluate();
            this._Benchmark_benchmark_5.payloads.syncIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.disposeWhileIteratorNext.evaluate();
            this._Benchmark_benchmark_1.payloads.hasNextThrowsSecondTimeFastpath.evaluate();
            this._Benchmark_benchmark_1.payloads.hasNextCancelsAndCompletesSlowPath.evaluate();
            this._Benchmark_benchmark_1.payloads.nextThrowsFastpath.evaluate();
            this._Benchmark_benchmark_1.payloads.backpressureViaRequest.evaluate();
        }

   }

}