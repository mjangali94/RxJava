package io.reactivex.rxjava3.core.clusters;

public class Cluster_69 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTerminateTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.single.SingleContainstTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleZipArrayTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTerminateTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleContainstTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.single.SingleZipIterableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_69() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.factoryFailureResultsInErrorEmission.evaluate();
            this._Benchmark_benchmark_0.payloads.factoryFailureResultsInErrorEmissionFlowable.evaluate();
            this._Benchmark_benchmark_2.payloads.doOnSubscribeNormal.evaluate();
            this._Benchmark_benchmark_3.payloads.compose.evaluate();
            this._Benchmark_benchmark_4.payloads.oneArray.evaluate();
            this._Benchmark_benchmark_3.payloads.never.evaluate();
            this._Benchmark_benchmark_6.payloads.doOnTerminateSuccessCrash.evaluate();
            this._Benchmark_benchmark_7.payloads.comparerThrows.evaluate();
            this._Benchmark_benchmark_8.payloads.singleCollectionSupplierThrows.evaluate();
            this._Benchmark_benchmark_9.payloads.oneIterable.evaluate();
        }

   }

}