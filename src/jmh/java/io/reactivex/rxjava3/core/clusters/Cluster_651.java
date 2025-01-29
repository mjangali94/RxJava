package io.reactivex.rxjava3.core.clusters;

public class Cluster_651 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupByTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableCombineLatestTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_651() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.hasNextCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.closeCalledOnItemCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.mapFactoryThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerEmitterError.evaluate();
            this._Benchmark_benchmark_4.payloads.iterableNullPublisher.evaluate();
            this._Benchmark_benchmark_5.payloads.unorderedFastPathReject.evaluate();
        }

   }

}