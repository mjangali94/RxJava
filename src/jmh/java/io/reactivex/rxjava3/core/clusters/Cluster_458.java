package io.reactivex.rxjava3.core.clusters;

public class Cluster_458 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.maybe.MaybeTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.maybe.MaybeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_458() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.iterableUndeliverableUponCancelDelayError.evaluate();
            this._Benchmark_benchmark_1.payloads.ambArray2SignalsComplete.evaluate();
            this._Benchmark_benchmark_1.payloads.ambArray1SignalsComplete.evaluate();
            this._Benchmark_benchmark_1.payloads.ambIterable2SignalsComplete.evaluate();
            this._Benchmark_benchmark_1.payloads.ambIterable1SignalsComplete.evaluate();
            this._Benchmark_benchmark_1.payloads.ambArray2SignalsSuccess.evaluate();
        }

   }

}