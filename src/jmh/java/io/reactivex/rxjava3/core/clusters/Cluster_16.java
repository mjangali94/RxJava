package io.reactivex.rxjava3.core.clusters;

public class Cluster_16 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.flowable.FlowableDoOnTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithSingleTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithCompletableTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark _Benchmark_benchmark_10;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.flowable.FlowableDoOnTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeWithSingleTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMergeWithCompletableTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.jdk8.FlowableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_16() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.withEmpty.evaluate();
            this._Benchmark_benchmark_1.payloads.toFlowableError1.evaluate();
            this._Benchmark_benchmark_2.payloads.nonFused.evaluate();
            this._Benchmark_benchmark_3.payloads.otherError.evaluate();
            this._Benchmark_benchmark_4.payloads.doOnError.evaluate();
            this._Benchmark_benchmark_5.payloads.rangeToEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.take.evaluate();
            this._Benchmark_benchmark_7.payloads.normal.evaluate();
            this._Benchmark_benchmark_8.payloads.collectorFinisherCrashToFlowable.evaluate();
            this._Benchmark_benchmark_6.payloads.normal.evaluate();
            this._Benchmark_benchmark_10.payloads.moreValuesRemainingThanRequested.evaluate();
        }

   }

}