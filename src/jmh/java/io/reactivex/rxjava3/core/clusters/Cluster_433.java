package io.reactivex.rxjava3.core.clusters;

public class Cluster_433 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilPredicateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureDropTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeUntilPredicateTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeWhileTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureDropTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_433() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_1.payloads.takeFromScalarQueue.evaluate();
            this._Benchmark_benchmark_2.payloads.backpressure.evaluate();
            this._Benchmark_benchmark_3.payloads.requestOverflow.evaluate();
            this._Benchmark_benchmark_4.payloads.range.evaluate();
            this._Benchmark_benchmark_5.payloads.requestOverflowDoesNotOccur.evaluate();
            this._Benchmark_benchmark_4.payloads.rangeBackpressured.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarReentrant2.evaluate();
            this._Benchmark_benchmark_1.payloads.scalarReentrant.evaluate();
            this._Benchmark_benchmark_9.payloads.deferredUpstreamProducer.evaluate();
            this._Benchmark_benchmark_10.payloads.requestZero.evaluate();
            this._Benchmark_benchmark_10.payloads.requestNegative.evaluate();
        }

   }

}