package com.prometheontechnologies.aviationweatherwatchface.complication.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService.Companion.INTENT_DATA_UPDATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReceiverService : BroadcastReceiver() {

    companion object {
        private const val EXTRA_DATA_SOURCE_COMPONENT =
            "com.prometheontechnologies.aviationweatherwatchface.complication.action.DATA_SOURCE_COMPONENT"
        private const val EXTRA_COMPLICATION_ID =
            "com.prometheontechnologies.aviationweatherwatchface.complication.action.COMPLICATION_ID"

        /**
         * Returns a pending intent, suitable for use as a tap intent, that causes a complication to be
         * toggled and updated.
         */
        fun getToggleIntent(
            context: Context,
            dataSource: ComponentName,
            complicationId: Int
        ): PendingIntent {
            val intent = Intent(context, ReceiverService::class.java)
            intent.putExtra(EXTRA_DATA_SOURCE_COMPONENT, dataSource)
            intent.putExtra(EXTRA_COMPLICATION_ID, complicationId)

            // Pass complicationId as the requestCode to ensure that different complications get
            // different intents.
            return PendingIntent.getBroadcast(
                context,
                complicationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    override fun onReceive(context: Context?, intent: Intent?) {
        // Retrieve complication values from Intent's extras.
        val extras = intent?.extras ?: return
        val dataSource = extras.getParcelable<ComponentName>(EXTRA_DATA_SOURCE_COMPONENT) ?: return
        val complicationId = extras.getInt(EXTRA_COMPLICATION_ID)

        // Required when using async code in onReceive().
        val result = goAsync()

        scope.launch {
            try {

                when (intent.action) {
                    INTENT_DATA_UPDATE -> {

                        context.let {
                            ComplicationDataSourceUpdateRequester.create(
                                context = it!!,
                                complicationDataSourceComponent = dataSource
                            )
                        }.requestUpdate(complicationId)

                    }
                }

            } finally {
                // Always call finish, even if cancelled
                result.finish()
            }
        }
    }
}