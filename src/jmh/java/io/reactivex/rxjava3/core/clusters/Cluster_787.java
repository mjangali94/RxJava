package io.reactivex.rxjava3.core.clusters;

public class Cluster_787 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromArrayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRetryTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRepeatTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.observable.ObservableNullTests._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_787() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.empty.evaluate();
            this._Benchmark_benchmark_1.payloads.takeNegative.evaluate();
            this._Benchmark_benchmark_2.payloads.retryLongPredicateInvalid.evaluate();
            this._Benchmark_benchmark_3.payloads.repeatLongPredicateInvalid.evaluate();
            this._Benchmark_benchmark_4.payloads.zipWithIterableCombinerReturnsNull.evaluate();
        }

   }

}