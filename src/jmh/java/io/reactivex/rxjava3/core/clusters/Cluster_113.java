package io.reactivex.rxjava3.core.clusters;

public class Cluster_113 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFromIterableTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastOneTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_113() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatWithObservableOfObservable.evaluate();
            this._Benchmark_benchmark_0.payloads.concatCovariance.evaluate();
            this._Benchmark_benchmark_0.payloads.concatCovariance2.evaluate();
            this._Benchmark_benchmark_3.payloads.concatReportsDisposedOnComplete.evaluate();
            this._Benchmark_benchmark_4.payloads.fusionWithConcatMap.evaluate();
            this._Benchmark_benchmark_0.payloads.concatWithIterableOfObservable.evaluate();
            this._Benchmark_benchmark_3.payloads.noCancelPrevious.evaluate();
            this._Benchmark_benchmark_3.payloads.concatReportsDisposedOnError.evaluate();
            this._Benchmark_benchmark_3.payloads.rejectedFusion.evaluate();
            this._Benchmark_benchmark_3.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.reentrantNoOverflowHidden.evaluate();
            this._Benchmark_benchmark_11.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_13.payloads.dispose.evaluate();
            this._Benchmark_benchmark_14.payloads.dispose.evaluate();
        }

   }

}