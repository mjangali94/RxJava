package io.reactivex.rxjava3.core.clusters;

public class Cluster_648 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.observers.CallbackCompletableObserverTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.observers.ConsumerSingleObserverTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeCallbackObserverTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.disposables.FutureDisposableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark _Benchmark_benchmark_9;
       private io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark _Benchmark_benchmark_10;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark _Benchmark_benchmark_13;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark _Benchmark_benchmark_17;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_20;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.observers.CallbackCompletableObserverTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.observers.ConsumerSingleObserverTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeCallbackObserverTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.disposables.FutureDisposableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRepeatTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotificationTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark();
            _Benchmark_benchmark_10 = new io.reactivex.rxjava3.internal.observers.LambdaObserverTest._Benchmark();
            _Benchmark_benchmark_13 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableGenerateTest._Benchmark();
            _Benchmark_benchmark_17 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableFromIterableTest._Benchmark();
            _Benchmark_benchmark_20 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_10.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_13.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_17.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_20.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_648() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.customOnErrorShouldReportCustomOnError.evaluate();
            this._Benchmark_benchmark_1.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
            this._Benchmark_benchmark_1.payloads.customOnErrorShouldReportCustomOnError.evaluate();
            this._Benchmark_benchmark_3.payloads.invalidPrefetch.evaluate();
            this._Benchmark_benchmark_4.payloads.customOnErrorShouldReportCustomOnError.evaluate();
            this._Benchmark_benchmark_4.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
            this._Benchmark_benchmark_6.payloads.normalDone.evaluate();
            this._Benchmark_benchmark_7.runBenchmark(this._Benchmark_benchmark_7.payloads.repeatAndDistinctUnbounded);
            this._Benchmark_benchmark_8.payloads.dispose.evaluate();
            this._Benchmark_benchmark_9.payloads.identityFunctionToString.evaluate();
            this._Benchmark_benchmark_10.payloads.customOnErrorShouldReportCustomOnError.evaluate();
            this._Benchmark_benchmark_10.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
            this._Benchmark_benchmark_6.payloads.interruptible.evaluate();
            this._Benchmark_benchmark_13.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyConsumerToString.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyActionToString.evaluate();
            this._Benchmark_benchmark_9.payloads.emptyRunnableToString.evaluate();
            this._Benchmark_benchmark_17.payloads.normalConditionalBackpressured.evaluate();
            this._Benchmark_benchmark_17.payloads.normalConditionalBackpressured2.evaluate();
            this._Benchmark_benchmark_6.payloads.normal.evaluate();
            this._Benchmark_benchmark_20.payloads.backpressureWithEmissionThenError.evaluate();
        }

   }

}