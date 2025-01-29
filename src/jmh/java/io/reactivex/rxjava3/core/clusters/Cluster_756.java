package io.reactivex.rxjava3.core.clusters;

public class Cluster_756 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCreateTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromActionTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromRunnableTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFromSupplierTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTimerTest._Benchmark _Benchmark_benchmark_14;
       private io.reactivex.rxjava3.disposables.SerialDisposableTests._Benchmark _Benchmark_benchmark_15;
       private io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark _Benchmark_benchmark_16;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCreateTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromCallableTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromActionTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromRunnableTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFromSupplierTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTimerTest._Benchmark();
            _Benchmark_benchmark_15 = new io.reactivex.rxjava3.disposables.SerialDisposableTests._Benchmark();
            _Benchmark_benchmark_16 = new io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
            this._Benchmark_benchmark_15.makePayloads();
            this._Benchmark_benchmark_16.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_756() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.onErrorThrows.evaluate();
            this._Benchmark_benchmark_1.payloads.fromCallableThrows.evaluate();
            this._Benchmark_benchmark_2.payloads.cancelWhileRunning.evaluate();
            this._Benchmark_benchmark_3.payloads.cancelWhileRunning.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnable.evaluate();
            this._Benchmark_benchmark_2.payloads.fromAction.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableInvokesLazy.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionInvokesLazy.evaluate();
            this._Benchmark_benchmark_2.payloads.fromActionTwice.evaluate();
            this._Benchmark_benchmark_9.payloads.fromSupplier.evaluate();
            this._Benchmark_benchmark_3.payloads.fromRunnableTwice.evaluate();
            this._Benchmark_benchmark_9.payloads.fromSupplierInvokesLazy.evaluate();
            this._Benchmark_benchmark_9.payloads.fromSupplierTwice.evaluate();
            this._Benchmark_benchmark_9.payloads.success.evaluate();
            this._Benchmark_benchmark_14.payloads.cancelledAndRun.evaluate();
            this._Benchmark_benchmark_15.payloads.disposeState.evaluate();
            this._Benchmark_benchmark_16.payloads.toFunction9.evaluate();
        }

   }

}