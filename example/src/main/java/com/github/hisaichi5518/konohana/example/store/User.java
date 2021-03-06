package com.github.hisaichi5518.konohana.example.store;

import android.support.annotation.Nullable;

import com.github.hisaichi5518.konohana.annotation.Key;
import com.github.hisaichi5518.konohana.annotation.Store;

@Store
interface User {
    @Key
    String name = "default name";

    @Key
    @Nullable
    String bio = null;
}
