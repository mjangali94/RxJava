package io.reactivex.rxjava3.core.clusters;

public class Cluster_560 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableSwitchOnNextTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.FlowableSwitchMapCompletableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.completable.CompletableConcatTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.completable.CompletableSwitchOnNextTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_560() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.normal.evaluate();
            this._Benchmark_benchmark_0.payloads.checkDisposed.evaluate();
            this._Benchmark_benchmark_0.payloads.switchOver.evaluate();
            this._Benchmark_benchmark_0.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_0.payloads.mainError.evaluate();
            this._Benchmark_benchmark_0.payloads.dispose.evaluate();
            this._Benchmark_benchmark_0.payloads.mainErrorDelayed.evaluate();
            this._Benchmark_benchmark_7.payloads.syncFusedUnboundedIn.evaluate();
            this._Benchmark_benchmark_8.payloads.noDelaySwitch.evaluate();
            this._Benchmark_benchmark_9.runBenchmark(this._Benchmark_benchmark_9.payloads.justCompletableComplete);
            this._Benchmark_benchmark_0.payloads.innerErrorDelayed.evaluate();
        }

   }

}