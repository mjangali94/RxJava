package io.reactivex.rxjava3.core.clusters;

public class Cluster_1063 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorCompleteTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.FlowableFromOptionalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark _Benchmark_benchmark_8;
       private io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableNextTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDelaySubscriptionOtherTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.FlowableFromOptionalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDetachTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeLongTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableDoAfterNextTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableRangeTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableMaterializeTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableLatestTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableNextTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1063() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.runBenchmark(this._Benchmark_benchmark_0.payloads.empty);
            this._Benchmark_benchmark_1.payloads.badRequest.evaluate();
            this._Benchmark_benchmark_2.payloads.empty.evaluate();
            this._Benchmark_benchmark_3.payloads.empty.evaluate();
            this._Benchmark_benchmark_4.payloads.rangeWithOverflow5.evaluate();
            this._Benchmark_benchmark_5.payloads.empty.evaluate();
            this._Benchmark_benchmark_6.payloads.rangeWithOverflow5.evaluate();
            this._Benchmark_benchmark_7.payloads.unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving.evaluate();
            this._Benchmark_benchmark_8.payloads.empty2.evaluate();
            this._Benchmark_benchmark_9.payloads.interrupt.evaluate();
        }

   }

}