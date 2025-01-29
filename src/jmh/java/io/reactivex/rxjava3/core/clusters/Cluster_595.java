package io.reactivex.rxjava3.core.clusters;

public class Cluster_595 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableDelayTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDelayTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableUsingTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableSequenceEqualTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_595() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.delaySupplierCompletes.evaluate();
            this._Benchmark_benchmark_1.payloads.eagerDisposeResourceThenDisposeUpstream.evaluate();
            this._Benchmark_benchmark_1.payloads.nonEagerDisposeUpstreamThenDisposeResource.evaluate();
            this._Benchmark_benchmark_3.payloads.bufferSkipSupplierCrash2.evaluate();
            this._Benchmark_benchmark_4.payloads.prefetchObservable.evaluate();
        }

   }

}