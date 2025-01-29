package io.reactivex.rxjava3.core.clusters;

public class Cluster_43 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoOnEachTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableCollectWithCollectorTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoOnEachTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_43() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.seedDisposed.evaluate();
            this._Benchmark_benchmark_1.payloads.predicateThrowsExceptionAndValueInCauseMessageObservable.evaluate();
            this._Benchmark_benchmark_1.payloads.predicateThrowsExceptionAndValueInCauseMessage.evaluate();
            this._Benchmark_benchmark_3.payloads.toMapValueSelectorReturnsNull.evaluate();
            this._Benchmark_benchmark_3.payloads.toMultiMapValueSelectorReturnsNullAllowed.evaluate();
            this._Benchmark_benchmark_5.payloads.dispose.evaluate();
            this._Benchmark_benchmark_6.payloads.onCompleteAfter.evaluate();
            this._Benchmark_benchmark_5.payloads.empty.evaluate();
        }

   }

}