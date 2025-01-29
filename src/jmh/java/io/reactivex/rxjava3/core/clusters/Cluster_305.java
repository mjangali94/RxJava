package io.reactivex.rxjava3.core.clusters;

public class Cluster_305 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.observers.QueueDrainObserverTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark _Benchmark_benchmark_3;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark _Benchmark_benchmark_6;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableScanTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.observers.QueueDrainObserverTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.util.QueueDrainHelperTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapSchedulerTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_305() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.scanErrorBackpressured.evaluate();
            this._Benchmark_benchmark_1.payloads.orderedSlowPathNonEmptyQueue.evaluate();
            this._Benchmark_benchmark_2.payloads.orderedSlowPathNonEmptyQueue.evaluate();
            this._Benchmark_benchmark_3.payloads.drainMaxLoopMissingBackpressure.evaluate();
            this._Benchmark_benchmark_2.payloads.unorderedSlowPath.evaluate();
            this._Benchmark_benchmark_2.payloads.orderedSlowPath.evaluate();
            this._Benchmark_benchmark_6.payloads.callableCrash.evaluate();
            this._Benchmark_benchmark_6.payloads.scalarInnerEmptyDisposeDelayError.evaluate();
        }

   }

}