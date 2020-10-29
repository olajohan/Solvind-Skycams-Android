package com.solvind.skycams.app.data.repo.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.solvind.skycams.app.core.IMapper
import com.solvind.skycams.app.domain.model.Alarm
import javax.inject.Inject

class SnapshotToAlarmMapper @Inject constructor() : IMapper<DocumentSnapshot, Alarm> {

    override fun singleFromLeftToRight(left: DocumentSnapshot): Alarm {
        return Alarm(
            skycamKey = left.id,
            alarmAvailableUntilEpochSeconds = left.getLong("alarmAvailableUntilEpochSeconds") ?: 0L,
            isActive = left.getBoolean("isActive") ?: false
        )
    }

    override fun singleFromRightToLeft(right: Alarm): DocumentSnapshot {
        TODO("Not yet implemented")
    }


}