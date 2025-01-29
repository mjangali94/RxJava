package io.reactivex.rxjava3.core.clusters;

public class Cluster_776 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.SingleFlatMapObservableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleConcatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAllTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_776() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelMain.evaluate();
            this._Benchmark_benchmark_0.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_2.payloads.mapsToEmpty.evaluate();
            this._Benchmark_benchmark_3.payloads.mapsToEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.predicateThrowsExceptionAndValueInCauseMessageObservable.evaluate();
            this._Benchmark_benchmark_4.payloads.predicateThrowsExceptionAndValueInCauseMessage.evaluate();
            this._Benchmark_benchmark_6.payloads.disposed.evaluate();
            this._Benchmark_benchmark_0.payloads.errorOther.evaluate();
            this._Benchmark_benchmark_0.payloads.errorMain.evaluate();
        }

   }

}