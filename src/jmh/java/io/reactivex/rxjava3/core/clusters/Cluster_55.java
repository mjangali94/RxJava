package io.reactivex.rxjava3.core.clusters;

public class Cluster_55 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeStartWithTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark _Benchmark_benchmark_11;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableStartWithTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.single.SingleStartWithTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeStartWithTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.maybe.MaybeRetryTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromMaybeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatDelayErrorTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableToFutureTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_55() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.maybeNormal);
            this._Benchmark_benchmark_1.runBenchmark(this._Benchmark_benchmark_1.payloads.justMaybeJust);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justSingleJust);
            this._Benchmark_benchmark_2.runBenchmark(this._Benchmark_benchmark_2.payloads.justCompletableComplete);
            this._Benchmark_benchmark_4.payloads.untilTrueJust.evaluate();
            this._Benchmark_benchmark_4.payloads.untilFalseJust.evaluate();
            this._Benchmark_benchmark_6.payloads.success.evaluate();
            this._Benchmark_benchmark_6.payloads.syncFusionRejected.evaluate();
            this._Benchmark_benchmark_8.payloads.innerWithScalar.evaluate();
            this._Benchmark_benchmark_8.payloads.innerWithEmpty.evaluate();
            this._Benchmark_benchmark_11.runBenchmark(this._Benchmark_benchmark_11.payloads.exceptionWithMoreThanOneElement);
        }

   }

}