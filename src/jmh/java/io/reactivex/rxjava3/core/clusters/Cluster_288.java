package io.reactivex.rxjava3.core.clusters;

public class Cluster_288 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.schedulers.IoSchedulerInternalTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark _Benchmark_benchmark_7;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.schedulers.IoSchedulerInternalTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_288() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.emissionQueueTrigger.evaluate();
            this._Benchmark_benchmark_0.payloads.disposeInner.evaluate();
            this._Benchmark_benchmark_0.payloads.onNextDrainCancel.evaluate();
            this._Benchmark_benchmark_3.payloads.expiredWorkerRemoved.evaluate();
            this._Benchmark_benchmark_4.payloads.tryRemoveIfNotIn.evaluate();
            this._Benchmark_benchmark_0.payloads.successError.evaluate();
            this._Benchmark_benchmark_6.payloads.normalDelayErrorObservable.evaluate();
            this._Benchmark_benchmark_7.payloads.disposeRun.evaluate();
        }

   }

}