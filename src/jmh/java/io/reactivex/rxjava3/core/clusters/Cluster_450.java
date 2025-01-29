package io.reactivex.rxjava3.core.clusters;

public class Cluster_450 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_450() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapScalarBackpressured);
            this._Benchmark_benchmark_1.payloads.asyncFusedSource.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.callableCrash);
            this._Benchmark_benchmark_1.payloads.scalarCallableSource.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.repeatUntilFalse);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatIterableDelayError);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapInnerError);
        }

   }

}