package io.reactivex.rxjava3.core.clusters;

public class Cluster_389 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableFlowableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableFlowableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsFlowableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_389() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.iteratorCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.successMany.evaluate();
            this._Benchmark_benchmark_1.payloads.successJust.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_1.payloads.manyBackpressured.evaluate();
            this._Benchmark_benchmark_1.payloads.requestOneByOne.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedJust.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedMany.evaluate();
            this._Benchmark_benchmark_1.payloads.fusedManyRejected.evaluate();
        }

   }

}