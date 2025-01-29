package io.reactivex.rxjava3.core.clusters;

public class Cluster_293 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureDropTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSupplierTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromCallableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureDropTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastOneTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_293() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.toFutureWithException);
            this._Benchmark_benchmark_1.payloads.onNextThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.fusedFlatMapExecution.evaluate();
            this._Benchmark_benchmark_3.payloads.fusedFlatMapExecution.evaluate();
            this._Benchmark_benchmark_4.payloads.scanNoSeed.evaluate();
            this._Benchmark_benchmark_5.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_6.payloads.just.evaluate();
            this._Benchmark_benchmark_7.payloads.lastWithBackpressure.evaluate();
        }

   }

}