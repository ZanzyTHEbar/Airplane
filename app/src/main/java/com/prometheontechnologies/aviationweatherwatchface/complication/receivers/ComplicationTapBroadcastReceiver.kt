package com.prometheontechnologies.aviationweatherwatchface.complication.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationToggleArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ComplicationTapBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getArgs()
        val result = goAsync()

        scope.launch {
            try {
                ComplicationDataSourceUpdateRequester
                    .create(
                        context = context,
                        complicationDataSourceComponent = args.providerComponent
                    )
                    .requestUpdate(args.complicationInstanceId)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ARGS = "arguments"
        fun getToggleIntent(
            context: Context,
            args: ComplicationToggleArgs
        ): PendingIntent {
            val intent = Intent(context, ComplicationTapBroadcastReceiver::class.java).apply {
                putExtra(EXTRA_ARGS, args)
            }

            return PendingIntent.getBroadcast(
                context,
                args.complicationInstanceId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun Intent.getArgs(): ComplicationToggleArgs =
            when {
                Build.VERSION.SDK_INT >= 33 ->
                    requireNotNull(
                        extras?.getParcelable(EXTRA_ARGS, ComplicationToggleArgs::class.java)
                    )

                else -> requireNotNull(
                    @Suppress("DEPRECATION")
                    extras?.getParcelable(EXTRA_ARGS)
                )
            }
    }
}