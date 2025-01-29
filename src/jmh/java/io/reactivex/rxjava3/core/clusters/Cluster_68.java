package io.reactivex.rxjava3.core.clusters;

public class Cluster_68 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleMapTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTerminateTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.single.SingleDetachTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.observers.FutureSingleObserverTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoAfterSuccessTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.single.SingleDoAfterTerminateTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleBlockingSubscribeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleMapTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTerminateTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.single.SingleDetachTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.observers.FutureSingleObserverTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.single.SingleDoAfterSuccessTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.single.SingleDoAfterTerminateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_68() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.observerSuccess);
            this._Benchmark_benchmark_1.payloads.mapValue.evaluate();
            this._Benchmark_benchmark_2.payloads.doOnTerminateSuccess.evaluate();
            this._Benchmark_benchmark_3.payloads.collectorSupplierCrash.evaluate();
            this._Benchmark_benchmark_4.payloads.onSuccess.evaluate();
            this._Benchmark_benchmark_5.payloads.contains.evaluate();
            this._Benchmark_benchmark_6.payloads.cancel.evaluate();
            this._Benchmark_benchmark_7.payloads.rangeWithOverflow3.evaluate();
            this._Benchmark_benchmark_8.payloads.onErrorSuccess.evaluate();
            this._Benchmark_benchmark_9.payloads.collectorSupplierCrash.evaluate();
            this._Benchmark_benchmark_10.payloads.just.evaluate();
            this._Benchmark_benchmark_7.payloads.noOverflow.evaluate();
            this._Benchmark_benchmark_12.payloads.just.evaluate();
        }

   }

}