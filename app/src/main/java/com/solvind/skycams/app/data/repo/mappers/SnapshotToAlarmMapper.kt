package com.solvind.skycams.app.data.repo.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.solvind.skycams.app.core.IMapper
import com.solvind.skycams.app.domain.model.AlarmConfig
import javax.inject.Inject

class SnapshotToAlarmMapper @Inject constructor() : IMapper<DocumentSnapshot, AlarmConfig> {

    override fun singleFromLeftToRight(left: DocumentSnapshot): AlarmConfig {
        return AlarmConfig(
            skycamKey = left.id,
            alarmAvailableUntilEpochSeconds = left.getLong("alarmAvailableUntilEpochSeconds") ?: 0L,
            isActive = left.getBoolean("isActive") ?: false
        )
    }

    override fun singleFromRightToLeft(right: AlarmConfig): DocumentSnapshot {
        TODO("Not yet implemented")
    }


}