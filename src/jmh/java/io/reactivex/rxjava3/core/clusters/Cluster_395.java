package io.reactivex.rxjava3.core.clusters;

public class Cluster_395 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleSwitchOnNextTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapSingleTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.single.SingleSwitchOnNextTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_395() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.backpressured.evaluate();
            this._Benchmark_benchmark_0.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.requestMoreOnNext.evaluate();
            this._Benchmark_benchmark_0.payloads.innerError.evaluate();
            this._Benchmark_benchmark_0.payloads.limit.evaluate();
            this._Benchmark_benchmark_5.payloads.normal.evaluate();
            this._Benchmark_benchmark_5.payloads.normalDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_5.payloads.noDelaySwitch.evaluate();
            this._Benchmark_benchmark_0.payloads.cancel.evaluate();
        }

   }

}