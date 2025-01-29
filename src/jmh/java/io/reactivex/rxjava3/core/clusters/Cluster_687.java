package io.reactivex.rxjava3.core.clusters;

public class Cluster_687 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnLifecycleTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnLifecycleTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilPublisherTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_687() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.firstIgnoredCancelAndOnError.evaluate();
            this._Benchmark_benchmark_1.payloads.onSubscribeCrashed.evaluate();
            this._Benchmark_benchmark_2.payloads.takeFinalValueThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.normalJustConditional.evaluate();
            this._Benchmark_benchmark_4.runBenchmark(this._Benchmark_benchmark_4.payloads.repeatUntil);
            this._Benchmark_benchmark_2.payloads.producerRequestThroughTakeIsModified.evaluate();
            this._Benchmark_benchmark_2.payloads.producerRequestThroughTake.evaluate();
            this._Benchmark_benchmark_7.payloads.otherSignalsAndCompletes.evaluate();
        }

   }

}