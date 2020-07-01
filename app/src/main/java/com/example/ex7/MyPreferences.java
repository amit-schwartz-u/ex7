package com.example.ex7;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreferences {

    public static void setUserToken(Context context, String token) {
        final SharedPreferences reader = context.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = reader.edit();
        preferencesEditor.putString("userToken", token);
        preferencesEditor.apply();
    }

    public static String getUserTokenFromPreferences(Context context) {
        final SharedPreferences reader = context.getSharedPreferences("myPreferences", Context.MODE_PRIVATE);
        return reader.getString("userToken", "");
    }
}
