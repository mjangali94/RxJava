package io.reactivex.rxjava3.core.clusters;

public class Cluster_279 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromStreamTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_279() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.onErrorLate.evaluate();
            this._Benchmark_benchmark_1.payloads.pollThrowsNoSubscribers.evaluate();
            this._Benchmark_benchmark_2.payloads.manyBackpressuredConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.takeConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.closeCalledOnCancelConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.manyBackpressured.evaluate();
            this._Benchmark_benchmark_6.payloads.fusedClearIsEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.conditionalSlowPathTakeExact.evaluate();
            this._Benchmark_benchmark_6.payloads.slowPathTakeExact.evaluate();
            this._Benchmark_benchmark_9.payloads.multipleOnNext.evaluate();
        }

   }

}