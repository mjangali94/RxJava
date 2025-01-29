package io.reactivex.rxjava3.core.clusters;

public class Cluster_266 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSwitchIfEmptyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_266() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapDelayError);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatObservableDelayError);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapEmpty);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.concatMapJustSource);
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.mapperThrows);
            this._Benchmark_benchmark_5.payloads.delayErrorCallableEager.evaluate();
            this._Benchmark_benchmark_5.payloads.pollThrows.evaluate();
            this._Benchmark_benchmark_7.payloads.concatDelayErrorIterable.evaluate();
            this._Benchmark_benchmark_8.payloads.backpressureNoRequest.evaluate();
        }

   }

}