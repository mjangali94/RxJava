package io.reactivex.rxjava3.core.clusters;

public class Cluster_486 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest2._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableToListTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithSingleTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureLatestTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest2._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategyTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_486() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.takeLastZeroProcessesAllItemsButIgnoresThem.evaluate();
            this._Benchmark_benchmark_1.payloads.upstreamIsProcessedButIgnoredObservable.evaluate();
            this._Benchmark_benchmark_2.payloads.toFlowableError2.evaluate();
            this._Benchmark_benchmark_2.payloads.toFlowableDrop.evaluate();
            this._Benchmark_benchmark_4.payloads.capacityHintFlowable.evaluate();
            this._Benchmark_benchmark_5.payloads.nearMaxValueWithoutBackpressure.evaluate();
            this._Benchmark_benchmark_6.payloads.collectorFinisherCrash.evaluate();
            this._Benchmark_benchmark_7.payloads.otherError.evaluate();
            this._Benchmark_benchmark_8.payloads.simpleBackpressure.evaluate();
            this._Benchmark_benchmark_9.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_10.payloads.withObservable.evaluate();
            this._Benchmark_benchmark_11.payloads.range.evaluate();
            this._Benchmark_benchmark_12.payloads.withEmptyFlowable.evaluate();
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.concatMapJustRange);
            this._Benchmark_benchmark_13.runBenchmark(this._Benchmark_benchmark_13.payloads.concatMapDelayErrorJustRange);
            this._Benchmark_benchmark_15.payloads.dispose.evaluate();
            this._Benchmark_benchmark_16.payloads.overflowNullAction.evaluate();
            this._Benchmark_benchmark_2.payloads.toFlowableMissing.evaluate();
        }

   }

}