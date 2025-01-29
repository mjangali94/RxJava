package io.reactivex.rxjava3.core.clusters;

public class Cluster_992 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableScanTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableForEachTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark _Benchmark_benchmark_12;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableScanTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToXTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatWithCompletableTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableForEachTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatWithSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_992() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.seedFactory.evaluate();
            this._Benchmark_benchmark_1.payloads.capacityHint.evaluate();
            this._Benchmark_benchmark_2.payloads.upstreamIsProcessedButIgnored.evaluate();
            this._Benchmark_benchmark_3.payloads.disposed.evaluate();
            this._Benchmark_benchmark_4.payloads.basic.evaluate();
            this._Benchmark_benchmark_5.payloads.toFlowableLatest.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.payloads.capacityHintObservable.evaluate();
            this._Benchmark_benchmark_8.payloads.normal.evaluate();
            this._Benchmark_benchmark_9.payloads.reduceWithSingle.evaluate();
            this._Benchmark_benchmark_10.payloads.requestOverflowDoesNotOccur.evaluate();
            this._Benchmark_benchmark_11.payloads.forEachWile.evaluate();
            this._Benchmark_benchmark_12.payloads.otherError.evaluate();
        }

   }

}