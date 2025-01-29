package io.reactivex.rxjava3.core.clusters;

public class Cluster_357 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.jdk8.ObservableBlockingStreamTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.jdk8.ObservableBlockingStreamTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.FlowableBlockingStreamTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_357() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.close.evaluate();
            this._Benchmark_benchmark_1.payloads.close.evaluate();
            this._Benchmark_benchmark_2.payloads.callableCrashDelayError.evaluate();
            this._Benchmark_benchmark_2.payloads.scalarInnerJustDispose.evaluate();
            this._Benchmark_benchmark_4.payloads.drainMaxLoopDontAccept.evaluate();
            this._Benchmark_benchmark_2.payloads.scalarInnerJustDisposeDelayError.evaluate();
            this._Benchmark_benchmark_2.payloads.concatMapScalarBackpressuredDelayError.evaluate();
            this._Benchmark_benchmark_7.payloads.delayErrorBuffer.evaluate();
            this._Benchmark_benchmark_2.payloads.concatMapInnerErrorDelayError.evaluate();
        }

   }

}