package com.ahn.ggriggri.di

import com.ahn.domain.common.KakaoTokenProvider
import com.ahn.ggriggri.auth.KakaoSdkTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindKakaoTokenProvider(impl: KakaoSdkTokenProvider): KakaoTokenProvider
}
