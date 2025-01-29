package io.reactivex.rxjava3.core.clusters;

public class Cluster_802 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_802() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.drainMaxLoopMissingBackpressureWithResource.evaluate();
            this._Benchmark_benchmark_1.payloads.scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.sourceOverflows.evaluate();
        }

   }

}