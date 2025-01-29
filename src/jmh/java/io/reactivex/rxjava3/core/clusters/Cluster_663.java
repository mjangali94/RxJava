package io.reactivex.rxjava3.core.clusters;

public class Cluster_663 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchOnNextTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchOnNextTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFilterTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_663() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_1.payloads.normalDelayError.evaluate();
            this._Benchmark_benchmark_1.payloads.normal.evaluate();
            this._Benchmark_benchmark_3.payloads.conditionalNone2.evaluate();
            this._Benchmark_benchmark_3.payloads.doesntRequestOnItsOwn.evaluate();
            this._Benchmark_benchmark_5.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
        }

   }

}