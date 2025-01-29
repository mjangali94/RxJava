package io.reactivex.rxjava3.core.clusters;

public class Cluster_99 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMaterializeTest._Benchmark _Benchmark_benchmark_16;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark _Benchmark_benchmark_17;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableObservableTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.jdk8.SingleFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIgnoreElementsTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMaterializeTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_99() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.normal.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedNoSync.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_0.payloads.fused.evaluate();
            this._Benchmark_benchmark_0.payloads.nextCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCrash2.evaluate();
            this._Benchmark_benchmark_6.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.payloads.fusedEmptyCheck.evaluate();
            this._Benchmark_benchmark_0.payloads.iteratorCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.hasNextCrash.evaluate();
            this._Benchmark_benchmark_10.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_11.payloads.isDisposed.evaluate();
            this._Benchmark_benchmark_12.payloads.issue1522.evaluate();
            this._Benchmark_benchmark_13.payloads.withEmptyObservable.evaluate();
            this._Benchmark_benchmark_10.payloads.dispose.evaluate();
            this._Benchmark_benchmark_15.payloads.covarianceOfFrom.evaluate();
            this._Benchmark_benchmark_16.payloads.dispose.evaluate();
            this._Benchmark_benchmark_17.payloads.success.evaluate();
        }

   }

}