package io.reactivex.rxjava3.core.clusters;

public class Cluster_241 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithSizeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithSizeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapEagerTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCreateTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_241() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.windowAbandonmentCancelsUpstreamOverlap.evaluate();
            this._Benchmark_benchmark_0.payloads.windowAbandonmentCancelsUpstreamSkip.evaluate();
            this._Benchmark_benchmark_2.payloads.mainErrorsDelayBoundary.evaluate();
            this._Benchmark_benchmark_2.payloads.mainErrorsDelayEnd.evaluate();
            this._Benchmark_benchmark_4.payloads.serializedOnNextAfterComplete.evaluate();
            this._Benchmark_benchmark_2.payloads.mainErrorsImmediate.evaluate();
        }

   }

}