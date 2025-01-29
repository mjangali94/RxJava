package io.reactivex.rxjava3.core.clusters;

public class Cluster_552 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFilterTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.jdk8.ObservableMapOptionalTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMapTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_552() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.fusedSync.evaluate();
            this._Benchmark_benchmark_1.payloads.syncFused.evaluate();
            this._Benchmark_benchmark_2.payloads.syncFused.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.innerWithEmpty);
            this._Benchmark_benchmark_1.payloads.clearIsEmptyConditional.evaluate();
            this._Benchmark_benchmark_5.payloads.syncFusedNone.evaluate();
            this._Benchmark_benchmark_6.payloads.fusedSync.evaluate();
            this._Benchmark_benchmark_5.payloads.syncFusedNoneConditional.evaluate();
            this._Benchmark_benchmark_2.payloads.asyncFusedRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.filterThrows.evaluate();
            this._Benchmark_benchmark_5.payloads.syncFusedAll.evaluate();
            this._Benchmark_benchmark_5.payloads.syncFusedMixed.evaluate();
            this._Benchmark_benchmark_5.payloads.syncFusedAllConditional.evaluate();
            this._Benchmark_benchmark_1.payloads.nonFused.evaluate();
        }

   }

}