package io.reactivex.rxjava3.core.clusters;

public class Cluster_79 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIntervalRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.schedulers.SchedulerWhenTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.schedulers.TrampolineSchedulerInternalTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.schedulers.SingleSchedulerTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSubscribeOnTest._Benchmark _Benchmark_benchmark_9;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIntervalRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.schedulers.SchedulerWhenTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.schedulers.TrampolineSchedulerInternalTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.schedulers.SingleSchedulerTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableWindowWithTimeTest._Benchmark();
            _Benchmark_benchmark_9 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSubscribeOnTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_9.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_79() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.longOverflow.evaluate();
            this._Benchmark_benchmark_1.payloads.queueWorkerDispose.evaluate();
            this._Benchmark_benchmark_2.payloads.dispose.evaluate();
            this._Benchmark_benchmark_3.payloads.scheduleDirectNullRunnable.evaluate();
            this._Benchmark_benchmark_3.payloads.scheduleDirectWithDelayNullRunnable.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantScheduleShutdown2.evaluate();
            this._Benchmark_benchmark_6.payloads.invalidSpan.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantScheduleShutdown.evaluate();
            this._Benchmark_benchmark_1.payloads.combineCrashInConstructor.evaluate();
            this._Benchmark_benchmark_9.payloads.dispose.evaluate();
            this._Benchmark_benchmark_2.payloads.reentrantScheduleDispose.evaluate();
        }

   }

}