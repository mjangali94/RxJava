package io.reactivex.rxjava3.core.clusters;

public class Cluster_289 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleAmbTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableSequenceEqualTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleAmbTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleEqualsTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableSequenceEqualTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_289() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.manySources.evaluate();
            this._Benchmark_benchmark_1.payloads.bothSucceedNotEqual.evaluate();
            this._Benchmark_benchmark_1.payloads.bothSucceedEqual.evaluate();
            this._Benchmark_benchmark_0.payloads.ambWithOrder.evaluate();
            this._Benchmark_benchmark_0.payloads.ambIterableOrder.evaluate();
            this._Benchmark_benchmark_5.payloads.equals.evaluate();
            this._Benchmark_benchmark_0.payloads.error.evaluate();
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.firstFails);
        }

   }

}