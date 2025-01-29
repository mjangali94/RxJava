package io.reactivex.rxjava3.core.clusters;

public class Cluster_835 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableOnErrorReturnTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.functions.FunctionsTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.subscribers.BoundedSubscriberTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableSkipLastTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_835() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.returnItem.evaluate();
            this._Benchmark_benchmark_1.payloads.toFunction7.evaluate();
            this._Benchmark_benchmark_2.payloads.cancel.evaluate();
            this._Benchmark_benchmark_3.payloads.error.evaluate();
            this._Benchmark_benchmark_1.payloads.utilityClass.evaluate();
        }

   }

}