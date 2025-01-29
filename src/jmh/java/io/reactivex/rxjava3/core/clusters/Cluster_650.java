package io.reactivex.rxjava3.core.clusters;

public class Cluster_650 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromActionTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromRunnableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromActionTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromRunnableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_650() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerSubscriberError.evaluate();
            this._Benchmark_benchmark_1.payloads.getIteratorThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerMaybeObserverNoError.evaluate();
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerObserverNoError.evaluate();
            this._Benchmark_benchmark_4.payloads.fromActionThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.fromRunnableThrows.evaluate();
            this._Benchmark_benchmark_6.payloads.stateSupplierThrows.evaluate();
            this._Benchmark_benchmark_0.payloads.tryTerminateConsumerEmitterNoError.evaluate();
        }

   }

}