package io.reactivex.rxjava3.core.clusters;

public class Cluster_499 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_499() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.combineToNull2.evaluate();
            this._Benchmark_benchmark_1.payloads.normalOnError.evaluate();
            this._Benchmark_benchmark_0.payloads.manyCombinerThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.timeoutSelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.producerRequestThroughAmb.evaluate();
            this._Benchmark_benchmark_5.payloads.noPrematureSubscription.evaluate();
            this._Benchmark_benchmark_5.payloads.noMultipleSubscriptions.evaluate();
        }

   }

}