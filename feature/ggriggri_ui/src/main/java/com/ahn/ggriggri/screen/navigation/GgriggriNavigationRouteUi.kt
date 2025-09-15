package com.ahn.ggriggri.screen.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface GgriggriNavigationRouteUi {

    @Serializable
    data object HomeTab : GgriggriNavigationRouteUi

    @Serializable
    data object MemoryTab : GgriggriNavigationRouteUi

    @Serializable
    data object MyPageTab : GgriggriNavigationRouteUi

    @Serializable
    data object PasswordReset : GgriggriNavigationRouteUi

    @Serializable
    data object SettingGroup : GgriggriNavigationRouteUi

    @Serializable
    data object ModifyGroupPw : GgriggriNavigationRouteUi

    @Serializable
    data object ModifyGroupName : GgriggriNavigationRouteUi

    @Serializable
    data class QuestionAnswer(val questionDataId: String) : GgriggriNavigationRouteUi

    @Serializable
    data object Login : GgriggriNavigationRouteUi

    @Serializable
    data object DevLogin : GgriggriNavigationRouteUi

    @Serializable
    data object Group: GgriggriNavigationRouteUi

    @Serializable
    data object Answer: GgriggriNavigationRouteUi

    @Serializable
    data object QuestionList: GgriggriNavigationRouteUi

    @Serializable
    data object RequestList: GgriggriNavigationRouteUi

    @Serializable
    data class RequestDetail(val requestId: String): GgriggriNavigationRouteUi

    @Serializable
    data object Request: GgriggriNavigationRouteUi

    @Serializable
    data object Response: GgriggriNavigationRouteUi
}