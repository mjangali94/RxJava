package io.reactivex.rxjava3.core.clusters;

public class Cluster_520 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromMaybeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableObserveOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_520() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerError.evaluate();
            this._Benchmark_benchmark_1.payloads.emptyIterable.evaluate();
            this._Benchmark_benchmark_2.payloads.concatReportsDisposedOnErrorDelayError.evaluate();
            this._Benchmark_benchmark_3.payloads.mainError.evaluate();
            this._Benchmark_benchmark_4.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.concatReportsDisposedOnError.evaluate();
            this._Benchmark_benchmark_6.payloads.error.evaluate();
            this._Benchmark_benchmark_7.payloads.unsubscribesFromUpstream.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.payloads.cancelComposes.evaluate();
            this._Benchmark_benchmark_2.payloads.concatOuterBackpressure.evaluate();
            this._Benchmark_benchmark_11.payloads.outputFusedOneSignal.evaluate();
        }

   }

}