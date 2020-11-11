package com.lenebf.android.androidaoptutorial;

import android.app.Application;

public class DemoApplication extends Application {
    private static final String TAG = "lenebf";

    @Override
    public void onCreate() {
        super.onCreate();
        // registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
        //     @Override
        //     public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //
        //     }
        //
        //     @Override
        //     public void onActivityStarted(@NonNull Activity activity) {
        //
        //     }
        //
        //     @Override
        //     public void onActivityResumed(@NonNull Activity activity) {
        //         Log.d(TAG, "${activity.javaClass.simpleName}: onResume");
        //     }
        //
        //     @Override
        //     public void onActivityPaused(@NonNull Activity activity) {
        //         Log.d(TAG, "${activity.javaClass.simpleName}: onPause");
        //     }
        //
        //     @Override
        //     public void onActivityStopped(@NonNull Activity activity) {
        //
        //     }
        //
        //     @Override
        //     public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        //
        //     }
        //
        //     @Override
        //     public void onActivityDestroyed(@NonNull Activity activity) {
        //
        //     }
        // });
    }
}
