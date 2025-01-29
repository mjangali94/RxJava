package io.reactivex.rxjava3.core.clusters;

public class Cluster_41 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableCacheTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDoFinallyTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableTakeUntilTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCacheTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDoFinallyTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableTakeUntilTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_41() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.payloads.actionThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.mainCompletes.evaluate();
            this._Benchmark_benchmark_2.payloads.otherCompletes.evaluate();
            this._Benchmark_benchmark_0.payloads.crossDisposeOnError.evaluate();
            this._Benchmark_benchmark_2.payloads.otherCompleteLate.evaluate();
            this._Benchmark_benchmark_2.payloads.mainCompleteLate.evaluate();
            this._Benchmark_benchmark_7.payloads.disposeBeforeTime.evaluate();
        }

   }

}