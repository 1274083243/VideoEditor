/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ike.videoeditor;

import android.app.Application;
import android.content.Context;

import com.ike.videoeditor.config.DynamicConfigImplDemo;
import com.tencent.matrix.Matrix;
import com.tencent.matrix.iocanary.IOCanaryPlugin;
import com.tencent.matrix.iocanary.config.IOConfig;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.util.MatrixLog;

import java.io.File;

/**
 * Created by caichongyang on 17/5/18.
 */

public class MatrixApplication extends Application {
    private static final String TAG = "Matrix.Application";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        Matrix.Builder builder = new Matrix.Builder(this); // build matrix
        builder.pluginListener(new TestPluginListener(this)); // add general pluginListener
        DynamicConfigImplDemo dynamicConfig = new DynamicConfigImplDemo(); // dynamic config

        // init plugin
        IOCanaryPlugin ioCanaryPlugin = new IOCanaryPlugin(new IOConfig.Builder()
                .dynamicConfig(dynamicConfig)
                .build());
        //add to matrix
        TracePlugin tracePlugin = configureTracePlugin(dynamicConfig);
        builder.plugin(tracePlugin);
        builder.plugin(ioCanaryPlugin);
        builder.pluginListener(new TestPluginListener(this));
        //init matrix
        Matrix.init(builder.build());

        // start plugin
        ioCanaryPlugin.start();
        tracePlugin.start();
    }

    private TracePlugin configureTracePlugin(DynamicConfigImplDemo dynamicConfig) {

        boolean fpsEnable = dynamicConfig.isFPSEnable();
        boolean traceEnable = dynamicConfig.isTraceEnable();
        boolean signalAnrTraceEnable = dynamicConfig.isSignalAnrTraceEnable();

        File traceFileDir = new File(getApplicationContext().getFilesDir(), "matrix_trace");
        if (!traceFileDir.exists()) {
            if (traceFileDir.mkdirs()) {
                MatrixLog.e(TAG, "failed to create traceFileDir");
            }
        }

        File anrTraceFile = new File(traceFileDir, "anr_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/anr_trace
        File printTraceFile = new File(traceFileDir, "print_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/print_trace

        TraceConfig traceConfig = new TraceConfig.Builder()
                .dynamicConfig(dynamicConfig)
                .enableFPS(fpsEnable)
                .enableEvilMethodTrace(traceEnable)
                .enableAnrTrace(traceEnable)
                .enableStartup(traceEnable)
                .enableIdleHandlerTrace(traceEnable)                    // Introduced in Matrix 2.0
                .enableMainThreadPriorityTrace(true)                    // Introduced in Matrix 2.0
                .enableSignalAnrTrace(signalAnrTraceEnable)             // Introduced in Matrix 2.0
                .anrTracePath(anrTraceFile.getAbsolutePath())
                .printTracePath(printTraceFile.getAbsolutePath())
                .splashActivities("sample.tencent.matrix.SplashActivity;")
                .isDebug(true)
                .isDevEnv(false)
                .build();

        //Another way to use SignalAnrTracer separately
        //useSignalAnrTraceAlone(anrTraceFile.getAbsolutePath(), printTraceFile.getAbsolutePath());

        return new TracePlugin(traceConfig);
    }








    public static Context getContext() {
        return sContext;
    }
}
