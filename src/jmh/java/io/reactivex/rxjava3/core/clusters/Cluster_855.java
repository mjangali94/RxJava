package io.reactivex.rxjava3.core.clusters;

public class Cluster_855 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.disposables.CompositeDisposableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.util.AtomicThrowableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.completable.CompletableAmbTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.completable.CompletableDelaySubscriptionTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_855() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.addAll.evaluate();
            this._Benchmark_benchmark_0.payloads.initializeVarargs.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionThrows.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerCompletableObserverTerminated.evaluate();
            this._Benchmark_benchmark_4.payloads.ambRace.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerCompletableObserverNoError.evaluate();
            this._Benchmark_benchmark_6.payloads.timestep.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerSingleNoError.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerSingleTerminated.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerMaybeObserverTerminated.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerObserverTerminated.evaluate();
            this._Benchmark_benchmark_3.payloads.tryTerminateConsumerEmitterTerminated.evaluate();
        }

   }

}