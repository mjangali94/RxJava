package io.reactivex.rxjava3.core.clusters;

public class Cluster_229 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableMaterializeTest._Benchmark _Benchmark_benchmark_12;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark _Benchmark_benchmark_13;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapSingleTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToFutureTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableAnyTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.observable.ObservableElementAtTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableCollectTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.SubscribeWithTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.schedulers.ScheduledRunnableTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.observable.ObservableReduceTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingTest._Benchmark();
            _Benchmark_benchmark_12 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMaterializeTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapCompletableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_12.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_229() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.successError.evaluate();
            this._Benchmark_benchmark_1.payloads.toFutureList.evaluate();
            this._Benchmark_benchmark_1.payloads.toFuture.evaluate();
            this._Benchmark_benchmark_3.payloads.withFollowingFirst.evaluate();
            this._Benchmark_benchmark_4.payloads.elementAtOrDefaultWithIndexOutOfBounds.evaluate();
            this._Benchmark_benchmark_5.payloads.collectToList.evaluate();
            this._Benchmark_benchmark_6.payloads.withSingle.evaluate();
            this._Benchmark_benchmark_4.payloads.elementAtOrDefault.evaluate();
            this._Benchmark_benchmark_8.payloads.disposeRun.evaluate();
            this._Benchmark_benchmark_9.payloads.backpressureWithInitialValue.evaluate();
            this._Benchmark_benchmark_8.payloads.withParentIsDisposed.evaluate();
            this._Benchmark_benchmark_11.payloads.blockingLastNormal.evaluate();
            this._Benchmark_benchmark_12.payloads.dispose.evaluate();
            this._Benchmark_benchmark_13.payloads.normalDelayErrorObservable.evaluate();
        }

   }

}