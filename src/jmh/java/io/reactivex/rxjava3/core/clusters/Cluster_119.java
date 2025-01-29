package io.reactivex.rxjava3.core.clusters;

public class Cluster_119 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToFutureTest._Benchmark _Benchmark_benchmark_13;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableElementAtTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithMaybeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArrayTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriberTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToFutureTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_119() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.as.evaluate();
            this._Benchmark_benchmark_0.payloads.to.evaluate();
            this._Benchmark_benchmark_0.payloads.concatArrayOne.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeArrayOne.evaluate();
            this._Benchmark_benchmark_4.payloads.emptyToFlowable.evaluate();
            this._Benchmark_benchmark_5.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.simpleInequalObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.asyncSourceCompare.evaluate();
            this._Benchmark_benchmark_8.payloads.doubleOnSubscribeMain.evaluate();
            this._Benchmark_benchmark_9.payloads.normal.evaluate();
            this._Benchmark_benchmark_8.payloads.mainError.evaluate();
            this._Benchmark_benchmark_9.payloads.fusedPollMixed.evaluate();
            this._Benchmark_benchmark_12.payloads.completeAfterNext.evaluate();
            this._Benchmark_benchmark_13.payloads.backpressure.evaluate();
        }

   }

}