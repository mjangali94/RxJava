package io.reactivex.rxjava3.core.clusters;

public class Cluster_963 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchOnNextTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapMaybeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeSwitchOnNextTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_963() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.mainErrorInnerCompleteDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.mainErrorInnerSuccessDelayError.evaluate();
            this._Benchmark_benchmark_2.payloads.delaySwitch.evaluate();
            this._Benchmark_benchmark_0.payloads.switchOverDelayError.evaluate();
        }

   }

}