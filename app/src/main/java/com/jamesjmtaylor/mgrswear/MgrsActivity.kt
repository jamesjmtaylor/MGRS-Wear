package com.jamesjmtaylor.mgrswear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class MgrsActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mgrs)

        // Enables Always-on
        setAmbientEnabled()
    }
}
