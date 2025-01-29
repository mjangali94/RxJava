package io.reactivex.rxjava3.core.clusters;

public class Cluster_633 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_633() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.combineLatestIterableOneIsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.generateConsumerEmitsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.fromFutureTimedReturnsNull.evaluate();
            this._Benchmark_benchmark_0.payloads.usingFlowableSupplierReturnsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.badRequest.evaluate();
        }

   }

}