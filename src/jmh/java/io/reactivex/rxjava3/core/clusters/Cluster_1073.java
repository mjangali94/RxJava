package io.reactivex.rxjava3.core.clusters;

public class Cluster_1073 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.observers.FutureObserverTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeUsingTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeOnErrorXTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.observers.FutureObserverTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1073() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.supplierAndDisposerCrashEager.evaluate();
            this._Benchmark_benchmark_1.payloads.onErrorCompleteDoubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_2.payloads.onSubscribe.evaluate();
        }

   }

}