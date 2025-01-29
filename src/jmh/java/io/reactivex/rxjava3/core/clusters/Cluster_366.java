package io.reactivex.rxjava3.core.clusters;

public class Cluster_366 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromPublisherTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnSubscribeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.flowable.FlowableStartWithTests._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromOptionalTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapSingleTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark _Benchmark_benchmark_15;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromPublisherTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.flowable.FlowableNullTests._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToCompletableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoOnSubscribeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.flowable.FlowableStartWithTests._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromOptionalTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapSingleTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.flowable.FlowableFuseableTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.subscribers.StrictSubscriberTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_366() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.justTwoEmissionsObservableThrowsError.evaluate();
            this._Benchmark_benchmark_1.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_2.payloads.fromPublisher.evaluate();
            this._Benchmark_benchmark_3.payloads.fromArrayOneIsNull.evaluate();
            this._Benchmark_benchmark_4.payloads.onSubscribeThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.justSingleItemObservable.evaluate();
            this._Benchmark_benchmark_6.payloads.doOnSubscribe.evaluate();
            this._Benchmark_benchmark_7.payloads.conditionalSlowPathSkipCancel.evaluate();
            this._Benchmark_benchmark_8.payloads.startWithEmpty.evaluate();
            this._Benchmark_benchmark_9.payloads.hasValue.evaluate();
            this._Benchmark_benchmark_10.runBenchmark(this._Benchmark_benchmark_10.payloads.requestOverflowDoesNotStallStream);
            this._Benchmark_benchmark_10.runBenchmark(this._Benchmark_benchmark_10.payloads.concatMapDelayErrorJustSource);
            this._Benchmark_benchmark_12.payloads.disposeOnNextAfterFirst.evaluate();
            this._Benchmark_benchmark_13.payloads.syncArray.evaluate();
            this._Benchmark_benchmark_12.payloads.disposeBeforeSwitchInOnNext.evaluate();
            this._Benchmark_benchmark_15.payloads.strictMode.evaluate();
        }

   }

}