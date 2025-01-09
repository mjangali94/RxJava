package io.reactivex.rxjava3.core.clusters;

public class Cluster_1102 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleTimeoutTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleTimeoutTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableCreateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1102() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.ambArrayOneIsNull.evaluate();
            this._Benchmark_benchmark_1.payloads.disposeWhenFallback.evaluate();
            this._Benchmark_benchmark_0.payloads.onErrorResumeWithEmpty.evaluate();
            this._Benchmark_benchmark_3.payloads.basicWithCancellable.evaluate();
            this._Benchmark_benchmark_0.payloads.errorToCompletable.evaluate();
            this._Benchmark_benchmark_0.payloads.ignoreElementError.evaluate();
        }

   }

}