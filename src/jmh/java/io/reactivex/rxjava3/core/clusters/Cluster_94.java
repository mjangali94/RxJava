package io.reactivex.rxjava3.core.clusters;

public class Cluster_94 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapCompletableTest._Benchmark _Benchmark_benchmark_1;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_94() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.concatReportsDisposedOnComplete.evaluate();
            this._Benchmark_benchmark_1.payloads.basicSyncFused.evaluate();
            this._Benchmark_benchmark_1.payloads.simple.evaluate();
            this._Benchmark_benchmark_0.payloads.concatReportsDisposedOnError.evaluate();
            this._Benchmark_benchmark_1.payloads.simple2.evaluate();
            this._Benchmark_benchmark_1.payloads.disposed.evaluate();
            this._Benchmark_benchmark_0.payloads.asyncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_1.payloads.basicNonFused.evaluate();
            this._Benchmark_benchmark_0.payloads.notFused.evaluate();
            this._Benchmark_benchmark_1.payloads.basicFusionRejected.evaluate();
            this._Benchmark_benchmark_1.payloads.basicAsyncFused.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
        }

   }

}