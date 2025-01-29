package io.reactivex.rxjava3.core.clusters;

public class Cluster_239 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.jdk8.ObservableFlatMapStreamTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_239() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedAsync.evaluate();
            this._Benchmark_benchmark_1.payloads.nullThrowableSync.evaluate();
            this._Benchmark_benchmark_1.payloads.nullValueSync.evaluate();
            this._Benchmark_benchmark_3.payloads.asyncFusedBoundary.evaluate();
            this._Benchmark_benchmark_3.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_5.payloads.boundaryFusedNone.evaluate();
            this._Benchmark_benchmark_5.payloads.boundaryFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_7.payloads.asyncUpstreamFused.evaluate();
            this._Benchmark_benchmark_8.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyThrowsNoSuch.evaluate();
        }

   }

}