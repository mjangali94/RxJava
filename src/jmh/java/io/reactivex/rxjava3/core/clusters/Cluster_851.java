package io.reactivex.rxjava3.core.clusters;

public class Cluster_851 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWithLatestFromTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromSourceTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_851() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.manySourcesIterable.evaluate();
            this._Benchmark_benchmark_1.payloads.bothCompleteWhileComparing.evaluate();
            this._Benchmark_benchmark_1.payloads.bothCompleteWhileComparingAsObservable.evaluate();
            this._Benchmark_benchmark_3.payloads.unsubscribedNoCancelDrop.evaluate();
            this._Benchmark_benchmark_3.payloads.unsubscribedNoCancelLatest.evaluate();
        }

   }

}