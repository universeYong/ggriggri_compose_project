package com.ahn.data.local.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// 앱 전체에서 하나의 DataStore 인스턴스를 사용하도록 Context의 확장 프로퍼티로 정의
 val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ggriggri_user_session")