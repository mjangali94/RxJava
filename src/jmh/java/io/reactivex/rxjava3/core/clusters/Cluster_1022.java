package io.reactivex.rxjava3.core.clusters;

public class Cluster_1022 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.single.SingleZipTest._Benchmark _Benchmark_benchmark_3;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.single.SingleDoOnTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.single.SingleMiscTest._Benchmark();
            _Benchmark_benchmark_3 = new io.reactivex.rxjava3.internal.operators.single.SingleZipTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
            this._Benchmark_benchmark_3.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_1022() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.doOnEventThrowsSuccess.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSuccessCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.cast.evaluate();
            this._Benchmark_benchmark_3.payloads.zip2.evaluate();
            this._Benchmark_benchmark_3.payloads.zip3.evaluate();
            this._Benchmark_benchmark_3.payloads.zip4.evaluate();
            this._Benchmark_benchmark_0.payloads.doOnSubscribeJustCrash.evaluate();
            this._Benchmark_benchmark_3.payloads.zip5.evaluate();
            this._Benchmark_benchmark_3.payloads.zip6.evaluate();
            this._Benchmark_benchmark_3.payloads.zip7.evaluate();
            this._Benchmark_benchmark_3.payloads.zip8.evaluate();
            this._Benchmark_benchmark_3.payloads.zip9.evaluate();
        }

   }

}