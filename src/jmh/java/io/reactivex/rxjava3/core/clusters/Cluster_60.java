package io.reactivex.rxjava3.core.clusters;

public class Cluster_60 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableIntervalRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableIntervalRangeTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTimedTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.schedulers.SingleSchedulerTest._Benchmark _Benchmark_benchmark_5;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableIntervalRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableIntervalRangeTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.observable.ObservableTakeLastTimedTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableTakeLastTimedTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.schedulers.SingleSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_60() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.countNegative.evaluate();
            this._Benchmark_benchmark_1.payloads.countNegative.evaluate();
            this._Benchmark_benchmark_2.payloads.takeLastTimedWithNegativeCount.evaluate();
            this._Benchmark_benchmark_3.payloads.takeLastTimedWithNegativeCount.evaluate();
            this._Benchmark_benchmark_0.payloads.longOverflow.evaluate();
            this._Benchmark_benchmark_5.payloads.schedulePeriodicallyDirectNullRunnable.evaluate();
        }

   }

}