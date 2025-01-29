package io.reactivex.rxjava3.core.clusters;

public class Cluster_625 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapMaybeTest._Benchmark();
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
        public void benchmark_Cluster_625() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.simpleEmpty.evaluate();
            this._Benchmark_benchmark_0.payloads.simpleMixed.evaluate();
            this._Benchmark_benchmark_0.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.drainReentrant.evaluate();
            this._Benchmark_benchmark_0.payloads.take.evaluate();
            this._Benchmark_benchmark_0.payloads.mainErrorInnerCompleteDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.mainErrorInnerSuccessDelayError.evaluate();
        }

   }

}