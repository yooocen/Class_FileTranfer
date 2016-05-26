package com.whangyx.whang_file;

import android.app.Application;

/**
 * Created by chen on 16-5-12.
 */
public class ContextUtil extends Application {

    /**
     * Created by chen on 16-5-12.
     */

        private static ContextUtil instance;

        public static ContextUtil getInstance() {
            return instance;
        }

        @Override
        public void onCreate() {
            // TODO Auto-generated method stub
            super.onCreate();
            instance = this;
        }


}
