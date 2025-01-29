package io.reactivex.rxjava3.core.clusters;

public class Cluster_694 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableRangeLongTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorCompleteTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDelayTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableDetachTest._Benchmark _Benchmark_benchmark_8;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRangeTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableRangeLongTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrDefaultTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.observable.ObservableOnErrorCompleteTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDelayTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.jdk8.ObservableStageSubscriberOrErrorTest._Benchmark();
            _Benchmark_benchmark_8 = new io.reactivex.rxjava3.internal.operators.observable.ObservableDetachTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_8.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_694() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero.evaluate();
            this._Benchmark_benchmark_1.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero.evaluate();
            this._Benchmark_benchmark_2.payloads.firstEmpty.evaluate();
            this._Benchmark_benchmark_3.runBenchmark(this._Benchmark_benchmark_3.payloads.empty);
            this._Benchmark_benchmark_2.payloads.singleEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.lastEmpty.evaluate();
            this._Benchmark_benchmark_6.payloads.onCompleteFinal.evaluate();
            this._Benchmark_benchmark_7.payloads.firstEmpty.evaluate();
            this._Benchmark_benchmark_8.payloads.empty.evaluate();
        }

   }

}