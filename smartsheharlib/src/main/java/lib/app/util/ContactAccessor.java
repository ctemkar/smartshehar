package lib.app.util;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public abstract class ContactAccessor {

    /**
     * Static singleton instance of {@link ContactAccessor} holding the
     * SDK-specific implementation of the class.
     */
    private static ContactAccessor sInstance;

    public static ContactAccessor getInstance() {
        if (sInstance == null) {
            String className;

            /*
             * Check the version of the SDK we are running on. Choose an
             * implementation class designed for that version of the SDK.
             *
             * Unfortunately we have to use strings to represent the class
             * names. If we used the conventional ContactAccessorSdk5.class.getName()
             * syntax, we would get a ClassNotFoundException at runtime on pre-Eclair SDKs.
             * Using the above syntax would force Dalvik to load the class and try to
             * resolve references to all other classes it uses. Since the pre-Eclair
             * does not have those classes, the loading of ContactAccessorSdk5 would fail.
             */
            @SuppressWarnings("deprecation")
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);       // Cupcake style
            if (sdkVersion >= Build.VERSION_CODES.ECLAIR) {
                className = "lib.app.util.ContactAccessorSdk5";
            } else {

                className = "lib.app.util.ContactAccessorSdk3_4";
            }

            /*
             * Find the required class by name and instantiate it.
             */
            try {
                Class<? extends ContactAccessor> clazz =
                        Class.forName(className).asSubclass(ContactAccessor.class);
                sInstance = clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return sInstance;
    }

    /**
     * Returns the {@link Intent#ACTION_PICK} intent configured for the right authority: legacy
     * or current.
     */
    public abstract Intent getPickContactIntent();

    public abstract ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri);
}