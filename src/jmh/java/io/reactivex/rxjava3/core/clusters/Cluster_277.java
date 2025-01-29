package io.reactivex.rxjava3.core.clusters;

public class Cluster_277 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_277() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.error.evaluate();
            this._Benchmark_benchmark_0.payloads.oneByOne.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedCurrentIteratorEmpty.evaluate();
            this._Benchmark_benchmark_3.payloads.disposeWhileIteratorNextConditional.evaluate();
            this._Benchmark_benchmark_4.payloads.takeWithError.evaluate();
            this._Benchmark_benchmark_3.payloads.hasNext2Throws.evaluate();
            this._Benchmark_benchmark_3.payloads.hasNextCancelsAndCompletesFastPathConditional.evaluate();
        }

   }

}