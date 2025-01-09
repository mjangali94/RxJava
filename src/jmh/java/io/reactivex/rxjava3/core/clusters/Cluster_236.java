package io.reactivex.rxjava3.core.clusters;

public class Cluster_236 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.schedulers.IoSchedulerInternalTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableMergeDelayErrorTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableToListTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableFlatMapMaybeTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.exceptions.ExceptionsTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.observable.ObservableConcatTests._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.observable.ObservableLastTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.BlockingObservableToIteratorTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.schedulers.IoSchedulerInternalTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.observable.ObservableStartWithTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_236() throws java.lang.Throwable {
            
            this._Benchmark_benchmark_0.payloads.mergeDelayError3.evaluate();
            this._Benchmark_benchmark_1.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.disposeInner.evaluate();
            this._Benchmark_benchmark_3.payloads.stackOverflowErrorIsThrown.evaluate();
            this._Benchmark_benchmark_4.payloads.concatCovariance4.evaluate();
            this._Benchmark_benchmark_5.payloads.lastOrErrorOneElement.evaluate();
            this._Benchmark_benchmark_3.payloads.threadDeathIsThrown.evaluate();
            this._Benchmark_benchmark_0.payloads.mergeDelayError.evaluate();
            this._Benchmark_benchmark_8.payloads.toIterator.evaluate();
            this._Benchmark_benchmark_2.payloads.emissionQueueTrigger.evaluate();
            this._Benchmark_benchmark_10.payloads.noExpiredWorker.evaluate();

            this._Benchmark_benchmark_11.runBenchmark(this._Benchmark_benchmark_11.payloads.justObservableJust);
            
        }

   }

}