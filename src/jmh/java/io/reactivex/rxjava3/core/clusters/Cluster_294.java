package io.reactivex.rxjava3.core.clusters;

public class Cluster_294 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnRequestTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromActionTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromRunnableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.flowable.FlowableZipTests._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSubscribeOnTest._Benchmark _Benchmark_benchmark_15;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableBlockingTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnRequestTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromActionTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromRunnableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.flowable.FlowableZipTests._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.jdk8.FlowableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableAmbTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.jdk8.FlowableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSubscribeOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_294() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.shouldDisposeInnerObservable);
            this._Benchmark_benchmark_1.payloads.disposeUpFront.evaluate();
            this._Benchmark_benchmark_2.payloads.unsubscribeHappensAgainstParent.evaluate();
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.repeatUntilCancel);
            this._Benchmark_benchmark_4.payloads.cancelWhileRunning.evaluate();
            this._Benchmark_benchmark_5.payloads.cancelWhileRunning.evaluate();
            this._Benchmark_benchmark_6.payloads.zipWithDelayError.evaluate();
            this._Benchmark_benchmark_7.payloads.shouldDisposeInnerFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.zipWithDelayErrorBufferSize.evaluate();
            this._Benchmark_benchmark_9.payloads.withEmpty.evaluate();
            this._Benchmark_benchmark_10.payloads.firstCancels.evaluate();
            this._Benchmark_benchmark_11.payloads.firstCancels.evaluate();
            this._Benchmark_benchmark_12.payloads.ambArrayOrder.evaluate();
            this._Benchmark_benchmark_13.payloads.justHidden.evaluate();
            this._Benchmark_benchmark_12.payloads.ambWithOrder.evaluate();
            this._Benchmark_benchmark_15.payloads.cancelBeforeActualSubscribe.evaluate();
        }

   }

}