package io.reactivex.rxjava3.core.clusters;

public class Cluster_717 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.completable.CompletableMergeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_717() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.innerObserver.evaluate();
            this._Benchmark_benchmark_1.payloads.withParentIsDisposed.evaluate();
            this._Benchmark_benchmark_2.payloads.removeUnsubscribes.evaluate();
            this._Benchmark_benchmark_3.payloads.maxConcurrencyOne.evaluate();
            this._Benchmark_benchmark_3.payloads.maxConcurrencyOneDelayError.evaluate();
            this._Benchmark_benchmark_0.payloads.normalDelayErrorFlowable.evaluate();
            this._Benchmark_benchmark_0.payloads.normalFlowable.evaluate();
            this._Benchmark_benchmark_3.payloads.innerIsDisposed.evaluate();
        }

   }

}