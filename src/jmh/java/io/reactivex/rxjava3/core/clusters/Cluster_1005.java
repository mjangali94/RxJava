package io.reactivex.rxjava3.core.clusters;

public class Cluster_1005 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark _Benchmark_benchmark_0;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableSwitchMapCompletableTest._Benchmark();
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
        public void benchmark_Cluster_1005() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.normal.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperCancels.evaluate();
            this._Benchmark_benchmark_0.payloads.checkDisposed.evaluate();
            this._Benchmark_benchmark_0.payloads.switchOver.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.mainErrorDelayed.evaluate();
            this._Benchmark_benchmark_0.payloads.innerErrorDelayed.evaluate();
        }

   }

}