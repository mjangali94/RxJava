package io.reactivex.rxjava3.core.clusters;

public class Cluster_649 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_649() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.customOnErrorShouldReportCustomOnError.evaluate();
            this._Benchmark_benchmark_0.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction5.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction2.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction3.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction4.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction6.evaluate();
            this._Benchmark_benchmark_2.payloads.toFunction8.evaluate();
        }

   }

}