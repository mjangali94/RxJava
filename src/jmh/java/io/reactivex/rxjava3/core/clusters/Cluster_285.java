package io.reactivex.rxjava3.core.clusters;

public class Cluster_285 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAllTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAnyTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFromPublisherTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableLastTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.single.SingleTakeUntilTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_285() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.empty.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.lastOrErrorNoElement.evaluate();
            this._Benchmark_benchmark_5.payloads.empty.evaluate();
            this._Benchmark_benchmark_0.payloads.backpressureIfOneRequestedOneShouldBeDelivered.evaluate();
            this._Benchmark_benchmark_7.payloads.withPublisherDispose.evaluate();
            this._Benchmark_benchmark_2.payloads.error.evaluate();
            this._Benchmark_benchmark_9.payloads.fusedEmpty.evaluate();
            this._Benchmark_benchmark_10.runBenchmark(this._Benchmark_benchmark_10.payloads.repeatUntilSupplierCrash);
        }

   }

}