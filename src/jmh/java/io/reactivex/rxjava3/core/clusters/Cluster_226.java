package io.reactivex.rxjava3.core.clusters;

public class Cluster_226 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScalarXMapTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScalarXMapTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_226() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.emptyConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_2.payloads.overlapEmpty.evaluate();
            this._Benchmark_benchmark_0.payloads.closeCalledOnEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.fusionRejected.evaluate();
            this._Benchmark_benchmark_4.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_6.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_7.payloads.scalarMapToCrashingCallable.evaluate();
            this._Benchmark_benchmark_8.payloads.collectionSupplierThrows.evaluate();
        }

   }

}