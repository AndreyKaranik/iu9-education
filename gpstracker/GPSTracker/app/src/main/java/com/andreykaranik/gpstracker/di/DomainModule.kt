package com.andreykaranik.gpstracker.di

import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository
import com.andreykaranik.gpstracker.domain.usecase.CreateGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetGroupDataUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetGroupMembersUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetPeriodClustersUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetPeriodLocationsUseCase
import com.andreykaranik.gpstracker.domain.usecase.GetUserDataUseCase
import com.andreykaranik.gpstracker.domain.usecase.JoinGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.LeaveGroupUseCase
import com.andreykaranik.gpstracker.domain.usecase.LoginUseCase
import com.andreykaranik.gpstracker.domain.usecase.Mode11UseCase
import com.andreykaranik.gpstracker.domain.usecase.Mode12UseCase
import com.andreykaranik.gpstracker.domain.usecase.RefreshTokenUseCase
import com.andreykaranik.gpstracker.domain.usecase.RegisterUseCase
import com.andreykaranik.gpstracker.domain.usecase.SaveUserDataUseCase
import com.andreykaranik.gpstracker.domain.usecase.SendDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
class DomainModule {
    @Provides
    fun provideRegisterUseCase(
        userRepository: UserRepository
    ): RegisterUseCase {
        return RegisterUseCase(
            userRepository = userRepository
        )
    }

    @Provides
    fun provideLoginUseCase(
        userRepository: UserRepository
    ): LoginUseCase {
        return LoginUseCase(
            userRepository = userRepository
        )
    }

    @Provides
    fun provideRefreshTokenUseCase(
        userRepository: UserRepository
    ): RefreshTokenUseCase {
        return RefreshTokenUseCase(
            userRepository = userRepository
        )
    }

    @Provides
    fun provideSaveUserDataUseCase(
        userRepository: UserRepository
    ): SaveUserDataUseCase {
        return SaveUserDataUseCase(
            userRepository = userRepository
        )
    }

    @Provides
    fun provideGetUserDataUseCase(
        userRepository: UserRepository
    ): GetUserDataUseCase {
        return GetUserDataUseCase(
            userRepository = userRepository
        )
    }

    @Provides
    fun provideGetGroupDataUseCase(
        groupRepository: GroupRepository,
        userRepository: UserRepository
    ): GetGroupDataUseCase {
        return GetGroupDataUseCase(
            groupRepository = groupRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideCreateGroupUseCase(
        groupRepository: GroupRepository,
        userRepository: UserRepository
    ): CreateGroupUseCase {
        return CreateGroupUseCase(
            groupRepository = groupRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideJoinGroupUseCase(
        groupRepository: GroupRepository,
        userRepository: UserRepository
    ): JoinGroupUseCase {
        return JoinGroupUseCase(
            groupRepository = groupRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideLeaveGroupUseCase(
        groupRepository: GroupRepository,
        userRepository: UserRepository
    ): LeaveGroupUseCase {
        return LeaveGroupUseCase(
            groupRepository = groupRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideGetGroupMembersUseCase(
        groupRepository: GroupRepository,
        userRepository: UserRepository
    ): GetGroupMembersUseCase {
        return GetGroupMembersUseCase(
            groupRepository = groupRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideMode11UseCase(
        dataRepository: DataRepository,
        userRepository: UserRepository
    ): Mode11UseCase {
        return Mode11UseCase(
            dataRepository = dataRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideMode12UseCase(
        dataRepository: DataRepository,
        userRepository: UserRepository
    ): Mode12UseCase {
        return Mode12UseCase(
            dataRepository = dataRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideGetPeriodLocationsUseCase(
        dataRepository: DataRepository,
        userRepository: UserRepository
    ): GetPeriodLocationsUseCase {
        return GetPeriodLocationsUseCase(
            dataRepository = dataRepository,
            userRepository = userRepository
        )
    }

    @Provides
    fun provideGetPeriodClustersUseCase(
        dataRepository: DataRepository,
        userRepository: UserRepository
    ): GetPeriodClustersUseCase {
        return GetPeriodClustersUseCase(
            dataRepository = dataRepository,
            userRepository = userRepository
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideSendDataUseCase(
        dataRepository: DataRepository,
        userRepository: UserRepository
    ): SendDataUseCase {
        return SendDataUseCase(
            dataRepository = dataRepository,
            userRepository = userRepository
        )
    }
}


