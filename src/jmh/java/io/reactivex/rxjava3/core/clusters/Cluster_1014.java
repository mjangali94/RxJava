package io.reactivex.rxjava3.core.clusters;

public class Cluster_1014 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.flowable.FlowableCovarianceTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeMaterializeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAmbTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsObservableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCountTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1014() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.reduceMaybeDoubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_1.payloads.sortedList.evaluate();
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.isDisposed);
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_4.payloads.dispose.evaluate();
            this._Benchmark_benchmark_5.payloads.manySources.evaluate();
            this._Benchmark_benchmark_6.payloads.fused2.evaluate();
            this._Benchmark_benchmark_7.payloads.hasSource.evaluate();
        }

   }

}