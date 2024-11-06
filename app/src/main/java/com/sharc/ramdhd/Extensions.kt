package com.sharc.ramdhd

import android.content.Context
import com.sharc.ramdhd.data.dao.RoutineDao

val Context.routineDao: RoutineDao
    get() = (applicationContext as RAMDHDApplication).database.routineDao()