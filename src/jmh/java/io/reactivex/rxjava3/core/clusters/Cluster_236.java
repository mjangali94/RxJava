package io.reactivex.rxjava3.core.clusters;

public class Cluster_236 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTimedTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSkipLastTimedTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_236() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.takeLastTime.evaluate();
            this._Benchmark_benchmark_0.payloads.takeLastTimeAndSize.evaluate();
            this._Benchmark_benchmark_2.payloads.doubleOnSubscribe.evaluate();
            this._Benchmark_benchmark_0.payloads.doubleOnSubscribe.evaluate();
        }

   }

}