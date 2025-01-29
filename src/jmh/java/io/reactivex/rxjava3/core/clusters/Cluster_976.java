package io.reactivex.rxjava3.core.clusters;

public class Cluster_976 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingleTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_976() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.basicSyncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_0.payloads.basicNonFused.evaluate();
            this._Benchmark_benchmark_0.payloads.basicAsyncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.fusedPollCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.cancel.evaluate();
            this._Benchmark_benchmark_0.payloads.basicFusionRejected.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.disposed.evaluate();
            this._Benchmark_benchmark_0.payloads.mainCompletesWhileInnerActive.evaluate();
        }

   }

}